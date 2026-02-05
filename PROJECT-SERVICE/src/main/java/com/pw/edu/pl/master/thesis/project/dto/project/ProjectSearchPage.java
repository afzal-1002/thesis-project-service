package com.pw.edu.pl.master.thesis.project.dto.project;

import lombok.Data;

import java.util.List;

@Data
public   class ProjectSearchPage {
    private boolean isLast;
    private int maxResults;
    private int startAt;
    private int total;
    private List<ProjectSummary> values;
}