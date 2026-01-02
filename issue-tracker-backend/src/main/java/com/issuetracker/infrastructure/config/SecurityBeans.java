package com.issuetracker.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * SecurityBeans - Spring Bean Configuration
 *
 * DESIGN PATTERN: Singleton Pattern (Creational)
 * - @Bean methods create singleton beans (one instance per application)
 * - PasswordEncoder is singleton (shared across all services)
 * - Benefits: Memory efficiency, thread-safe, consistent hashing
 *
 * DESIGN PATTERN: Strategy Pattern (Behavioral)
 * - PasswordEncoder is an interface (strategy)
 * - BCryptPasswordEncoder is ONE strategy implementation
 * - Could swap to: Argon2PasswordEncoder, Pbkdf2PasswordEncoder, SCryptPasswordEncoder
 * - UserService depends on PasswordEncoder interface, not BCrypt specifically
 * - Benefits: Flexible hashing algorithm, easy to upgrade security
 *
 * Provides security-related beans for dependency injection.
 *
 * Why separate from SecurityConfig?
 * - SecurityConfig handles HTTP security (filters, CORS, etc.)
 * - SecurityBeans provides reusable components (PasswordEncoder)
 * - Cleaner separation of concerns
 */
@Configuration
public class SecurityBeans {

    /**
     * Password encoder bean using BCrypt algorithm.
     *
     * DESIGN PATTERN: Singleton Pattern
     * - @Bean creates singleton bean (one instance per application)
     *
     * DESIGN PATTERN: Strategy Pattern
     * - Returns PasswordEncoder interface (not BCryptPasswordEncoder directly)
     * - BCrypt is ONE strategy; could be swapped to Argon2, PBKDF2, etc.
     *
     * BCrypt Features:
     * - Adaptive hashing (cost factor can be increased over time)
     * - Automatic salt generation
     * - One-way hashing (cannot be decrypted)
     * - Industry standard for password hashing
     *
     * Cost Factor: 10 (default)
     * - 2^10 = 1024 iterations
     * - Higher = more secure but slower
     * - 10 is a good balance for 2024
     *
     * @return BCryptPasswordEncoder instance
     */
    @Bean  // DESIGN PATTERN: Singleton - creates singleton bean
    public PasswordEncoder passwordEncoder() {  // DESIGN PATTERN: Strategy - returns interface, not concrete class
        return new BCryptPasswordEncoder();  // BCrypt is ONE strategy implementation
    }
}
