package com.pw.edu.pl.master.thesis.project.service.implementation;

import com.pw.edu.pl.master.thesis.project.client.CredentialClient;
import com.pw.edu.pl.master.thesis.project.configuration.JiraClientConfiguration;
import com.pw.edu.pl.master.thesis.project.dto.credentials.UserCredentialResponse;
import com.pw.edu.pl.master.thesis.project.dto.project.*;
import com.pw.edu.pl.master.thesis.project.dto.project.*;
import com.pw.edu.pl.master.thesis.project.enums.JiraApiEndpoint;
import com.pw.edu.pl.master.thesis.project.exceptions.UserNotAuthorizedException;
import com.pw.edu.pl.master.thesis.project.mapper.ProjectMapper;
import com.pw.edu.pl.master.thesis.project.model.Project;
import com.pw.edu.pl.master.thesis.project.dto.helper.JiraUrlBuilder;
import com.pw.edu.pl.master.thesis.project.repository.ProjectRepository;
import com.pw.edu.pl.master.thesis.project.service.ProjectService;
import com.pw.edu.pl.master.thesis.project.service.ProjectUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectServiceImplementation implements ProjectService {

    private final ProjectRepository projectRepository;
    private final JiraClientConfiguration jiraClientConfiguration;
    private final JiraUrlBuilder jiraUrlBuilder;
    private final CredentialClient credentialClient;
    private final ProjectMapper projectMapper;
    private final ProjectUserService  projectUserService;

    // ────────────────────────────────────────────────
    // Helpers (current user’s Jira cred)
    // ────────────────────────────────────────────────
    private UserCredentialResponse currentCred() {
        UserCredentialResponse cred = credentialClient.getMine(); // requires FeignBasicRelay
        if (cred == null || isBlank(cred.getBaseUrl()) || isBlank(cred.getToken()) || isBlank(cred.getUsername())) {
            throw new IllegalStateException("Missing Jira credential (username/baseUrl/token) for current user");
        }
        cred.setBaseUrl(jiraUrlBuilder.normalizeJiraBaseUrl(cred.getBaseUrl()));
        return cred;
    }

    // ────────────────────────────────────────────────
    // CREATE: Local only (no username in request)
    // ────────────────────────────────────────────────
    @Override
    @Transactional
    public ProjectResponse createProjectLocalOnly(CreateProjectRequest request) {
        validateCreateMinimalNoUsername(request);

        final UserCredentialResponse currentUser = currentCred();
        final String baseUrl = currentUser.getBaseUrl();

        final String key  = requireNotBlank(request.getKey(), "Project key is required").trim().toUpperCase();
        final String name = requireNotBlank(request.getProjectName(), "Project name is required").trim();
        final String type = requireNotBlank(request.getProjectTypeKey(), "projectTypeKey is required").trim().toLowerCase();
        final String desc = emptyToNull(request.getDescription());
        final String leadAccountId = request.getLeadAccountId();

        Project entity = projectRepository.findByKeyAndBaseUrl(key, baseUrl).orElse(null);
        if (entity == null) {
            entity = Project.builder()
                    .jiraId(null)
                    .key(key)
                    .name(name)
                    .description(desc)
                    .projectTypeKey(type)
                    .projectCategory(null)
                    .baseUrl(baseUrl)
                    .build();
        } else {
            entity.setName(name);
            entity.setDescription(desc);
            entity.setProjectTypeKey(type);
            entity.setBaseUrl(baseUrl);
        }

        entity.setLeadUserId(currentUser.getAccountId());

        Project saved = projectRepository.saveAndFlush(entity);
        boolean addMember = projectUserService.addMember(saved.getKey(), currentUser.getAccountId(), currentUser.getUsername());

        log.info("Saved project:  {}", saved.getKey());

        return projectMapper.fromProjectToResponse(saved);
    }

    @Override
    @Transactional
    public JiraProjectResponse createProjectJira(CreateProjectRequest request) {
        Objects.requireNonNull(request, "request cannot be null");
        validateCreateMinimalNoUsername(request);

        final UserCredentialResponse cred = currentCred();
        final String baseUrl = cred.getBaseUrl();
        final String jiraUser = cred.getUsername();
        final String token    = cred.getToken();

        final String key      = request.getKey().trim().toUpperCase();
        final String name     = request.getProjectName().trim();
        final String type     = request.getProjectTypeKey().toLowerCase();
        final String desc     = nullToEmpty(request.getDescription());
        final String leadAccountId = request.getLeadAccountId();
        final String assigneeType  = (!isBlank(leadAccountId)) ? "PROJECT_LEAD" : "UNASSIGNED";

        JiraCreateProjectRequest body = JiraCreateProjectRequest.builder()
                .key(key)
                .name(name)
                .projectTypeKey(type)
                .description(desc)
                .assigneeType(assigneeType)
                .leadAccountId(leadAccountId)
                .build();

        final String createUrl = jiraUrlBuilder.url(baseUrl, JiraApiEndpoint.PROJECT);
        JiraProjectResponse created = jiraClientConfiguration.post(createUrl, body, JiraProjectResponse.class,
                jiraUser, token);

        String getUrl = String.format(jiraUrlBuilder.url(baseUrl, JiraApiEndpoint.PROJECT_ID_OR_KEY), created.getId());
        getUrl = UriComponentsBuilder.fromUriString(getUrl)
                .queryParam("expand",
                        "lead,issueTypes,description,roles,avatarUrls,versions,projectTypeKey,projectTemplateKey,style,isPrivate,properties")
                .toUriString();

        return jiraClientConfiguration.get(getUrl, JiraProjectResponse.class, jiraUser, token);
    }

    // ────────────────────────────────────────────────
    // CREATE: Jira ➜ Local (no username in request)
    // ────────────────────────────────────────────────
    @Override
    @Transactional
    public ProjectResponse createProjectJiraAndLocal(CreateProjectRequest request) {
        JiraProjectResponse created = createProjectJira(request);
        return persistFromJira(created);
    }

    private ProjectResponse persistFromJira(JiraProjectResponse jira) {
        if (jira == null) throw new IllegalStateException("Jira did not return a project payload.");
        final UserCredentialResponse currentUser = currentCred();
        final String baseUrl = currentUser.getBaseUrl();
        final String username = currentUser.getUsername();

        final String jiraId = jira.getId();
        final String key    = requireNotBlank(jira.getKey(),  "Jira did not return a project key");
        final String name   = requireNotBlank(jira.getName(), "Jira did not return a project name");
        final String type   = (jira.getProjectTypeKey() == null) ? null : jira.getProjectTypeKey().trim().toLowerCase();
        final String desc   = jira.getDescription();
        final String projectCategory = (jira.getProperties() != null)
                ? Objects.toString(jira.getProperties().get("projectCategory"), null)
                : null;

        Project entity = null;
        if (!isBlank(jiraId)) {
            entity = projectRepository.findByJiraIdAndBaseUrl(jiraId, baseUrl).orElse(null);
        }
        if (entity == null) {
            entity = projectRepository.findByKeyAndBaseUrl(key, baseUrl).orElse(null);
        }

        if (entity == null) {
            entity = Project.builder()
                    .jiraId(jiraId)
                    .key(key)
                    .name(name)
                    .description(desc)
                    .projectTypeKey(type)
                    .projectCategory(projectCategory)
                    .baseUrl(baseUrl)
                    .build();
        } else {
            entity.setJiraId(jiraId);
            entity.setKey(key);
            entity.setName(name);
            entity.setDescription(desc);
            entity.setProjectTypeKey(type);
            entity.setProjectCategory(projectCategory);
            entity.setBaseUrl(baseUrl);
        }

        entity.setLeadUserId(currentUser.getAccountId());
        Project saved = projectRepository.saveAndFlush(entity);
        boolean addMember = projectUserService.addMember(saved.getKey(), currentUser.getAccountId(), username);
        return projectMapper.fromProjectToResponse(saved);
    }

    // ────────────────────────────────────────────────
    // READ from Jira (paged) — no username in request
    // ────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<JiraProjectResponse> getAllProjectsFromJira(String baseUrl) {
        final UserCredentialResponse cred = currentCred();
        final String jiraUser = cred.getUsername();
        final String token    = cred.getToken();

        final List<ProjectSummary> compact = new ArrayList<>();
        int startAt = 0, pageSize = 50;
        while (true) {
            final String listUrl = UriComponentsBuilder
                    .fromUriString(jiraUrlBuilder.url(baseUrl, JiraApiEndpoint.PROJECT_SEARCH))
                    .queryParam("startAt", startAt)
                    .queryParam("maxResults", pageSize)
                    .build(true)
                    .toUriString();

            ProjectSearchPage page;
            try {
                page = jiraClientConfiguration.get(listUrl, ProjectSearchPage.class, jiraUser, token);
            } catch (HttpStatusCodeException e) {
                HttpStatusCode sc = e.getStatusCode();
                throw new UserNotAuthorizedException("Jira list projects failed: " + sc.value() + " " + sc + " — " + e.getResponseBodyAsString());
            }

            if (page == null || page.getValues() == null || page.getValues().isEmpty()) break;
            compact.addAll(page.getValues());
            if (page.isLast()) break;
            startAt += page.getMaxResults();
        }

        if (compact.isEmpty()) return List.of();

        final List<JiraProjectResponse> out = new ArrayList<>(compact.size());
        for (ProjectSummary ps : compact) {
            final String idOrKey = nonEmpty(ps.getId()).orElse(ps.getKey());
            out.add(fetchFullProject(baseUrl, idOrKey, jiraUser, token));
        }
        return out;
    }

    @Override
    public ProjectResponse getProjectByKey(String projectKey){
        Project entity = projectRepository.findByKey(projectKey).orElse(null);
        if (entity == null) return null;
        return projectMapper.fromProjectToResponse(entity);
    }

    @Override
    @Transactional
    public ProjectResponse getJiraProjectByKey(String projectKey) {
        final String keyOrId = requireNotBlank(projectKey, "projectKey required").trim();

        final UserCredentialResponse cred = currentCred(); // normalizes baseUrl
        final String baseUrl = cred.getBaseUrl();
        final String jiraUser = cred.getUsername();
        final String token    = cred.getToken();

        // Fetch full project payload from Jira, then persist/upsert locally
        JiraProjectResponse jira = fetchFullProject(baseUrl, keyOrId, jiraUser, token);
        return persistFromJira(jira);
    }


    @Override
    public JiraProjectResponse fetchFullProject(String baseUrl, String idOrKey, String jiraUser, String token) {
        String pattern = jiraUrlBuilder.url(baseUrl, JiraApiEndpoint.PROJECT_ID_OR_KEY);
        String url = String.format(pattern, idOrKey);
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("expand",
                        "lead,issueTypes,description,roles,avatarUrls,versions,projectTypeKey,projectTemplateKey,style,isPrivate,properties")
                .build(true)
                .toUriString();

        return jiraClientConfiguration.get(url, JiraProjectResponse.class, jiraUser, token);
    }

    // ────────────────────────────────────────────────
    // READ local — scoped to current baseUrl
    // ────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjectsFromLocalDb(String baseUrl) {
        return projectRepository.findAllByBaseUrl(baseUrl)
                .stream()
                .map(projectMapper::fromProjectToResponse)
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public List<JiraProjectResponse> getAllProjectsFromJiraForCurrentUserUrl() {
        final UserCredentialResponse cred = currentCred();

        // ✅ use baseUrl, not username
        final String baseUrl = jiraUrlBuilder.normalizeJiraBaseUrl(cred.getBaseUrl());
        final String jiraUser = cred.getUsername();
        final String token    = cred.getToken();

        log.info("Current base url: {}", baseUrl);

        final List<ProjectSummary> compact = new ArrayList<>();
        int startAt = 0, pageSize = 50;

        while (true) {
            final String listUrl = UriComponentsBuilder
                    .fromUriString(jiraUrlBuilder.url(baseUrl, JiraApiEndpoint.PROJECT_SEARCH))
                    .queryParam("startAt", startAt)
                    .queryParam("maxResults", pageSize)
                    .build(true)
                    .toUriString();

            ProjectSearchPage page;
            try {
                page = jiraClientConfiguration.get(listUrl, ProjectSearchPage.class, jiraUser, token);
            } catch (HttpStatusCodeException e) {
                HttpStatusCode sc = e.getStatusCode();
                throw new UserNotAuthorizedException("Jira list projects failed: " + sc.value() + " " + sc
                        + " — " + e.getResponseBodyAsString());
            }

            if (page == null || page.getValues() == null || page.getValues().isEmpty()) break;
            compact.addAll(page.getValues());
            if (page.isLast()) break;
            startAt += page.getMaxResults();
        }

        if (compact.isEmpty()) return List.of();

        final List<JiraProjectResponse> out = new ArrayList<>(compact.size());
        for (ProjectSummary ps : compact) {
            final String idOrKey = nonEmpty(ps.getId()).orElse(ps.getKey());
            out.add(fetchFullProject(baseUrl, idOrKey, jiraUser, token));
        }
        return out;
    }


        // ────────────────────────────────────────────────
    // UPDATE local (no username)
    // ────────────────────────────────────────────────
    @Override
    @Transactional
    public ProjectResponse updateProject(Long id, UpdateProjectRequest request) {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(request, "request is required");

        final String baseUrl = currentCred().getBaseUrl();

        Project entity = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project id not found: " + id));

        if (!isBlank(request.getProjectName()))    entity.setName(request.getProjectName().trim());
        if (!isBlank(request.getDescription()))    entity.setDescription(request.getDescription().trim());
        if (!isBlank(request.getProjectTypeKey())) entity.setProjectTypeKey(request.getProjectTypeKey().trim().toLowerCase());
        entity.setBaseUrl(baseUrl);

        if (!isBlank(request.getLeadAccountId())) {
            addOrUpdateLeadLink(entity, request.getLeadAccountId().trim());
        }

        Project saved = projectRepository.saveAndFlush(entity);
        return projectMapper.fromProjectToResponse(saved);
    }

    // ────────────────────────────────────────────────
    // DELETE helpers (no username)
    // ────────────────────────────────────────────────
    @Override
    @Transactional
    public int deleteAllLocalProjectsForCurrentBaseUrl(String ignored) {
        final String baseUrl = currentCred().getBaseUrl();
        List<Project> all = projectRepository.findAllByBaseUrl(baseUrl);
        int count = all.size();
        projectRepository.deleteAllInBatch(all);
        return count;
    }

    @Override
    @Transactional
    public boolean deleteLocalProjectByKey(String projectKey) {
        final String baseUrl  = currentCred().getBaseUrl();
        final String key = requireNotBlank(projectKey, "projectKey required").trim().toUpperCase();
        return projectRepository.findByKeyAndBaseUrl(key, baseUrl)
                .map(p -> { projectRepository.delete(p); return true; })
                .orElse(false);
    }

    @Override
    @Transactional
    public void deleteJiraProjectByKeyOrId(String projectKeyOrId) {
        final UserCredentialResponse cred = currentCred();
        final String baseUrl = cred.getBaseUrl();
        final String jiraUser = cred.getUsername();
        final String token    = cred.getToken();

        final String url = String.format(jiraUrlBuilder.url(baseUrl, JiraApiEndpoint.PROJECT_ID_OR_KEY),
                requireNotBlank(projectKeyOrId, "projectKeyOrId required"));
        jiraClientConfiguration.delete(url, Void.class, jiraUser, token);
    }

    @Override
    @Transactional
    public String deleteProjectFromJiraAndLocalDb(String projectKey) {
        final UserCredentialResponse cred = currentCred();
        final String baseUrl = cred.getBaseUrl();

        final String key = requireNotBlank(projectKey, "projectKey required").trim();
        deleteJiraProjectByKeyOrId(key);
        projectRepository.findByKeyAndBaseUrl(key, baseUrl).ifPresent(projectRepository::delete);
        return "Deleted project '" + key + "' from Jira and local DB";
    }

    // ────────────────────────────────────────────────
    // SYNC / SET LEAD (no username)
    // ────────────────────────────────────────────────
    @Override
    @Transactional
    public List<ProjectResponse> syncAllProjectsFromJira() {
        final UserCredentialResponse cred = currentCred();
        final String baseUrl = jiraUrlBuilder.normalizeJiraBaseUrl(cred.getBaseUrl());

        List<JiraProjectResponse> jiraProjects = getAllProjectsFromJira(baseUrl);
        List<ProjectResponse> out = new ArrayList<>(jiraProjects.size());
        for (JiraProjectResponse jp : jiraProjects) {
            out.add(persistFromJira(jp));
        }
        return out;
    }

    @Override
    @Transactional
    public ProjectResponse syncProjectByKeyOrId(SyncProjectRequest request) {
        final UserCredentialResponse cred = currentCred();
        final String baseUrl = cred.getBaseUrl();
        final String jiraUser = cred.getUsername();
        final String token    = cred.getToken();

        final String keyOrId = requireNotBlank(request.getProjectKey(), "keyOrId required");
        JiraProjectResponse jira = fetchFullProject(baseUrl, keyOrId, jiraUser, token);
        return persistFromJira(jira);
    }

    @Override
    @Transactional
    public ProjectResponse syncProjectFromJira(SyncProjectRequest request) {
        final UserCredentialResponse cred = currentCred();
        final String baseUrl = cred.getBaseUrl();
        final String jiraUser = cred.getUsername();
        final String token    = cred.getToken();

        final String keyOrId  = requireNotBlank(request.getProjectKey(), "projectKeyOrId required").trim();
        JiraProjectResponse jira = fetchFullProject(baseUrl, keyOrId, jiraUser, token);
        return persistFromJira(jira);
    }

    @Override
    @Transactional
    public ProjectResponse syncProjectFromLocalToJira(SyncProjectRequest request) {
        Objects.requireNonNull(request, "request is required");
        final String keyOrId = requireNotBlank(request.getProjectKey(), "projectKeyOrId required").trim();

        // ✅ Get Jira credentials for the currently authenticated user
        final UserCredentialResponse cred = currentCred();
        final String baseUrl = cred.getBaseUrl();
        final String jiraUser = cred.getUsername();
        final String token    = cred.getToken();

        // ✅ Load local project by key (prefer KEY match) or fallback to local JIRA ID match
        Project local = projectRepository.findByKeyAndBaseUrl(keyOrId.toUpperCase(), baseUrl)
                .orElseGet(() -> projectRepository.findByJiraIdAndBaseUrl(keyOrId, baseUrl).orElse(null));

        if (local == null) {
            throw new IllegalArgumentException("Local project not found by key or id: " + keyOrId);
        }

        // ✅ Prepare minimal Jira update payload
        Map<String, Object> body = new HashMap<>();
        if (!isBlank(local.getName()))        body.put("name", local.getName());
        if (!isBlank(local.getDescription())) body.put("description", local.getDescription());
        if (!isBlank(local.getLeadUserId()))  body.put("leadAccountId", local.getLeadUserId());

        // ✅ Build update URL
        String updateUrl = String.format(jiraUrlBuilder.url(baseUrl, JiraApiEndpoint.PROJECT_ID_OR_KEY), keyOrId);

        // ✅ Send update request to Jira
        jiraClientConfiguration.put(updateUrl, body, Map.class, jiraUser, token);

        // ✅ Re-fetch updated Jira project and persist locally
        JiraProjectResponse jira = fetchFullProject(baseUrl, keyOrId, jiraUser, token);
        return persistFromJira(jira);
    }

    @Override
    @Transactional
    public ProjectResponse setProjectLead(SetProjectLeadRequest request) {
        Objects.requireNonNull(request, "request is required");
        final String keyOrId     = requireNotBlank(request.getProjectKey(), "projectKeyOrId required").trim();
        final String leadAccount = requireNotBlank(request.getAccountId(), "leadAccountId required").trim();

        final UserCredentialResponse cred = currentCred();
        final String baseUrl = cred.getBaseUrl();
        final String jiraUser = cred.getUsername();
        final String token    = cred.getToken();

        Map<String, Object> body = new HashMap<>();
        body.put("leadAccountId", leadAccount);

        String updateUrl = String.format(jiraUrlBuilder.url(baseUrl, JiraApiEndpoint.PROJECT_ID_OR_KEY), keyOrId);
        jiraClientConfiguration.put(updateUrl, body, Map.class, jiraUser, token);

        JiraProjectResponse jira = fetchFullProject(baseUrl, keyOrId, jiraUser, token);
        return persistFromJira(jira);
    }

    @Override
    @Transactional
    public List<ProjectResponse> syncAllProjectsFromLocalToJira() {
        final UserCredentialResponse cred = currentCred();
        final String baseUrl = cred.getBaseUrl();
        final String jiraUser = cred.getUsername();
        final String token    = cred.getToken();

        List<Project> locals = projectRepository.findAllByBaseUrl(baseUrl);
        List<ProjectResponse> out = new ArrayList<>(locals.size());
        for (Project p : locals) {
            String keyOrId = !isBlank(p.getKey()) ? p.getKey() : p.getJiraId();
            if (isBlank(keyOrId)) continue;

            // Minimal update payload example (same as before)
            Map<String, Object> body = new HashMap<>();
            if (!isBlank(p.getName()))        body.put("name", p.getName());
            if (!isBlank(p.getDescription())) body.put("description", p.getDescription());
            if (!isBlank(p.getLeadUserId()))  body.put("leadAccountId", p.getLeadUserId());

            String updateUrl = String.format(jiraUrlBuilder.url(baseUrl, JiraApiEndpoint.PROJECT_ID_OR_KEY), keyOrId);
            jiraClientConfiguration.put(updateUrl, body, Map.class, jiraUser, token);

            JiraProjectResponse jira = fetchFullProject(baseUrl, keyOrId, jiraUser, token);
            out.add(persistFromJira(jira));
        }
        return out;
    }

    // ────────────────────────────────────────────────
    // (unchanged) lead linking + small utils
    // ────────────────────────────────────────────────
    private void addOrUpdateLeadLink(Project saved, String leadAccountId) { /* your original code unchanged */ }

    private void validateCreateMinimalNoUsername(CreateProjectRequest request) {
        Objects.requireNonNull(request, "Request cannot be null");
        if (isBlank(request.getKey()))            throw new IllegalArgumentException("Project key is required");
        if (isBlank(request.getProjectName()))    throw new IllegalArgumentException("Project name is required");
        if (isBlank(request.getProjectTypeKey())) throw new IllegalArgumentException("projectTypeKey is required");
        if (isBlank(request.getLeadAccountId()))  throw new IllegalArgumentException("leadAccountId is required");
        validateProjectType(request.getProjectTypeKey());
    }

    private void validateProjectType(String type) {
        if (type == null) throw new IllegalArgumentException("projectTypeKey is required");
        String t = type.toLowerCase();
        if (!List.of("software", "business", "service_desk").contains(t)) {
            throw new IllegalArgumentException("Invalid projectTypeKey: " + type);
        }
    }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
    private static String emptyToNull(String s) { if (s == null) return null; String t = s.trim(); return t.isEmpty()? null: t; }
    private static String nullToEmpty(String s) { return (s == null) ? "" : s; }
    private static String requireNotBlank(String s, String msg) { if (s == null || s.isBlank()) throw new IllegalArgumentException(msg); return s; }
    private static Optional<String> nonEmpty(String s) { return (s == null || s.isBlank()) ? Optional.empty() : Optional.of(s); }
}
