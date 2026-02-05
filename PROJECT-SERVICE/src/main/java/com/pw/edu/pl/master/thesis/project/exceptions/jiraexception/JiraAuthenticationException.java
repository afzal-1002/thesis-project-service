package com.pw.edu.pl.master.thesis.project.exceptions.jiraexception;


import com.pw.edu.pl.master.thesis.project.exceptions.CustomException;
import org.springframework.http.HttpStatus;

public class JiraAuthenticationException extends CustomException {
    public JiraAuthenticationException(String reason) {
        super(reason, HttpStatus.UNAUTHORIZED);
    }
}