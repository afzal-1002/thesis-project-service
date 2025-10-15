package com.ii.pw.edu.pl.master.thesis.project.exceptions.jiraexception;

import com.ii.pw.edu.pl.master.thesis.project.exceptions.CustomException;
import org.springframework.http.HttpStatus;

public class JiraIntegrationException extends CustomException {

    public JiraIntegrationException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
