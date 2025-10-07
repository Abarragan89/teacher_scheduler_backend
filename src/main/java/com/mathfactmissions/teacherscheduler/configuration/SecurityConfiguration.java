package com.mathfactmissions.teacherscheduler.configuration;

import com.mathfactmissions.teacherscheduler.security.CrossDomainHeaderAndCookieCsrfTokenRepository;
import com.mathfactmissions.teacherscheduler.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;



//@Configuration
//@EnableWebSecurity
//@EnableMethodSecurity
//public class SecurityConfiguration {
//
//    private final JwtAuthenticationFilter jwtFilter;
//
//    @Value("${client.url}")
//    private String clientUrl;
//
//    @Autowired
//    public SecurityConfiguration(JwtAuthenticationFilter jwtFilter) {
//        this.jwtFilter = jwtFilter;
//    }
//
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
////        This makes the shorter token the correct version to check from frontend to backend
//        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
//        // Set the attribute name to null to force token generation on all requests
//        requestHandler.setCsrfRequestAttributeName(null);
//
////        CookieCsrfTokenRepository tokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
////        tokenRepository.setCookieCustomizer(builder -> builder
////                .sameSite("None")              // allow cross-site usage
////                .secure(true)                  // must be Secure when SameSite=None (HTTPS required)
////                .httpOnly(false)                // readable by JS (so frontend can read XSRF-TOKEN cookie)
////                .path("/")                     // global path
////        );
//
//
//        http
//            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
////            .csrf(csrf -> csrf
////                    .csrfTokenRepository(tokenRepository)
////                    .ignoringRequestMatchers(
////                            "/auth/magic-link-request",
////                            "/auth/magic-link-verify",
////                            "/auth/logout",
////                            "/auth/refresh")
////                    .csrfTokenRequestHandler(requestHandler)
////            )
//
//                .csrf(csrf -> csrf
//                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()) // Example: Use HttpOnlyFalse for client-side access
//                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()) // For modern Spring Security
//                        .ignoringRequestMatchers(
//                                "/auth/magic-link-request",
//                                "/auth/magic-link-verify",
//                                "/auth/logout",
//                                "/auth/refresh"
//                        )
//                )
//            .sessionManagement(session -> session
//                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//            )
//            .authorizeHttpRequests(auth -> auth
//                    .requestMatchers(
//                            "/auth/magic-link-request",
//                            "/auth/magic-link-verify",
//                            "/auth/logout",
//                            "/auth/refresh"
//                    ).permitAll()
//                    .anyRequest().authenticated()
//            )
//            .addFilterBefore(jwtFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
//            .exceptionHandling(ex -> ex
//                .authenticationEntryPoint((request, response, authException) -> {
//                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                })
//        );
//        return http.build();
//    }
//
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(List.of(clientUrl));
//        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//        configuration.setAllowedHeaders(List.of("*"));
//        configuration.setAllowCredentials(true);
//        configuration.setExposedHeaders(List.of("Set-Cookie"));
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//
//        return source;
//    }
//
//}







@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtFilter;

    @Value("${client.url}")
    private String clientUrl;

    @Value("${cookie.domain}")
    private String cookieDomain;

    @Autowired
    public SecurityConfiguration(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName(null);

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository()) // Use the custom token repository
                        .ignoringRequestMatchers(
                                "/auth/magic-link-request",
                                "/auth/magic-link-verify",
                                "/auth/logout",
                                "/auth/refresh")
                        .csrfTokenRequestHandler(requestHandler)
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/magic-link-request",
                                "/auth/magic-link-verify",
                                "/auth/logout",
                                "/auth/refresh"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        })
                );
        return http.build();
    }

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        return new CrossDomainHeaderAndCookieCsrfTokenRepository("X-XSRF-TOKEN", ".teachforfree.com");
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(clientUrl));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setExposedHeaders(List.of("Set-Cookie", "X-XSRF-TOKEN"));
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}

