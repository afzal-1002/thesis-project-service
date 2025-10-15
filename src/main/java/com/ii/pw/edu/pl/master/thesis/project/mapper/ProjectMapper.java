package com.ii.pw.edu.pl.master.thesis.project.mapper;

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

    private final UserClient userClient;     // <- adjust to your client
//    private final IssueService issueService; // <- or remove if you don’t have issues yet

    public ProjectResponse fromProjectToResponse(Project project) {
        if (project == null) return null;

        // Lead
        UserSummary lead = null;
        if (project.getLeadUserId() != null && !project.getLeadUserId().isBlank()) {
            try {
                Long leadId = Long.parseLong(project.getLeadUserId());
                lead = userClient.getUserById(leadId); // rename to your actual method
            } catch (NumberFormatException ignored) {
                // leadUserId wasn’t a Long; leave null
            } catch (Exception ignored) {
                // remote call failed; leave null
            }
        }

        // Users (project members)
        List<UserSummary> users = new ArrayList<>();
        if (project.getProjectUsers() != null) {
            for (ProjectUser pu : project.getProjectUsers()) {
                if (pu == null || pu.getUserId() == null || pu.getUserId().isBlank()) continue;
                try {
                    Long uid = Long.parseLong(pu.getUserId());
                    UserSummary u = userClient.getUserById(uid); // rename to your actual method
                    if (u != null) users.add(u);
                } catch (Exception ignored) {
                    // skip missing/bad ids
                }
            }
            // de-dup, just in case
            users = users.stream().filter(Objects::nonNull).distinct().toList();
        }

        // Issues
        List<IssueSummary> issues = new ArrayList<>();
        try {
            // Prefer a “by project key” method if you have one
//            issues = issueService.findSummariesByProjectKey(project.getKey());
            issues = new ArrayList<>();
        } catch (Exception ignored) {
            // leave empty list if not available
        }


        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .key(project.getKey())
                .baseUrl(project.getBaseUrl())
                .issues(issues)
                .lead(lead)
                .users(users)
                .build();

    }
}
