package com.ii.pw.edu.pl.master.thesis.project.service;

import com.ii.pw.edu.pl.master.thesis.project.dto.project.CreateProjectRequest;
import com.ii.pw.edu.pl.master.thesis.project.dto.project.ProjectResponse;

public interface ProjectService {

    // ────────────────────────────────────────────────
    // CREATE METHODS
    // ────────────────────────────────────────────────
//    JiraProjectResponse createProjectJira(CreateProjectMinimalRequest request);
    ProjectResponse     createProjectLocalDb(CreateProjectRequest request);
//    public List<ProjectSummary> getAllProjectsFromJira();
}
