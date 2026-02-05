package com.pw.edu.pl.master.thesis.issues.controller;

import com.pw.edu.pl.master.thesis.issues.dto.comment.*;
import com.pw.edu.pl.master.thesis.issues.dto.comment.*;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.CommentResponse;
import com.pw.edu.pl.master.thesis.issues.exception.ResourceNotFoundException;
import com.pw.edu.pl.master.thesis.issues.service.JiraCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping(value = "/api/wut/jira/comment")
public class JiraCommentController {

    private final JiraCommentService jiraCommentService;

    // -------- Create (minimal CommentRequest) --------
    @PostMapping(value = "/{issueKey}")
    public ResponseEntity<CommentResponse> createCommentByIssueKey(
            @PathVariable String issueKey,
            @RequestBody CommentRequest request
    ) {
        CommentResponse created = jiraCommentService.createCommentsByIssueKey(issueKey, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // -------- Create (full CreateCommentRequest) --------
    @PostMapping(value = "/{issueKey}/full")
    public ResponseEntity<CommentResponse> addFullComment(
            @PathVariable String issueKey,
            @RequestBody CreateCommentRequest request
    ) {
        CommentResponse created = jiraCommentService.addFullComment(issueKey, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // -------- List for a single issue --------
    @GetMapping("/{issueKey}")
    public ResponseEntity<List<CommentResponse>> getCommentsByIssueKey(@PathVariable String issueKey) {
        return ResponseEntity.ok(jiraCommentService.getCommentsByIssueKey(issueKey));
    }

    // -------- Batch list for multiple issues --------
    @PostMapping(value = "/byIssueKeysList")
    public ResponseEntity<List<CommentResponse>> getCommentsByIssueKeys(@RequestBody ListIssueKeys payload) {
        if (payload == null || payload.getListIssueKeys() == null || payload.getListIssueKeys().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(jiraCommentService.getCommentsByIssueKeysList(payload));
    }

    // -------- Get by issueKey + commentId (Jira standard endpoint) --------
    @GetMapping("/{issueKey}/{commentId}")
    public ResponseEntity<CommentResponse> getCommentByIssueKeyAndId(
            @PathVariable String issueKey,
            @PathVariable String commentId
    ) {
        return ResponseEntity.ok(jiraCommentService.getCommentsByIssueKeyAndCommentId(issueKey, commentId));
    }

    // -------- Get by commentId only (Cloud bulk: POST /rest/api/3/comment/list) --------
    @GetMapping("/by-id/{commentId}")
    public ResponseEntity<CommentResponse> getCommentById(@PathVariable String commentId) {
        CommentResponse dto = jiraCommentService.getCommentById(commentId);
        return (dto != null) ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    // -------- Get by multiple comment IDs (Cloud bulk) --------
    @PostMapping(value = "/byCommentIdsList")
    public ResponseEntity<CommentListResponse> getCommentsByIds(@RequestBody JiraCommentIdsList request) {
        return ResponseEntity.ok(jiraCommentService.getCommentByCommentIdsList(request));
    }

    @PostMapping(value = "/byIds/report")
    public ResponseEntity<CommentListEnvelope> getCommentsByIdsReport(@RequestBody JiraCommentIdsList payload) {
        if (payload == null || payload.getIds() == null || payload.getIds().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(jiraCommentService.getCommentsByIdsWithReport(payload));
    }

    // -------- Update by issueKey + commentId --------
    @PutMapping(
            value = "/{issueKey}/{commentId}",
            consumes = "application/json",
            produces = "application/json"
    )
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable String issueKey,
            @PathVariable String commentId,
            @RequestBody UpdateCommentRequest request
    ) {

        // 🔴 MAIN FIX: enforce VALID ADF body
        if (request.getBody() == null || request.getBody().getContent() == null) {

            request.setBody(
                    com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.Body.builder()
                            .type("doc")
                            .version(1)
                            .content(List.of(
                                    com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.Body.Content.builder()
                                            .type("paragraph")
                                            .content(List.of(
                                                    com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.Body.Content.builder()
                                                            .type("text")
                                                            .text("✅ Updated via API")
                                                            .build()
                                            ))
                                            .build()
                            ))
                            .build()
            );
        }

        CommentResponse updated =
                jiraCommentService.updateCommentByIssueKeyCommentId(issueKey, commentId, request);

        return ResponseEntity.ok(updated);
    }



    // -------- Delete by issueKey + commentId --------
    @DeleteMapping("/{issueKey}/{commentId}")
    public ResponseEntity<String> deleteComment(
            @PathVariable String issueKey,
            @PathVariable String commentId
    ) {
        String deleteComment = jiraCommentService.deleteComment(issueKey, commentId);
        return ResponseEntity.ok(deleteComment);
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
