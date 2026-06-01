package com.example.cv_evaluator.exception;

import org.springframework.http.HttpStatus;

public class CvEvaluationException extends RuntimeException {

    private final HttpStatus status;

    public CvEvaluationException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

