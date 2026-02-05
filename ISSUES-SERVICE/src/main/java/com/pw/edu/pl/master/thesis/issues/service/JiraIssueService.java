package com.pw.edu.pl.master.thesis.issues.service;

import com.pw.edu.pl.master.thesis.issues.dto.issue.CreateIssueRequest;
import com.pw.edu.pl.master.thesis.issues.dto.issue.request.*;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.*;
import com.pw.edu.pl.master.thesis.issues.dto.issuetype.IssueTypeSummary;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.IssueResponse;

import java.util.List;

public interface JiraIssueService {
    // existing
    IssueResponseSummary createIssue(CreateIssueRequest request);
    List<IssueTypeSummary>      listAllIssueTypes();
    IssueResponse               getIssueByKeyJira(String issueKey);

    IssueResponse               getIssueByIdOrKey(String issueKey);
    IssueResponse               getIssueById(int issueKey);
    IssueResponse               getIssueByKeyIssuesSummaryResponse(String issueKey);

//    List<CommentResponse>       syncCommentsForIssue(Issue issue, List<CommentResponse> jiraComments);
//    public void                 upsertFromJira(String issueKey, CommentResponse dto);
public IssueResponse getIssueWithSelectedFields(String issueKey, String fieldsCsv);

    JqlSearchResponse           searchIssuesByJqlPost(JqlSearchRequest request);
     List<IssueResponse>        getAllIssuesForProject(JqlSearchRequest jqlQuery);
    List<IssueTypeSummary>      getAllJiraIssueForProject(String projectKey);
    JqlSearchResponse           searchIssuesByJqlPostSummary(String issueSummary, String projectKey);
    JqlSearchResponse           searchIssuesByJqlPostSummaryBody(JQLIssueSummary request);

    // ─── NEW ENDPOINTS ────────────────────────────────────────────────────────
    /** Bulk create or update. */
    IssueResponseList           bulkCreateOrUpdateIssues(BulkCreateOrUpdateIssues request) ;
    IssueResponseList           bulkFetchIssuesByIdOrKey(GetIssuesListRequest request);
    CreateMetaResponse getCreateMeta();

    /** Create‐meta for a single project. */
    CreateMetaResponse          getCreateMetaForProject(String projectIdOrKey);
    IssueResponse               updateIssue(String issueIdOrKey, CreateIssueRequest request);

    /** Delete an issue. */
    void deleteIssueByKey(String issueKey);

    /** Assign (or reassign) an issue to a user. */
    void                        assignIssue(String issueIdOrKey, String accountId);

    /** Get an issue’s changelog. */
    ChangelogResponse getChangelog(String issueIdOrKey);

    /** List specific changelog entries by ID. */
    ChangelogListResponse       listChangelog(String issueIdOrKey, ChangelogListRequest request);
    ArchiveResponse archiveIssues(ArchiveRequest request);
    ArchiveResponse             unarchiveIssues(ArchiveRequest request);
    CustomFieldResponse createCustomField(CustomFieldRequest request);
    void                        setCustomFieldToIssue(SetCustomFieldRequest request);
    String                      deleteIssueByKey(String issueKey, boolean deleteSubtasks);
    List<String>                deleteMultipleIssueByKey(DeleteIssuesRequest request);


}
