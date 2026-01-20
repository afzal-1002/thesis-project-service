package com.pw.edu.pl.master.thesis.issues.exception;

import org.springframework.http.HttpStatus;

public class CommentNotFoundException extends CustomException {

    public CommentNotFoundException(String message, HttpStatus status) {
        super(message, status);
    }

}
