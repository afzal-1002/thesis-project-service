package com.pw.edu.pl.master.thesis.ai.exception;

import org.springframework.http.HttpStatus;

public class ExternalApiException extends CustomException {

    public ExternalApiException(String message, HttpStatus status) {
        super(message, status);
    }
}
