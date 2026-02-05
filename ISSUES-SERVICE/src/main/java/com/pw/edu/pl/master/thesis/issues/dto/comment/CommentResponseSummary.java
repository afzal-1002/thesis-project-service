package com.pw.edu.pl.master.thesis.issues.dto.comment;

import com.pw.edu.pl.master.thesis.issues.dto.issue.IssueSummary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponseSummary {
    private String self;
    private String id;
    private String authorDisplayName;
    private OffsetDateTime created;
    private OffsetDateTime updated;
    private String issueKey;
    private IssueSummary issueSummary;
}


