package com.mathfactmissions.teacherscheduler.security;

import java.util.UUID;

public class UserPrincipal {

    private final String email;
    private final UUID userId;

    public UserPrincipal(String email, UUID userId) {
        this.email = email;
        this.userId = userId;
    }

    public UUID getId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }
}
