package com.pw.edu.pl.master.thesis.ai.dto.issue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueSummary {
    private String id;
    private String key;
    private String summary;
    private String description;
    private String status;
    private String issueType;
    private String projectKey;
}

