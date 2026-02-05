package com.pw.edu.pl.master.thesis.ai.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UserAlreadyExistException extends CustomException {
    public UserAlreadyExistException (String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
