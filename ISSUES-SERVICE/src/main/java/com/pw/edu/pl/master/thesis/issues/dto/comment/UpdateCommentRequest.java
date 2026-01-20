package com.pw.edu.pl.master.thesis.issues.dto.comment;

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
public class UpdateCommentRequest {
    private Body body;
    private Visibility visibility;
}
