package com.pw.edu.pl.master.thesis.ai.dto.issue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor @Builder
public class ParentIssue {
    private String id;
    private String key;
    private String self;
    private ParentFields fields;
}