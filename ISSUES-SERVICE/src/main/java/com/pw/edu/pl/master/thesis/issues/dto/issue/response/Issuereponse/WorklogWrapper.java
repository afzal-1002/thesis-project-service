package com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public  class WorklogWrapper {
    private Integer startAt;
    private Integer maxResults;
    private Integer total;
    private List<Worklog> worklogs;
}
