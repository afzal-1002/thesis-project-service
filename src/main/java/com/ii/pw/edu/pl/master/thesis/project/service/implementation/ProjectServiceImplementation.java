package com.ii.pw.edu.pl.master.thesis.project.service.implementation;

import com.ii.pw.edu.pl.master.thesis.project.client.CredentialClient;
import com.ii.pw.edu.pl.master.thesis.project.client.UserClient;
import com.ii.pw.edu.pl.master.thesis.project.configuration.JiraClientConfiguration;
import com.ii.pw.edu.pl.master.thesis.project.dto.credentials.UserCredentialResponse;
import com.ii.pw.edu.pl.master.thesis.project.dto.project.CreateProjectRequest;
import com.ii.pw.edu.pl.master.thesis.project.dto.project.ProjectResponse;
import com.ii.pw.edu.pl.master.thesis.project.dto.user.TokenResponse;
import com.ii.pw.edu.pl.master.thesis.project.dto.user.UserSummary;
import com.ii.pw.edu.pl.master.thesis.project.enums.JiraApiEndpoint;
import com.ii.pw.edu.pl.master.thesis.project.mapper.ProjectMapper;
import com.ii.pw.edu.pl.master.thesis.project.model.*;
import com.ii.pw.edu.pl.master.thesis.project.model.helper.JiraUrlBuilder;
import com.ii.pw.edu.pl.master.thesis.project.repository.ProjectRepository;
import com.ii.pw.edu.pl.master.thesis.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectServiceImplementation implements ProjectService {

    private final ProjectRepository projectRepository;
    private final JiraClientConfiguration jiraClientConfiguration;
    private final JiraUrlBuilder jiraUrlBuilder;
    private final UserClient userClient;
    private final CredentialClient credentialClient;
    private final ProjectMapper projectMapper;

    // ────────────────────────────────────────────────
    // CREATE (Local DB)
    // ────────────────────────────────────────────────
    @Override
    @Transactional
    public ProjectResponse createProjectLocalDb(CreateProjectRequest request) {
        Objects.requireNonNull(request, "Request cannot be null");

        if (isBlank(request.getKey()))            throw new IllegalArgumentException("Project key is required");
        if (isBlank(request.getProjectName()))    throw new IllegalArgumentException("Project name is required");
        if (isBlank(request.getProjectTypeKey())) throw new IllegalArgumentException("projectTypeKey is required");
        if (isBlank(request.getUsername()))       throw new IllegalArgumentException("username is required");
        if (isBlank(request.getLeadAccountId()))  throw new IllegalArgumentException("leadAccountId is required");

        final String key        = request.getKey().trim().toUpperCase();
        final String name       = request.getProjectName().trim();
        final String type       = request.getProjectTypeKey().trim().toLowerCase();
        final String desc       = emptyToNull(request.getDescription());
        final String username   = request.getUsername().trim();
        final String leadAcctId = request.getLeadAccountId().trim();

        // 1) Get Jira credentials for this user (baseUrl + encrypted token + jira username)
        UserCredentialResponse cred = credentialClient.getCredentialByUserName(username);
        if (cred == null || isBlank(cred.getJiraBaseUrl())
                || isBlank(cred.getToken()) || isBlank(cred.getJiraUsername())) {
            throw new IllegalStateException("Missing Jira credential (baseURL/token/username) for user: " + username);
        }

        final String baseUrl = cred.getJiraBaseUrl();

        // 2) Decrypt token if necessary
        String plainToken = cred.getToken();
        try {
            TokenResponse decrypted = credentialClient.decryptToken(cred.getToken());
            if (decrypted != null && !isBlank(decrypted.getResult())) {
                plainToken = decrypted.getResult();
            }
        } catch (Exception ignored) {
            // If token is already plain, decryption endpoint may fail—ignore
        }

        // 3) Build Jira v3 create-project URL
        final String createUrl = JiraApiEndpoint.PROJECT.buildUrl(baseUrl, "3");

        // 4) Prepare Jira request body (minimal)
        record JiraCreateProjectBody(
                String key,
                String name,
                String projectTypeKey,
                String description,
                String leadAccountId
        ) {}
        JiraCreateProjectBody body = new JiraCreateProjectBody(
                key, name, type, desc, leadAcctId
        );

        // 5) Call Jira
        try {
            // We don't rely on Jira's response structure here — a 201/200 means success.
            jiraClientConfiguration.post(createUrl, body, Map.class, cred.getJiraUsername(), plainToken);
        } catch (HttpClientErrorException.Conflict e) {
            throw new IllegalArgumentException("Jira says project with key '" +
                    key + "' already exists at " + baseUrl
            );
        } catch (HttpStatusCodeException e) {
            String bodyTxt = e.getResponseBodyAsString();
            throw new IllegalArgumentException(
                    "Jira create project failed: HTTP " + e.getStatusCode() + " — " + bodyTxt, e
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Jira create project failed: " + e.getMessage(), e);
        }

        // 6) Upsert local by (key, baseUrl)
        Project entity = projectRepository.findByKeyAndBaseUrl(key, baseUrl).orElse(null);
        final boolean isNew = (entity == null);

        if (isNew) {
            entity = Project.builder()
                    .key(key)
                    .name(name)
                    .projectTypeKey(type)
                    .description(desc)
                    .baseUrl(baseUrl)
                    .build();
        } else {
            entity.setName(name);
            entity.setProjectTypeKey(type);
            entity.setDescription(desc);
            entity.setBaseUrl(baseUrl);
        }

        // 7) Resolve local lead user id from accountId (via User Service through credential client)
        Long leadLocalUserId = null;
        try {
            UserSummary leadSummary = credentialClient.getUserSummaryByAccountId(leadAcctId);
            if (leadSummary != null && leadSummary.getId() != null) {
                leadLocalUserId = leadSummary.getId();
            }
        } catch (Exception ex) {
            log.warn("UserService lookup by accountId failed for '{}': {}", leadAcctId, ex.getMessage());
        }
        if (leadLocalUserId != null) {
            entity.setLeadUserId(String.valueOf(leadLocalUserId));
        }

        // 8) Persist local project
        Project saved = projectRepository.saveAndFlush(entity);

        // 9) Ensure a local ProjectUser link for the lead (optional)
        if (isNew && leadLocalUserId != null) {
            try {
                ProjectUser link = new ProjectUser();
                link.setUserId(String.valueOf(leadLocalUserId));
                saved.addProjectUser(link);               // assumes helper sets back-ref
                projectRepository.saveAndFlush(saved);
            } catch (Exception ex) {
                log.debug("Could not add ProjectUser link locally: {}", ex.getMessage());
            }
        }

        // 10) (Optional) initialize lazy collections if your mapper needs them
        // initProjectCollections(saved);

        // 11) Map to API response
        return projectMapper.fromProjectToResponse(saved);
    }



    // ────────────────────────────────────────────────
    // String helpers
    // ────────────────────────────────────────────────
    private boolean isBlank(String s) { return s == null || s.isBlank(); }
    private String emptyToNull(String s) { if (s == null) return null; String t = s.trim(); return t.isEmpty() ? null : t; }






}
