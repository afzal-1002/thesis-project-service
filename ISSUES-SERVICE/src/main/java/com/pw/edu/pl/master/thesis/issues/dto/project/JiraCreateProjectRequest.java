package com.pw.edu.pl.master.thesis.issues.dto.project;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JiraCreateProjectRequest {
    private String key;
    private String name;
    private String projectTypeKey;
    private String projectTemplateKey;
    private String description;
    private String assigneeType;         // "PROJECT_LEAD" | "UNASSIGNED"
    private String leadAccountId;        // optional

    // Optional extras supported by Jira (uncomment if you use them)
    // private Integer permissionScheme;
    // private Integer notificationScheme;
    // private Integer issueSecurityScheme;
    // private Integer categoryId;
    // private Integer avatarId;
    // private Boolean simplified;
    // private String url;
}