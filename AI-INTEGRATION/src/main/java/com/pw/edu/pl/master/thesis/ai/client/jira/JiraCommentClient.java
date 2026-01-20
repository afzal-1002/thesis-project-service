package com.pw.edu.pl.master.thesis.ai.client.jira;

import com.pw.edu.pl.master.thesis.ai.dto.comment.*;
import com.pw.edu.pl.master.thesis.ai.dto.issue.response.Issuereponse.CommentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@FeignClient(
//        name = "jira-comment-client",
//        url = "${issues.service.base-url}",
//        path = "/api/wut/jira/comment"
//)


@FeignClient(
        name = "ISSUES-SERVICE",
        contextId = "JiraCommentClient",
        path = "/api/wut/jira/comment"
)
public interface JiraCommentClient {

    // Create (minimal)
    @PostMapping(value = "/{issueKey}", consumes = MediaType.APPLICATION_JSON_VALUE)
    CommentResponse createCommentByIssueKey(@PathVariable("issueKey") String issueKey,
                                            @RequestBody CommentRequest request);

    // Create (full)
    @PostMapping(value = "/{issueKey}/full", consumes = MediaType.APPLICATION_JSON_VALUE)
    CommentResponse addFullComment(@PathVariable("issueKey") String issueKey,
                                   @RequestBody CreateCommentRequest request);

    // List for a single issue
    @GetMapping("/{issueKey}")
    List<CommentResponse> getCommentsByIssueKey(@PathVariable("issueKey") String issueKey);

    // Batch list for multiple issues
    @PostMapping(value = "/byIssueKeysList", consumes = MediaType.APPLICATION_JSON_VALUE)
    List<CommentResponse> getCommentsByIssueKeys(@RequestBody ListIssueKeys payload);

    // Get by issueKey + commentId
    @GetMapping("/{issueKey}/{commentId}")
    CommentResponse getCommentByIssueKeyAndId(@PathVariable("issueKey") String issueKey,
                                              @PathVariable("commentId") String commentId);

    // Get by commentId only
    @GetMapping("/by-id/{commentId}")
    CommentResponse getCommentById(@PathVariable("commentId") String commentId);

    // Get by multiple comment IDs (bulk)
    @PostMapping(value = "/byCommentIdsList", consumes = MediaType.APPLICATION_JSON_VALUE)
    CommentListResponse getCommentsByIds(@RequestBody JiraCommentIdsList request);

    @PostMapping(value = "/byIds/report", consumes = MediaType.APPLICATION_JSON_VALUE)
    CommentListEnvelope getCommentsByIdsReport(@RequestBody JiraCommentIdsList payload);

    // Update
    @PutMapping(value = "/{issueKey}/{commentId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    CommentResponse updateComment(@PathVariable("issueKey") String issueKey,
                                  @PathVariable("commentId") String commentId,
                                  @RequestBody UpdateCommentRequest request);

    // Delete
    @DeleteMapping("/{issueKey}/{commentId}")
    String deleteComment(@PathVariable("issueKey") String issueKey,
                         @PathVariable("commentId") String commentId);
}