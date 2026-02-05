package com.pw.edu.pl.master.thesis.ai.dto.issue.issuedetails;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IssueDetails {
    private String title;
    private String description;
    private boolean hasAttachment;
    private List<CommentDetail> comments;

    private IssueFields fields;

    public boolean hasAttachment() { return hasAttachment; }
}