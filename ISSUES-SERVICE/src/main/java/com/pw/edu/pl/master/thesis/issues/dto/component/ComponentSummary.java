package com.pw.edu.pl.master.thesis.issues.dto.component;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.UserSummary;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ComponentSummary {
    private String self;
    private String id;
    private String name;
    private String description;

    private UserSummary lead;            // may be null
    private String assigneeType;         // e.g., "PROJECT_DEFAULT", "COMPONENT_LEAD"

    private UserSummary assignee;        // resolved assignee if set
    private String realAssigneeType;     // actual assignee type after resolution
    private UserSummary realAssignee;    // actual assignee user
    private Boolean isAssigneeTypeValid; // whether assigneeType is valid for this component

    private String project;              // project key or self (varies)
    private Long projectId;              // numeric id when provided
}
