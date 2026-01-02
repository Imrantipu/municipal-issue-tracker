package com.issuetracker.infrastructure.adapter.in.web;

import com.issuetracker.domain.model.User;
import com.issuetracker.domain.port.in.LoginUserUseCase;
import com.issuetracker.domain.port.in.RegisterUserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController - REST API for Authentication
 *
 * DESIGN PATTERN: DTO Pattern (Data Transfer Object)
 * - RegisterRequest: Transfers registration data from client → controller
 * - LoginRequest: Transfers login credentials from client → controller
 * - UserResponse: Transfers user data from controller → client (without password)
 * - DTOs prevent exposing domain objects directly to clients
 * - Benefits: Security (hide password), versioning, validation
 *
 * DESIGN PATTERN: Dependency Injection Pattern (Creational)
 * - @RequiredArgsConstructor generates constructor for dependency injection
 * - Dependencies (use cases) injected by Spring container
 * - Benefits: Testability (can inject mocks), loose coupling
 *
 * Endpoints:
 * - POST /api/auth/register - Register new user
 * - POST /api/auth/login - Login and get JWT token
 *
 * SOLID Principle: Dependency Inversion (D)
 * - Depends on use case interfaces (not concrete UserService)
 * - Direction: Infrastructure → Domain
 *
 * Error Handling:
 * - 400 Bad Request: Validation errors
 * - 409 Conflict: Email already exists
 * - 401 Unauthorized: Invalid credentials
 * - 201 Created: Successful registration
 * - 200 OK: Successful login
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor  // DESIGN PATTERN: Dependency Injection
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;  // Injected by Spring
    private final LoginUserUseCase loginUserUseCase;  // Injected by Spring

    /**
     * Register a new user.
     *
     * Request body:
     * {
     *   "name": "John Doe",
     *   "email": "john@example.com",
     *   "password": "password123",
     *   "role": "CITIZEN"  // optional, defaults to CITIZEN
     * }
     *
     * Response (201 Created):
     * {
     *   "id": 1,
     *   "name": "John Doe",
     *   "email": "john@example.com",
     *   "role": "CITIZEN",
     *   "createdAt": "2024-01-01T12:00:00"
     * }
     *
     * Error responses:
     * - 400: Validation error (name too short, invalid email, weak password)
     * - 409: Email already exists
     *
     * @param request registration data
     * @return created user (without password)
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        try {
            // Create command from request
            RegisterUserUseCase.RegisterUserCommand command =
                    new RegisterUserUseCase.RegisterUserCommand(
                            request.name,
                            request.email,
                            request.password,
                            request.role
                    );

            // Execute use case
            User user = registerUserUseCase.registerUser(command);

            // Convert to response (hide password)
            UserResponse response = new UserResponse(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getRole().name(),
                    user.getCreatedAt()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RegisterUserUseCase.EmailAlreadyExistsException e) {
            // Email already exists - 409 Conflict
            throw new EmailAlreadyExistsException(e.getMessage());

        } catch (IllegalArgumentException e) {
            // Validation error - 400 Bad Request
            throw new ValidationException(e.getMessage());
        }
    }

    /**
     * Login user and get JWT token.
     *
     * Request body:
     * {
     *   "email": "john@example.com",
     *   "password": "password123"
     * }
     *
     * Response (200 OK):
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "userId": 1,
     *   "name": "John Doe",
     *   "email": "john@example.com",
     *   "role": "CITIZEN"
     * }
     *
     * Error responses:
     * - 401: Invalid email or password
     * - 401: User account deleted
     *
     * @param request login credentials
     * @return auth response with JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<LoginUserUseCase.AuthResponse> login(@RequestBody LoginRequest request) {
        try {
            // Create command from request
            LoginUserUseCase.LoginCommand command =
                    new LoginUserUseCase.LoginCommand(
                            request.email,
                            request.password
                    );

            // Execute use case
            LoginUserUseCase.AuthResponse response = loginUserUseCase.login(command);

            return ResponseEntity.ok(response);

        } catch (LoginUserUseCase.InvalidCredentialsException |
                 LoginUserUseCase.UserDeletedException e) {
            // Invalid credentials or deleted user - 401 Unauthorized
            throw new UnauthorizedException(e.getMessage());
        }
    }

    // ========================
    // Request/Response DTOs
    // ========================

    /**
     * Registration request DTO.
     *
     * DESIGN PATTERN: DTO (Data Transfer Object)
     * - Transfers data from HTTP request → controller
     * - Immutable record (Java 17+)
     * - Separates API contract from domain model
     */
    record RegisterRequest(  // DESIGN PATTERN: DTO Pattern
            String name,
            String email,
            String password,
            String role
    ) {}

    /**
     * Login request DTO.
     *
     * DESIGN PATTERN: DTO (Data Transfer Object)
     * - Transfers login credentials from client → controller
     */
    record LoginRequest(  // DESIGN PATTERN: DTO Pattern
            String email,
            String password
    ) {}

    /**
     * User response DTO (without password).
     *
     * DESIGN PATTERN: DTO (Data Transfer Object)
     * - Transfers user data from controller → client
     * - Security: Excludes password field (User domain model has password)
     * - Prevents exposing internal domain structure to API clients
     */
    record UserResponse(  // DESIGN PATTERN: DTO Pattern
            Long id,
            String name,
            String email,
            String role,
            java.time.LocalDateTime createdAt
    ) {}

    // ========================
    // Exception Handlers
    // ========================

    @ResponseStatus(HttpStatus.CONFLICT)  // 409
    static class EmailAlreadyExistsException extends RuntimeException {
        public EmailAlreadyExistsException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)  // 400
    static class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)  // 401
    static class UnauthorizedException extends RuntimeException {
        public UnauthorizedException(String message) {
            super(message);
        }
    }
}
