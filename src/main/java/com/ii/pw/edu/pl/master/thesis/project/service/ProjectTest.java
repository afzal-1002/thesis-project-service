//package com.ii.pw.edu.pl.master.thesis.project.service;
//
//import com.ii.pw.edu.pl.master.thesis.project.dto.project.*;
//import com.ii.pw.edu.pl.master.thesis.project.model.Project;
//import com.ii.pw.edu.pl.master.thesis.project.model.StyleOption;
//
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//public class ProjectTest {
//}
//package com.ii.pw.edu.pl.master.thesis.project.service;
//
//import com.ii.pw.edu.pl.master.thesis.project.dto.project.*;
//        import com.ii.pw.edu.pl.master.thesis.project.model.Project;
//import com.ii.pw.edu.pl.master.thesis.project.model.StyleOption;
//
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//public interface ProjectService {
//
//    // ──────────────────────────────────────────────────────────────
//    // CREATE (Jira only, Local only, Jira+Local)
//    // ──────────────────────────────────────────────────────────────
//    JiraProjectResponse createProjectJira(CreateProjectRequest request);
//    ProjectResponse createProjectLocalDb(CreateProjectRequest request);
//    ProjectResponse     createProjectJiraLocalDb(CreateProjectRequest request);
//
//    // Convenience: create + persist (your previous name)
//    ProjectResponse     createProject(CreateProjectRequest request);
//
//    // ──────────────────────────────────────────────────────────────
//    // GET (reads, queries, selects)
//    // ──────────────────────────────────────────────────────────────
//
//    List<ProjectTemplate> listTemplatesForType(String projectTypeKey);
//    List<StyleOption>         listStyleOptionsForType(String projectTypeKey);
//
//    // From Jira (rich)
//    List<JiraProjectResponse> getAllProjectsFromJira(ListProjectsRequest request);
//    List<JiraProjectResponse> getAllProjectsFromJira(); // no-arg: current principal
//
//    // Samples from Jira / local
//    ProjectDetailsSummary     getProjectDetailsByKeyOrId(String projectKeyOrId);
//    List<String>              listProjectKeys();
//    Map<String, Object> validateProjectKey(String key);
//    String                    generateProjectKey(String desiredKey);
//    String                    generateProjectName(String desiredName);
//
//    // Selection / finders / local reads
//    ProjectResponse           selectProject(Long projectId);
//    ProjectResponse           getSelectedProject();
//    Optional<Project> findByIdOrKey(ProjectReference reference);
//    Optional<Project>         findById(Long id);
//    Optional<Project>         findByKey(String key);
//    ProjectResponse           buildProjectResponse(Project project);
//    List<ProjectResponse>     getAllProjectsFromDatabase();
//    List<ProjectResponse>     getProjectsByUserAccountId(String accountId);
//    List<ProjectResponse>     getProjectsByLeadAccountId(String accountId);
//
//    // ──────────────────────────────────────────────────────────────
//    // Update
//    // ──────────────────────────────────────────────────────────────
//    ProjectResponse updateProject(Long id, UpdateProjectRequest request);
//    ProjectResponse updateProject(String key, UpdateProjectRequest request);
//
//    // ──────────────────────────────────────────────────────────────
//    // DELETE
//    // ──────────────────────────────────────────────────────────────
//    int     deleteAllLocalProjectsForCurrentBaseUrl();
//    boolean deleteLocalProjectByKey(String projectKey);
//    void    deleteJiraProjectByKeyOrId(String projectKeyOrId);
//    String  deleteProjectFromLocalDb(String projectKey);
//    String  deleteProjectFromJira(String projectKeyOrId);
//    String  deleteProjectFromJiraAndLocalDb(String projectKeyOrId);
//    String  selectProjectByKeyFromJira(String projectKey);
//
//    // ──────────────────────────────────────────────────────────────
//    // SYNC / UPDATE
//    // ──────────────────────────────────────────────────────────────
//    List<ProjectResponse> syncAllProjects();
//    ProjectResponse       syncProjectByKeyOrId(String keyOrId);
//    ProjectResponse       syncProjectFromJira(SyncProjectRequest project);
//    ProjectResponse       setProjectLead(SetProjectLeadRequest request);
//    ProjectResponse       syncProjectFromLocalToJira(SyncProjectRequest request);
//    List<ProjectResponse> syncAllProjectsFromLocalToJira();
//}
