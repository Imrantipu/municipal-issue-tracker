package com.issuetracker.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JwtAuthenticationFilter - Validates JWT tokens on every request
 *
 * DESIGN PATTERN: Filter Chain Pattern (Behavioral)
 * - Part of Spring Security's filter chain
 * - Each filter processes request and calls next filter (chain of responsibility)
 * - This filter checks JWT token, then passes request to next filter
 * - Benefits: Separation of concerns, modular request processing, order control
 *
 * DESIGN PATTERN: Template Method Pattern (Behavioral)
 * - OncePerRequestFilter defines template method (doFilter)
 * - We override doFilterInternal() which is called by template method
 * - Parent class handles: ensuring once-per-request, OPTIONS handling, error handling
 * - Subclass implements: specific filtering logic (JWT validation)
 * - Benefits: Reuses framework logic, focuses on specific behavior
 *
 * How it works:
 * 1. Extract JWT token from Authorization header ("Bearer <token>")
 * 2. Validate token using JwtTokenProvider
 * 3. If valid, set authentication in Spring Security context
 * 4. If invalid or missing, continue without authentication (public endpoints still work)
 *
 * Extends OncePerRequestFilter:
 * - Guarantees filter runs once per request (Spring Boot optimization)
 * - Automatically handles OPTIONS requests (CORS preflight)
 */
@Component
@RequiredArgsConstructor  // DESIGN PATTERN: Dependency Injection
public class JwtAuthenticationFilter extends OncePerRequestFilter {  // DESIGN PATTERN: Filter Chain + Template Method

    private final JwtTokenProvider jwtTokenProvider;  // Injected by Spring

    /**
     * DESIGN PATTERN: Template Method Pattern
     * - This method is called by OncePerRequestFilter's template method
     * - Parent handles: once-per-request guarantee, exception handling
     * - This method implements: specific JWT validation logic
     */
    @Override
    protected void doFilterInternal(  // DESIGN PATTERN: Template Method - hook method
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain  // DESIGN PATTERN: Filter Chain - next filter in chain
    ) throws ServletException, IOException {

        try {
            // 1. Extract JWT from request header
            String jwt = getJwtFromRequest(request);

            // 2. Validate token and set authentication
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                // Extract user information from token
                Long userId = jwtTokenProvider.getUserIdFromToken(jwt);
                String email = jwtTokenProvider.getEmailFromToken(jwt);
                String role = jwtTokenProvider.getRoleFromToken(jwt);

                // Create Spring Security authentication object
                // Principal: email
                // Credentials: null (already authenticated via JWT)
                // Authorities: role (e.g., "ROLE_ADMIN")
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role))
                        );

                // Set additional details (IP address, session ID, etc.)
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Set authentication in Spring Security context
                // This makes user authenticated for this request
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            // Log error but don't fail the request
            // Let SecurityConfig handle unauthorized access
            logger.error("Could not set user authentication in security context", ex);
        }

        // Continue filter chain (next filter or controller)
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header.
     *
     * Expected format: "Authorization: Bearer <token>"
     *
     * @param request HTTP request
     * @return JWT token string or null if not found
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // Remove "Bearer " prefix
        }

        return null;
    }
}
