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
                    JWTClaimsSet claims = jwtService.validateToken(accessTokenCookie.get().getValue());

                    // 2. Create Authentication object
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            claims.getSubject(),   // principal (email)
                            null,                 // credentials (none)
                            Collections.emptyList() // roles/authorities if you want
                    );
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 3. Store in SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(auth);

                } catch (Exception e) {
                    // Token invalid or expired → ignore, let it proceed as unauthenticated
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
