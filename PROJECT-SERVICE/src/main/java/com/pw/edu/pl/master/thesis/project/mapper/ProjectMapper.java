package com.pw.edu.pl.master.thesis.project.mapper;

import com.pw.edu.pl.master.thesis.project.client.CredentialClient;
import com.pw.edu.pl.master.thesis.project.client.UserClient;
import com.pw.edu.pl.master.thesis.project.dto.issue.IssueSummary;
import com.pw.edu.pl.master.thesis.project.dto.project.JiraProjectResponse;
import com.pw.edu.pl.master.thesis.project.dto.project.ProjectResponse;
import com.pw.edu.pl.master.thesis.project.dto.user.UserSummary;
import com.pw.edu.pl.master.thesis.project.model.Project;
import com.pw.edu.pl.master.thesis.project.model.ProjectUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class ProjectMapper {

    private final UserClient userClient;
    private final CredentialClient credentialClient;
//    private final IssueService issueService; // <- or remove if you don’t have issues yet

    public ProjectResponse fromProjectToResponse(Project project) {
        if (project == null) return null;

        // ── Lead ─────────────────────────────────────────
        UserSummary lead = null;
        String leadIdRaw = project.getLeadUserId();
        if (leadIdRaw != null && !leadIdRaw.isBlank()) {
            try {
                // Try local numeric user id first
                Long localId = Long.parseLong(leadIdRaw.trim());
                lead = userClient.getUserById(localId);
            } catch (NumberFormatException nfe) {
                // Not a number → treat as Jira accountId
                try {
                    lead = credentialClient.getUserSummaryByAccountId(leadIdRaw.trim());
                } catch (Exception ignored) { /* leave null */ }
            } catch (Exception ignored) { /* leave null */ }
        }

        // ── Members ──────────────────────────────────────
        List<UserSummary> users = new ArrayList<>();
        if (project.getProjectUsers() != null) {
            for (ProjectUser pu : project.getProjectUsers()) {
                if (pu == null || pu.getUserId() == null || pu.getUserId().isBlank()) continue;
                try {
                    Long uid = Long.parseLong(pu.getUserId().trim());
                    UserSummary u = userClient.getUserById(uid);
                    if (u != null) users.add(u);
                } catch (NumberFormatException nfe) {
                    // fallback: Jira accountId
                    try {
                        UserSummary u = credentialClient.getUserSummaryByAccountId(pu.getUserId().trim());
                        if (u != null) users.add(u);
                    } catch (Exception ignored) {}
                } catch (Exception ignored) {}
            }
            users = users.stream().filter(Objects::nonNull).distinct().toList();
        }

        // ── Issues (placeholder) ─────────────────────────
        List<IssueSummary> issues = List.of(); // keep as-is unless you wire it

        // ── Map all fields (include projectTypeKey!) ─────
        return ProjectResponse.builder()
                .id(project.getId())
                .key(project.getKey())
                .name(project.getName())
                .description(project.getDescription())
                .baseUrl(project.getBaseUrl())
                .projectTypeKey(project.getProjectTypeKey()) // <-- missing before
                .lead(lead)
                .users(users)
                .issues(issues)
                .build();
    }

    public JiraProjectResponse fromProjectToJiraResponse(Project project) {
        if (project == null) return null;
        UserSummary lead = null;
        String leadIdRaw = project.getLeadUserId();
        if (leadIdRaw != null && !leadIdRaw.isBlank()) {
            try {
                // Try local numeric user id first
                Long localId = Long.parseLong(leadIdRaw.trim());
                lead = userClient.getUserById(localId);
            } catch (NumberFormatException nfe) {
                // Not a number → treat as Jira accountId
                try {
                    lead = credentialClient.getUserSummaryByAccountId(leadIdRaw.trim());
                } catch (Exception ignored) { /* leave null */ }
            } catch (Exception ignored) { /* leave null */ }
        }

        // ── Properties (carry over what we have locally) ──────────────────────
        Map<String, Object> properties = new HashMap<>();
        if (project.getProjectCategory() != null && !project.getProjectCategory().isBlank()) {
            properties.put("projectCategory", project.getProjectCategory());
        }

        // ── Build Jira-like response from local data ──────────────────────────
        return JiraProjectResponse.builder()
                .expand(null)
                .self(null) // not known from local DB
                .id(project.getJiraId()) // Jira’s id is a String in your model
                .key(project.getKey())
                .description(project.getDescription())
                .lead(lead)

                // Jira-only collections we don't keep locally → empty
                .issueTypes(List.of())
                .assigneeType(null)
                .versions(List.of())
                .roles(null)

                .name(project.getName())
                .projectTypeKey(project.getProjectTypeKey())
                .projectTemplateKey(null)
                .simplified(false)
                .style(null)
                .isPrivate(false)
                .properties(properties)
                .avatarUrls(null) // not stored locally
                .build();
    }


}
