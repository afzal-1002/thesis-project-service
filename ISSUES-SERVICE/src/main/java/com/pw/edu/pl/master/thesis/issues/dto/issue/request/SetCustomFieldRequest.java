package com.pw.edu.pl.master.thesis.issues.dto.issue.request;


import lombok.Data;

@Data
public class SetCustomFieldRequest {
    private String issueKey;        // e.g., "BUG-5"
    private String customFieldId;   // e.g., "customfield_10200"
    private String value;           // e.g., "Estimation time from AI: 210 days"
}
