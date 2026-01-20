package com.pw.edu.pl.master.thesis.ai.enums;

import lombok.Data;

@Data
public class JiraUserSearchResult {
    private String accountId;
    private String displayName;
    private String emailAddress; // Jira Cloud often omits this unless permitted
}
