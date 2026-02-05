package com.pw.edu.pl.master.thesis.project.controller;

import com.pw.edu.pl.master.thesis.project.client.ProfileClient;
import com.pw.edu.pl.master.thesis.project.client.UserClient;
import com.pw.edu.pl.master.thesis.project.dto.appuser.AuthUserDTO;
import com.pw.edu.pl.master.thesis.project.dto.project.*;
import com.pw.edu.pl.master.thesis.project.dto.project.*;
import com.pw.edu.pl.master.thesis.project.dto.user.UserSummary;
import com.pw.edu.pl.master.thesis.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wut/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final UserClient userClient;
    private final ProfileClient profileClient;
    private final ProjectService projectService;

    // ──────────────────────────────────────────────────────────────
    // USERS (via user-service)
    // ──────────────────────────────────────────────────────────────
    @GetMapping("/users")
    public List<UserSummary> getAllUsersViaUserService() {
        return userClient.getAllUsers();
    }

    @GetMapping("/authUser")
    public AuthUserDTO getAuthUser() {
        return profileClient.getCurrentUserProfile();
    }


    // ──────────────────────────────────────────────────────────────
    // CREATE
    // ──────────────────────────────────────────────────────────────
    /** Create ONLY in local DB (no Jira call). Requires username in request to resolve baseUrl. */
    @PostMapping("/create/local")
    public ResponseEntity<ProjectResponse> createLocal(@RequestBody CreateProjectRequest request) {
        ProjectResponse created = projectService.createProjectLocalOnly(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** Create in Jira (Cloud/DC) and return Jira payload (rich). */
    @PostMapping("/create/jira")
    public ResponseEntity<JiraProjectResponse> createJira(@RequestBody CreateProjectRequest request) {
        JiraProjectResponse created = projectService.createProjectJira(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** Create in Jira, then upsert into local DB, and return local ProjectResponse. */
    @PostMapping("/create")
    public ResponseEntity<ProjectResponse> createJiraAndLocal(@RequestBody CreateProjectRequest request) {
        ProjectResponse created = projectService.createProjectJiraAndLocal(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ──────────────────────────────────────────────────────────────
    // LIST (READ)
    // ──────────────────────────────────────────────────────────────
    /** List Jira projects for provided username (recommended). */
    @PostMapping("/list/jira")
    public ResponseEntity<List<JiraProjectResponse>> listJira(@RequestParam(name = "baseUrl") String baseUrl) {
        return ResponseEntity.ok(projectService.getAllProjectsFromJira(baseUrl));
    }

    /** List Jira projects for current authenticated principal (if your service reads SecurityContext). */
    @PostMapping("/list/jira/current")
    public ResponseEntity<List<JiraProjectResponse>> getAllProjectsFromJiraForCurrentUserUrl() {
        return ResponseEntity.ok(projectService.getAllProjectsFromJiraForCurrentUserUrl());
    }

    @PostMapping("/list/local/{projectKey}")
    public ResponseEntity<ProjectResponse> getProjectByKey(@PathVariable("projectKey") String projectKey) {
        return ResponseEntity.ok(projectService.getProjectByKey(projectKey));
    }
    @PostMapping("/list/jira/{projectKey}")
    public ResponseEntity<ProjectResponse> getJiraProjectByKey(@PathVariable("projectKey") String projectKey) {
        return ResponseEntity.ok(projectService.getJiraProjectByKey(projectKey));
    }


    /** List local DB projects scoped by user's stored baseUrl. */
    @PostMapping("/list/local")
    public ResponseEntity<List<ProjectResponse>> listLocal(@RequestParam(name = "baseUrl") String baseUrl) {
        return ResponseEntity.ok(projectService.getAllProjectsFromLocalDb(baseUrl));
    }

    // ──────────────────────────────────────────────────────────────
    // UPDATE
    // ──────────────────────────────────────────────────────────────
    /** Update a local project by numeric id (name/description/type/category/lead). */
    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateById(
            @PathVariable Long id,
            @RequestBody UpdateProjectRequest request
    ) {
        return ResponseEntity.ok(projectService.updateProject(id, request));
    }

    /** Update a local project by Jira KEY (e.g., ABC). */
    @PutMapping("/by-key/{id}")
    public ResponseEntity<ProjectResponse> updateByKey( @PathVariable Long id, @RequestBody UpdateProjectRequest request
    ) {
        return ResponseEntity.ok(projectService.updateProject(id, request));
    }

    // ──────────────────────────────────────────────────────────────
    // DELETE
    // ──────────────────────────────────────────────────────────────
    /** Delete all local projects for current principal’s baseUrl. Returns number of rows deleted. */
    @DeleteMapping("/local/all")
    public ResponseEntity<Integer> deleteAllLocalForCurrentBase(@RequestParam String baseUrl) {
        int deleted = projectService.deleteAllLocalProjectsForCurrentBaseUrl(baseUrl);
        return ResponseEntity.ok(deleted);
    }

    /** Delete a single local project by KEY only. Returns true if deleted. */
    @DeleteMapping("/local/{projectKey}")
    public ResponseEntity<Boolean> deleteLocalByKey( @PathVariable String projectKey) {
        boolean ok = projectService.deleteLocalProjectByKey(projectKey);
        return ResponseEntity.ok(ok);
    }

    /** Delete a Jira project by key or id (Jira only). No content on success. */
    @DeleteMapping("/jira/{projectKeyOrId}")
    public void deleteJira(@PathVariable String projectKeyOrId) {
        projectService.deleteJiraProjectByKeyOrId(projectKeyOrId);
    }

    /** Delete from LOCAL DB by key and return a human-readable message. */
    @DeleteMapping("/local/db/{projectKey}")
    public ResponseEntity<Boolean> deleteLocalDb(@PathVariable String projectKey) {
        return ResponseEntity.ok(projectService.deleteLocalProjectByKey(projectKey));
    }

    /** Delete from JIRA by key or id and return a human-readable message. */
    @DeleteMapping("/jira/db/{projectKeyOrId}")
    public ResponseEntity<String> deleteJiraDb(@PathVariable String projectKeyOrId) {
        return ResponseEntity.ok(projectService.deleteProjectFromJiraAndLocalDb(projectKeyOrId));
    }

    /** Delete from Jira and also remove local copy (by key or id). */
    @DeleteMapping("/jira-and-local/{projectKeyOrId}")
    public ResponseEntity<String> deleteJiraAndLocal(@PathVariable String projectKeyOrId) {
        return ResponseEntity.ok(projectService.deleteProjectFromJiraAndLocalDb(projectKeyOrId));
    }

    // ──────────────────────────────────────────────────────────────
    // SELECT (pull from Jira and persist locally as the selected one)
    // ──────────────────────────────────────────────────────────────


    // ──────────────────────────────────────────────────────────────
    // SYNC / UPDATE
    // ──────────────────────────────────────────────────────────────
    /** Sync ALL Jira projects -> local DB for current principal. */
    @PostMapping("/sync/all")
    public ResponseEntity<List<ProjectResponse>> syncAllFromJira() {
        return ResponseEntity.ok(projectService.syncAllProjectsFromJira());
    }

    /** Sync a single project (by Jira key or id) -> local DB for current principal. */
    @PostMapping("/sync/Key")
    public ResponseEntity<ProjectResponse> syncOne(@RequestBody SyncProjectRequest request) {
        return ResponseEntity.ok(projectService.syncProjectByKeyOrId(request));
    }

    /** Sync a single project from Jira -> local DB using explicit username + key/id. */
    @PostMapping("/sync/from-jira")
    public ResponseEntity<ProjectResponse> syncFromJira(@RequestBody SyncProjectRequest request) {
        return ResponseEntity.ok(projectService.syncProjectFromJira(request));
    }

    /** Push LOCAL -> Jira for one project (then refresh local copy). */
    @PostMapping("/sync/local-to-jira")
    public ResponseEntity<ProjectResponse> syncLocalToJira(@RequestBody SyncProjectRequest request) {
        return ResponseEntity.ok(projectService.syncProjectFromLocalToJira(request));
    }

    /** Push LOCAL -> Jira for ALL local projects at user's baseUrl. */
    @PostMapping("/sync/local-to-jira/all")
    public ResponseEntity<List<ProjectResponse>> syncAllLocalToJira() {
        return ResponseEntity.ok(projectService.syncAllProjectsFromLocalToJira());
    }


    /** Set Jira project lead, then refresh local copy. */
    @PostMapping("/lead")
    public ResponseEntity<ProjectResponse> setLead(@RequestBody SetProjectLeadRequest request) {
        return ResponseEntity.ok(projectService.setProjectLead(request));
    }




}
