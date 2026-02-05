package com.pw.edu.pl.master.thesis.project.exceptions;

import org.springframework.http.HttpStatus;

public class DuplicateProjectNameException extends CustomException  {
    public DuplicateProjectNameException(String message, HttpStatus status) {
        super(message, status);
    }
}
