package com.pw.edu.pl.master.thesis.issues.controller;

import com.pw.edu.pl.master.thesis.issues.client.ProjectClient;
import com.pw.edu.pl.master.thesis.issues.dto.appuser.AuthUserDTO;
import com.pw.edu.pl.master.thesis.issues.dto.issue.CreateIssueRequest;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.IssueResponse;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.UserSummary;
import com.pw.edu.pl.master.thesis.issues.exception.ProjectNotFoundException;
import com.pw.edu.pl.master.thesis.issues.exception.ResourceNotFoundException;
import com.pw.edu.pl.master.thesis.issues.service.IssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping(value = "/api/wut/issues")
public class IssueController {

    private final IssueService issueService;
    private final ProjectClient projectClient;

    /** 1) Create a new issue in Jira and save locally */
    @PostMapping(value = "/create")
    public ResponseEntity<IssueResponse> createIssue(@RequestBody CreateIssueRequest request) {
        IssueResponse created = issueService.createIssue(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** 2) Utility: current authenticated user via ProjectClient */
    @GetMapping("/authUser")
    public ResponseEntity<AuthUserDTO> getAuthUser() {
        return ResponseEntity.ok(projectClient.getAuthUser());
    }

    /** 3) Utility: all users via ProjectClient */
    @GetMapping("/users")
    public ResponseEntity<List<UserSummary>> getAllUsersViaUserService() {
        return ResponseEntity.ok(projectClient.getAllUsersViaUserService());
    }

    /** 4) Get (and upsert) one issue by its Jira key */
    @GetMapping("/{key}")
    public ResponseEntity<IssueResponse> getIssueByKey(@PathVariable("key") String issueKey) {
        return ResponseEntity.ok(issueService.getIssueByKey(issueKey));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<IssueResponse> getIssueById(@PathVariable("id") int id) {
        return ResponseEntity.ok(issueService.getIssueById(id));
    }


    /** 5) Synchronize all issues for a given project key (fetch from Jira → save locally → return list) */
    @PostMapping("/projects/sync/issues")
    public ResponseEntity<List<IssueResponse>> synchronizeProjectIssues(@RequestParam String projectKey) {
        return ResponseEntity.ok(issueService.synchronizeProjectIssues(projectKey));
    }

    /** 6) Update an existing issue in Jira (and local DB) */
    @PutMapping(value = "/{key}")
    public ResponseEntity<IssueResponse> updateIssue(
            @PathVariable("key") String issueKey,
            @RequestBody CreateIssueRequest request
    ) {
        return ResponseEntity.ok(issueService.updateIssue(issueKey, request));
    }

    /** 7) List issues for a project key (alias that also synchronizes) */
    @GetMapping("/projects/{projectKey}/issues")
    public ResponseEntity<List<IssueResponse>> listIssuesForProjectKey(@PathVariable String projectKey) {
        return ResponseEntity.ok(issueService.synchronizeProjectIssues(projectKey));
    }

    /** 8) List (local) issues for a project key (kept for compatibility; currently also syncs) */
    @GetMapping("/projects/{projectKey}/issues/local")
    public ResponseEntity<List<IssueResponse>> listIssueResponsesByProjectId(@PathVariable String projectKey) {
        return ResponseEntity.ok(issueService.listIssueResponsesByProjectId(projectKey));
    }

    /** 9) Sync a single issue by key (pull from Jira → upsert locally → return Jira DTO) */
    @PostMapping("/{key}/sync")
    public ResponseEntity<IssueResponse> synchronizeIssueByKey(@PathVariable("key") String issueKey) {
        return ResponseEntity.ok(issueService.synchronizeIssueByKey(issueKey));
    }

    /** 10) Sync a single issue by key in a new transaction (void service; respond 202) */
    @PostMapping("/{key}/sync-detached")
    public ResponseEntity<Void> syncIssueByIssueKey(@PathVariable("key") String issueKey) {
        issueService.syncIssueByIssueKey(issueKey);
        return ResponseEntity.accepted().build();
    }

    /* =========================
     * Minimal exception mapping
     * ========================= */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<String> handleProjectNotFound(ProjectNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleConflict(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}