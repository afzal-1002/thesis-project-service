package com.pw.edu.pl.master.thesis.issues.exception.jiraexception;

import com.pw.edu.pl.master.thesis.issues.exception.CustomException;
import org.springframework.http.HttpStatus;

public class JiraIntegrationException extends CustomException {

    public JiraIntegrationException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
