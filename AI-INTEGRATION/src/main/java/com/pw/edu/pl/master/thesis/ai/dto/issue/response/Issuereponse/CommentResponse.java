package com.pw.edu.pl.master.thesis.ai.dto.issue.response.Issuereponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public  class CommentResponse {
    private String self;
    private String id;
    private UserSummary author;
    @JsonProperty("body")
    private Body body;
    private UserSummary updateAuthor;
    private OffsetDateTime created;
    private OffsetDateTime updated;
    private Visibility visibility;
    @JsonProperty("jsdPublic")
    private Boolean jsdPublic;

}

