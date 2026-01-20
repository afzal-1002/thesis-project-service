package com.pw.edu.pl.master.thesis.ai.dto.issue.response.Issuereponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public  class Worklog {
    private String id;
    private String issueId;
    private String self;
    private UserSummary author;
    private UserSummary updateAuthor;
    private Body comment;
    private OffsetDateTime created;
    private OffsetDateTime updated;
    private OffsetDateTime started;
    private String timeSpent;
    private Integer timeSpentSeconds;
    private Visibility visibility;
}
