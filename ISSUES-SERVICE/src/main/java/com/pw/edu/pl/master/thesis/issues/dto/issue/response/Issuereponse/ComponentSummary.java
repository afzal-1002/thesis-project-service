package com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public  class ComponentSummary {
    private String self;
    private String id;
    private String name;
    private String description;
    private UserSummary lead;
    private String assigneeType;
    private UserSummary assignee;
    private String realAssigneeType;
    private UserSummary realAssignee;
    private Boolean isAssigneeTypeValid;
    private String project;
    private Long projectId;
}
