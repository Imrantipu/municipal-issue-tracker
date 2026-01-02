package com.issuetracker.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for User domain model.
 *
 * Testing Strategy:
 * - Test all validation methods (name, email, password, role)
 * - Test business logic methods (isDeleted, isActive, isAdmin, etc.)
 * - Test soft delete functionality
 * - Use descriptive test names: should_ExpectedBehavior_When_Condition()
 *
 * No Spring context needed - pure Java unit tests (fast execution).
 */
@DisplayName("User Domain Model Tests")
class UserTest {

    // ========================
    // Builder Tests
    // ========================

    @Test
    @DisplayName("should_CreateUser_When_UsingBuilder")
    void should_CreateUser_When_UsingBuilder() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        // When
        User user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .password("password123")
                .role(Role.CITIZEN)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getName()).isEqualTo("John Doe");
        assertThat(user.getEmail()).isEqualTo("john@example.com");
        assertThat(user.getPassword()).isEqualTo("password123");
        assertThat(user.getRole()).isEqualTo(Role.CITIZEN);
        assertThat(user.getCreatedAt()).isEqualTo(now);
        assertThat(user.getUpdatedAt()).isEqualTo(now);
        assertThat(user.getDeletedAt()).isNull();
    }

    // ========================
    // Name Validation Tests
    // ========================

    @Nested
    @DisplayName("Name Validation Tests")
    class NameValidationTests {

        @Test
        @DisplayName("should_PassValidation_When_NameIsValid")
        void should_PassValidation_When_NameIsValid() {
            // Given
            User user = createValidUser();

            // When/Then
            assertThatNoException().isThrownBy(user::validate);
        }

        @Test
        @DisplayName("should_ThrowException_When_NameIsNull")
        void should_ThrowException_When_NameIsNull() {
            // Given
            User user = createValidUser();
            user.setName(null);

            // When/Then
            assertThatThrownBy(user::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Name cannot be empty");
        }

        @Test
        @DisplayName("should_ThrowException_When_NameIsEmpty")
        void should_ThrowException_When_NameIsEmpty() {
            // Given
            User user = createValidUser();
            user.setName("");

            // When/Then
            assertThatThrownBy(user::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Name cannot be empty");
        }

        @Test
        @DisplayName("should_ThrowException_When_NameIsBlank")
        void should_ThrowException_When_NameIsBlank() {
            // Given
            User user = createValidUser();
            user.setName("   ");

            // When/Then
            assertThatThrownBy(user::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Name cannot be empty");
        }

        @Test
        @DisplayName("should_ThrowException_When_NameIsTooShort")
        void should_ThrowException_When_NameIsTooShort() {
            // Given
            User user = createValidUser();
            user.setName("J");

            // When/Then
            assertThatThrownBy(user::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Name must be at least 2 characters");
        }

        @Test
        @DisplayName("should_ThrowException_When_NameIsTooLong")
        void should_ThrowException_When_NameIsTooLong() {
            // Given
            User user = createValidUser();
            user.setName("A".repeat(101));  // 101 characters

            // When/Then
            assertThatThrownBy(user::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Name cannot exceed 100 characters");
        }

        @Test
        @DisplayName("should_PassValidation_When_NameIsMinimumLength")
        void should_PassValidation_When_NameIsMinimumLength() {
            // Given
            User user = createValidUser();
            user.setName("AB");  // 2 characters (minimum)

            // When/Then
            assertThatNoException().isThrownBy(user::validate);
        }

        @Test
        @DisplayName("should_PassValidation_When_NameIsMaximumLength")
        void should_PassValidation_When_NameIsMaximumLength() {
            // Given
            User user = createValidUser();
            user.setName("A".repeat(100));  // 100 characters (maximum)

            // When/Then
            assertThatNoException().isThrownBy(user::validate);
        }
    }

    // ========================
    // Email Validation Tests
    // ========================

    @Nested
    @DisplayName("Email Validation Tests")
    class EmailValidationTests {

        @Test
        @DisplayName("should_PassValidation_When_EmailIsValid")
        void should_PassValidation_When_EmailIsValid() {
            // Given
            User user = createValidUser();
            user.setEmail("john@example.com");

            // When/Then
            assertThatNoException().isThrownBy(user::validate);
        }

        @Test
        @DisplayName("should_ThrowException_When_EmailIsNull")
        void should_ThrowException_When_EmailIsNull() {
            // Given
            User user = createValidUser();
            user.setEmail(null);

            // When/Then
            assertThatThrownBy(user::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Email cannot be empty");
        }

        @Test
        @DisplayName("should_ThrowException_When_EmailIsEmpty")
        void should_ThrowException_When_EmailIsEmpty() {
            // Given
            User user = createValidUser();
            user.setEmail("");

            // When/Then
            assertThatThrownBy(user::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Email cannot be empty");
        }

        @Test
        @DisplayName("should_ThrowException_When_EmailIsInvalidFormat")
        void should_ThrowException_When_EmailIsInvalidFormat() {
            // Given
            User user = createValidUser();
            user.setEmail("invalid-email");

            // When/Then
            assertThatThrownBy(user::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Email format is invalid");
        }

        @Test
        @DisplayName("should_ThrowException_When_EmailMissingAtSymbol")
        void should_ThrowException_When_EmailMissingAtSymbol() {
            // Given
            User user = createValidUser();
            user.setEmail("johndoe.com");

            // When/Then
            assertThatThrownBy(user::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Email format is invalid");
        }

        @Test
        @DisplayName("should_ThrowException_When_EmailMissingDomain")
        void should_ThrowException_When_EmailMissingDomain() {
            // Given
            User user = createValidUser();
            user.setEmail("john@");

            // When/Then
            assertThatThrownBy(user::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Email format is invalid");
        }

        @Test
        @DisplayName("should_ThrowException_When_EmailIsTooLong")
        void should_ThrowException_When_EmailIsTooLong() {
            // Given
            User user = createValidUser();
            user.setEmail("a".repeat(250) + "@example.com");  // > 255 characters

            // When/Then
            assertThatThrownBy(user::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Email cannot exceed 255 characters");
        }

        @Test
        @DisplayName("should_PassValidation_When_EmailHasSubdomain")
        void should_PassValidation_When_EmailHasSubdomain() {
            // Given
            User user = createValidUser();
            user.setEmail("john@mail.example.com");

            // When/Then
            assertThatNoException().isThrownBy(user::validate);
        }

        @Test
        @DisplayName("should_PassValidation_When_EmailHasPlus")
        void should_PassValidation_When_EmailHasPlus() {
            // Given
            User user = createValidUser();
            user.setEmail("john+test@example.com");

            // When/Then
            assertThatNoException().isThrownBy(user::validate);
        }
    }

    // ========================
    // Password Validation Tests
    // ========================

    @Nested
    @DisplayName("Password Validation Tests")
    class PasswordValidationTests {

        @Test
        @DisplayName("should_PassValidation_When_PasswordIsValid")
        void should_PassValidation_When_PasswordIsValid() {
            // Given
            User user = createValidUser();
            user.setPassword("password123");

            // When/Then
            assertThatNoException().isThrownBy(user::validate);
        }

        @Test
        @DisplayName("should_ThrowException_When_PasswordIsNull")
        void should_ThrowException_When_PasswordIsNull() {
            // Given
            User user = createValidUser();
            user.setPassword(null);

            // When/Then
            assertThatThrownBy(user::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Password cannot be empty");
        }

        @Test
        @DisplayName("should_ThrowException_When_PasswordIsEmpty")
        void should_ThrowException_When_PasswordIsEmpty() {
            // Given
            User user = createValidUser();
            user.setPassword("");

            // When/Then
            assertThatThrownBy(user::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Password cannot be empty");
        }

        @Test
        @DisplayName("should_ThrowException_When_PasswordIsTooShort")
        void should_ThrowException_When_PasswordIsTooShort() {
            // Given
            User user = createValidUser();
            user.setPassword("pass");  // 4 characters

            // When/Then
            assertThatThrownBy(user::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Password must be at least 8 characters");
        }

        @Test
        @DisplayName("should_ThrowException_When_PasswordIsTooLong")
        void should_ThrowException_When_PasswordIsTooLong() {
            // Given
            User user = createValidUser();
            user.setPassword("p".repeat(256));  // 256 characters

            // When/Then
            assertThatThrownBy(user::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Password cannot exceed 255 characters");
        }

        @Test
        @DisplayName("should_PassValidation_When_PasswordIsMinimumLength")
        void should_PassValidation_When_PasswordIsMinimumLength() {
            // Given
            User user = createValidUser();
            user.setPassword("12345678");  // 8 characters (minimum)

            // When/Then
            assertThatNoException().isThrownBy(user::validate);
        }

        @Test
        @DisplayName("should_PassValidation_When_PasswordIsMaximumLength")
        void should_PassValidation_When_PasswordIsMaximumLength() {
            // Given
            User user = createValidUser();
            user.setPassword("p".repeat(255));  // 255 characters (maximum)

            // When/Then
            assertThatNoException().isThrownBy(user::validate);
        }
    }

    // ========================
    // Role Validation Tests
    // ========================

    @Nested
    @DisplayName("Role Validation Tests")
    class RoleValidationTests {

        @Test
        @DisplayName("should_PassValidation_When_RoleIsSet")
        void should_PassValidation_When_RoleIsSet() {
            // Given
            User user = createValidUser();
            user.setRole(Role.CITIZEN);

            // When/Then
            assertThatNoException().isThrownBy(user::validate);
        }

        @Test
        @DisplayName("should_ThrowException_When_RoleIsNull")
        void should_ThrowException_When_RoleIsNull() {
            // Given
            User user = createValidUser();
            user.setRole(null);

            // When/Then
            assertThatThrownBy(user::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Role must be specified");
        }
    }

    // ========================
    // Business Logic Tests
    // ========================

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("should_ReturnFalse_When_UserIsNotDeleted")
        void should_ReturnFalse_When_UserIsNotDeleted() {
            // Given
            User user = createValidUser();
            user.setDeletedAt(null);

            // When
            boolean deleted = user.isDeleted();

            // Then
            assertThat(deleted).isFalse();
        }

        @Test
        @DisplayName("should_ReturnTrue_When_UserIsDeleted")
        void should_ReturnTrue_When_UserIsDeleted() {
            // Given
            User user = createValidUser();
            user.setDeletedAt(LocalDateTime.now());

            // When
            boolean deleted = user.isDeleted();

            // Then
            assertThat(deleted).isTrue();
        }

        @Test
        @DisplayName("should_ReturnTrue_When_UserIsActive")
        void should_ReturnTrue_When_UserIsActive() {
            // Given
            User user = createValidUser();
            user.setDeletedAt(null);

            // When
            boolean active = user.isActive();

            // Then
            assertThat(active).isTrue();
        }

        @Test
        @DisplayName("should_ReturnFalse_When_UserIsNotActive")
        void should_ReturnFalse_When_UserIsNotActive() {
            // Given
            User user = createValidUser();
            user.setDeletedAt(LocalDateTime.now());

            // When
            boolean active = user.isActive();

            // Then
            assertThat(active).isFalse();
        }

        @Test
        @DisplayName("should_ReturnTrue_When_UserIsAdmin")
        void should_ReturnTrue_When_UserIsAdmin() {
            // Given
            User user = createValidUser();
            user.setRole(Role.ADMIN);

            // When
            boolean admin = user.isAdmin();

            // Then
            assertThat(admin).isTrue();
        }

        @Test
        @DisplayName("should_ReturnFalse_When_UserIsNotAdmin")
        void should_ReturnFalse_When_UserIsNotAdmin() {
            // Given
            User user = createValidUser();
            user.setRole(Role.CITIZEN);

            // When
            boolean admin = user.isAdmin();

            // Then
            assertThat(admin).isFalse();
        }

        @Test
        @DisplayName("should_ReturnTrue_When_UserIsStaff")
        void should_ReturnTrue_When_UserIsStaff() {
            // Given
            User user = createValidUser();
            user.setRole(Role.STAFF);

            // When
            boolean staff = user.isStaff();

            // Then
            assertThat(staff).isTrue();
        }

        @Test
        @DisplayName("should_ReturnFalse_When_UserIsNotStaff")
        void should_ReturnFalse_When_UserIsNotStaff() {
            // Given
            User user = createValidUser();
            user.setRole(Role.ADMIN);

            // When
            boolean staff = user.isStaff();

            // Then
            assertThat(staff).isFalse();
        }

        @Test
        @DisplayName("should_ReturnTrue_When_UserIsCitizen")
        void should_ReturnTrue_When_UserIsCitizen() {
            // Given
            User user = createValidUser();
            user.setRole(Role.CITIZEN);

            // When
            boolean citizen = user.isCitizen();

            // Then
            assertThat(citizen).isTrue();
        }

        @Test
        @DisplayName("should_ReturnFalse_When_UserIsNotCitizen")
        void should_ReturnFalse_When_UserIsNotCitizen() {
            // Given
            User user = createValidUser();
            user.setRole(Role.ADMIN);

            // When
            boolean citizen = user.isCitizen();

            // Then
            assertThat(citizen).isFalse();
        }
    }

    // ========================
    // Soft Delete Tests
    // ========================

    @Nested
    @DisplayName("Soft Delete Tests")
    class SoftDeleteTests {

        @Test
        @DisplayName("should_SetDeletedAt_When_SoftDeleteCalled")
        void should_SetDeletedAt_When_SoftDeleteCalled() {
            // Given
            User user = createValidUser();
            assertThat(user.getDeletedAt()).isNull();

            // When
            user.softDelete();

            // Then
            assertThat(user.getDeletedAt()).isNotNull();
            assertThat(user.getDeletedAt()).isBeforeOrEqualTo(LocalDateTime.now());
            assertThat(user.isDeleted()).isTrue();
            assertThat(user.isActive()).isFalse();
        }

        @Test
        @DisplayName("should_ClearDeletedAt_When_RestoreCalled")
        void should_ClearDeletedAt_When_RestoreCalled() {
            // Given
            User user = createValidUser();
            user.softDelete();
            assertThat(user.getDeletedAt()).isNotNull();

            // When
            user.restore();

            // Then
            assertThat(user.getDeletedAt()).isNull();
            assertThat(user.isDeleted()).isFalse();
            assertThat(user.isActive()).isTrue();
        }
    }

    // ========================
    // Helper Methods
    // ========================

    private User createValidUser() {
        return User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .password("password123")
                .role(Role.CITIZEN)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
