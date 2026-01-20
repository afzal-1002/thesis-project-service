package com.pw.edu.pl.master.thesis.issues.service;

import com.pw.edu.pl.master.thesis.issues.dto.issue.CreateIssueRequest;
import com.pw.edu.pl.master.thesis.issues.dto.issue.response.Issuereponse.IssueResponse;

import java.util.List;

public interface IssueService {
    IssueResponse createIssue(CreateIssueRequest request);
    IssueResponse getIssueByKey(String issueKey);
    IssueResponse getIssueById(int id);
    IssueResponse updateIssue(String issueKey, CreateIssueRequest request);
    List<IssueResponse> listIssueResponsesByProjectId(String projectKey);
    List<IssueResponse> listIssuesForProjectKey(String projectKey);

    List<IssueResponse> synchronizeProjectIssues(String projectKey);

     CreateIssueRequest createIssueRequest(String issueKey);
     IssueResponse synchronizeIssueByKey(String issueKey) ;

    void syncIssueByIssueKey(String issueKey);
}
