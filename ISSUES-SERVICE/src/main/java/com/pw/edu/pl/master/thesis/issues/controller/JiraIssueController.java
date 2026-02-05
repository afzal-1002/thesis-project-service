package com.pw.edu.pl.master.thesis.issues.controller;

import com.pw.edu.pl.master.thesis.issues.dto.issue.CreateIssueRequest;
import com.pw.edu.pl.master.thesis.issues.dto.issue.attachment.AttachmentDto;
import com.pw.edu.pl.master.thesis.issues.dto.issue.attachment.DownloadedFile;
import com.pw.edu.pl.master.thesis.issues.dto.issue.request.*;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.*;
import com.pw.edu.pl.master.thesis.issues.dto.issue.request.*;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.*;
import com.pw.edu.pl.master.thesis.issues.dto.issuetype.IssueTypeSummary;
import com.pw.edu.pl.master.thesis.issues.service.AttachmentService;
import com.pw.edu.pl.master.thesis.issues.service.JiraIssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.IssueResponse;

import java.util.Base64;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/jira/issues")
public class JiraIssueController {

    private final JiraIssueService jiraIssueService;
    private final AttachmentService attachmentService;

    // ──────────────── Create / Read / Update / Delete ────────────────

    @PostMapping
    public ResponseEntity<IssueResponseSummary> createIssue(@RequestBody CreateIssueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jiraIssueService.createIssue(request));
    }

    @PostMapping("/search/project/issues")
    public List<IssueResponse> fetchAllIssuesByJqlPost(@RequestBody JqlSearchRequest jqlQuery){
        return jiraIssueService.getAllIssuesForProject(jqlQuery);
    }

    @PostMapping("/search/post")
    public ResponseEntity<JqlSearchResponse> testJqlSearchPost(@RequestBody JqlSearchRequest request) {
        JqlSearchResponse response = jiraIssueService.searchIssuesByJqlPost(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{issueKey}")
    public ResponseEntity<IssueResponse> getIssueByKey(@PathVariable String issueKey) {
        return ResponseEntity.ok(jiraIssueService.getIssueByIdOrKey(issueKey));
    }

    @GetMapping("/{issueKey}/summary")
    public ResponseEntity<IssueResponse> getIssueSummary(@PathVariable String issueKey) {
        return ResponseEntity.ok(jiraIssueService.getIssueByKeyIssuesSummaryResponse(issueKey));
    }

    @PutMapping("/{issueIdOrKey}")
    public ResponseEntity<IssueResponse> updateIssue(@PathVariable String issueIdOrKey,
                                                     @RequestBody CreateIssueRequest request) {
        return ResponseEntity.ok(jiraIssueService.updateIssue(issueIdOrKey, request));
    }

    // ──────────────── Bulk ───────────────────────────────────────────
    @PostMapping("/bulk/create-or-update")
    public ResponseEntity<IssueResponseList> bulkCreateOrUpdate(@RequestBody BulkCreateOrUpdateIssues request) {
        return ResponseEntity.ok(jiraIssueService.bulkCreateOrUpdateIssues(request));
    }

    @PostMapping("/bulk/fetch")
    public ResponseEntity<IssueResponseList> bulkFetch(@RequestBody GetIssuesListRequest request) {
        return ResponseEntity.ok(jiraIssueService.bulkFetchIssuesByIdOrKey(request));
    }

    // ──────────────── Issue Types & Create-meta ──────────────────────
    @GetMapping("/types")
    public ResponseEntity<List<IssueTypeSummary>> listAllIssueTypes() {
        return ResponseEntity.ok(jiraIssueService.listAllIssueTypes());
    }

    /** Note: your service takes projectKey (string) now */
    @GetMapping("/types/project/{projectKey}")
    public ResponseEntity<List<IssueTypeSummary>> getIssueTypesForProject(@PathVariable String projectKey) throws Exception {
        return ResponseEntity.ok(jiraIssueService.getAllJiraIssueForProject(projectKey));
    }

    @GetMapping("/createmeta")
    public ResponseEntity<CreateMetaResponse> getCreateMeta() {
        return ResponseEntity.ok(jiraIssueService.getCreateMeta());
    }

    @GetMapping("/createmeta/{projectIdOrKey}/issuetypes")
    public ResponseEntity<CreateMetaResponse> getCreateMetaForProject(@PathVariable String projectIdOrKey) {
        return ResponseEntity.ok(jiraIssueService.getCreateMetaForProject(projectIdOrKey));
    }


    @GetMapping("/project/issues/all")
    public ResponseEntity<List<IssueResponse> > fetchAllIssuesForProject(@RequestBody JqlSearchRequest request) {
        return ResponseEntity.ok(jiraIssueService.getAllIssuesForProject(request));
    }

    @GetMapping("/get/issues/projectKey/summary")
    public ResponseEntity<JqlSearchResponse> searchIssuesByJqlPostSummary(@RequestBody GetIssueWithSummary request) {
        return ResponseEntity.ok(jiraIssueService.searchIssuesByJqlPostSummary(request.getSummary(), request.getProjectKey()));
    }


    @PostMapping("/search/issues/summary")
    public ResponseEntity<JqlSearchResponse> searchBySummary(@RequestBody JQLIssueSummary body) {
        JqlSearchResponse resp = jiraIssueService.searchIssuesByJqlPostSummaryBody(body);
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{issueKey}")
    public ResponseEntity<String> deleteIssue(@PathVariable String issueKey,
                                            @RequestParam(defaultValue = "false") boolean deleteSubtasks) {
        String result =  jiraIssueService.deleteIssueByKey(issueKey, deleteSubtasks);
        return ResponseEntity.ok(result); // 204
    }

    @PostMapping("/delete-multiple")
    public ResponseEntity<List<String>> deleteMultipleIssues(@RequestBody DeleteIssuesRequest request) {
        List<String> results = jiraIssueService.deleteMultipleIssueByKey(request);
        return ResponseEntity.ok(results);
    }

    // ──────────────── Generic & project-filtered JQL search ──────────

    @PostMapping("/search/jql")
    public ResponseEntity<JqlSearchResponse> searchByJql(@RequestBody JqlSearchRequest request) {
        return ResponseEntity.ok(jiraIssueService.searchIssuesByJqlPost(request));
    }

    @PostMapping("/search/project/issues/all")
    public ResponseEntity<JqlSearchResponse> searchByJqlAndProject(@RequestBody JqlSearchRequest request) {
        return ResponseEntity.ok(jiraIssueService.searchIssuesByJqlPost(request));
    }

    // ──────────────── Assignee & Changelog ───────────────────────────

    @PutMapping("/{issueIdOrKey}/assignee")
    public ResponseEntity<Void> assignIssue(@PathVariable String issueIdOrKey,
                                            @RequestBody AssigneeRequest request) {
        jiraIssueService.assignIssue(issueIdOrKey, request.getAccountId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{issueIdOrKey}/changelog")
    public ResponseEntity<ChangelogResponse> getChangelog(@PathVariable String issueIdOrKey) {
        return ResponseEntity.ok(jiraIssueService.getChangelog(issueIdOrKey));
    }

    @PostMapping("/{issueIdOrKey}/changelog/list")
    public ResponseEntity<ChangelogListResponse> listChangelog(@PathVariable String issueIdOrKey,
                                                               @RequestBody ChangelogListRequest request) {
        return ResponseEntity.ok(jiraIssueService.listChangelog(issueIdOrKey, request));
    }

    // ──────────────── Archive / Unarchive ────────────────────────────

    @PutMapping("/archive")
    public ResponseEntity<ArchiveResponse> archiveIssues(@RequestBody ArchiveRequest request) {
        return ResponseEntity.ok(jiraIssueService.archiveIssues(request));
    }

    @PutMapping("/unarchive")
    public ResponseEntity<ArchiveResponse> unarchiveIssues(@RequestBody ArchiveRequest request) {
        return ResponseEntity.ok(jiraIssueService.unarchiveIssues(request));
    }

    // ──────────────── Custom Fields ──────────────────────────────────

    @PostMapping("/custom-fields")
    public ResponseEntity<CustomFieldResponse> createCustomField(@RequestBody CustomFieldRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jiraIssueService.createCustomField(request));
    }

    @PutMapping("/custom-fields/set")
    public ResponseEntity<Void> setCustomField(@RequestBody SetCustomFieldRequest request) {
        jiraIssueService.setCustomFieldToIssue(request);
        return ResponseEntity.noContent().build();
    }

    // WebClient version
    @GetMapping("/{issueKey}/attachments/first/web")
    public ResponseEntity<byte[]> getFirstWeb(@PathVariable String issueKey) {
        var f = attachmentService.downloadFirstAttachmentWithWeb(issueKey);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + f.filename() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(f.bytes());
    }

    // RestTemplate version
    @GetMapping("/{issueKey}/attachments/first/rest")
    public ResponseEntity<byte[]> getFirstRest(@PathVariable String issueKey) {
        var f = attachmentService.downloadFirstAttachmentWithRest(issueKey);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + f.filename() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(f.bytes());
    }




        @GetMapping("/{issueKey}/attachments/first")
        public ResponseEntity<byte[]> downloadFirst(@PathVariable String issueKey) {
            var f = attachmentService.downloadAttachment(issueKey);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(f.filename()).toString())
                    .contentType(MediaType.parseMediaType(f.contentType()))
                    .contentLength(f.contentLength() >= 0 ? f.contentLength() : f.bytes().length)
                    .body(f.bytes());
        }


    @GetMapping(value = "/{issueKey}/attachments", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AttachmentDto>> downloadAll(@PathVariable String issueKey) {
        var files = attachmentService.downloadAllAttachments(issueKey);

        // map to DTO for JSON
        List<AttachmentDto> out = files.stream()
                .map(f -> new AttachmentDto(
                        f.filename(),
                        f.contentType(),
                        f.contentLength(),
                        java.util.Base64.getEncoder().encodeToString(f.bytes())
                ))
                .toList();

        return ResponseEntity.ok(out);
    }




}
