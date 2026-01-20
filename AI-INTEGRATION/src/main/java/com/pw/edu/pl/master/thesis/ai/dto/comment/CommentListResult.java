package com.pw.edu.pl.master.thesis.ai.dto.comment;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pw.edu.pl.master.thesis.ai.dto.issue.response.Issuereponse.CommentResponse;
import lombok.*;

import java.util.List;

@Getter
@Setter @ToString @Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public   class CommentListResult {
    private int total;
    private int startAt;
    private int maxResults;

    @JsonProperty("comments")
    private List<CommentResponse> comments;
}