package com.mathfactmissions.teacherscheduler.controller;

import com.mathfactmissions.teacherscheduler.dto.user.request.CreateUserRequest;
import com.mathfactmissions.teacherscheduler.dto.user.response.UserResponse;
import com.mathfactmissions.teacherscheduler.model.User;
import com.mathfactmissions.teacherscheduler.security.JwtService;
import com.mathfactmissions.teacherscheduler.service.MagicLinkService;
import com.mathfactmissions.teacherscheduler.service.UserService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.servlet.http.HttpServletResponse;
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
        UserResponse user = userService.findOrCreateUser(dto.email());
        magicLinkService.sendMagicLink(user);
        return ResponseEntity.ok(Map.of(
                "message", "Magic link sent",
                "email", user.email()
        ));
    }

    @PostMapping("/magic-link-verify")
    public ResponseEntity<?> verifyMagicLink(
            @RequestBody Map<String, String> body
    )
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
        accessClaims.put("user_id", user.getId());

        // Generate real tokens
        String accessToken = jwtService.generateToken(user.getEmail(), accessClaims, 15); // 15 minute expiration
        String refreshToken = jwtService.generateToken(user.getEmail(), Map.of("type", "refresh"), 43200); // 30 day expiration

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
                .maxAge(2592000) // 30 days
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(Map.of("message", "Link Verified"));
    }



    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue(name="refresh_token") String refreshToken) throws JOSEException, ParseException {

       // Validate refresh token
        JWTClaimsSet claims = jwtService.validateToken(refreshToken);

        // Get user email
        String email = claims.getSubject();

        // Look up user
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Build new access token with queried roles
        Map<String, Object> accessClaims = new HashMap<>();
        accessClaims.put("roles", user.getRoles());
        accessClaims.put("user_id", user.getId());

        // Create new access token
        String newAccessToken = jwtService.generateToken(email, accessClaims, 15);

        ResponseCookie newAccessCookie = ResponseCookie.from("access_token", newAccessToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(900) // 15 min
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, newAccessCookie.toString())
                .body(Map.of("message", "Token refreshed"));

    }

    @GetMapping("/session")
    public ResponseEntity<?> verifySession(
            @CookieValue(name="access_token", required = false) String accessToken
    ) {
        System.out.println("ACEESS TOKEN IN SESSIoN " + accessToken);
       if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("authenticated", false));
        }

        try {
            JWTClaimsSet claims = jwtService.validateToken(accessToken);

            return ResponseEntity.ok(Map.of(
                    "authenticated", true,
                    "email", claims.getSubject(),
                    "roles", claims.getClaim("roles"),
                    "message", "session verified")
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("authenticated", false));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletResponse response) {
        // Clear access token cookie
        ResponseCookie accessTokenCookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0) // expire immediately
                .build();

        // Clear refresh token cookie
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        // If you’re storing refresh tokens in DB/Redis, also delete it there
        // refreshTokenService.invalidateUserTokens(userId);

        return ResponseEntity.ok("Logged out successfully");
    }
}
