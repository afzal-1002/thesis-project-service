package com.pw.edu.pl.master.thesis.issues.mapper;


import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.StatusCategory;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.StatusResponse;
import com.pw.edu.pl.master.thesis.issues.dto.issuestatus.Scope;
import com.pw.edu.pl.master.thesis.issues.dto.issuestatus.ScopeProject;
import com.pw.edu.pl.master.thesis.issues.model.status.Status;
import org.springframework.stereotype.Component;

@Component
public class StatusMapper {

    public Status mapFromJira(StatusResponse dto) {
        return mapFromJira(dto, null);
    }

    public Status mapFromJira(StatusResponse statusResponse, Long ignoredProjectId) {
        if (statusResponse == null) {
            return null;
        }

        // Build the statusResponse entity
        Status.StatusBuilder statusBuilder = Status.builder()
                .name(statusResponse.getName())
                .description(statusResponse.getDescription())
                .iconUrl(statusResponse.getIconUrl());

        // Map statusResponse category
        if (statusResponse.getStatusCategory() != null) {
            statusBuilder.category(StatusCategory.builder()
                    .id(statusResponse.getStatusCategory().getId())
                    .key(statusResponse.getStatusCategory().getKey())
                    .name(statusResponse.getStatusCategory().getName())
                    .colorName(statusResponse.getStatusCategory().getColorName())
                    .build());
        }

        // Map scope if provided
        if (statusResponse.getScope() != null) {
            statusBuilder.scope(Scope.builder()
                    .type(statusResponse.getScope().getType())
                    .project(statusResponse.getScope().getProject() != null
                            ? ScopeProject.builder()
                            .id(statusResponse.getScope().getProject().getId())
                            .build()
                            : null)
                    .build());
        }

        return statusBuilder.build();
    }
}
