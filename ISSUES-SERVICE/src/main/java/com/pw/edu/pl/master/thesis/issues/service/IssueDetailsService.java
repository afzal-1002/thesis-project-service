package com.pw.edu.pl.master.thesis.issues.service;

import com.pw.edu.pl.master.thesis.issues.dto.issue.issuedetails.IssueDetails;
import org.springframework.stereotype.Service;

@Service
public interface IssueDetailsService {

    IssueDetails getIssueDetails(String issueKey);
}
