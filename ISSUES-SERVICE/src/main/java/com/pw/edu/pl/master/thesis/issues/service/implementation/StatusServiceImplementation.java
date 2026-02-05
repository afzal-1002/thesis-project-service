package com.pw.edu.pl.master.thesis.issues.service.implementation;

import com.pw.edu.pl.master.thesis.issues.client.CredentialClient;
import com.pw.edu.pl.master.thesis.issues.configuration.JiraClientConfiguration;
import com.pw.edu.pl.master.thesis.issues.dto.credentials.UserCredentialResponse;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.IssueTypeResponse;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.StatusCategory;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.StatusResponse;
import com.pw.edu.pl.master.thesis.issues.enums.JiraApiEndpoint;
import com.pw.edu.pl.master.thesis.issues.exception.ResourceNotFoundException;
import com.pw.edu.pl.master.thesis.issues.dto.helper.JiraUrlBuilder;
import com.pw.edu.pl.master.thesis.issues.model.status.Status;
import com.pw.edu.pl.master.thesis.issues.repository.StatusRepository;
import com.pw.edu.pl.master.thesis.issues.service.StatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatusServiceImplementation implements StatusService {

    private final StatusRepository statusRepository;
    private final JiraClientConfiguration jiraClientConfiguration;
    private final JiraUrlBuilder jiraUrlBuilder;
    private final CredentialClient credentialClient;

    private UserCredentialResponse currentCred() {
        UserCredentialResponse cred = credentialClient.getMine();
        if (cred == null || isBlank(cred.getBaseUrl()) || isBlank(cred.getToken()) || isBlank(cred.getUsername())) {
            throw new IllegalStateException("Missing Jira credential (username/baseUrl/token) for current user");
        }
        cred.setBaseUrl(jiraUrlBuilder.normalizeJiraBaseUrl(cred.getBaseUrl()));
        return cred;
    }

    @Override
    @Transactional
    public List<StatusResponse> synchronizeStatusesForProject(String projectKey) {
        log.info("🔄 Synchronizing statuses for Jira project key: {}", projectKey);
        if (isBlank(projectKey)) throw new ResourceNotFoundException("Project key cannot be null/blank.");

        List<StatusResponse> statusResponseResponseList = getStatusesByProjectKey(projectKey);

        for (StatusResponse response : statusResponseResponseList) {
            Status status = Status.builder()
                    .name(response.getName())
                    .description(response.getDescription())
                    .iconUrl(response.getIconUrl())
                    .jiraId(response.getId())
                    .build();

            StatusCategory category = response.getStatusCategory();
            status.setCategory(category == null ? null :
                    StatusCategory.builder()
                            .id(category.getId())
                            .key(category.getKey())
                            .name(category.getName())
                            .colorName(category.getColorName())
                            .self(category.getSelf())
                            .build());

            saveStausIfNotExistsOrUpdate(status);
        }
        return statusResponseResponseList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StatusResponse> getStatusesByProjectKey(String projectKey) {
        if (isBlank(projectKey)) throw new IllegalArgumentException("projectKey is required");
        log.info("📥 Fetching statuses for Jira project key: {}", projectKey);
        UserCredentialResponse cred = currentCred();
        String baseUrl = cred.getBaseUrl();
        String jiraUser = cred.getUsername();
        String token = cred.getToken();
        String projectStatusesUrl = jiraUrlBuilder.url(baseUrl, JiraApiEndpoint.PROJECT) + "/" + projectKey + "/statuses";
        IssueTypeResponse[] issueTypeGroups;
        try {
            issueTypeGroups = jiraClientConfiguration.get(projectStatusesUrl, IssueTypeResponse[].class, jiraUser, token);
            log.info("✅ Received status groups for project '{}'", projectKey);
        } catch (Exception ex) {
            log.error("❌ Error calling Jira: {}", ex.getMessage(), ex);
            throw new RuntimeException("Unable to fetch statuses for project: " + projectKey);
        }

        Set<StatusResponse> uniqueStatuses = new HashSet<>();
        for (IssueTypeResponse issueTypeGroup : issueTypeGroups) {
            if (issueTypeGroup != null && issueTypeGroup.getStatuses() != null) {

                for (StatusResponse statusResponse : issueTypeGroup.getStatuses()) {
                    if (statusResponse != null) {
                        uniqueStatuses.add(statusResponse);
                    }
                }
            }
        }

        List<StatusResponse> flatStatuses = new ArrayList<>(uniqueStatuses);
        log.info("🔍 Found {} unique statuses for project '{}'", flatStatuses.size(), projectKey);

        return flatStatuses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StatusResponse> getAllStatusesFromJira() {
        log.info("🌐 Fetching all global statuses from Jira");
        UserCredentialResponse cred = currentCred();
        String url = jiraUrlBuilder.url(cred.getBaseUrl(), JiraApiEndpoint.STATUS);
        StatusResponse[] arr = jiraClientConfiguration.get(url, StatusResponse[].class, cred.getUsername(), cred.getToken());
        return arr == null ? List.of() : Arrays.asList(arr);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Status> getAllStatuses() {
        return statusRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Status getStatusById(String id) {
        return statusRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new ResourceNotFoundException("StatusResponse not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Status> getStatusByName(String name) {
        return statusRepository.findByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Status> getStatusesByCategoryName(String categoryName) {
        return statusRepository.findByCategory_Name(categoryName);
    }

    @Override
    @Transactional
    public Status createOrUpdateStatus(Status status) {
        return  saveStausIfNotExistsOrUpdate(status);
    }

    // In StatusServiceImplementation.java
    @Override
    @Transactional
    public Status saveStausIfNotExistsOrUpdate(Status status) {
        if (status == null || isBlank(status.getName())) {
            log.warn("Attempted to save/update Status with missing name. Skipping persistence for: {}", status);
            return null;
        }

        Optional<Status> existing = statusRepository.findByName(status.getName());

        if (existing.isPresent()) {
            Status toUpdate = existing.get();
            // ... update logic
            toUpdate.setCategory(status.getCategory());
            toUpdate.setJiraId(status.getJiraId());
            return statusRepository.save(toUpdate);
        } else {
            return statusRepository.save(status);
        }
    }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
}