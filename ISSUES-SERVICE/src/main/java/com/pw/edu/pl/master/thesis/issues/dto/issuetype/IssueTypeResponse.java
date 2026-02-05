package com.pw.edu.pl.master.thesis.issues.dto.issuetype;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder @NoArgsConstructor
@AllArgsConstructor
@Data
public class IssueTypeResponse {
    private String id;
    private String name;
    private String description;
    private String iconUrl;
    private Boolean subtask;
}
