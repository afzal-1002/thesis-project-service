package com.pw.edu.pl.master.thesis.ai.dto.issue.issuedetails;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class IssueFields {

    @JsonProperty("created")
    private OffsetDateTime created;

    @JsonProperty("resolutiondate")
    private OffsetDateTime resolutionDate;

    @JsonProperty("status")
    private Status status;
}
