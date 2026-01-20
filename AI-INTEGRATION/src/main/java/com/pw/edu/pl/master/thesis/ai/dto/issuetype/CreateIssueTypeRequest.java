package com.pw.edu.pl.master.thesis.ai.dto.issuetype;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor @AllArgsConstructor @Builder
public class CreateIssueTypeRequest {
    private String name;
    private String type;
    private String description;
    private String hierarchyLevel;
}