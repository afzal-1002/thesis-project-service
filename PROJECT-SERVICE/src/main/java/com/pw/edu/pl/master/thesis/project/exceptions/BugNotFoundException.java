package com.pw.edu.pl.master.thesis.project.exceptions;


import org.springframework.http.HttpStatus;

public class BugNotFoundException extends CustomException {

    public BugNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
