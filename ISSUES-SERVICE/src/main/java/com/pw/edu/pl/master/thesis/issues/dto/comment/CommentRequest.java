package com.pw.edu.pl.master.thesis.issues.dto.comment;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.Body;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.Visibility;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentRequest {
    private Body body;
    private Visibility visibility;
    @JsonProperty("public")
    private Boolean    jsdPublic;
}