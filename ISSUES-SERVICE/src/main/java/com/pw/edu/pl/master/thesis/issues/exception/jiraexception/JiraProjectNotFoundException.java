package com.pw.edu.pl.master.thesis.issues.exception.jiraexception;

import com.pw.edu.pl.master.thesis.issues.exception.CustomException;
import org.springframework.http.HttpStatus;



public class JiraProjectNotFoundException extends CustomException {
    public JiraProjectNotFoundException(String projectKey) {
        super("Project not found in Jira: " + projectKey, HttpStatus.NOT_FOUND);
    }
}