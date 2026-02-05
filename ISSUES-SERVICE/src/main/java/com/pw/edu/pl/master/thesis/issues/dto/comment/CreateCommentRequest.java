package com.pw.edu.pl.master.thesis.issues.dto.comment;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.Body;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.Visibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCommentRequest {
    private Body body;
    private Visibility visibility;

    @JsonAlias({ "public" })
    @JsonProperty("public")
    private Boolean isPublic;
}