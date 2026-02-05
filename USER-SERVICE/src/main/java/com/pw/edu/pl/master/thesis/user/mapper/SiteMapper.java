package com.pw.edu.pl.master.thesis.user.mapper;


import com.pw.edu.pl.master.thesis.user.dto.site.SiteProjectSummary;
import com.pw.edu.pl.master.thesis.user.dto.site.SiteResponse;
import com.pw.edu.pl.master.thesis.user.model.user.User;
import com.pw.edu.pl.master.thesis.user.dto.user.UserSummary;
import com.pw.edu.pl.master.thesis.user.model.site.Site;
import com.pw.edu.pl.master.thesis.user.model.site.SiteProject;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Maps between Site-related entities and DTOs.
 */
@Component
public class SiteMapper {

    public SiteResponse toResponse(Site site) {
        if (site == null) return null;

        List<SiteProjectSummary> projectSummaries =  site.getProjects().stream()
                .map(project -> {
                    return SiteProjectSummary.builder()
                            .id(project.getId())
                            .projectKey(project.getKey())
                            .projectName(project.getName())
                            .jiraId(project.getJiraId())
                            .build();
                }).toList();

        return SiteResponse.builder()
                .id(site.getId())
                .siteName(site.getSiteName())
                .hostPart(site.getHostPart())
                .baseURL(site.getBaseUrl())
                .createdAt(site.getCreationDate())
                .updatedAt(site.getUpdateDate())
                .projects(projectSummaries)
                .build();
    }

    public UserSummary toUserSummary(User user) {
        if (user == null) return null;

        return UserSummary.builder()
                .id(user.getId())
                .username(user.getUsername())
                .emailAddress(user.getEmailAddress())
                .build();
    }



    public List<UserSummary> toUserSummaryList(List<User> users) {
        if (users == null || users.isEmpty()) return List.of();
        return users.stream()
                .map(this::toUserSummary)
                .collect(Collectors.toList());
    }


    public SiteProjectSummary toProjectSummary(SiteProject p) {
        return SiteProjectSummary.builder()
                .id(p.getId())
                .projectKey(p.getKey())
                .projectName(p.getName())
                .jiraId(p.getJiraId())
                .build();
    }



    public SiteResponse toResponseWithoutProjects(Site s) {
        if (s == null) return null;
        return SiteResponse.builder()
                .id(s.getId())
                .siteName(s.getSiteName())
                .hostPart(s.getHostPart())
                .baseURL(s.getBaseUrl())
                .createdAt(s.getCreationDate()) // entity field names: creationDate/updateDate
                .updatedAt(s.getUpdateDate())
                .build();
    }
}
