package com.pw.edu.pl.master.thesis.issues.exception.jiraexception;

import com.pw.edu.pl.master.thesis.issues.exception.CustomException;
import org.springframework.http.HttpStatus;

public class JiraApiException extends CustomException {
    public JiraApiException(String reason) {
        super(reason, HttpStatus.UNAUTHORIZED);
    }
}