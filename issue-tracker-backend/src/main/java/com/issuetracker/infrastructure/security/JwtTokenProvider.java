package com.issuetracker.infrastructure.security;

import com.issuetracker.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JwtTokenProvider - JWT Token Generation and Validation
 *
 * DESIGN PATTERN: Strategy Pattern (Behavioral)
 * - Implements JwtTokenProvider interface from application layer
 * - Provides ONE strategy for JWT token generation (JJWT library + HMAC-SHA256)
 * - Could swap to different strategy: RS256, OAuth2, Keycloak, etc.
 * - UserService depends on interface, not this concrete implementation
 * - Benefits: Flexible authentication mechanism, testable, open/closed principle
 *
 * DESIGN PATTERN: Singleton Pattern (Creational)
 * - @Component creates singleton bean (one instance per application)
 * - Shared across all requests (thread-safe because stateless)
 * - Benefits: Memory efficiency, consistent token generation
 *
 * Responsibilities:
 * - Generate JWT tokens after successful login
 * - Validate JWT tokens from requests
 * - Extract user information from tokens
 *
 * JWT Structure:
 * - Header: Algorithm (HS256)
 * - Payload: userId, email, role, expiration
 * - Signature: HMAC SHA-256 with secret key
 *
 * Security Notes:
 * - Secret key must be at least 256 bits (32 characters)
 * - Tokens expire after jwt.expiration milliseconds (24 hours default)
 * - Never store sensitive data in JWT (e.g., password)
 */
@Component  // DESIGN PATTERN: Singleton - creates singleton bean
public class JwtTokenProvider implements com.issuetracker.application.service.UserService.JwtTokenProvider {  // DESIGN PATTERN: Strategy Pattern

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Generate JWT token for authenticated user.
     *
     * Token contains:
     * - Subject: user ID
     * - Claim "email": user email
     * - Claim "role": user role
     * - Issued at: current time
     * - Expiration: current time + jwt.expiration
     *
     * @param user the authenticated user
     * @return JWT token string
     */
    @Override
    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .setSubject(user.getId().toString())  // User ID
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract user ID from JWT token.
     *
     * @param token JWT token
     * @return user ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * Extract email from JWT token.
     *
     * @param token JWT token
     * @return user email
     */
    public String getEmailFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("email", String.class);
    }

    /**
     * Extract role from JWT token.
     *
     * @param token JWT token
     * @return user role
     */
    public String getRoleFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("role", String.class);
    }

    /**
     * Validate JWT token.
     *
     * Checks:
     * - Token signature is valid
     * - Token is not expired
     *
     * @param token JWT token
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            // Invalid signature, expired token, malformed token, etc.
            return false;
        }
    }

    /**
     * Parse JWT token and extract claims.
     *
     * @param token JWT token
     * @return claims (payload)
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Get signing key from secret.
     * Converts secret string to SecretKey object for HMAC SHA-256.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
