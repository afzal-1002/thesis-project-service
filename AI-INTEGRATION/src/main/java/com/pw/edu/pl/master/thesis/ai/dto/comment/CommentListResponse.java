package com.pw.edu.pl.master.thesis.ai.dto.comment;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pw.edu.pl.master.thesis.ai.dto.issue.response.Issuereponse.CommentResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentListResponse {
    private Integer maxResults;
    private Long startAt;
    private Long total;
    private Boolean isLast;

    @JsonProperty("values")
    private List<CommentResponse> comments;

}