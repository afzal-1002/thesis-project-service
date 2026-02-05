package com.pw.edu.pl.master.thesis.issues.exception;

import org.springframework.http.HttpStatus;

public class ValidationException extends CustomException {

    public ValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
