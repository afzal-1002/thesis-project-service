package com.pw.edu.pl.master.thesis.ai.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pw.edu.pl.master.thesis.ai.dto.issue.response.Issuereponse.UserSummary;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Worklog {
    private UserSummary author;
    private Body comment;
    private String       id;
    private String       issueId;
    private String       self;
    private OffsetDateTime started;
    private String       timeSpent;
    private int          timeSpentSeconds;
    private UserSummary updateAuthor;
    private OffsetDateTime updated;
}
