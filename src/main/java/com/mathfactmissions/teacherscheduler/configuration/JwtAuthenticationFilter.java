package com.mathfactmissions.teacherscheduler.security;

import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Get access_token cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Optional<Cookie> accessTokenCookie =
                    java.util.Arrays.stream(cookies)
                            .filter(c -> "access_token".equals(c.getName()))
                            .findFirst();

            if (accessTokenCookie.isPresent()) {
                try {

                    // 2. Get JWT data to put in cookie
                    JWTClaimsSet claims = jwtService.validateToken(accessTokenCookie.get().getValue());
                    UUID userId = UUID.fromString(claims.getStringClaim("user_id"));
                    String email = claims.getSubject();

                    UserPrincipal principal = new UserPrincipal(email, userId);

                    // 2. Create Authentication object
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            principal,   // principal (email)
                            null,                 // credentials (none)
                            Collections.emptyList() // roles/authorities if you want
                    );
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 3. Store in SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(auth);

                } catch (Exception e) {
                    // Token invalid or expired → ignore, let it proceed as unauthenticated
                    System.out.println("exception " + e);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
