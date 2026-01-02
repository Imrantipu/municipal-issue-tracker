package com.issuetracker.domain.port.in;

import com.issuetracker.domain.model.User;

/**
 * RegisterUserUseCase - Input Port
 *
 * DESIGN PATTERN: Command Pattern (Behavioral)
 * - RegisterUserCommand encapsulates registration request as an object
 * - Method takes single command parameter instead of multiple individual parameters
 * - Benefits: Extensibility (add fields without breaking method signature), validation, logging
 *
 * SOLID Principle: Interface Segregation (I)
 * - One interface for ONE use case (register user)
 * - Not a giant "UserService" with 20 methods
 *
 * SOLID Principle: Dependency Inversion (D)
 * - Controllers depend on this interface (not concrete implementation)
 * - Implementation is in application layer (UserService)
 *
 * Why "port/in"?
 * - "In" = External world calls domain (controller → use case)
 * - Defines WHAT the application can do
 * - Controllers implement UI, this defines business capability
 */
public interface RegisterUserUseCase {  // DESIGN PATTERN: Command Pattern

    /**
     * Register a new user in the system.
     *
     * Business Rules Enforced:
     * - Email must be unique
     * - Password will be hashed
     * - User must be validated
     * - Default role is CITIZEN (unless specified)
     *
     * @param command the registration data (name, email, password, role)
     * @return the created user with generated ID
     * @throws IllegalArgumentException if validation fails
     * @throws EmailAlreadyExistsException if email is already registered
     */
    User registerUser(RegisterUserCommand command);

    /**
     * Command object for user registration.
     *
     * DESIGN PATTERN: Command Pattern
     * - Encapsulates request as immutable object
     * - Java Record: Immutable, auto-generates constructor, getters, equals, hashCode
     * - Using command object (not individual parameters) makes API cleaner and extensible
     */
    record RegisterUserCommand(  // DESIGN PATTERN: Command Pattern (using Java Record)
            String name,
            String email,
            String password,
            String role  // "ADMIN", "STAFF", or "CITIZEN"
    ) {}

    /**
     * Exception thrown when email already exists.
     */
    class EmailAlreadyExistsException extends RuntimeException {
        public EmailAlreadyExistsException(String email) {
            super("Email already exists: " + email);
        }
    }
}
