package com.pw.edu.pl.master.thesis.issues.service;

import com.pw.edu.pl.master.thesis.issues.dto.comment.*;
import com.pw.edu.pl.master.thesis.issues.dto.comment.*;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.CommentResponse;
import com.pw.edu.pl.master.thesis.issues.model.issue.Issue;
import jakarta.transaction.Transactional;

import java.util.List;

public interface JiraCommentService {
    CommentResponse         createCommentsByIssueKey(String issueKey, CommentRequest request);
    CommentResponse         addFullComment(String issueKey, CreateCommentRequest request);
    CommentResponse         getCommentById(String commentId);
    CommentListResponse getCommentByCommentIdsList(JiraCommentIdsList jiraCommentIdsList);
    CommentListEnvelope getCommentsByIdsWithReport(JiraCommentIdsList payload);

    CommentResponse         syncCommentByCommentId(String jiraCommentId);
     void                   saveCommentFromJira(Issue issue, CommentResponse jiraComment);
     int                    synchronizeCommentsByIssueKey(String issueKey);
     int                    synchronizeComments(String issueKey);
    CommentResponse         synchronizeSingleCommentForIssue(Issue issue, CommentResponse comment);
    void                    synchronizeCommentsByIdsList(String issueKey, List<String> jiraCommentIds);
    void                    saveCommentFromJiraByIssueKey(String issueKey, CommentResponse jiraComment);


    List<CommentResponse>   getCommentsByIssueKey(String issueKey);
    List<CommentResponse>   getCommentsByIssueKeysList(ListIssueKeys request);

    CommentResponse         getCommentsByIssueKeyAndCommentId(String issueKey, String commentId);

    CommentResponse         updateCommentByIssueKeyCommentId(String issueKey, String commentId, UpdateCommentRequest request);
    String                    deleteComment(String issueKey, String commentId);

    String findIssueKeyByCommentId(String jiraCommentId);

    @Transactional
    CommentResponse synchronizeACommentForIssue(String jiraCommentId);

}
