package com.pw.edu.pl.master.thesis.ai.dto.issue.response.Issuereponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public  class Version {
    private String self;
    private String id;
    private String description;
    private String name;
    private Boolean archived;
    private Boolean released;
    private LocalDate releaseDate;
    private String userReleaseDate;
    private Long projectId;
    private LocalDate startDate;
    private String userStartDate;
    private Boolean overdue;
    private String releaseDescription;
}

