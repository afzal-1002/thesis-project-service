package com.pw.edu.pl.master.thesis.ai.exception;

import org.springframework.http.HttpStatus;

public class BadCredentialsException extends CustomException {

    public BadCredentialsException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
