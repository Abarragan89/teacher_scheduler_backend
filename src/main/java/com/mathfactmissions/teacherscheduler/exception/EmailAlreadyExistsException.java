package com.mathfactmissions.teacherscheduler.exception;

public class EmailAlreadyExistsException extends ApiException {
    public EmailAlreadyExistsException(String email) {
        super("Email already exists" + email);
    }
}