package com.pw.edu.pl.master.thesis.issues.controller;

import com.pw.edu.pl.master.thesis.issues.dto.comment.CreateCommentRequest;
import com.pw.edu.pl.master.thesis.issues.dto.comment.UpdateCommentRequest;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.CommentResponse;
import com.pw.edu.pl.master.thesis.issues.exception.ResourceNotFoundException;
import com.pw.edu.pl.master.thesis.issues.service.CommentService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/wut/comments")
@RequiredArgsConstructor
@Validated
public class CommentController {

    private final CommentService commentService;

    /* =========================
     * Jira-first (write → upsert local)
     * ========================= */

    /** Create a Jira comment on an issue key (body is Atlassian ADF inside CreateCommentRequest). */
    @PostMapping(value = "/{issueKey}")
    public ResponseEntity<CommentResponse> create(
            @PathVariable String issueKey,
            @RequestBody CreateCommentRequest request
    ) {
        CommentResponse created = commentService.createComment(issueKey, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** Update a Jira comment by issue key + Jira comment id. */
    @PutMapping(value = "/{issueKey}/{jiraCommentId}")
    public ResponseEntity<CommentResponse> update(
            @PathVariable String issueKey,
            @PathVariable String jiraCommentId,
            @RequestBody UpdateCommentRequest request
    ) {
        return ResponseEntity.ok(commentService.updateComment(issueKey, jiraCommentId, request));
    }

    /** Delete a Jira comment by issue key + Jira comment id. */
    @DeleteMapping("/{issueKey}/{jiraCommentId}")
    public ResponseEntity<Void> delete(
            @PathVariable String issueKey,
            @PathVariable String jiraCommentId
    ) {
        commentService.deleteComment(issueKey, jiraCommentId);
        return ResponseEntity.noContent().build();
    }

    /* =========================
     * Reads (Jira fresh → upsert local)
     * ========================= */

    /** Get all Jira comments for an issue key (also syncs them locally). */
    @GetMapping("/by-issue/{issueKey}")
    public ResponseEntity<List<CommentResponse>> findAllByIssueKey(@PathVariable String issueKey) {
        return ResponseEntity.ok(commentService.findAllByIssueKey(issueKey));
    }

    /** Get all locally stored comments for an issue key (no Jira call). */
    @GetMapping("/local/by-issue/{issueKey}")
    public ResponseEntity<List<CommentResponse>> getAllLocalByIssueKey(@PathVariable String issueKey) {
        return ResponseEntity.ok(commentService.getAllLocalByIssueKey(issueKey));
    }


    /** Get ONE local comment by local PK id. */
    @GetMapping("/local/{id}")
    public ResponseEntity<CommentResponse> getLocalById(@PathVariable String id) {
        return ResponseEntity.ok(commentService.getCommentById(id));
    }

    /** Get ONE local comment mapped by Jira comment id (reads from local DB). */
    @GetMapping("/by-jira-id/{jiraCommentId}")
    public ResponseEntity<CommentResponse> getLocalByJiraId(@PathVariable String jiraCommentId) {
        return ResponseEntity.ok(commentService.getCommentByJiraId(jiraCommentId));
    }

    /** Fetch ONE from Jira by Jira comment id (not local PK) and upsert locally. */
    @GetMapping("/fetch-one/{jiraCommentId}")
    public ResponseEntity<CommentResponse> fetchOneFromJira(@PathVariable String jiraCommentId) {
        return ResponseEntity.ok(commentService.findAllById(jiraCommentId));
    }

    /** Fetch MANY from Jira by Jira comment ids (array in body), upsert locally, return Jira DTOs. */
    @PostMapping(value = "/fetch-many")
    public ResponseEntity<List<CommentResponse>> fetchManyFromJira(@RequestBody IdsPayload<String> jiraCommentIds) {
        List<String> ids = (jiraCommentIds != null && jiraCommentIds.getIds() != null)
                ? jiraCommentIds.getIds() : List.of();
        return ResponseEntity.ok(commentService.getCommentsByJiraIds(ids));
    }

    /* =========================
     * Local update/delete by local PK
     * ========================= */

    /** Update a comment by local PK; will proxy to Jira when linked. */
    @PutMapping(value = "/local/{id}")
    public ResponseEntity<CommentResponse> updateLocal(
            @PathVariable Long id,
            @RequestBody UpdateCommentRequest request
    ) {
        return ResponseEntity.ok(commentService.updateComment(id, request));
    }

    /** Delete a comment by local PK; will attempt Jira delete if linked. */
    @DeleteMapping("/local/{id}")
    public ResponseEntity<Void> deleteLocal(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }

    /* =========================
     * Sync (Jira → local)
     * ========================= */

    /** Sync comments from Jira to local by issue key. */
    @PostMapping("/sync/by-issue/{issueKey}")
    public ResponseEntity<String> syncByIssueKey(@PathVariable String issueKey) {
        return ResponseEntity.ok(commentService.syncCommentsFromJira(issueKey));
    }

    /** Sync comments from Jira to local by local Issue PK. */
    @PostMapping("/sync/by-issue-id/{issueId}")
    public ResponseEntity<String> syncByIssueId(@PathVariable String issueId) {
        return ResponseEntity.ok(commentService.syncCommentsFromJira(issueId));
    }

    /* =========================
     * Local → Jira (push)
     * ========================= */

    /** Push one local comment (by local PK) to a Jira issue. */
    @PostMapping("/{issueKey}/push-one/{localCommentId}")
    public ResponseEntity<CommentResponse> pushOne(
            @PathVariable String issueKey,
            @PathVariable String localCommentId
    ) {
        return ResponseEntity.ok(commentService.pushLocalCommentToJira(issueKey, localCommentId));
    }

    /** Push many local comments (by local PK list) to a Jira issue. */
    @PostMapping(value = "/{issueKey}/push-many", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CommentResponse>> pushMany(
            @PathVariable String issueKey,
            @RequestBody IdsPayload<String> localCommentIds
    ) {
        List<String> ids = (localCommentIds != null && localCommentIds.getIds() != null)
                ? localCommentIds.getIds() : List.of();
        return ResponseEntity.ok(commentService.pushLocalCommentsToJira(issueKey, ids));
    }

    /** Push all pending local comments for an issue to Jira. */
    @PostMapping("/{issueKey}/push-all-pending")
    public ResponseEntity<List<CommentResponse>> pushAllPending(@PathVariable String issueKey) {
        return ResponseEntity.ok(commentService.pushAllPendingCommentsToJira(issueKey));
    }

    /** Push all pending local comments across all issues to Jira. */
    @PostMapping("/push-all-pending")
    public ResponseEntity<List<CommentResponse>> pushAllPendingEverywhere() {
        return ResponseEntity.ok(commentService.pushAllPendingCommentsEverywhere());
    }

    /* =========================
     * AI Comments (local)
     * ========================= */

    /** List all AI-generated comments from local DB (DTO view). */
    @GetMapping("/AIModel")
    public ResponseEntity<List<CommentResponse>> listAiComments() {
        return ResponseEntity.ok(commentService.getAllAiComments());
    }

    /* =========================
     * Simple payload helpers
     * ========================= */

    @Data
    public static class IdsPayload<T> {
        private List<T> ids;
    }

    /* =========================
     * Minimal exception mapping
     * ========================= */

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleNotFound(ResourceNotFoundException ex) {
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
