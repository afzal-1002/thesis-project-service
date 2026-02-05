package com.pw.edu.pl.master.thesis.issues.client;

import com.pw.edu.pl.master.thesis.issues.configuration.FeignSecurityConfiguration;
import com.pw.edu.pl.master.thesis.issues.dto.appuser.AuthUserDTO;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.UserSummary;
import com.pw.edu.pl.master.thesis.issues.dto.project.*;
import com.pw.edu.pl.master.thesis.issues.dto.project.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;


import org.springframework.web.bind.annotation.*;

import java.util.List;

//@FeignClient(
//        name = "project-service",
//        url = "${project.service.base-url}",
//        path = "/api/wut/projects",
//        configuration = FeignSecurityConfiguration .class)

@FeignClient(
        name = "USER-SERVICE",
        contextId = "ProjectClient",
        path = "/api/wut/projects",
        configuration = FeignSecurityConfiguration.class
)
public interface ProjectClient {

    @GetMapping("/users")
    List<UserSummary> getAllUsersViaUserService();

    @GetMapping("/authUser")
    AuthUserDTO getAuthUser();

    // ──────────────────────────────────────────────────────────────
    // CREATE
    // ──────────────────────────────────────────────────────────────
    /** Create ONLY in local DB (no Jira call). Requires username in request to resolve baseUrl. */
    @PostMapping("/create/local")
    public ProjectResponse createLocal(@RequestBody CreateProjectRequest request);

    /** Create in Jira (Cloud/DC) and return Jira payload (rich). */
    @PostMapping("/create/jira")
    public ResponseEntity<JiraProjectResponse> createJira(@RequestBody CreateProjectRequest request);

    /** Create in Jira, then upsert into local DB, and return local ProjectResponse. */
    @PostMapping("/create")
    public ProjectResponse createJiraAndLocal(@RequestBody CreateProjectRequest request);

    // ──────────────────────────────────────────────────────────────
    // LIST (READ)
    // ──────────────────────────────────────────────────────────────
    /** List Jira projects for provided username (recommended). */
    @PostMapping("/list/jira")
    public ResponseEntity<List<JiraProjectResponse>> listJira(@RequestParam(name = "baseUrl") String baseUrl);

    /** List Jira projects for current authenticated principal (if your service reads SecurityContext). */
    @PostMapping("/list/jira/current")
    ResponseEntity<List<JiraProjectResponse>> getAllProjectsFromJiraForCurrentUserUrl();

    @PostMapping("/list/local/{projectKey}")
    ProjectResponse getProjectByKey(@PathVariable("projectKey") String projectKey);

    @PostMapping("/list/jira/{projectKey}")
    ProjectResponse getJiraProjectByKey(@PathVariable("projectKey") String projectKey);

    /** List local DB projects scoped by user's stored baseUrl. */
    @PostMapping("/list/local")
    List<ProjectResponse> listLocal(@RequestParam(name = "baseUrl") String baseUrl);

    // ──────────────────────────────────────────────────────────────
    // UPDATE
    // ──────────────────────────────────────────────────────────────
    /** Update a local project by numeric id (name/description/type/category/lead). */
    @PutMapping("/{id}")
    ProjectResponse updateById( @PathVariable Long id, @RequestBody UpdateProjectRequest request);

    /** Update a local project by Jira KEY (e.g., ABC). */
    @PutMapping("/by-key/{id}")
    ProjectResponse updateByKey( @PathVariable Long id, @RequestBody UpdateProjectRequest request);

    // ──────────────────────────────────────────────────────────────
    // DELETE
    // ──────────────────────────────────────────────────────────────
    /** Delete all local projects for current principal’s baseUrl. Returns number of rows deleted. */
    @DeleteMapping("/local/all")
    ResponseEntity<Integer> deleteAllLocalForCurrentBase(@RequestParam String baseUrl);

    /** Delete a single local project by KEY only. Returns true if deleted. */
    @DeleteMapping("/local/{projectKey}")
     ResponseEntity<Boolean> deleteLocalByKey( @PathVariable String projectKey);

    /** Delete a Jira project by key or id (Jira only). No content on success. */
    @DeleteMapping("/jira/{projectKeyOrId}")
    void deleteJira(@PathVariable String projectKeyOrId);

    /** Delete from LOCAL DB by key and return a human-readable message. */
    @DeleteMapping("/local/db/{projectKey}")
    ResponseEntity<Boolean> deleteLocalDb(@PathVariable String projectKey);


    /** Delete from JIRA by key or id and return a human-readable message. */
    @DeleteMapping("/jira/db/{projectKeyOrId}")
    ResponseEntity<String> deleteJiraDb(@PathVariable String projectKeyOrId);

    /** Delete from Jira and also remove local copy (by key or id). */
    @DeleteMapping("/jira-and-local/{projectKeyOrId}")
    ResponseEntity<String> deleteJiraAndLocal(@PathVariable String projectKeyOrId);

    // ──────────────────────────────────────────────────────────────
    // SYNC / UPDATE
    // ──────────────────────────────────────────────────────────────
    /** Sync ALL Jira projects -> local DB for current principal. */
    @PostMapping("/sync/all")
    List<ProjectResponse> syncAllFromJira();


    /** Sync a single project (by Jira key or id) -> local DB for current principal. */
    @PostMapping("/sync/Key")
    ProjectResponse syncOne(@RequestBody SyncProjectRequest request);

    /** Sync a single project from Jira -> local DB using explicit username + key/id. */
    @PostMapping("/sync/from-jira")
    ProjectResponse syncFromJira(@RequestBody SyncProjectRequest request);

    /** Push LOCAL -> Jira for one project (then refresh local copy). */
    @PostMapping("/sync/local-to-jira")
    ProjectResponse syncLocalToJira(@RequestBody SyncProjectRequest request);

    /** Push LOCAL -> Jira for ALL local projects at user's baseUrl. */
    @PostMapping("/sync/local-to-jira/all")
    List<ProjectResponse> syncAllLocalToJira();

    /** Set Jira project lead, then refresh local copy. */
    @PostMapping("/lead")
    ProjectResponse setLead(@RequestBody SetProjectLeadRequest request);


}
