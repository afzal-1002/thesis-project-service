package com.pw.edu.pl.master.thesis.project.exceptions;

import org.springframework.http.HttpStatus;

public class UserNotAuthorizedException extends CustomException {

    public UserNotAuthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
