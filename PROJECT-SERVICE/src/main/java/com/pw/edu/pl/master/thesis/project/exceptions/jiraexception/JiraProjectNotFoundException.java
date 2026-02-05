package com.pw.edu.pl.master.thesis.project.exceptions.jiraexception;

import com.pw.edu.pl.master.thesis.project.exceptions.CustomException;
import org.springframework.http.HttpStatus;



public class JiraProjectNotFoundException extends CustomException {
    public JiraProjectNotFoundException(String projectKey) {
        super("Project not found in Jira: " + projectKey, HttpStatus.NOT_FOUND);
    }
}