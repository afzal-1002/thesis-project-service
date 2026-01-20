package com.pw.edu.pl.master.thesis.ai.dto.issue.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class GetIssuesListRequest {
    @JsonProperty("issueIdsOrKeys")
    private List<String> issueIdsOrKeys;
}

