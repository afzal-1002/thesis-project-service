package com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public  class PriorityResponse {
    private String self;
    private String iconUrl;
    private String name;
    private String id;
    private String description;
    @JsonProperty("statusColor")
    private String statusColor;
}
