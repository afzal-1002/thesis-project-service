package com.pw.edu.pl.master.thesis.issues.dto.issue.request;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class JQLIssueSummary {
    private String jql;
    private Integer maxResults;
    private String nextPageToken;
    private String expand;
    private String summary;
    private List<String> fields;
    private Boolean fieldsByKeys;
    private List<String> properties;
    private List<Integer> reconcileIssues;
}