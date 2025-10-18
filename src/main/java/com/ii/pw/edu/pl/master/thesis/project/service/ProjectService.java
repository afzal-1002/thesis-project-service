package com.ii.pw.edu.pl.master.thesis.project.service;

import com.ii.pw.edu.pl.master.thesis.project.dto.project.*;
import com.ii.pw.edu.pl.master.thesis.project.model.Project;
import com.ii.pw.edu.pl.master.thesis.project.model.StyleOption;

import java.util.List;

public interface ProjectService {

    ProjectResponse createProjectLocalOnly(CreateProjectRequest request);
    JiraProjectResponse createProjectJira(CreateProjectRequest request);
    ProjectResponse createProjectJiraAndLocal(CreateProjectRequest request);
    List<JiraProjectResponse> getAllProjectsFromJira(ListProjectsRequest request);
    List<ProjectResponse> getAllProjectsFromLocalDb(ListProjectsRequest request);


     JiraProjectResponse fetchFullProject(String baseUrl,  String idOrKey, String username, String token);
    ProjectResponse updateProject(Long id, UpdateProjectRequest request);
    int deleteAllLocalProjectsForCurrentBaseUrl(String baseUrl);
    boolean deleteLocalProjectByKey(DeleteProjectRequest request) ;
    void deleteJiraProjectByKeyOrId(String projectKeyOrId, String username);
     String deleteProjectFromJiraAndLocalDb(DeleteProjectRequest request);

     List<ProjectResponse> syncAllProjects(ListProjectsRequest request);
     ProjectResponse syncProjectByKeyOrId(SyncProjectRequest request);
     ProjectResponse syncProjectFromJira(SyncProjectRequest request);
     ProjectResponse setProjectLead(SetProjectLeadRequest request);
     ProjectResponse syncProjectFromLocalToJira(SyncProjectRequest request);
     List<ProjectResponse> syncAllProjectsFromLocalToJira(SyncProjectRequest request);


}
