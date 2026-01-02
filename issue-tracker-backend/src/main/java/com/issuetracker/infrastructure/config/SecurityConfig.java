package com.issuetracker.infrastructure.config;

import com.issuetracker.infrastructure.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * SecurityConfig - Spring Security Configuration
 *
 * DESIGN PATTERN: Filter Chain Pattern (Behavioral)
 * - Configures Spring Security's filter chain
 * - Request flows through filters: CORS → JWT → Authorization → Controller
 * - Each filter can process, modify, or reject requests
 * - Benefits: Modular security, separation of concerns, ordered execution
 *
 * DESIGN PATTERN: Singleton Pattern (Creational)
 * - @Bean methods create singleton beans (one instance per application)
 * - SecurityFilterChain is singleton (shared across all requests)
 * - CorsConfigurationSource is singleton
 * - Benefits: Memory efficiency, shared configuration, thread-safe
 *
 * DESIGN PATTERN: Proxy Pattern (Structural)
 * - @EnableMethodSecurity creates proxies for @PreAuthorize annotations
 * - Spring creates proxy around methods with security annotations
 * - Proxy checks authorization before calling actual method
 *
 * Configures:
 * 1. Public endpoints (register, login)
 * 2. Protected endpoints (require JWT authentication)
 * 3. JWT filter (validates tokens on each request)
 * 4. CORS (allow Next.js frontend)
 * 5. Session management (stateless for JWT)
 * 6. CSRF (disabled for stateless API)
 *
 * Security Flow:
 * Request → CORS filter → JWT filter → Authorization check → Controller
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // DESIGN PATTERN: Proxy - enables @PreAuthorize proxies
@RequiredArgsConstructor  // DESIGN PATTERN: Dependency Injection
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;  // Injected by Spring

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    /**
     * Security filter chain - configures HTTP security.
     *
     * DESIGN PATTERN: Singleton Pattern
     * - @Bean creates a singleton SecurityFilterChain
     * - One instance shared across all HTTP requests
     *
     * DESIGN PATTERN: Filter Chain Pattern
     * - Configures chain of security filters
     * - Order: CORS → JWT → Authorization → Controller
     *
     * Order matters:
     * 1. CSRF disabled (not needed for stateless JWT)
     * 2. CORS enabled
     * 3. Session management (stateless)
     * 4. Authorization rules (public vs protected)
     * 5. JWT filter added
     *
     * @param http HttpSecurity builder
     * @return configured SecurityFilterChain
     */
    @Bean  // DESIGN PATTERN: Singleton - creates singleton bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {  // DESIGN PATTERN: Filter Chain
        http
                // Disable CSRF (not needed for stateless JWT API)
                .csrf(csrf -> csrf.disable())

                // Enable CORS with configuration
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Session management: Stateless (no sessions, only JWT)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints (no authentication required)
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )

                // Add JWT filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    /**
     * CORS configuration - allows Next.js frontend to call API.
     *
     * DESIGN PATTERN: Singleton Pattern
     * - @Bean creates singleton CorsConfigurationSource
     * - One instance shared across all requests
     *
     * CORS (Cross-Origin Resource Sharing):
     * - Frontend: http://localhost:3000 (Next.js)
     * - Backend: http://localhost:8080 (Spring Boot)
     * - Without CORS, browser blocks requests (security)
     *
     * @return CORS configuration source
     */
    @Bean  // DESIGN PATTERN: Singleton - creates singleton bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allowed origins (from application.properties)
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));

        // Allowed HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        // Allowed headers
        configuration.setAllowedHeaders(List.of("*"));

        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Max age (how long browser can cache CORS response)
        configuration.setMaxAge(3600L);

        // Apply CORS configuration to all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
