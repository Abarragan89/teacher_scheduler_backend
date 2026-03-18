package com.mathfactmissions.teacherscheduler.security;

import java.util.UUID;

public class UserPrincipal {
    
    private final String email;
    private final UUID userId;
    private final String timeZone;
    
    public UserPrincipal(String email, UUID userId, String timeZone) {
        this.email = email;
        this.userId = userId;
        this.timeZone = timeZone;
    }
    
    public UUID getId() {
        return userId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getTimeZone() {
        return timeZone;
    }
}
