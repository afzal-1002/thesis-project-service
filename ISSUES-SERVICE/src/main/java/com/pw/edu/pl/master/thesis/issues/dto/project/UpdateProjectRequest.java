package com.pw.edu.pl.master.thesis.issues.dto.project;

import lombok.Data;

@Data
public class UpdateProjectRequest {
    private String projectName;              // optional; if provided, update
    private String description;       // optional; if provided, update (empty -> clear)
    private String projectTypeKey;    // optional; if provided, update local only (Jira ignores type changes)

    // Lead: provide ONE of these if you want to update the lead in Jira
    private String leadAccountId;
    private String baseUrl;
    private String leadUsernameOrEmail;
    private String username;
}