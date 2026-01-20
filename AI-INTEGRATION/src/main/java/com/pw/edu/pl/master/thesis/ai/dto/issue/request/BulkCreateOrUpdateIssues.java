package com.pw.edu.pl.master.thesis.ai.dto.issue.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pw.edu.pl.master.thesis.ai.dto.issue.CreateIssueRequest;
import lombok.Data;

import java.util.List;

@Data
public class BulkCreateOrUpdateIssues {
    @JsonProperty("issues")
    private List<CreateIssueRequest> createOrUpdateIssues;
}