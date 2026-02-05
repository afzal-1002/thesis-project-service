package com.pw.edu.pl.master.thesis.project.dto.project;

import lombok.Data;

@Data
public class UpdateProjectRequest {
    private String projectName;              // optional; if provided, update
    private String description;       // optional; if provided, update (empty -> clear)
    private String projectTypeKey;    // optional; if provided, update local only (Jira ignores type changes)

    private String leadAccountId;
    private String leadUsernameOrEmail;
}