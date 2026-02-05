package com.pw.edu.pl.master.thesis.project.dto.project;

import com.pw.edu.pl.master.thesis.project.dto.issue.IssueSummary;
import com.pw.edu.pl.master.thesis.project.dto.user.UserSummary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    private Long id;
    private String key;
    private String name;
    private String description;
    private String baseUrl;
    private String projectTypeKey;
    private UserSummary lead;
    private List<UserSummary> users;
    private List<IssueSummary> issues;
}