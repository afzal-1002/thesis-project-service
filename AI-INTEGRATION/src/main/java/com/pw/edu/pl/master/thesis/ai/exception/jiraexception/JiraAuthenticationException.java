package com.pw.edu.pl.master.thesis.ai.exception.jiraexception;


import com.pw.edu.pl.master.thesis.ai.exception.CustomException;
import org.springframework.http.HttpStatus;

public class JiraAuthenticationException extends CustomException {
    public JiraAuthenticationException(String reason) {
        super(reason, HttpStatus.UNAUTHORIZED);
    }
}