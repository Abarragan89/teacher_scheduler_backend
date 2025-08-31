package com.mathfactmissions.teacherscheduler.exception;

public class EmailAlreadyExistsException extends ApiException {
    public EmailAlreadyExistsException() {
        super("Email already exists");
    }
}