package com.issuetracker.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.issuetracker.domain.model.Role;
import com.issuetracker.domain.model.User;
import com.issuetracker.domain.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController.
 *
 * Testing Strategy:
 * - Full Spring Boot context loaded (real beans, no mocks)
 * - Test complete HTTP request/response flow
 * - Test JSON serialization/deserialization
 * - Test HTTP status codes and response bodies
 * - Uses in-memory H2 database for tests (faster than PostgreSQL)
 * - @Transactional rolls back database changes after each test
 *
 * Uses MockMvc for simulating HTTP requests without starting server.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthController Integration Tests")
class AuthControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Set up MockMvc with Spring Security
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    // ========================
    // POST /api/auth/register Tests
    // ========================

    @Nested
    @DisplayName("POST /api/auth/register Tests")
    class RegisterEndpointTests {

        @Test
        @DisplayName("should_ReturnCreatedAndUserData_When_RegistrationIsValid")
        void should_ReturnCreatedAndUserData_When_RegistrationIsValid() throws Exception {
            // Given
            String requestBody = """
                    {
                        "name": "John Doe",
                        "email": "john@example.com",
                        "password": "password123",
                        "role": "CITIZEN"
                    }
                    """;

            // When/Then
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.name").value("John Doe"))
                    .andExpect(jsonPath("$.email").value("john@example.com"))
                    .andExpect(jsonPath("$.role").value("CITIZEN"))
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.password").doesNotExist());  // Password should NOT be in response

            // Verify user was saved to database
            assertThat(userRepository.findByEmail("john@example.com")).isPresent();
        }

        @Test
        @DisplayName("should_HashPassword_When_Registering")
        void should_HashPassword_When_Registering() throws Exception {
            // Given
            String requestBody = """
                    {
                        "name": "Jane Smith",
                        "email": "jane@example.com",
                        "password": "plainPassword",
                        "role": "CITIZEN"
                    }
                    """;

            // When
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated());

            // Then - Verify password was hashed in database
            User savedUser = userRepository.findByEmail("jane@example.com").orElseThrow();
            assertThat(savedUser.getPassword()).isNotEqualTo("plainPassword");
            assertThat(savedUser.getPassword()).startsWith("$2a$");  // BCrypt hash prefix
            assertThat(passwordEncoder.matches("plainPassword", savedUser.getPassword())).isTrue();
        }

        @Test
        @DisplayName("should_DefaultToCitizenRole_When_RoleNotProvided")
        void should_DefaultToCitizenRole_When_RoleNotProvided() throws Exception {
            // Given
            String requestBody = """
                    {
                        "name": "Bob Johnson",
                        "email": "bob@example.com",
                        "password": "password123"
                    }
                    """;

            // When/Then
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.role").value("CITIZEN"));
        }

        @Test
        @DisplayName("should_AcceptAdminRole_When_RoleIsAdmin")
        void should_AcceptAdminRole_When_RoleIsAdmin() throws Exception {
            // Given
            String requestBody = """
                    {
                        "name": "Admin User",
                        "email": "admin@example.com",
                        "password": "password123",
                        "role": "ADMIN"
                    }
                    """;

            // When/Then
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.role").value("ADMIN"));
        }

        @Test
        @DisplayName("should_ReturnConflict_When_EmailAlreadyExists")
        void should_ReturnConflict_When_EmailAlreadyExists() throws Exception {
            // Given - Create existing user
            User existingUser = User.builder()
                    .name("Existing User")
                    .email("existing@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.CITIZEN)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            userRepository.save(existingUser);

            String requestBody = """
                    {
                        "name": "New User",
                        "email": "existing@example.com",
                        "password": "password123",
                        "role": "CITIZEN"
                    }
                    """;

            // When/Then
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value(containsString("Email already exists")));
        }

        @Test
        @DisplayName("should_ReturnBadRequest_When_NameIsTooShort")
        void should_ReturnBadRequest_When_NameIsTooShort() throws Exception {
            // Given
            String requestBody = """
                    {
                        "name": "J",
                        "email": "john@example.com",
                        "password": "password123",
                        "role": "CITIZEN"
                    }
                    """;

            // When/Then
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("Name must be at least 2 characters")));
        }

        @Test
        @DisplayName("should_ReturnBadRequest_When_EmailIsInvalid")
        void should_ReturnBadRequest_When_EmailIsInvalid() throws Exception {
            // Given
            String requestBody = """
                    {
                        "name": "John Doe",
                        "email": "invalid-email",
                        "password": "password123",
                        "role": "CITIZEN"
                    }
                    """;

            // When/Then
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("Email format is invalid")));
        }

        @Test
        @DisplayName("should_ReturnBadRequest_When_PasswordIsTooShort")
        void should_ReturnBadRequest_When_PasswordIsTooShort() throws Exception {
            // Given
            String requestBody = """
                    {
                        "name": "John Doe",
                        "email": "john@example.com",
                        "password": "short",
                        "role": "CITIZEN"
                    }
                    """;

            // When/Then
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("Password must be at least 8 characters")));
        }
    }

    // ========================
    // POST /api/auth/login Tests
    // ========================

    @Nested
    @DisplayName("POST /api/auth/login Tests")
    class LoginEndpointTests {

        @Test
        @DisplayName("should_ReturnOkAndJwtToken_When_CredentialsAreValid")
        void should_ReturnOkAndJwtToken_When_CredentialsAreValid() throws Exception {
            // Given - Create user in database
            User user = User.builder()
                    .name("John Doe")
                    .email("john@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.CITIZEN)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            userRepository.save(user);

            String requestBody = """
                    {
                        "email": "john@example.com",
                        "password": "password123"
                    }
                    """;

            // When/Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").exists())
                    .andExpect(jsonPath("$.token").isString())
                    .andExpect(jsonPath("$.userId").isNumber())
                    .andExpect(jsonPath("$.name").value("John Doe"))
                    .andExpect(jsonPath("$.email").value("john@example.com"))
                    .andExpect(jsonPath("$.role").value("CITIZEN"));
        }

        @Test
        @DisplayName("should_ReturnJwtWithCorrectClaims_When_LoginSuccessful")
        void should_ReturnJwtWithCorrectClaims_When_LoginSuccessful() throws Exception {
            // Given
            User user = User.builder()
                    .name("Admin User")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("adminPass"))
                    .role(Role.ADMIN)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            User savedUser = userRepository.save(user);

            String requestBody = """
                    {
                        "email": "admin@example.com",
                        "password": "adminPass"
                    }
                    """;

            // When/Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(savedUser.getId()))
                    .andExpect(jsonPath("$.name").value("Admin User"))
                    .andExpect(jsonPath("$.email").value("admin@example.com"))
                    .andExpect(jsonPath("$.role").value("ADMIN"));
        }

        @Test
        @DisplayName("should_ReturnUnauthorized_When_EmailNotFound")
        void should_ReturnUnauthorized_When_EmailNotFound() throws Exception {
            // Given
            String requestBody = """
                    {
                        "email": "nonexistent@example.com",
                        "password": "password123"
                    }
                    """;

            // When/Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Invalid email or password"));
        }

        @Test
        @DisplayName("should_ReturnUnauthorized_When_PasswordIsIncorrect")
        void should_ReturnUnauthorized_When_PasswordIsIncorrect() throws Exception {
            // Given - Create user
            User user = User.builder()
                    .name("John Doe")
                    .email("john@example.com")
                    .password(passwordEncoder.encode("correctPassword"))
                    .role(Role.CITIZEN)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            userRepository.save(user);

            String requestBody = """
                    {
                        "email": "john@example.com",
                        "password": "wrongPassword"
                    }
                    """;

            // When/Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Invalid email or password"));
        }

        @Test
        @DisplayName("should_ReturnUnauthorized_When_UserIsDeleted")
        void should_ReturnUnauthorized_When_UserIsDeleted() throws Exception {
            // Given - Create deleted user
            User user = User.builder()
                    .name("Deleted User")
                    .email("deleted@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.CITIZEN)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .deletedAt(LocalDateTime.now())  // Soft deleted
                    .build();
            userRepository.save(user);

            String requestBody = """
                    {
                        "email": "deleted@example.com",
                        "password": "password123"
                    }
                    """;

            // When/Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("User account has been deleted"));
        }

        @Test
        @DisplayName("should_AcceptUppercaseEmail_When_LoggingIn")
        void should_AcceptUppercaseEmail_When_LoggingIn() throws Exception {
            // Given - User registered with lowercase email
            User user = User.builder()
                    .name("John Doe")
                    .email("john@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.CITIZEN)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            userRepository.save(user);

            String requestBody = """
                    {
                        "email": "JOHN@EXAMPLE.COM",
                        "password": "password123"
                    }
                    """;

            // When/Then - Should work with uppercase email
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").exists());
        }
    }
}
