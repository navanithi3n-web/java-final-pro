package com.gym.tracker.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.tracker.service.AppUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

import java.util.Map;

/**
 * Security configuration for IronPulse.
 *
 * Design notes:
 * - Session-based login (no JWT). Simple and beginner-friendly.
 * - CSRF is disabled because all state-changing calls are JSON-over-fetch from the
 *   same origin, and we rely on the session cookie for auth. For a production system
 *   with third-party form submissions, you would want to re-enable CSRF and use the
 *   SameSite cookie attribute instead.
 * - CORS is configured in CorsConfig. With Spring Security present, CorsConfig's
 *   WebMvcConfigurer is still honoured because we call cors(Customizer.withDefaults())
 *   which delegates to the MVC CORS configuration.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final AppUserService appUserService;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public SecurityConfig(AppUserService appUserService, PasswordEncoder passwordEncoder, ObjectMapper objectMapper) {
        this.appUserService = appUserService;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper   = objectMapper;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(appUserService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF disabled – see class-level javadoc
            .csrf(csrf -> csrf.disable())

            // CORS: delegate to the WebMvcConfigurer in CorsConfig
            .cors(cors -> {})

            .authenticationProvider(authenticationProvider())

            .authorizeHttpRequests(auth -> auth
                // Allow forwarded requests from PageController and error dispatches
                .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.FORWARD, jakarta.servlet.DispatcherType.ERROR).permitAll()
                // Public static assets
                .requestMatchers("/css/**", "/js/**", "/images/**", "/fonts/**", "/favicon.ico").permitAll()
                // Login page (HTML) and auth API
                .requestMatchers("/login", "/login.html", "/api/auth/login", "/api/auth/logout").permitAll()
                // H2 console (dev only, guarded by profile in production)
                .requestMatchers("/h2-console/**").permitAll()
                // ADMIN-only: user management and DELETE operations
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                // Everything else requires authentication
                .anyRequest().authenticated()
            )

            // When an unauthenticated request hits an /api/** path -> 401 JSON (not a redirect)
            .exceptionHandling(ex -> ex
                .defaultAuthenticationEntryPointFor(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                    request -> request.getRequestURI().startsWith("/api/")
                )
                // For page requests, redirect to /login
                .authenticationEntryPoint((request, response, authException) -> {
                    String uri = request.getRequestURI();
                    if (uri.startsWith("/api/")) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        objectMapper.writeValue(response.getWriter(),
                            Map.of("status", 401, "error", "Unauthorized",
                                   "message", "Authentication required"));
                    } else {
                        response.sendRedirect("/login");
                    }
                })
                // 403 for API calls should return JSON, not a redirect
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    objectMapper.writeValue(response.getWriter(),
                        Map.of("status", 403, "error", "Forbidden",
                               "message", "You do not have permission to perform this action"));
                })
            )

            // Allow frames for H2 console (dev only)
            .headers(headers -> headers.frameOptions(fo -> fo.sameOrigin()));

        return http.build();
    }
}
