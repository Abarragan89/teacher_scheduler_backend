package com.mathfactmissions.teacherscheduler.controller;

import com.mathfactmissions.teacherscheduler.dto.CreateUserRequest;
import com.mathfactmissions.teacherscheduler.dto.LoginRequest;
import com.mathfactmissions.teacherscheduler.model.User;
import com.mathfactmissions.teacherscheduler.security.JwtService;
import com.mathfactmissions.teacherscheduler.service.MagicLinkService;
import com.mathfactmissions.teacherscheduler.service.UserService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

   private final JwtService jwtService;
   private final UserService userService;
   private final MagicLinkService magicLinkService;

   @Autowired
    public AuthController(
            JwtService jwtService,
            UserService userService,
            MagicLinkService magicLinkService
   ) {
       this.jwtService = jwtService;
       this.userService = userService;
       this.magicLinkService = magicLinkService;
   }

    @PostMapping("/magic-link-request")
    public ResponseEntity<?> requestMagicLink(@Valid @RequestBody CreateUserRequest dto) throws JOSEException {
        // Ensure user exists or create them
        User user = userService.findOrCreateUser(dto.getEmail());
        magicLinkService.sendMagicLink(user);
        return ResponseEntity.ok(Map.of(
                "message", "Magic link sent",
                "email", user.getEmail()
        ));
    }

    @PostMapping("/magic-link-verify")
    public ResponseEntity<?> verifyMagicLink(@RequestBody Map<String, String> body)
            throws JOSEException, ParseException {

        // Get magic token from request
        String magicToken = body.get("token");

        JWTClaimsSet magicClaims = jwtService.validateToken(magicToken);

        if (magicClaims.getBooleanClaim("magic") == null || !magicClaims.getBooleanClaim("magic")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Find user by email
        String email = magicClaims.getSubject();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Build claims for access token
        Map<String, Object> accessClaims = new HashMap<>();
        accessClaims.put("roles", user.getRoles());

        // Generate real tokens
        String accessToken = jwtService.generateToken(user.getEmail(), accessClaims);
        String refreshToken = jwtService.generateToken(user.getEmail(), Map.of("type", "refresh"));

        // Cookies
        ResponseCookie accessCookie = ResponseCookie.from("access_token", accessToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(900) // 15 min
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(604800) // 7 days
                .build();

        // CSRF token
        String csrfToken = UUID.randomUUID().toString();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(Map.of("csrfToken", csrfToken));
    }



    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue(name="refresh_token") String refreshToken) throws JOSEException, ParseException {

       // Validate refresh token
        JWTClaimsSet claims = jwtService.validateToken(refreshToken);

        String email = claims.getSubject();

        // Generate new access token
        String newAccessToken = jwtService.generateToken(email, Map.of("roles", claims.getClaim("roles")));

        ResponseCookie newAccessCookie = ResponseCookie.from("access_token", newAccessToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(900)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, newAccessCookie.toString())
                .body(Map.of("csrfToken", UUID.randomUUID().toString()));

    }


}
