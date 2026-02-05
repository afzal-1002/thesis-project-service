package com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public  class Attachment {
    private String id;
    private String self;
    private String filename;
    private UserSummary author;
    private OffsetDateTime created;
    private Long size;
    @JsonProperty("mimeType")
    private String mimeType;
    private String content;
    private String thumbnail;
}
