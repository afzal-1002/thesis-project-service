package com.pw.edu.pl.master.thesis.issues.dto.issue;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.IssueResponse;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class IssueSearchResult {
    private String expand;
    private int startAt;
    private int maxResults;
    private int total;
    private List<IssueResponse> issues;
}