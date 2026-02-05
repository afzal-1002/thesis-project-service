package com.pw.edu.pl.master.thesis.user.exception.jiraexception;

import com.pw.edu.pl.master.thesis.user.exception.CustomException;
import org.springframework.http.HttpStatus;

public class InvalidJiraCredentialException extends CustomException {
    public InvalidJiraCredentialException(String reason) {
        super(reason, HttpStatus.UNAUTHORIZED);
    }
}