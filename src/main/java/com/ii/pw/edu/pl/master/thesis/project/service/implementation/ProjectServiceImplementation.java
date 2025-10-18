package com.ii.pw.edu.pl.master.thesis.project.service.implementation;

import com.ii.pw.edu.pl.master.thesis.project.client.CredentialClient;
import com.ii.pw.edu.pl.master.thesis.project.configuration.JiraClientConfiguration;
import com.ii.pw.edu.pl.master.thesis.project.dto.credentials.UserCredentialResponse;
import com.ii.pw.edu.pl.master.thesis.project.dto.project.*;
import com.ii.pw.edu.pl.master.thesis.project.dto.user.UserSummary;
import com.ii.pw.edu.pl.master.thesis.project.enums.JiraApiEndpoint;
import com.ii.pw.edu.pl.master.thesis.project.exceptions.UserNotAuthorizedException;
import com.ii.pw.edu.pl.master.thesis.project.mapper.ProjectMapper;
import com.ii.pw.edu.pl.master.thesis.project.model.Project;
import com.ii.pw.edu.pl.master.thesis.project.model.ProjectUser;
import com.ii.pw.edu.pl.master.thesis.project.model.StyleOption;
import com.ii.pw.edu.pl.master.thesis.project.model.helper.JiraUrlBuilder;
import com.ii.pw.edu.pl.master.thesis.project.repository.ProjectRepository;
import com.ii.pw.edu.pl.master.thesis.project.service.ProjectService;
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

    // ────────────────────────────────────────────────
    // CREATE: Local only
    // ────────────────────────────────────────────────
    @Override
    @Transactional
    public ProjectResponse createProjectLocalOnly(CreateProjectRequest request) {
        validateCreateMinimal(request);
        final String username = request.getUsername().trim();

        // Resolve and normalize baseUrl (no session; from credentials)
        UserCredentialResponse cred = credentialClient.getByUsername(username);
        if (cred == null || isBlank(cred.getBaseUrl())) {
            throw new IllegalStateException("Missing Jira baseURL for user: " + username);
        }
        final String baseUrl = jiraUrlBuilder.normalizeJiraBaseUrl(cred.getBaseUrl());

        // Canonical fields from REQUEST (no Jira call)
        final String key  = requireNotBlank(request.getKey(), "Project key is required").trim().toUpperCase();
        final String name = requireNotBlank(request.getProjectName(), "Project name is required").trim();
        final String type = requireNotBlank(request.getProjectTypeKey(), "projectTypeKey is required").trim().toLowerCase();
        final String desc = emptyToNull(request.getDescription());

        // Upsert by (key, baseUrl)
        Project entity = projectRepository.findByKeyAndBaseUrl(key, baseUrl).orElse(null);
        final boolean isNew = (entity == null);

        if (isNew) {
            entity = Project.builder()
                    .jiraId(null) // local-only doesn't have Jira id
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

        Project saved = projectRepository.saveAndFlush(entity);

        // Optional: add lead link if provided; ensure user_name (NOT NULL) is set
        if (!isBlank(request.getLeadAccountId())) {
            addOrUpdateLeadLink(saved, request.getLeadAccountId().trim());
            saved = projectRepository.saveAndFlush(saved);
        }

        return projectMapper.fromProjectToResponse(saved);
    }

    // ────────────────────────────────────────────────
    // CREATE: Jira only
    // ────────────────────────────────────────────────
    @Override
    @Transactional
    public JiraProjectResponse createProjectJira(CreateProjectRequest request) {
        Objects.requireNonNull(request, "request cannot be null");
        validateCreateMinimal(request);

        final String username = request.getUsername().trim();
        final String key      = request.getKey().trim().toUpperCase();
        final String name     = request.getProjectName().trim();
        final String type     = request.getProjectTypeKey().toLowerCase();
        final String desc     = nullToEmpty(request.getDescription());
        final String leadAccountId = request.getLeadAccountId();
        final String assigneeType  = (!isBlank(leadAccountId)) ? "PROJECT_LEAD" : "UNASSIGNED";

        UserCredentialResponse cred = credentialClient.getByUsername(username);
        if (cred == null || isBlank(cred.getBaseUrl()) || isBlank(cred.getToken())) {
            throw new IllegalStateException("Missing Jira credential (baseURL/token) for user: " + username);
        }
        final String baseUrl = jiraUrlBuilder.normalizeJiraBaseUrl(cred.getBaseUrl());

        JiraCreateProjectRequest body = JiraCreateProjectRequest.builder()
                .key(key)
                .name(name)
                .projectTypeKey(type)
                .description(desc)
                .assigneeType(assigneeType)
                .leadAccountId(leadAccountId)
                .build();

        log.info("Creating Jira project: key={}, name={}, type={}, assigneeType={}, hasLeadId={}",
                key, name, type, assigneeType, (leadAccountId != null));

        // POST create
        final String createUrl = jiraUrlBuilder.url(baseUrl, JiraApiEndpoint.PROJECT);
        JiraProjectResponse created = jiraClientConfiguration.post(createUrl, body, JiraProjectResponse.class,
                username, cred.getToken());

        // GET rich
        String getUrl = String.format(jiraUrlBuilder.url(baseUrl, JiraApiEndpoint.PROJECT_ID_OR_KEY), created.getId());
        getUrl = UriComponentsBuilder.fromUriString(getUrl)
                .queryParam("expand",
                        "lead,issueTypes,description,roles,avatarUrls,versions,projectTypeKey,projectTemplateKey,style,isPrivate,properties")
                .toUriString();

        return jiraClientConfiguration.get(getUrl, JiraProjectResponse.class, username, cred.getToken());
    }

    // ────────────────────────────────────────────────
    // CREATE: Jira ➜ Local
    // ────────────────────────────────────────────────
    @Override
    @Transactional
    public ProjectResponse createProjectJiraAndLocal(CreateProjectRequest request) {
        // Create in Jira and persist locally from the rich payload
        JiraProjectResponse created = createProjectJira(request);
        return persistFromJira(created, request.getUsername());
    }

    private ProjectResponse persistFromJira(JiraProjectResponse jira, String username) {
        if (jira == null) throw new IllegalStateException("Jira did not return a project payload.");
        UserCredentialResponse cred = credentialClient.getByUsername(username);
        if (cred == null || isBlank(cred.getBaseUrl())) {
            throw new IllegalStateException("Missing Jira baseURL for user: " + username);
        }
        final String baseUrl = jiraUrlBuilder.normalizeJiraBaseUrl(cred.getBaseUrl());

        // Canonical fields
        final String jiraId = jira.getId();
        final String key    = requireNotBlank(jira.getKey(),  "Jira did not return a project key");
        final String name   = requireNotBlank(jira.getName(), "Jira did not return a project name");
        final String type   = (jira.getProjectTypeKey() == null) ? null : jira.getProjectTypeKey().trim().toLowerCase();
        final String desc   = jira.getDescription();
        final String projectCategory = (jira.getProperties() != null)
                ? Objects.toString(jira.getProperties().get("projectCategory"), null)
                : null;

        // Upsert: (jiraId, baseUrl) -> (key, baseUrl)
        Project entity = null;
        if (!isBlank(jiraId)) {
            entity = projectRepository.findByJiraIdAndBaseUrl(jiraId, baseUrl).orElse(null);
        }
        if (entity == null) {
            entity = projectRepository.findByKeyAndBaseUrl(key, baseUrl).orElse(null);
        }

        final boolean isNew = (entity == null);
        if (isNew) {
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

        Project saved = projectRepository.saveAndFlush(entity);

        // Optional: add lead link (ensure NOT NULL user_name)
        String leadAccountId = (jira.getLead() != null) ? jira.getLead().getAccountId() : null;
        if (!isBlank(leadAccountId)) {
            addOrUpdateLeadLink(saved, leadAccountId.trim());
            saved = projectRepository.saveAndFlush(saved);
        }

        return projectMapper.fromProjectToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JiraProjectResponse> getAllProjectsFromJira(ListProjectsRequest request) {
        final String username = requireNotBlank(request.getUsername(), "username is required").trim();

        UserCredentialResponse cred = credentialClient.getByUsername(username);
        if (cred == null) throw new UserNotAuthorizedException("No credentials found for username: " + username);

        final String baseUrl = requireNotBlank(cred.getBaseUrl(), "No Jira base URL stored for user: " + username);
        final String token   = Objects.requireNonNull(cred.getToken(), "Missing Jira token");

        // Page through /project/search
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
                page = jiraClientConfiguration.get(listUrl, ProjectSearchPage.class, username, token);
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
            out.add(fetchFullProject(baseUrl, idOrKey, username, token));
        }
        return out;
    }

    @Override
    public JiraProjectResponse fetchFullProject(String baseUrl, String idOrKey, String username, String token) {
        String pattern = jiraUrlBuilder.url(baseUrl, JiraApiEndpoint.PROJECT_ID_OR_KEY);
        String url = String.format(pattern, idOrKey);
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("expand",
                        "lead,issueTypes,description,roles,avatarUrls,versions,projectTypeKey,projectTemplateKey,style,isPrivate,properties")
                .build(true)
                .toUriString();

        return jiraClientConfiguration.get(url, JiraProjectResponse.class, username, token);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjectsFromLocalDb(ListProjectsRequest request) {
        final String username = requireNotBlank(request.getUsername(), "username is required").trim();

        UserCredentialResponse cred = credentialClient.getByUsername(username);
        if (cred == null || isBlank(cred.getBaseUrl())) {
            throw new IllegalStateException("Missing Jira baseURL for user: " + username);
        }
        final String baseUrl = jiraUrlBuilder.normalizeJiraBaseUrl(cred.getBaseUrl());

        return projectRepository.findAllByBaseUrl(baseUrl)
                .stream()
                .map(projectMapper::fromProjectToResponse)
                .toList();
    }


    @Override
    @Transactional
    public ProjectResponse updateProject(Long id, UpdateProjectRequest request) {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(request, "request is required");
        final String username = requireNotBlank(request.getUsername(), "username is required").trim();

        // Resolve baseUrl for scoping
        final String baseUrl = request.getBaseUrl();

        Project entity = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project id not found: " + id));

        // Local-only update (name/description/type/category). You can expand to sync to Jira if needed.
        if (!isBlank(request.getProjectName()))    entity.setName(request.getProjectName().trim());
        if (!isBlank(request.getDescription()))    entity.setDescription(request.getDescription().trim());
        if (!isBlank(request.getProjectTypeKey())) entity.setProjectTypeKey(request.getProjectTypeKey().trim().toLowerCase());
        entity.setBaseUrl(baseUrl);

        // Lead update locally (optionally sync with Jira via setProjectLead)
        if (!isBlank(request.getLeadAccountId())) {
            addOrUpdateLeadLink(entity, request.getLeadAccountId().trim());
        }

        Project saved = projectRepository.saveAndFlush(entity);
        return projectMapper.fromProjectToResponse(saved);
    }


    @Override
    @Transactional
    public int deleteAllLocalProjectsForCurrentBaseUrl(String baseUrl) {
        List<Project> all = projectRepository.findAllByBaseUrl(baseUrl);
        int count = all.size();
        projectRepository.deleteAllInBatch(all);
        return count;
    }

    @Override
    @Transactional
    public boolean deleteLocalProjectByKey(DeleteProjectRequest request) {
        UserCredentialResponse userCredential = credentialClient.getByUsername(request.getUsername());
        final String baseUrl  = userCredential.getBaseUrl();
        return projectRepository.findByKeyAndBaseUrl(requireNotBlank(request.getProjectKey(),"projectKey required").trim().toUpperCase(), baseUrl)
                .map(p -> { projectRepository.delete(p); return true; })
                .orElse(false);
    }

    @Override
    @Transactional
    public void deleteJiraProjectByKeyOrId(String projectKeyOrId, String username) {
        UserCredentialResponse userCredential = credentialClient.getByUsername(username);
        final String baseUrl  = userCredential.getBaseUrl();
        final String url = String.format(jiraUrlBuilder.url(baseUrl, JiraApiEndpoint.PROJECT_ID_OR_KEY),
                requireNotBlank(projectKeyOrId, "projectKeyOrId required"));
        jiraClientConfiguration.delete(url, Void.class, username, userCredential.getToken());
    }


    @Override
    @Transactional
    public String deleteProjectFromJiraAndLocalDb(DeleteProjectRequest request) {
        UserCredentialResponse userCredential = credentialClient.getByUsername(request.getUsername());
        final String baseUrl  = userCredential.getBaseUrl();
        deleteJiraProjectByKeyOrId(request.getProjectKey(), request.getUsername());
        projectRepository.findByKeyAndBaseUrl(request.getProjectKey(), baseUrl)
                .ifPresent(projectRepository::delete);
        return "Deleted project '" + request.getProjectKey() + "' from Jira and local DB";
    }




    // ────────────────────────────────────────────────
    // SYNC / UPDATE
    // ────────────────────────────────────────────────

    @Override
    @Transactional
    public List<ProjectResponse> syncAllProjects(ListProjectsRequest request) {
        List<JiraProjectResponse> jiraProjects = getAllProjectsFromJira(request); // uses current principal
        List<ProjectResponse> projectResponses = new ArrayList<>(jiraProjects.size());
        for (JiraProjectResponse jp : jiraProjects) {
            projectResponses.add(persistFromJira(jp, request.getUsername()));
        }
        return projectResponses;
    }

    @Override
    @Transactional
    public ProjectResponse syncProjectByKeyOrId(SyncProjectRequest request) {
        final UserCredentialResponse cred = requireCred(request.getUsername());
        final String baseUrl = jiraUrlBuilder.normalizeJiraBaseUrl(cred.getBaseUrl());
        JiraProjectResponse jira = fetchFullProject(baseUrl, requireNotBlank(request.getProjectKey(),"keyOrId required"),
                request.getUsername(), cred.getToken());
        return persistFromJira(jira, request.getUsername());
    }

    @Override
    @Transactional
    public ProjectResponse syncProjectFromJira(SyncProjectRequest request) {
        Objects.requireNonNull(request, "request is required");
        final String username = requireNotBlank(request.getUsername(),"username required").trim();
        final String keyOrId  = requireNotBlank(request.getProjectKey(), "projectKeyOrId required").trim();

        final UserCredentialResponse cred = requireCred(username);
        final String baseUrl = jiraUrlBuilder.normalizeJiraBaseUrl(cred.getBaseUrl());

        JiraProjectResponse jira = fetchFullProject(baseUrl, keyOrId, username, cred.getToken());
        return persistFromJira(jira, username);
    }

    @Override
    @Transactional
    public ProjectResponse setProjectLead(SetProjectLeadRequest request) {
        Objects.requireNonNull(request, "request is required");
        final String username    = requireNotBlank(request.getUsername(), "username required").trim();
        final String keyOrId     = requireNotBlank(request.getProjectKey(), "projectKeyOrId required").trim();
        final String leadAccount = requireNotBlank(request.getAccountId(), "leadAccountId required").trim();

        final UserCredentialResponse cred = requireCred(username);
        final String baseUrl = jiraUrlBuilder.normalizeJiraBaseUrl(cred.getBaseUrl());

        // Jira update (minimal) — PUT /project/{keyOrId}
        Map<String, Object> body = new HashMap<>();
        body.put("leadAccountId", leadAccount);

        String updateUrl = String.format(jiraUrlBuilder.url(baseUrl, JiraApiEndpoint.PROJECT_ID_OR_KEY), keyOrId);
        jiraClientConfiguration.put(updateUrl, body, Map.class, username, cred.getToken());

        // Re-fetch rich and persist locally
        JiraProjectResponse jira = fetchFullProject(baseUrl, keyOrId, username, cred.getToken());
        return persistFromJira(jira, username);
    }

    @Override
    @Transactional
    public ProjectResponse syncProjectFromLocalToJira(SyncProjectRequest request) {
        Objects.requireNonNull(request, "request is required");
        final String username = requireNotBlank(request.getUsername(), "username required").trim();
        final String keyOrId  = requireNotBlank(request.getProjectKey(), "projectKeyOrId required").trim();

        final UserCredentialResponse cred = requireCred(username);
        final String baseUrl = jiraUrlBuilder.normalizeJiraBaseUrl(cred.getBaseUrl());

        // Load local project by key (prefer) or by jiraId
        Project local = projectRepository.findByKeyAndBaseUrl(keyOrId.toUpperCase(), baseUrl)
                .orElseGet(() -> projectRepository.findByJiraIdAndBaseUrl(keyOrId, baseUrl).orElse(null));
        if (local == null) throw new IllegalArgumentException("Local project not found by key or id: " + keyOrId);

        // Prepare minimal Jira update payload
        Map<String, Object> body = new HashMap<>();
        if (!isBlank(local.getName()))        body.put("name", local.getName());
        if (!isBlank(local.getDescription())) body.put("description", local.getDescription());
        if (!isBlank(local.getLeadUserId())) {
            // If you store local lead as user-service id, you may need to resolve to accountId before updating Jira.
            // If you already store Jira accountId in leadUserId, you can set it directly:
            body.put("leadAccountId", local.getLeadUserId());
        }

        String updateUrl = String.format(jiraUrlBuilder.url(baseUrl, JiraApiEndpoint.PROJECT_ID_OR_KEY), keyOrId);
        jiraClientConfiguration.put(updateUrl, body, Map.class, username, cred.getToken());

        // Re-fetch rich and persist
        JiraProjectResponse jira = fetchFullProject(baseUrl, keyOrId, username, cred.getToken());
        return persistFromJira(jira, username);
    }

    @Override
    @Transactional
    public List<ProjectResponse> syncAllProjectsFromLocalToJira(SyncProjectRequest request) {
        final UserCredentialResponse cred = requireCred(request.getUsername());
        final String baseUrl = jiraUrlBuilder.normalizeJiraBaseUrl(cred.getBaseUrl());

        List<Project> locals = projectRepository.findAllByBaseUrl(baseUrl);
        List<ProjectResponse> out = new ArrayList<>(locals.size());
        for (Project p : locals) {
            String keyOrId = !isBlank(p.getKey()) ? p.getKey() : p.getJiraId();
            if (isBlank(keyOrId)) continue;
            SyncProjectRequest req = new SyncProjectRequest();
            req.setUsername(request.getUsername());
            req.setProjectKey(keyOrId);
            out.add(syncProjectFromLocalToJira(req));
        }
        return out;
    }



    // ────────────────────────────────────────────────
    // Helpers (NO session)
    // ────────────────────────────────────────────────
    private void addOrUpdateLeadLink(Project saved, String leadAccountId) {
        String userNameForLink = null;
        Long leadLocalUserId = null;

        try {
            UserSummary leadSummary = credentialClient.getUserSummaryByAccountId(leadAccountId);
            if (leadSummary != null) {
                leadLocalUserId = leadSummary.getId();
                if (!isBlank(leadSummary.getUsername())) {
                    userNameForLink = leadSummary.getUsername().trim();
                } else if (!isBlank(leadSummary.getFirstName()) || !isBlank(leadSummary.getLastName())) {
                    userNameForLink = String.join(" ",
                            Optional.ofNullable(leadSummary.getFirstName()).orElse(""),
                            Optional.ofNullable(leadSummary.getLastName()).orElse("")).trim();
                } else if (!isBlank(leadSummary.getEmailAddress())) {
                    userNameForLink = leadSummary.getEmailAddress().trim();
                }
            }
        } catch (Exception ex) {
            log.warn("UserService lookup by accountId failed for '{}': {}", leadAccountId, ex.getMessage());
        }

        if (isBlank(userNameForLink)) userNameForLink = leadAccountId;
        if (leadLocalUserId != null) saved.setLeadUserId(String.valueOf(leadLocalUserId));

        Long finalLeadLocalUserId = leadLocalUserId;
        boolean alreadyLinked = saved.getProjectUsers().stream()
                .anyMatch(u -> Objects.equals(u.getUserId(), String.valueOf(finalLeadLocalUserId)));
        if (!alreadyLinked) {
            ProjectUser link = new ProjectUser();
            link.setUserId(leadLocalUserId != null ? String.valueOf(leadLocalUserId) : leadAccountId);
            link.setUsername(userNameForLink); // NOT NULL
            saved.addProjectUser(link);
        }
    }

    private UserCredentialResponse requireCred(String username) {
        UserCredentialResponse cred = credentialClient.getByUsername(username);
        if (cred == null || isBlank(cred.getBaseUrl()) || isBlank(cred.getToken())) {
            throw new IllegalStateException("Missing Jira credential (baseUrl/token) for user: " + username);
        }
        return cred;
    }












    private void validateCreateMinimal(CreateProjectRequest request) {
        Objects.requireNonNull(request, "Request cannot be null");
        if (isBlank(request.getKey()))            throw new IllegalArgumentException("Project key is required");
        if (isBlank(request.getProjectName()))    throw new IllegalArgumentException("Project name is required");
        if (isBlank(request.getProjectTypeKey())) throw new IllegalArgumentException("projectTypeKey is required");
        if (isBlank(request.getUsername()))       throw new IllegalArgumentException("username is required");
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

    private static boolean isBlank(String string) { return string == null || string.isBlank(); }
    private static String emptyToNull(String string) {
        if (string == null) return null;
        String t = string.trim(); return t.isEmpty() ? null : t;
    }
    private static String nullToEmpty(String string) { return (string == null) ? "" : string; }
    private static String requireNotBlank(String string, String msg) {
        if (string == null || string.isBlank()) throw new IllegalArgumentException(msg);
        return string;
    }
    private static Optional<String> nonEmpty(String string) {
        return (string == null || string.isBlank()) ? Optional.empty() : Optional.of(string);
    }


}
