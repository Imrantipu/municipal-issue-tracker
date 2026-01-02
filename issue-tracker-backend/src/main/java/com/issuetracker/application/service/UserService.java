package com.issuetracker.application.service;

import com.issuetracker.domain.model.Role;
import com.issuetracker.domain.model.User;
import com.issuetracker.domain.port.in.LoginUserUseCase;
import com.issuetracker.domain.port.in.RegisterUserUseCase;
import com.issuetracker.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * UserService - Application Service (Orchestration Layer)
 *
 * DESIGN PATTERN: Facade Pattern (Structural)
 * - Provides simplified interface to complex subsystem
 * - Orchestrates: Domain validation + Password hashing + JWT generation + Database persistence
 * - Controllers only call one method instead of managing multiple steps
 * - Benefits: Simplifies client code, reduces dependencies, centralizes orchestration
 *
 * DESIGN PATTERN: Dependency Injection Pattern (Creational)
 * - @RequiredArgsConstructor generates constructor for dependency injection
 * - Dependencies (UserRepository, PasswordEncoder, JwtTokenProvider) injected by Spring
 * - Benefits: Testability (can inject mocks), loose coupling, flexible configuration
 *
 * DESIGN PATTERN: Strategy Pattern (Behavioral)
 * - Uses PasswordEncoder interface (BCrypt is ONE strategy for password hashing)
 * - Could swap BCrypt → Argon2 by changing Spring Bean configuration
 * - JwtTokenProvider is also a strategy (could use different JWT library)
 * - Benefits: Algorithm interchangeable at runtime, open/closed principle
 *
 * DESIGN PATTERN: Proxy Pattern (Structural)
 * - @Transactional creates dynamic proxy around this class
 * - Proxy intercepts method calls, opens DB transaction, commits/rolls back
 * - Benefits: Separation of concerns (business logic doesn't manage transactions)
 *
 * SOLID Principles Applied:
 * - Single Responsibility (S): Orchestrates user operations only
 * - Dependency Inversion (D): Depends on interfaces (UserRepository, PasswordEncoder)
 * - Interface Segregation (I): Implements specific use case interfaces
 *
 * Responsibilities:
 * 1. Implement use case interfaces (RegisterUserUseCase, LoginUserUseCase)
 * 2. Orchestrate domain logic + infrastructure (password hashing, JWT)
 * 3. Handle transactions
 * 4. Throw business exceptions
 *
 * NOT responsible for:
 * - HTTP/REST concerns (that's in controllers)
 * - Database details (that's in JpaUserRepository)
 * - Business validation (that's in User domain model)
 */
@Service
@RequiredArgsConstructor  // DESIGN PATTERN: Dependency Injection - generates constructor
@Transactional  // DESIGN PATTERN: Proxy - Spring creates transaction proxy around this class
public class UserService implements RegisterUserUseCase, LoginUserUseCase {

    // DESIGN PATTERN: Dependency Injection - dependencies injected via constructor
    // DESIGN PATTERN: Strategy Pattern - PasswordEncoder is a strategy interface
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;  // Strategy: BCrypt (could be Argon2, PBKDF2, etc.)
    private final JwtTokenProvider jwtTokenProvider;  // Strategy: JWT generation

    /**
     * Register a new user.
     *
     * Flow:
     * 1. Check if email already exists
     * 2. Create User domain object
     * 3. Validate business rules (User.validate())
     * 4. Hash password
     * 5. Save to database
     *
     * @param command registration data
     * @return created user
     * @throws EmailAlreadyExistsException if email exists
     * @throws IllegalArgumentException if validation fails
     */
    @Override
    public User registerUser(RegisterUserCommand command) {
        // Business Rule: Email must be unique
        if (userRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyExistsException(command.email());
        }

        // Create domain object
        User user = User.builder()
                .name(command.name())
                .email(command.email().toLowerCase())  // Normalize email
                .password(command.password())  // Plain text (will be hashed below)
                .role(parseRole(command.role()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Domain validation (throws IllegalArgumentException if invalid)
        user.validate();

        // Infrastructure concern: Hash password
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);

        // Save to database (via repository interface)
        return userRepository.save(user);
    }

    /**
     * Authenticate user and generate JWT token.
     *
     * Flow:
     * 1. Find user by email
     * 2. Check if user exists
     * 3. Check if user is active (not deleted)
     * 4. Verify password
     * 5. Generate JWT token
     *
     * @param command login credentials
     * @return auth response with JWT token
     * @throws InvalidCredentialsException if email/password wrong
     * @throws UserDeletedException if user is deleted
     */
    @Override
    public AuthResponse login(LoginCommand command) {
        // Find user by email
        User user = userRepository.findByEmail(command.email().toLowerCase())
                .orElseThrow(InvalidCredentialsException::new);

        // Business Rule: User must be active
        if (user.isDeleted()) {
            throw new UserDeletedException();
        }

        // Verify password (BCrypt comparison)
        if (!passwordEncoder.matches(command.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        // Generate JWT token (infrastructure concern)
        String token = jwtTokenProvider.generateToken(user);

        // Return auth response
        return new AuthResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name()
        );
    }

    /**
     * Helper: Parse role string to Role enum.
     * Defaults to CITIZEN if not specified or invalid.
     */
    private Role parseRole(String roleString) {
        if (roleString == null || roleString.trim().isEmpty()) {
            return Role.CITIZEN;  // Default role
        }

        try {
            return Role.valueOf(roleString.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Role.CITIZEN;  // Default if invalid
        }
    }

    /**
     * JwtTokenProvider interface - will be created in infrastructure layer.
     * This is just a placeholder to show the dependency.
     */
    public interface JwtTokenProvider {
        String generateToken(User user);
    }
}
