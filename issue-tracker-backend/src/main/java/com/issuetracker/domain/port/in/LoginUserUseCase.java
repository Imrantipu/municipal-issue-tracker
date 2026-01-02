package com.issuetracker.domain.port.in;

/**
 * LoginUserUseCase - Input Port
 *
 * DESIGN PATTERN: Command Pattern (Behavioral)
 * - LoginCommand encapsulates login request as an object
 * - Method takes single command parameter instead of multiple individual parameters
 * - Benefits: Extensibility, validation, logging, undo/redo capability
 *
 * DESIGN PATTERN: DTO Pattern (Data Transfer Object)
 * - AuthResponse transfers data from application layer → controller → client
 * - Immutable record (cannot be modified after creation)
 * - Prevents exposing domain User object (no password field in response)
 *
 * SOLID Principle: Interface Segregation (I)
 * - One interface for ONE use case (login)
 * - Separate from RegisterUserUseCase
 *
 * Why separate from RegisterUserUseCase?
 * - Login and registration are different business operations
 * - Controller can depend on only what it needs
 * - Testing is easier (mock only what you use)
 */
public interface LoginUserUseCase {  // DESIGN PATTERN: Command Pattern

    /**
     * Authenticate user and generate JWT token.
     *
     * Business Rules Enforced:
     * - Email must exist
     * - Password must match (after BCrypt comparison)
     * - User must be active (not soft-deleted)
     * - Generate JWT token with user ID and role
     *
     * @param command the login credentials (email, password)
     * @return authentication response with JWT token and user details
     * @throws InvalidCredentialsException if email/password is wrong
     * @throws UserDeletedException if user is soft-deleted
     */
    AuthResponse login(LoginCommand command);

    /**
     * Command object for login.
     *
     * DESIGN PATTERN: Command Pattern
     * - Encapsulates login request as immutable object
     * - Java Record: Immutable, auto-generates constructor, getters, equals, hashCode
     */
    record LoginCommand(  // DESIGN PATTERN: Command Pattern (using Java Record)
            String email,
            String password
    ) {}

    /**
     * Response object containing JWT token and user info.
     *
     * DESIGN PATTERN: DTO (Data Transfer Object)
     * - Immutable record for transferring authentication data
     * - Separates domain model (User) from API response
     * - Security: No password field (User has password, AuthResponse does not)
     */
    record AuthResponse(  // DESIGN PATTERN: DTO Pattern
            String token,
            Long userId,
            String name,
            String email,
            String role
    ) {}

    /**
     * Exception thrown when credentials are invalid.
     */
    class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException() {
            super("Invalid email or password");
        }
    }

    /**
     * Exception thrown when user is deleted.
     */
    class UserDeletedException extends RuntimeException {
        public UserDeletedException() {
            super("User account has been deleted");
        }
    }
}
