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
    
    public void sendMagicLink(String email, String timeZone) throws JOSEException {
        
        // Create short-lived JWT
        Map<String, Object> claims = new HashMap<>();
        claims.put("magic", true);
        claims.put("timeZone", timeZone);
        
        String magicToken = jwtService.generateToken(email, claims, 15);
        
        // Build link
        String link = clientURL + "/user-verification?token=" + magicToken;
        
        // Send email using resend
        emailService.sendEmail(email, link);
    }
}
