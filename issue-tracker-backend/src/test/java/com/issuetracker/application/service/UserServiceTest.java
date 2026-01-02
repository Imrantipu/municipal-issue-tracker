package com.issuetracker.application.service;

import com.issuetracker.domain.model.Role;
import com.issuetracker.domain.model.User;
import com.issuetracker.domain.port.in.LoginUserUseCase;
import com.issuetracker.domain.port.in.RegisterUserUseCase;
import com.issuetracker.domain.port.out.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService.
 *
 * Testing Strategy:
 * - Mock all dependencies (UserRepository, PasswordEncoder, JwtTokenProvider)
 * - Test business logic in isolation
 * - Verify interactions with mocks (verify method calls)
 * - Test both success and failure scenarios
 *
 * Uses Mockito for mocking dependencies.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserService.JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserService userService;

    // ========================
    // registerUser() Tests
    // ========================

    @Nested
    @DisplayName("registerUser() Tests")
    class RegisterUserTests {

        @Test
        @DisplayName("should_RegisterUser_When_ValidDataProvided")
        void should_RegisterUser_When_ValidDataProvided() {
            // Given
            RegisterUserUseCase.RegisterUserCommand command =
                    new RegisterUserUseCase.RegisterUserCommand(
                            "John Doe",
                            "john@example.com",
                            "password123",
                            "CITIZEN"
                    );

            when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedPassword");

            User savedUser = User.builder()
                    .id(1L)
                    .name("John Doe")
                    .email("john@example.com")
                    .password("$2a$10$hashedPassword")
                    .role(Role.CITIZEN)
                    .build();
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            // When
            User result = userService.registerUser(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("John Doe");
            assertThat(result.getEmail()).isEqualTo("john@example.com");
            assertThat(result.getPassword()).isEqualTo("$2a$10$hashedPassword");
            assertThat(result.getRole()).isEqualTo(Role.CITIZEN);

            // Verify interactions
            verify(userRepository).existsByEmail("john@example.com");
            verify(passwordEncoder).encode("password123");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("should_NormalizeEmailToLowercase_When_Registering")
        void should_NormalizeEmailToLowercase_When_Registering() {
            // Given
            RegisterUserUseCase.RegisterUserCommand command =
                    new RegisterUserUseCase.RegisterUserCommand(
                            "John Doe",
                            "JOHN@EXAMPLE.COM",  // Uppercase email
                            "password123",
                            "CITIZEN"
                    );

            when(userRepository.existsByEmail(any())).thenReturn(false);  // Accept any email
            when(passwordEncoder.encode(any())).thenReturn("$2a$10$hashedPassword");
            when(userRepository.save(any(User.class))).thenReturn(User.builder().build());

            // When
            userService.registerUser(command);

            // Then - Capture the saved user to verify email normalization
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getEmail()).isEqualTo("john@example.com");
        }

        @Test
        @DisplayName("should_HashPassword_When_Registering")
        void should_HashPassword_When_Registering() {
            // Given
            RegisterUserUseCase.RegisterUserCommand command =
                    new RegisterUserUseCase.RegisterUserCommand(
                            "John Doe",
                            "john@example.com",
                            "plainPassword",
                            "CITIZEN"
                    );

            when(userRepository.existsByEmail(any())).thenReturn(false);
            when(passwordEncoder.encode("plainPassword")).thenReturn("$2a$10$hashedPassword");
            when(userRepository.save(any(User.class))).thenReturn(User.builder().build());

            // When
            userService.registerUser(command);

            // Then - Verify password was hashed
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getPassword()).isEqualTo("$2a$10$hashedPassword");
            verify(passwordEncoder).encode("plainPassword");
        }

        @Test
        @DisplayName("should_DefaultToCitizenRole_When_RoleIsNull")
        void should_DefaultToCitizenRole_When_RoleIsNull() {
            // Given
            RegisterUserUseCase.RegisterUserCommand command =
                    new RegisterUserUseCase.RegisterUserCommand(
                            "John Doe",
                            "john@example.com",
                            "password123",
                            null  // No role specified
                    );

            when(userRepository.existsByEmail(any())).thenReturn(false);
            when(passwordEncoder.encode(any())).thenReturn("hashedPassword");
            when(userRepository.save(any(User.class))).thenReturn(User.builder().build());

            // When
            userService.registerUser(command);

            // Then - Verify role defaults to CITIZEN
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getRole()).isEqualTo(Role.CITIZEN);
        }

        @Test
        @DisplayName("should_DefaultToCitizenRole_When_RoleIsEmpty")
        void should_DefaultToCitizenRole_When_RoleIsEmpty() {
            // Given
            RegisterUserUseCase.RegisterUserCommand command =
                    new RegisterUserUseCase.RegisterUserCommand(
                            "John Doe",
                            "john@example.com",
                            "password123",
                            ""  // Empty role
                    );

            when(userRepository.existsByEmail(any())).thenReturn(false);
            when(passwordEncoder.encode(any())).thenReturn("hashedPassword");
            when(userRepository.save(any(User.class))).thenReturn(User.builder().build());

            // When
            userService.registerUser(command);

            // Then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getRole()).isEqualTo(Role.CITIZEN);
        }

        @Test
        @DisplayName("should_DefaultToCitizenRole_When_RoleIsInvalid")
        void should_DefaultToCitizenRole_When_RoleIsInvalid() {
            // Given
            RegisterUserUseCase.RegisterUserCommand command =
                    new RegisterUserUseCase.RegisterUserCommand(
                            "John Doe",
                            "john@example.com",
                            "password123",
                            "INVALID_ROLE"
                    );

            when(userRepository.existsByEmail(any())).thenReturn(false);
            when(passwordEncoder.encode(any())).thenReturn("hashedPassword");
            when(userRepository.save(any(User.class))).thenReturn(User.builder().build());

            // When
            userService.registerUser(command);

            // Then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getRole()).isEqualTo(Role.CITIZEN);
        }

        @Test
        @DisplayName("should_AcceptAdminRole_When_RoleIsAdmin")
        void should_AcceptAdminRole_When_RoleIsAdmin() {
            // Given
            RegisterUserUseCase.RegisterUserCommand command =
                    new RegisterUserUseCase.RegisterUserCommand(
                            "Admin User",
                            "admin@example.com",
                            "password123",
                            "ADMIN"
                    );

            when(userRepository.existsByEmail(any())).thenReturn(false);
            when(passwordEncoder.encode(any())).thenReturn("hashedPassword");
            when(userRepository.save(any(User.class))).thenReturn(User.builder().build());

            // When
            userService.registerUser(command);

            // Then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getRole()).isEqualTo(Role.ADMIN);
        }

        @Test
        @DisplayName("should_AcceptStaffRole_When_RoleIsStaff")
        void should_AcceptStaffRole_When_RoleIsStaff() {
            // Given
            RegisterUserUseCase.RegisterUserCommand command =
                    new RegisterUserUseCase.RegisterUserCommand(
                            "Staff User",
                            "staff@example.com",
                            "password123",
                            "STAFF"
                    );

            when(userRepository.existsByEmail(any())).thenReturn(false);
            when(passwordEncoder.encode(any())).thenReturn("hashedPassword");
            when(userRepository.save(any(User.class))).thenReturn(User.builder().build());

            // When
            userService.registerUser(command);

            // Then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getRole()).isEqualTo(Role.STAFF);
        }

        @Test
        @DisplayName("should_ThrowException_When_EmailAlreadyExists")
        void should_ThrowException_When_EmailAlreadyExists() {
            // Given
            RegisterUserUseCase.RegisterUserCommand command =
                    new RegisterUserUseCase.RegisterUserCommand(
                            "John Doe",
                            "existing@example.com",
                            "password123",
                            "CITIZEN"
                    );

            when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> userService.registerUser(command))
                    .isInstanceOf(RegisterUserUseCase.EmailAlreadyExistsException.class)
                    .hasMessage("Email already exists: existing@example.com");

            // Verify password encoder and save were NOT called
            verify(passwordEncoder, never()).encode(any());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should_ThrowException_When_ValidationFails")
        void should_ThrowException_When_ValidationFails() {
            // Given - Invalid name (too short)
            RegisterUserUseCase.RegisterUserCommand command =
                    new RegisterUserUseCase.RegisterUserCommand(
                            "J",  // Too short
                            "john@example.com",
                            "password123",
                            "CITIZEN"
                    );

            when(userRepository.existsByEmail(any())).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> userService.registerUser(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Name must be at least 2 characters");

            // Verify save was NOT called
            verify(userRepository, never()).save(any());
        }
    }

    // ========================
    // login() Tests
    // ========================

    @Nested
    @DisplayName("login() Tests")
    class LoginTests {

        @Test
        @DisplayName("should_ReturnAuthResponse_When_CredentialsAreValid")
        void should_ReturnAuthResponse_When_CredentialsAreValid() {
            // Given
            LoginUserUseCase.LoginCommand command =
                    new LoginUserUseCase.LoginCommand(
                            "john@example.com",
                            "password123"
                    );

            User user = User.builder()
                    .id(1L)
                    .name("John Doe")
                    .email("john@example.com")
                    .password("$2a$10$hashedPassword")
                    .role(Role.CITIZEN)
                    .build();

            when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("password123", "$2a$10$hashedPassword")).thenReturn(true);
            when(jwtTokenProvider.generateToken(user)).thenReturn("jwt-token-123");

            // When
            LoginUserUseCase.AuthResponse response = userService.login(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.token()).isEqualTo("jwt-token-123");
            assertThat(response.userId()).isEqualTo(1L);
            assertThat(response.name()).isEqualTo("John Doe");
            assertThat(response.email()).isEqualTo("john@example.com");
            assertThat(response.role()).isEqualTo("CITIZEN");

            verify(userRepository).findByEmail("john@example.com");
            verify(passwordEncoder).matches("password123", "$2a$10$hashedPassword");
            verify(jwtTokenProvider).generateToken(user);
        }

        @Test
        @DisplayName("should_NormalizeEmailToLowercase_When_Logging_In")
        void should_NormalizeEmailToLowercase_When_LoggingIn() {
            // Given
            LoginUserUseCase.LoginCommand command =
                    new LoginUserUseCase.LoginCommand(
                            "JOHN@EXAMPLE.COM",  // Uppercase email
                            "password123"
                    );

            User user = User.builder()
                    .id(1L)
                    .email("john@example.com")
                    .password("$2a$10$hashedPassword")
                    .role(Role.CITIZEN)
                    .build();

            when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(any(), any())).thenReturn(true);
            when(jwtTokenProvider.generateToken(any())).thenReturn("jwt-token");

            // When
            userService.login(command);

            // Then - Verify lowercase email was used for lookup
            verify(userRepository).findByEmail("john@example.com");
        }

        @Test
        @DisplayName("should_ThrowException_When_UserNotFound")
        void should_ThrowException_When_UserNotFound() {
            // Given
            LoginUserUseCase.LoginCommand command =
                    new LoginUserUseCase.LoginCommand(
                            "nonexistent@example.com",
                            "password123"
                    );

            when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> userService.login(command))
                    .isInstanceOf(LoginUserUseCase.InvalidCredentialsException.class)
                    .hasMessage("Invalid email or password");

            verify(userRepository).findByEmail("nonexistent@example.com");
            verify(passwordEncoder, never()).matches(any(), any());
            verify(jwtTokenProvider, never()).generateToken(any());
        }

        @Test
        @DisplayName("should_ThrowException_When_PasswordIsIncorrect")
        void should_ThrowException_When_PasswordIsIncorrect() {
            // Given
            LoginUserUseCase.LoginCommand command =
                    new LoginUserUseCase.LoginCommand(
                            "john@example.com",
                            "wrongPassword"
                    );

            User user = User.builder()
                    .id(1L)
                    .email("john@example.com")
                    .password("$2a$10$hashedPassword")
                    .role(Role.CITIZEN)
                    .build();

            when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrongPassword", "$2a$10$hashedPassword")).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> userService.login(command))
                    .isInstanceOf(LoginUserUseCase.InvalidCredentialsException.class)
                    .hasMessage("Invalid email or password");

            verify(userRepository).findByEmail("john@example.com");
            verify(passwordEncoder).matches("wrongPassword", "$2a$10$hashedPassword");
            verify(jwtTokenProvider, never()).generateToken(any());
        }

        @Test
        @DisplayName("should_ThrowException_When_UserIsDeleted")
        void should_ThrowException_When_UserIsDeleted() {
            // Given
            LoginUserUseCase.LoginCommand command =
                    new LoginUserUseCase.LoginCommand(
                            "deleted@example.com",
                            "password123"
                    );

            User deletedUser = User.builder()
                    .id(1L)
                    .email("deleted@example.com")
                    .password("$2a$10$hashedPassword")
                    .role(Role.CITIZEN)
                    .build();
            deletedUser.softDelete();  // Mark as deleted

            when(userRepository.findByEmail("deleted@example.com")).thenReturn(Optional.of(deletedUser));

            // When/Then
            assertThatThrownBy(() -> userService.login(command))
                    .isInstanceOf(LoginUserUseCase.UserDeletedException.class)
                    .hasMessage("User account has been deleted");

            verify(userRepository).findByEmail("deleted@example.com");
            verify(passwordEncoder, never()).matches(any(), any());
            verify(jwtTokenProvider, never()).generateToken(any());
        }
    }
}
