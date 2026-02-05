package com.pw.edu.pl.master.thesis.ai.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;



@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class IssueType {
    private String  self;
    private String  id;
    private String  description;
    private String  iconUrl;
    private String  name;
    private boolean subtask;
    private Integer avatarId;
    private Integer hierarchyLevel;
}