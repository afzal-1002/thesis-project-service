package com.ii.pw.edu.pl.master.thesis.project.dto.project;

import lombok.Data;

@Data
public class UpdateProjectRequest {
    private String name;              // optional; if provided, update
    private String description;       // optional; if provided, update (empty -> clear)
    private String projectTypeKey;    // optional; if provided, update local only (Jira ignores type changes)

    // Lead: provide ONE of these if you want to update the lead in Jira
    private String leadAccountId;     // preferred for Jira
    private String leadUsernameOrEmail; // fallback helper: we’ll resolve to accountId when possible
}