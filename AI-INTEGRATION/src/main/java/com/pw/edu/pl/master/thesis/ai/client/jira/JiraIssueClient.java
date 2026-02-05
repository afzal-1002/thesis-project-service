package com.pw.edu.pl.master.thesis.ai.client.jira;

import com.pw.edu.pl.master.thesis.ai.dto.issue.CreateIssueRequest;
import com.pw.edu.pl.master.thesis.ai.dto.issue.request.*;
import com.pw.edu.pl.master.thesis.ai.dto.issue.response.*;
import com.pw.edu.pl.master.thesis.ai.dto.issue.response.Issuereponse.IssueResponse;
import com.pw.edu.pl.master.thesis.ai.dto.issuetype.IssueTypeSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@FeignClient(
//        name = "jira-issue-client",
//        url = "${issues.service.base-url}",
//        path = "/api/jira/issues"
//)


@FeignClient(
        name = "ISSUES-SERVICE",
        contextId = "JiraIssueClient",
        path = "/api/jira/issues"
)
public interface JiraIssueClient {

    // CRUD
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    IssueResponseSummary createIssue(@RequestBody CreateIssueRequest request);

    @GetMapping("/{issueKey}")
    IssueResponse getIssueByKey(@PathVariable("issueKey") String issueKey);

    @GetMapping("/{issueKey}/summary")
    IssueResponse getIssueSummary(@PathVariable("issueKey") String issueKey);

    @PutMapping("/{issueIdOrKey}")
    IssueResponse updateIssue(@PathVariable("issueIdOrKey") String issueIdOrKey,
                              @RequestBody CreateIssueRequest request);

    @DeleteMapping("/{issueKey}")
    String deleteIssue(@PathVariable("issueKey") String issueKey,
                       @RequestParam(name = "deleteSubtasks", defaultValue = "false") boolean deleteSubtasks);

    @PostMapping("/delete-multiple")
    List<String> deleteMultipleIssues(@RequestBody DeleteIssuesRequest request);

    // Bulk
    @PostMapping("/bulk/create-or-update")
    IssueResponseList bulkCreateOrUpdate(@RequestBody BulkCreateOrUpdateIssues request);

    @PostMapping("/bulk/fetch")
    IssueResponseList bulkFetch(@RequestBody GetIssuesListRequest request);

    // Types & create-meta
    @GetMapping("/types")
    List<IssueTypeSummary> listAllIssueTypes();

    @GetMapping("/types/project/{projectKey}")
    List<IssueTypeSummary> getIssueTypesForProject(@PathVariable("projectKey") String projectKey);

    @GetMapping("/createmeta")
    CreateMetaResponse getCreateMeta();

    @GetMapping("/createmeta/{projectIdOrKey}/issuetypes")
    CreateMetaResponse getCreateMetaForProject(@PathVariable("projectIdOrKey") String projectIdOrKey);

    // JQL search helpers
    @PostMapping("/search/project/issues")
    List<IssueResponse> fetchAllIssuesByJqlPost(@RequestBody JqlSearchRequest jqlQuery);

    @PostMapping("/search/post")
    JqlSearchResponse searchIssuesByJqlPost(@RequestBody JqlSearchRequest request);

    @PostMapping("/search/jql")
    JqlSearchResponse searchByJql(@RequestBody JqlSearchRequest request);

    @PostMapping("/search/project/issues/all")
    JqlSearchResponse searchByJqlAndProject(@RequestBody JqlSearchRequest request);

    @GetMapping("/project/issues/all")
    List<IssueResponse> fetchAllIssuesForProject(@RequestBody JqlSearchRequest request);

    @GetMapping("/get/issues/projectKey/summary")
    JqlSearchResponse searchIssuesByJqlPostSummary(@RequestBody GetIssueWithSummary request);

    @PostMapping("/search/issues/summary")
    JqlSearchResponse searchBySummary(@RequestBody JQLIssueSummary body);

    // Assignee & changelog
    @PutMapping("/{issueIdOrKey}/assignee")
    void assignIssue(@PathVariable("issueIdOrKey") String issueIdOrKey,
                     @RequestBody AssigneeRequest request);

    @GetMapping("/{issueIdOrKey}/changelog")
    ChangelogResponse getChangelog(@PathVariable("issueIdOrKey") String issueIdOrKey);

    @PostMapping("/{issueIdOrKey}/changelog/list")
    ChangelogListResponse listChangelog(@PathVariable("issueIdOrKey") String issueIdOrKey,
                                        @RequestBody ChangelogListRequest request);

    // Archive / Unarchive
    @PutMapping("/archive")
    ArchiveResponse archiveIssues(@RequestBody ArchiveRequest request);

    @PutMapping("/unarchive")
    ArchiveResponse unarchiveIssues(@RequestBody ArchiveRequest request);

    // Custom fields
    @PostMapping("/custom-fields")
    CustomFieldResponse createCustomField(@RequestBody CustomFieldRequest request);

    @PutMapping("/custom-fields/set")
    void setCustomField(@RequestBody SetCustomFieldRequest request);

    @GetMapping(
            value = "/issues/{issueKey}/attachments/first",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    ResponseEntity<byte[]> downloadFirstAttachment(@PathVariable("issueKey") String issueKey);
}