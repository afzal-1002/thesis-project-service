package com.pw.edu.pl.master.thesis.ai.dto.project;

import lombok.Data;


@Data
// Keep exactly these optional fields; only projectKey is always required.
public class SetProjectLeadRequest {
    private String projectKey;     // required
    private String accountId;      // Jira accountId (preferred if present)
    private String username;   // legacy; Cloud often uses accountId instead of username
}

