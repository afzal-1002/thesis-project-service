package com.pw.edu.pl.master.thesis.ai.exception.jiraexception;

import com.pw.edu.pl.master.thesis.ai.exception.CustomException;
import org.springframework.http.HttpStatus;

public class JiraApiException extends CustomException {
    public JiraApiException(String reason) {
        super(reason, HttpStatus.UNAUTHORIZED);
    }
}