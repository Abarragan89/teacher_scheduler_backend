package com.mathfactmissions.teacherscheduler.service;

import com.mathfactmissions.teacherscheduler.security.JwtService;
import com.nimbusds.jose.JOSEException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MagicLinkService {
    
    private final JwtService jwtService;
    private final EmailService emailService;
    private final String clientURL;
    
    public MagicLinkService(
        JwtService jwtService,
        EmailService emailService,
        @Value("${client.url}") String clientURL
    ) {
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.clientURL = clientURL;
    }
    
    public void sendMagicLink(String email, String timeZone) {
        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("magic", true);
            claims.put("timeZone", timeZone);
            
            String magicToken = jwtService.generateToken(email, claims, 15);
            String link = clientURL + "user-verification?token=" + magicToken;
            
            emailService.sendEmail(email, link);
            System.out.println("✅ Magic link sent to: " + email);
            
        } catch (JOSEException e) {
            System.err.println("❌ Failed to generate magic link for: " + email + " - " + e.getMessage());
            throw new RuntimeException("Failed to generate magic link for " + email, e);
        }
    }
}
