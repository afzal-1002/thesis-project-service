package com.pw.edu.pl.master.thesis.ai.dto.issue.issuedetails;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentMediaRef {
    private String id;          // ADF attrs.id
    private String type;        // e.g., "file"
    private String collection;  // ADF attrs.collection (if present)
}