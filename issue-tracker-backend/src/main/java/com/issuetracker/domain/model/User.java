package com.issuetracker.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User domain model - represents a user in the issue tracking system.
 *
 * DESIGN PATTERN: Builder Pattern (Creational)
 * - Uses Lombok @Builder annotation to generate builder class at compile time
 * - Provides fluent API for object construction: User.builder().name("John").email("...").build()
 * - Why: User has 8 fields; builder is more readable than constructor with many parameters
 * - Benefits: Optional fields, immutability after build, method chaining
 *
 * SOLID Principles Applied:
 * - Single Responsibility (S): User knows about user data and validation only
 * - Open/Closed (O): Can extend with new validation methods without modifying existing code
 *
 * Why no JPA annotations?
 * - Domain layer must be framework-independent
 * - JPA annotations (@Entity, @Table) belong in infrastructure layer (UserEntity)
 * - This allows testing without Spring Boot or database
 *
 * Usage Example:
 * <pre>
 * User user = User.builder()
 *     .name("John Doe")
 *     .email("john@example.com")
 *     .password("hashed-password")
 *     .role(Role.CITIZEN)
 *     .createdAt(LocalDateTime.now())
 *     .build();
 * </pre>
 */
@Data
@Builder  // DESIGN PATTERN: Builder Pattern - generates builder class
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;
    private String name;
    private String email;
    private String password;  // Will be hashed by infrastructure layer
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;  // Soft delete for GDPR compliance

    /**
     * Business Logic: Validate user data before creation.
     *
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        validateName();
        validateEmail();
        validatePassword();
        validateRole();
    }

    /**
     * Business Rule: Name must be between 2 and 100 characters.
     */
    private void validateName() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (name.length() < 2) {
            throw new IllegalArgumentException("Name must be at least 2 characters");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Name cannot exceed 100 characters");
        }
    }

    /**
     * Business Rule: Email must be valid format.
     */
    private void validateEmail() {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        // Basic email regex (frontend does more detailed validation)
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!email.matches(emailRegex)) {
            throw new IllegalArgumentException("Email format is invalid");
        }
        if (email.length() > 255) {
            throw new IllegalArgumentException("Email cannot exceed 255 characters");
        }
    }

    /**
     * Business Rule: Password must be at least 8 characters.
     * Note: Password will be hashed before storage (infrastructure concern)
     */
    private void validatePassword() {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        if (password.length() > 255) {
            throw new IllegalArgumentException("Password cannot exceed 255 characters");
        }
    }

    /**
     * Business Rule: Role must be specified.
     */
    private void validateRole() {
        if (role == null) {
            throw new IllegalArgumentException("Role must be specified");
        }
    }

    /**
     * Business Logic: Check if user is deleted (soft delete).
     *
     * @return true if user is soft-deleted
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Business Logic: Check if user is active.
     *
     * @return true if user is not deleted
     */
    public boolean isActive() {
        return !isDeleted();
    }

    /**
     * Business Logic: Check if user has admin privileges.
     *
     * @return true if role is ADMIN
     */
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    /**
     * Business Logic: Check if user is staff member.
     *
     * @return true if role is STAFF
     */
    public boolean isStaff() {
        return role == Role.STAFF;
    }

    /**
     * Business Logic: Check if user is citizen.
     *
     * @return true if role is CITIZEN
     */
    public boolean isCitizen() {
        return role == Role.CITIZEN;
    }

    /**
     * Business Logic: Soft delete user (GDPR compliance).
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Business Logic: Restore soft-deleted user.
     */
    public void restore() {
        this.deletedAt = null;
    }
}
