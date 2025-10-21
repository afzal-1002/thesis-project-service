package com.ii.pw.edu.pl.master.thesis.project.mapper;

import com.ii.pw.edu.pl.master.thesis.project.client.CredentialClient;
import com.ii.pw.edu.pl.master.thesis.project.client.UserClient;
import com.ii.pw.edu.pl.master.thesis.project.dto.issue.IssueSummary;
import com.ii.pw.edu.pl.master.thesis.project.dto.project.ProjectResponse;
import com.ii.pw.edu.pl.master.thesis.project.dto.user.UserSummary;
import com.ii.pw.edu.pl.master.thesis.project.model.Project;
import com.ii.pw.edu.pl.master.thesis.project.model.ProjectUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

}
