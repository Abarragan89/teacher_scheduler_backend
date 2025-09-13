package com.mathfactmissions.teacherscheduler.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final byte[] secretKey;
    private final long expirationMinutes;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-minutes}") long expirationMinutes
    ) {
        this.secretKey = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationMinutes = expirationMinutes;
    }

    // Create a JWT
    public String generateToken(String subject, Map<String, Object> customClaims) throws JOSEException {
        Instant now = Instant.now();

        // Create the JWT object with standard payload
        JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                .subject(subject)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(expirationMinutes * 60)));

        // Add roles to payload if exists
        if (customClaims != null) {
            customClaims.forEach(claimsBuilder::claim);
        }

        // Create a Sign JWT Object with algorithm and the claimsBuilder's return of the build() method
        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader(JWSAlgorithm.HS256),
                claimsBuilder.build()
        );

        // Sign the JWT Object with our sectret key and the
        signedJWT.sign(new MACSigner(secretKey));

        return signedJWT.serialize();
    }

    // Validate & parse a JWT
    public JWTClaimsSet validateToken(String token) throws ParseException, JOSEException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        if (!signedJWT.verify(new MACVerifier(secretKey))) {
            throw new JOSEException("Invalid signature");
        }

        Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        if (expirationTime.before(new Date())) {
            throw new JOSEException("Token expired");
        }

        return signedJWT.getJWTClaimsSet();
    }
}
