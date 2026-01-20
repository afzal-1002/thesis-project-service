package com.pw.edu.pl.master.thesis.issues.service;

import com.pw.edu.pl.master.thesis.issues.dto.comment.CreateCommentRequest;
import com.pw.edu.pl.master.thesis.issues.dto.comment.UpdateCommentRequest;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.CommentResponse;

import java.util.List;

public interface CommentService {

    /* -------- Jira-first flows (writes to Jira, then upserts locally) -------- */
    CommentResponse createComment(String issueKey, CreateCommentRequest request);
    CommentResponse updateComment(String issueKey, String jiraCommentId, UpdateCommentRequest request);
    void            deleteComment(String issueKey, String jiraCommentId);

    /* -------- Sync (local → jira) -------- */
    String            syncCommentsFromJira(String issueKey);
    String               syncFromJiraByIssueId(String issueId);

    /* -------- Read (Jira fresh → upsert local → return Jira DTOs) -------- */
    List<CommentResponse> findAllByIssueKey(String issueKey);
    List<CommentResponse> getAllLocalByIssueKey(String issueKey);
    CommentResponse       getCommentById(String id);           // local PK
    CommentResponse       getCommentByJiraId(String jiraCommentId);

    /* -------- Local update/delete by local PK (proxy to Jira if possible) -------- */
    CommentResponse       updateComment(Long id, UpdateCommentRequest request);
    void                  deleteComment(Long id);

    /* -------- Batch local read by Jira ids (fetch from Jira, upsert local) -------- */
    List<CommentResponse> getCommentsByJiraIds(List<String> jiraCommentIds);
    CommentResponse       findAllById(String jiraCommentId); // fetch single from Jira by Jira ID

    /* -------- Local → Jira (push) -------- */
    CommentResponse       pushLocalCommentToJira(String issueKey, String localCommentId);
    List<CommentResponse> pushLocalCommentsToJira(String issueKey, List<String> localCommentIds);
    List<CommentResponse> pushAllPendingCommentsToJira(String issueKey);
    List<CommentResponse> pushAllPendingCommentsEverywhere();

    /* -------- AI comments (local) -------- */
    List<CommentResponse> getAllAiComments();
}
