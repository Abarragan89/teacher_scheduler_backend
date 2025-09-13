package com.mathfactmissions.teacherscheduler.service;

import com.mathfactmissions.teacherscheduler.model.User;
import com.mathfactmissions.teacherscheduler.security.JwtService;
import com.nimbusds.jose.JOSEException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MagicLinkService {

    private final JwtService jwtService;
    private final EmailService emailService;

    @Autowired
    public MagicLinkService(
            JwtService jwtService,
            EmailService emailService

    ) {
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    public void sendMagicLink(User user) throws JOSEException {

        // Create short-lived JWT
        Map<String, Object> claims = new HashMap<>();
        claims.put("magic", true);
        claims.put("roles", user.getRoles());

        String magicToken = jwtService.generateToken(user.getEmail(), claims);

        // Build link
        String link = "https://yourfrontend.com/magic-login?token=" + magicToken;

        // Send email (use Postmark/SendGrid/etc.)
        emailService.sendEmail(user.getEmail(), link);
    }
}
