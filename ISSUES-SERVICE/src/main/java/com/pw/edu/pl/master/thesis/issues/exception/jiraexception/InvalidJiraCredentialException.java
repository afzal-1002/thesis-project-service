package com.pw.edu.pl.master.thesis.issues.exception.jiraexception;

import com.pw.edu.pl.master.thesis.issues.exception.CustomException;
import org.springframework.http.HttpStatus;

public class InvalidJiraCredentialException extends CustomException {
    public InvalidJiraCredentialException(String reason) {
        super(reason, HttpStatus.UNAUTHORIZED);
    }
}