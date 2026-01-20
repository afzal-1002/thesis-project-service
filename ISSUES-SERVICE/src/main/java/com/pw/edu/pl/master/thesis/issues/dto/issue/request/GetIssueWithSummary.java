package com.pw.edu.pl.master.thesis.issues.dto.issue.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetIssueWithSummary {
    private String projectKey;
    private String summary;
}
