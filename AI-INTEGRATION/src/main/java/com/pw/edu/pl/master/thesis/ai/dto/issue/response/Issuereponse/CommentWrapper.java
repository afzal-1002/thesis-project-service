package com.pw.edu.pl.master.thesis.ai.dto.issue.response.Issuereponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public  class CommentWrapper {
    private int startAt;
    private int maxResults;
    private int total;
    private List<CommentResponse> comments;
}
