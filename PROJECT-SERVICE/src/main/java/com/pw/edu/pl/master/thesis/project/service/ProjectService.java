package com.pw.edu.pl.master.thesis.project.service;


import com.pw.edu.pl.master.thesis.project.dto.project.*;
import com.pw.edu.pl.master.thesis.project.dto.project.*;

import java.util.List;

public interface ProjectService {

    ProjectResponse createProjectLocalOnly(CreateProjectRequest request);

    JiraProjectResponse createProjectJira(CreateProjectRequest request);

    ProjectResponse createProjectJiraAndLocal(CreateProjectRequest request);
    ProjectResponse getProjectByKey(String projectKey);
    ProjectResponse getJiraProjectByKey(String projectKey);

    List<JiraProjectResponse> getAllProjectsFromJira(String request);

    List<JiraProjectResponse> getAllProjectsFromJiraForCurrentUserUrl();


    JiraProjectResponse fetchFullProject(String baseUrl, String idOrKey, String jiraUser, String token);

    List<ProjectResponse> getAllProjectsFromLocalDb(String request);

    ProjectResponse updateProject(Long id, UpdateProjectRequest request);

    int deleteAllLocalProjectsForCurrentBaseUrl(String ignored);

    boolean deleteLocalProjectByKey(String projectKey);

    /** OLD: deleteJiraProjectByKeyOrId(String projectKeyOrId, String username) */
    void deleteJiraProjectByKeyOrId(String projectKeyOrId);

    String deleteProjectFromJiraAndLocalDb(String request);

    List<ProjectResponse> syncAllProjectsFromJira();

    ProjectResponse syncProjectByKeyOrId(SyncProjectRequest request);

    /** OLD: took username from request; now it must not depend on username */
    ProjectResponse syncProjectFromJira(SyncProjectRequest request);

    /** Local → Jira sync without username in request */
    ProjectResponse syncProjectFromLocalToJira(SyncProjectRequest request);

    List<ProjectResponse> syncAllProjectsFromLocalToJira();

    public ProjectResponse setProjectLead(SetProjectLeadRequest request);
}
