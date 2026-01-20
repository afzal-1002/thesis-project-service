package com.pw.edu.pl.master.thesis.ai.client.comment;

import com.pw.edu.pl.master.thesis.ai.configuration.FeignSecurityConfiguration;
import com.pw.edu.pl.master.thesis.ai.dto.comment.CreateCommentRequest;
import com.pw.edu.pl.master.thesis.ai.dto.comment.UpdateCommentRequest;
import com.pw.edu.pl.master.thesis.ai.dto.issue.response.Issuereponse.CommentResponse;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@FeignClient(
//        name = "comment-client",
//        url = "${issues.service.base-url}",
//        path = "/api/wut/comments",
//        configuration = FeignSecurityConfiguration.class
//)

@FeignClient(
        name = "ISSUES-SERVICE",
        contextId = "CommentClient",
        path = "/api/wut/comments",
        configuration = FeignSecurityConfiguration.class
)
public interface CommentClient {

    // Jira-first (write → upsert local)
    @PostMapping(value = "/{issueKey}", consumes = MediaType.APPLICATION_JSON_VALUE)
    CommentResponse create(@PathVariable("issueKey") String issueKey,
                           @RequestBody CreateCommentRequest request);

    @PutMapping(value = "/{issueKey}/{jiraCommentId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    CommentResponse update(@PathVariable("issueKey") String issueKey,
                           @PathVariable("jiraCommentId") String jiraCommentId,
                           @RequestBody UpdateCommentRequest request);

    @DeleteMapping("/{issueKey}/{jiraCommentId}")
    void delete(@PathVariable("issueKey") String issueKey,
                @PathVariable("jiraCommentId") String jiraCommentId);

    // Reads (Jira fresh → upsert local)
    @GetMapping("/by-issue/{issueKey}")
    List<CommentResponse> findAllByIssueKey(@PathVariable("issueKey") String issueKey);

    @GetMapping("/local/by-issue/{issueKey}")
    List<CommentResponse> getAllLocalByIssueKey(@PathVariable("issueKey") String issueKey);

    @GetMapping("/local/{id}")
    CommentResponse getLocalById(@PathVariable("id") String id);

    @GetMapping("/by-jira-id/{jiraCommentId}")
    CommentResponse getLocalByJiraId(@PathVariable("jiraCommentId") String jiraCommentId);

    @GetMapping("/fetch-one/{jiraCommentId}")
    CommentResponse fetchOneFromJira(@PathVariable("jiraCommentId") String jiraCommentId);

    @PostMapping(value = "/fetch-many", consumes = MediaType.APPLICATION_JSON_VALUE)
    List<CommentResponse> fetchManyFromJira(@RequestBody IdsPayload<String> jiraCommentIds);

    // Local update/delete by local PK
    @PutMapping(value = "/local/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    CommentResponse updateLocal(@PathVariable("id") Long id,
                                @RequestBody UpdateCommentRequest request);

    @DeleteMapping("/local/{id}")
    void deleteLocal(@PathVariable("id") Long id);

    // Sync (Jira → local)
    @PostMapping("/sync/by-issue/{issueKey}")
    String syncByIssueKey(@PathVariable("issueKey") String issueKey);

    @PostMapping("/sync/by-issue-id/{issueId}")
    String syncByIssueId(@PathVariable("issueId") String issueId);

    // Local → Jira (push)
    @PostMapping("/{issueKey}/push-one/{localCommentId}")
    CommentResponse pushOne(@PathVariable("issueKey") String issueKey,
                            @PathVariable("localCommentId") String localCommentId);

    @PostMapping(value = "/{issueKey}/push-many", consumes = MediaType.APPLICATION_JSON_VALUE)
    List<CommentResponse> pushMany(@PathVariable("issueKey") String issueKey,
                                   @RequestBody IdsPayload<String> localCommentIds);

    @PostMapping("/{issueKey}/push-all-pending")
    List<CommentResponse> pushAllPending(@PathVariable("issueKey") String issueKey);

    @PostMapping("/push-all-pending")
    List<CommentResponse> pushAllPendingEverywhere();

    @Data
    class IdsPayload<T> { private List<T> ids;}
}