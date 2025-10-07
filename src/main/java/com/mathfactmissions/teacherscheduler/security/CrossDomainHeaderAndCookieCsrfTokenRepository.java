package com.mathfactmissions.teacherscheduler.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.util.Assert;

public class CrossDomainHeaderAndCookieCsrfTokenRepository implements CsrfTokenRepository {

    private final CookieCsrfTokenRepository cookieCsrfTokenRepository;
    private final String csrfHeaderName;
    private final String csrfParameterName;
    private final String cookieDomain;

    public CrossDomainHeaderAndCookieCsrfTokenRepository(String csrfHeaderName, String cookieDomain) {
        Assert.notNull(csrfHeaderName, "csrfHeaderName cannot be null");
        this.csrfHeaderName = csrfHeaderName;
        this.cookieDomain = cookieDomain;
        this.csrfParameterName = "_csrf"; // Default parameter name
        this.cookieCsrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
    }

    @Override
    public CsrfToken generateToken(HttpServletRequest request) {
        return this.cookieCsrfTokenRepository.generateToken(request);
    }

    @Override
    public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
        // Save the token in a cookie readable by JS
        this.cookieCsrfTokenRepository.saveToken(token, request, response);

        // Explicitly set the domain for cross-subdomain access
        if (token != null) {
            String cookieHeader = response.getHeader("Set-Cookie");
            if (cookieHeader != null) {
                // Ensure the domain is set correctly for cross-subdomain
                // Ex: .yourdomain.com will allow access from both api.yourdomain.com and app.yourdomain.com
                response.setHeader("Set-Cookie", cookieHeader.replaceAll("Path=/", "Path=/; Domain=" + this.cookieDomain + "; SameSite=None; Secure"));
            }
        }

        // Also add the token to a response header for easier client-side retrieval
        if (token != null) {
            response.setHeader(this.csrfHeaderName, token.getToken());
        }
    }

    @Override
    public CsrfToken loadToken(HttpServletRequest request) {
        CsrfToken token = this.cookieCsrfTokenRepository.loadToken(request);
        if (token == null) {
            String tokenValue = request.getHeader(this.csrfHeaderName);
            if (tokenValue != null) {
                token = new DefaultCsrfToken(this.csrfHeaderName, this.csrfParameterName, tokenValue);
            }
        }
        return token;
    }
}
