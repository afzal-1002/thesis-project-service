package com.pw.edu.pl.master.thesis.issues.dto.comment;

import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.CommentResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CommentListEnvelope {
    private List<CommentResponse> comments; // in input order
    private List<String> missingIds;        // input ids that Jira didn’t return
}
