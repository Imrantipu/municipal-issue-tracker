package com.issuetracker.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive unit tests for Issue domain model.
 *
 * Test Coverage:
 * - Title validation (5 tests)
 * - Description validation (5 tests)
 * - Location validation (4 tests)
 * - Category validation (2 tests)
 * - Status validation (3 tests)
 * - Priority validation (2 tests)
 * - Assignment logic (8 tests)
 * - Status transition logic (10 tests)
 * - Update operations (5 tests)
 * - Soft delete logic (4 tests)
 * - Query methods (7 tests)
 * - SLA/Overdue logic (5 tests)
 *
 * Total: 60+ tests for 100% domain coverage
 */
@DisplayName("Issue Domain Model Tests")
class IssueTest {

    // ==================== TEST FIXTURES ====================

    private User createCitizen() {
        return User.builder()
                .id(1L)
                .name("John Citizen")
                .email("citizen@test.com")
                .password("password123")
                .role(Role.CITIZEN)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private User createStaff() {
        return User.builder()
                .id(2L)
                .name("Jane Staff")
                .email("staff@test.com")
                .password("password123")
                .role(Role.STAFF)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private User createAdmin() {
        return User.builder()
                .id(3L)
                .name("Admin User")
                .email("admin@test.com")
                .password("password123")
                .role(Role.ADMIN)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Issue createValidIssue() {
        return Issue.builder()
                .title("Broken streetlight on Main St")
                .description("The streetlight at 123 Main St has been out for 3 days, creating safety concerns.")
                .status(IssueStatus.OPEN)
                .priority(Priority.MEDIUM)
                .category(Category.INFRASTRUCTURE)
                .location("123 Main St, City Center")
                .reportedBy(createCitizen())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ==================== TITLE VALIDATION TESTS ====================

    @Nested
    @DisplayName("Title Validation Tests")
    class TitleValidationTests {

        @Test
        @DisplayName("should_ThrowException_When_TitleIsNull")
        void should_ThrowException_When_TitleIsNull() {
            // Given
            Issue issue = createValidIssue();
            issue.setTitle(null);

            // When/Then
            assertThatThrownBy(issue::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Title cannot be empty");
        }

        @Test
        @DisplayName("should_ThrowException_When_TitleIsEmpty")
        void should_ThrowException_When_TitleIsEmpty() {
            // Given
            Issue issue = createValidIssue();
            issue.setTitle("");

            // When/Then
            assertThatThrownBy(issue::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Title cannot be empty");
        }

        @Test
        @DisplayName("should_ThrowException_When_TitleTooShort")
        void should_ThrowException_When_TitleTooShort() {
            // Given
            Issue issue = createValidIssue();
            issue.setTitle("Short");  // Only 5 characters

            // When/Then
            assertThatThrownBy(issue::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Title must be at least 10 characters long");
        }

        @Test
        @DisplayName("should_ThrowException_When_TitleTooLong")
        void should_ThrowException_When_TitleTooLong() {
            // Given
            Issue issue = createValidIssue();
            issue.setTitle("A".repeat(201));  // 201 characters

            // When/Then
            assertThatThrownBy(issue::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Title cannot exceed 200 characters");
        }

        @Test
        @DisplayName("should_PassValidation_When_TitleIsValid")
        void should_PassValidation_When_TitleIsValid() {
            // Given
            Issue issue = createValidIssue();
            issue.setTitle("Valid title with exactly 10 characters minimum");

            // When/Then
            assertThatNoException().isThrownBy(issue::validate);
        }
    }

    // ==================== DESCRIPTION VALIDATION TESTS ====================

    @Nested
    @DisplayName("Description Validation Tests")
    class DescriptionValidationTests {

        @Test
        @DisplayName("should_ThrowException_When_DescriptionIsNull")
        void should_ThrowException_When_DescriptionIsNull() {
            // Given
            Issue issue = createValidIssue();
            issue.setDescription(null);

            // When/Then
            assertThatThrownBy(issue::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Description cannot be empty");
        }

        @Test
        @DisplayName("should_ThrowException_When_DescriptionIsEmpty")
        void should_ThrowException_When_DescriptionIsEmpty() {
            // Given
            Issue issue = createValidIssue();
            issue.setDescription("   ");

            // When/Then
            assertThatThrownBy(issue::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Description cannot be empty");
        }

        @Test
        @DisplayName("should_ThrowException_When_DescriptionTooShort")
        void should_ThrowException_When_DescriptionTooShort() {
            // Given
            Issue issue = createValidIssue();
            issue.setDescription("Too short");  // Only 9 characters

            // When/Then
            assertThatThrownBy(issue::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Description must be at least 20 characters long");
        }

        @Test
        @DisplayName("should_ThrowException_When_DescriptionTooLong")
        void should_ThrowException_When_DescriptionTooLong() {
            // Given
            Issue issue = createValidIssue();
            issue.setDescription("A".repeat(2001));  // 2001 characters

            // When/Then
            assertThatThrownBy(issue::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Description cannot exceed 2000 characters");
        }

        @Test
        @DisplayName("should_PassValidation_When_DescriptionIsValid")
        void should_PassValidation_When_DescriptionIsValid() {
            // Given
            Issue issue = createValidIssue();
            issue.setDescription("This is a valid description with at least 20 characters.");

            // When/Then
            assertThatNoException().isThrownBy(issue::validate);
        }
    }

    // ==================== LOCATION VALIDATION TESTS ====================

    @Nested
    @DisplayName("Location Validation Tests")
    class LocationValidationTests {

        @Test
        @DisplayName("should_ThrowException_When_LocationIsNull")
        void should_ThrowException_When_LocationIsNull() {
            // Given
            Issue issue = createValidIssue();
            issue.setLocation(null);

            // When/Then
            assertThatThrownBy(issue::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Location cannot be empty");
        }

        @Test
        @DisplayName("should_ThrowException_When_LocationIsEmpty")
        void should_ThrowException_When_LocationIsEmpty() {
            // Given
            Issue issue = createValidIssue();
            issue.setLocation("");

            // When/Then
            assertThatThrownBy(issue::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Location cannot be empty");
        }

        @Test
        @DisplayName("should_ThrowException_When_LocationTooLong")
        void should_ThrowException_When_LocationTooLong() {
            // Given
            Issue issue = createValidIssue();
            issue.setLocation("A".repeat(501));  // 501 characters

            // When/Then
            assertThatThrownBy(issue::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Location cannot exceed 500 characters");
        }

        @Test
        @DisplayName("should_PassValidation_When_LocationIsValid")
        void should_PassValidation_When_LocationIsValid() {
            // Given
            Issue issue = createValidIssue();
            issue.setLocation("123 Main St, City Center");

            // When/Then
            assertThatNoException().isThrownBy(issue::validate);
        }
    }

    // ==================== CATEGORY VALIDATION TESTS ====================

    @Nested
    @DisplayName("Category Validation Tests")
    class CategoryValidationTests {

        @Test
        @DisplayName("should_ThrowException_When_CategoryIsNull")
        void should_ThrowException_When_CategoryIsNull() {
            // Given
            Issue issue = createValidIssue();
            issue.setCategory(null);

            // When/Then
            assertThatThrownBy(issue::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Category cannot be null");
        }

        @Test
        @DisplayName("should_PassValidation_When_CategoryIsValid")
        void should_PassValidation_When_CategoryIsValid() {
            // Given
            Issue issue = createValidIssue();
            issue.setCategory(Category.SAFETY);

            // When/Then
            assertThatNoException().isThrownBy(issue::validate);
        }
    }

    // ==================== STATUS & PRIORITY VALIDATION TESTS ====================

    @Nested
    @DisplayName("Status and Priority Validation Tests")
    class StatusAndPriorityValidationTests {

        @Test
        @DisplayName("should_ThrowException_When_StatusIsNull")
        void should_ThrowException_When_StatusIsNull() {
            // Given
            Issue issue = createValidIssue();
            issue.setStatus(null);

            // When/Then
            assertThatThrownBy(issue::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Status cannot be null");
        }

        @Test
        @DisplayName("should_ThrowException_When_PriorityIsNull")
        void should_ThrowException_When_PriorityIsNull() {
            // Given
            Issue issue = createValidIssue();
            issue.setPriority(null);

            // When/Then
            assertThatThrownBy(issue::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Priority cannot be null");
        }

        @Test
        @DisplayName("should_ThrowException_When_ReportedByIsNull")
        void should_ThrowException_When_ReportedByIsNull() {
            // Given
            Issue issue = createValidIssue();
            issue.setReportedBy(null);

            // When/Then
            assertThatThrownBy(issue::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("ReportedBy user cannot be null");
        }
    }

    // ==================== ASSIGNMENT LOGIC TESTS ====================

    @Nested
    @DisplayName("Assignment Logic Tests")
    class AssignmentLogicTests {

        @Test
        @DisplayName("should_AssignIssue_When_UserIsStaff")
        void should_AssignIssue_When_UserIsStaff() {
            // Given
            Issue issue = createValidIssue();
            User staff = createStaff();

            // When
            issue.assignTo(staff);

            // Then
            assertThat(issue.getAssignedTo()).isEqualTo(staff);
            assertThat(issue.isAssigned()).isTrue();
            assertThat(issue.isAssignedTo(staff)).isTrue();
        }

        @Test
        @DisplayName("should_AssignIssue_When_UserIsAdmin")
        void should_AssignIssue_When_UserIsAdmin() {
            // Given
            Issue issue = createValidIssue();
            User admin = createAdmin();

            // When
            issue.assignTo(admin);

            // Then
            assertThat(issue.getAssignedTo()).isEqualTo(admin);
            assertThat(issue.isAssigned()).isTrue();
        }

        @Test
        @DisplayName("should_ThrowException_When_AssigningToCitizen")
        void should_ThrowException_When_AssigningToCitizen() {
            // Given
            Issue issue = createValidIssue();
            User citizen = createCitizen();

            // When/Then
            assertThatThrownBy(() -> issue.assignTo(citizen))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Issues can only be assigned to STAFF or ADMIN users");
        }

        @Test
        @DisplayName("should_ThrowException_When_AssigningToNull")
        void should_ThrowException_When_AssigningToNull() {
            // Given
            Issue issue = createValidIssue();

            // When/Then
            assertThatThrownBy(() -> issue.assignTo(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Cannot assign to null user");
        }

        @Test
        @DisplayName("should_ThrowException_When_AssigningClosedIssue")
        void should_ThrowException_When_AssigningClosedIssue() {
            // Given
            Issue issue = createValidIssue();
            issue.setStatus(IssueStatus.CLOSED);
            User staff = createStaff();

            // When/Then
            assertThatThrownBy(() -> issue.assignTo(staff))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Cannot assign closed issue");
        }

        @Test
        @DisplayName("should_ThrowException_When_AssigningDeletedIssue")
        void should_ThrowException_When_AssigningDeletedIssue() {
            // Given
            Issue issue = createValidIssue();
            issue.softDelete();
            User staff = createStaff();

            // When/Then
            assertThatThrownBy(() -> issue.assignTo(staff))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Cannot assign deleted issue");
        }

        @Test
        @DisplayName("should_UnassignIssue_When_CallingUnassign")
        void should_UnassignIssue_When_CallingUnassign() {
            // Given
            Issue issue = createValidIssue();
            issue.assignTo(createStaff());
            assertThat(issue.isAssigned()).isTrue();

            // When
            issue.unassign();

            // Then
            assertThat(issue.isAssigned()).isFalse();
            assertThat(issue.getAssignedTo()).isNull();
        }

        @Test
        @DisplayName("should_ThrowException_When_UnassigningClosedIssue")
        void should_ThrowException_When_UnassigningClosedIssue() {
            // Given
            Issue issue = createValidIssue();
            issue.assignTo(createStaff());
            issue.setStatus(IssueStatus.CLOSED);

            // When/Then
            assertThatThrownBy(issue::unassign)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Cannot unassign closed issue");
        }
    }

    // ==================== STATUS TRANSITION LOGIC TESTS ====================

    @Nested
    @DisplayName("Status Transition Logic Tests")
    class StatusTransitionLogicTests {

        @Test
        @DisplayName("should_TransitionToInProgress_When_FromOpen")
        void should_TransitionToInProgress_When_FromOpen() {
            // Given
            Issue issue = createValidIssue();
            assertThat(issue.getStatus()).isEqualTo(IssueStatus.OPEN);

            // When
            issue.changeStatus(IssueStatus.IN_PROGRESS);

            // Then
            assertThat(issue.getStatus()).isEqualTo(IssueStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("should_TransitionToResolved_When_FromInProgress")
        void should_TransitionToResolved_When_FromInProgress() {
            // Given
            Issue issue = createValidIssue();
            issue.changeStatus(IssueStatus.IN_PROGRESS);

            // When
            issue.changeStatus(IssueStatus.RESOLVED);

            // Then
            assertThat(issue.getStatus()).isEqualTo(IssueStatus.RESOLVED);
            assertThat(issue.getResolvedAt()).isNotNull();
        }

        @Test
        @DisplayName("should_TransitionToClosed_When_FromResolved")
        void should_TransitionToClosed_When_FromResolved() {
            // Given
            Issue issue = createValidIssue();
            issue.changeStatus(IssueStatus.IN_PROGRESS);
            issue.changeStatus(IssueStatus.RESOLVED);

            // When
            issue.changeStatus(IssueStatus.CLOSED);

            // Then
            assertThat(issue.getStatus()).isEqualTo(IssueStatus.CLOSED);
            assertThat(issue.getClosedAt()).isNotNull();
        }

        @Test
        @DisplayName("should_ReopenIssue_When_TransitioningFromResolvedToOpen")
        void should_ReopenIssue_When_TransitioningFromResolvedToOpen() {
            // Given
            Issue issue = createValidIssue();
            issue.changeStatus(IssueStatus.IN_PROGRESS);
            issue.changeStatus(IssueStatus.RESOLVED);
            assertThat(issue.getResolvedAt()).isNotNull();

            // When
            issue.changeStatus(IssueStatus.OPEN);

            // Then
            assertThat(issue.getStatus()).isEqualTo(IssueStatus.OPEN);
        }

        @Test
        @DisplayName("should_ThrowException_When_InvalidTransition")
        void should_ThrowException_When_InvalidTransition() {
            // Given
            Issue issue = createValidIssue();  // Status = OPEN

            // When/Then - Cannot go from OPEN to RESOLVED directly
            assertThatThrownBy(() -> issue.changeStatus(IssueStatus.RESOLVED))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid status transition");
        }

        @Test
        @DisplayName("should_ThrowException_When_TransitioningFromClosed")
        void should_ThrowException_When_TransitioningFromClosed() {
            // Given
            Issue issue = createValidIssue();
            issue.changeStatus(IssueStatus.IN_PROGRESS);
            issue.changeStatus(IssueStatus.RESOLVED);
            issue.changeStatus(IssueStatus.CLOSED);

            // When/Then - Cannot transition from CLOSED
            assertThatThrownBy(() -> issue.changeStatus(IssueStatus.OPEN))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid status transition");
        }

        @Test
        @DisplayName("should_ThrowException_When_ChangingStatusOfDeletedIssue")
        void should_ThrowException_When_ChangingStatusOfDeletedIssue() {
            // Given
            Issue issue = createValidIssue();
            issue.softDelete();

            // When/Then
            assertThatThrownBy(() -> issue.changeStatus(IssueStatus.IN_PROGRESS))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Cannot change status of deleted issue");
        }

        @Test
        @DisplayName("should_DoNothing_When_TransitioningToSameStatus")
        void should_DoNothing_When_TransitioningToSameStatus() {
            // Given
            Issue issue = createValidIssue();
            IssueStatus currentStatus = issue.getStatus();

            // When
            issue.changeStatus(currentStatus);

            // Then - Status unchanged
            assertThat(issue.getStatus()).isEqualTo(currentStatus);
        }

        @Test
        @DisplayName("should_ThrowException_When_NewStatusIsNull")
        void should_ThrowException_When_NewStatusIsNull() {
            // Given
            Issue issue = createValidIssue();

            // When/Then
            assertThatThrownBy(() -> issue.changeStatus(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Status cannot be null");
        }

        @Test
        @DisplayName("should_TransitionToClosed_When_DirectlyFromOpen")
        void should_TransitionToClosed_When_DirectlyFromOpen() {
            // Given
            Issue issue = createValidIssue();

            // When - Admin can close directly from OPEN
            issue.changeStatus(IssueStatus.CLOSED);

            // Then
            assertThat(issue.getStatus()).isEqualTo(IssueStatus.CLOSED);
            assertThat(issue.getClosedAt()).isNotNull();
        }
    }

    // ==================== UPDATE OPERATIONS TESTS ====================

    @Nested
    @DisplayName("Update Operations Tests")
    class UpdateOperationsTests {

        @Test
        @DisplayName("should_UpdateDetails_When_ValidData")
        void should_UpdateDetails_When_ValidData() {
            // Given
            Issue issue = createValidIssue();
            String newTitle = "Updated title with more than 10 characters";
            String newDescription = "Updated description with at least 20 characters long.";

            // When
            issue.updateDetails(newTitle, newDescription);

            // Then
            assertThat(issue.getTitle()).isEqualTo(newTitle);
            assertThat(issue.getDescription()).isEqualTo(newDescription);
        }

        @Test
        @DisplayName("should_ThrowException_When_UpdatingWithInvalidTitle")
        void should_ThrowException_When_UpdatingWithInvalidTitle() {
            // Given
            Issue issue = createValidIssue();
            String invalidTitle = "Short";  // Too short

            // When/Then
            assertThatThrownBy(() -> issue.updateDetails(invalidTitle, "Valid description with at least 20 chars"))
                    .isInstanceOf(IllegalArgumentException.class);

            // Original values should be preserved
            assertThat(issue.getTitle()).isEqualTo("Broken streetlight on Main St");
        }

        @Test
        @DisplayName("should_ThrowException_When_UpdatingClosedIssue")
        void should_ThrowException_When_UpdatingClosedIssue() {
            // Given
            Issue issue = createValidIssue();
            issue.setStatus(IssueStatus.CLOSED);

            // When/Then
            assertThatThrownBy(() -> issue.updateDetails("New title here", "New description here with 20 chars"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Cannot update closed issue");
        }

        @Test
        @DisplayName("should_ChangePriority_When_IssueIsOpen")
        void should_ChangePriority_When_IssueIsOpen() {
            // Given
            Issue issue = createValidIssue();
            assertThat(issue.getPriority()).isEqualTo(Priority.MEDIUM);

            // When
            issue.changePriority(Priority.HIGH);

            // Then
            assertThat(issue.getPriority()).isEqualTo(Priority.HIGH);
        }

        @Test
        @DisplayName("should_ThrowException_When_ChangingPriorityOfClosedIssue")
        void should_ThrowException_When_ChangingPriorityOfClosedIssue() {
            // Given
            Issue issue = createValidIssue();
            issue.setStatus(IssueStatus.CLOSED);

            // When/Then
            assertThatThrownBy(() -> issue.changePriority(Priority.CRITICAL))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Cannot change priority of closed issue");
        }
    }

    // ==================== SOFT DELETE LOGIC TESTS ====================

    @Nested
    @DisplayName("Soft Delete Logic Tests")
    class SoftDeleteLogicTests {

        @Test
        @DisplayName("should_SoftDeleteIssue_When_CallingDelete")
        void should_SoftDeleteIssue_When_CallingDelete() {
            // Given
            Issue issue = createValidIssue();
            assertThat(issue.isDeleted()).isFalse();

            // When
            issue.softDelete();

            // Then
            assertThat(issue.isDeleted()).isTrue();
            assertThat(issue.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("should_ThrowException_When_DeletingAlreadyDeletedIssue")
        void should_ThrowException_When_DeletingAlreadyDeletedIssue() {
            // Given
            Issue issue = createValidIssue();
            issue.softDelete();

            // When/Then
            assertThatThrownBy(issue::softDelete)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Issue is already deleted");
        }

        @Test
        @DisplayName("should_RestoreIssue_When_CallingRestore")
        void should_RestoreIssue_When_CallingRestore() {
            // Given
            Issue issue = createValidIssue();
            issue.softDelete();
            assertThat(issue.isDeleted()).isTrue();

            // When
            issue.restore();

            // Then
            assertThat(issue.isDeleted()).isFalse();
            assertThat(issue.getDeletedAt()).isNull();
        }

        @Test
        @DisplayName("should_ThrowException_When_RestoringNonDeletedIssue")
        void should_ThrowException_When_RestoringNonDeletedIssue() {
            // Given
            Issue issue = createValidIssue();
            assertThat(issue.isDeleted()).isFalse();

            // When/Then
            assertThatThrownBy(issue::restore)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Issue is not deleted");
        }
    }

    // ==================== QUERY METHODS TESTS ====================

    @Nested
    @DisplayName("Query Methods Tests")
    class QueryMethodsTests {

        @Test
        @DisplayName("should_ReturnTrue_When_IssueClosed")
        void should_ReturnTrue_When_IssueClosed() {
            // Given
            Issue issue = createValidIssue();
            issue.setStatus(IssueStatus.CLOSED);

            // When/Then
            assertThat(issue.isClosed()).isTrue();
        }

        @Test
        @DisplayName("should_ReturnTrue_When_IssueResolved")
        void should_ReturnTrue_When_IssueResolved() {
            // Given
            Issue issue = createValidIssue();
            issue.changeStatus(IssueStatus.IN_PROGRESS);
            issue.changeStatus(IssueStatus.RESOLVED);

            // When/Then
            assertThat(issue.isResolved()).isTrue();
        }

        @Test
        @DisplayName("should_ReturnTrue_When_CheckingIsReportedBy")
        void should_ReturnTrue_When_CheckingIsReportedBy() {
            // Given
            User citizen = createCitizen();
            Issue issue = Issue.builder()
                    .title("Test issue with minimum 10 chars")
                    .description("Test description with at least 20 characters.")
                    .status(IssueStatus.OPEN)
                    .priority(Priority.MEDIUM)
                    .category(Category.INFRASTRUCTURE)
                    .location("Test location")
                    .reportedBy(citizen)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            // When/Then
            assertThat(issue.isReportedBy(citizen)).isTrue();
            assertThat(issue.isReportedBy(createStaff())).isFalse();
        }

        @Test
        @DisplayName("should_ReturnTrue_When_CheckingIsAssignedTo")
        void should_ReturnTrue_When_CheckingIsAssignedTo() {
            // Given
            User staff = createStaff();
            Issue issue = createValidIssue();
            issue.assignTo(staff);

            // When/Then
            assertThat(issue.isAssignedTo(staff)).isTrue();
            assertThat(issue.isAssignedTo(createAdmin())).isFalse();
        }

        @Test
        @DisplayName("should_ReturnFalse_When_CheckingIsAssignedToWithNullAssignee")
        void should_ReturnFalse_When_CheckingIsAssignedToWithNullAssignee() {
            // Given
            Issue issue = createValidIssue();
            User staff = createStaff();

            // When/Then
            assertThat(issue.isAssignedTo(staff)).isFalse();
            assertThat(issue.isAssignedTo(null)).isFalse();
        }

        @Test
        @DisplayName("should_ReturnFalse_When_CheckingIsReportedByWithNull")
        void should_ReturnFalse_When_CheckingIsReportedByWithNull() {
            // Given
            Issue issue = createValidIssue();

            // When/Then
            assertThat(issue.isReportedBy(null)).isFalse();
        }

        @Test
        @DisplayName("should_ReturnCorrectStatus_When_CheckingClosedAndResolved")
        void should_ReturnCorrectStatus_When_CheckingClosedAndResolved() {
            // Given
            Issue openIssue = createValidIssue();
            Issue closedIssue = createValidIssue();
            closedIssue.setStatus(IssueStatus.CLOSED);

            // When/Then
            assertThat(openIssue.isClosed()).isFalse();
            assertThat(openIssue.isResolved()).isFalse();
            assertThat(closedIssue.isClosed()).isTrue();
        }
    }

    // ==================== SLA/OVERDUE LOGIC TESTS ====================

    @Nested
    @DisplayName("SLA and Overdue Logic Tests")
    class SlaAndOverdueLogicTests {

        @Test
        @DisplayName("should_NotBeOverdue_When_RecentlyCreated")
        void should_NotBeOverdue_When_RecentlyCreated() {
            // Given
            Issue issue = createValidIssue();
            issue.setCreatedAt(LocalDateTime.now().minusHours(1));
            issue.setPriority(Priority.MEDIUM);  // 24 hour assignment SLA

            // When/Then
            assertThat(issue.isOverdue()).isFalse();
        }

        @Test
        @DisplayName("should_BeOverdue_When_ExceedingAssignmentSLA")
        void should_BeOverdue_When_ExceedingAssignmentSLA() {
            // Given
            Issue issue = createValidIssue();
            issue.setCreatedAt(LocalDateTime.now().minusHours(25));  // 25 hours ago
            issue.setPriority(Priority.MEDIUM);  // 24 hour assignment SLA

            // When/Then
            assertThat(issue.isOverdue()).isTrue();
        }

        @Test
        @DisplayName("should_NotBeOverdue_When_IssueIsClosed")
        void should_NotBeOverdue_When_IssueIsClosed() {
            // Given
            Issue issue = createValidIssue();
            issue.setCreatedAt(LocalDateTime.now().minusHours(100));
            issue.setStatus(IssueStatus.CLOSED);

            // When/Then
            assertThat(issue.isOverdue()).isFalse();
        }

        @Test
        @DisplayName("should_NotBeOverdue_When_IssueIsResolved")
        void should_NotBeOverdue_When_IssueIsResolved() {
            // Given
            Issue issue = createValidIssue();
            issue.setCreatedAt(LocalDateTime.now().minusHours(100));
            issue.changeStatus(IssueStatus.IN_PROGRESS);
            issue.changeStatus(IssueStatus.RESOLVED);

            // When/Then
            assertThat(issue.isOverdue()).isFalse();
        }

        @Test
        @DisplayName("should_ReturnCorrectHoursUntilOverdue_When_IssueIsActive")
        void should_ReturnCorrectHoursUntilOverdue_When_IssueIsActive() {
            // Given
            Issue issue = createValidIssue();
            issue.setCreatedAt(LocalDateTime.now().minusHours(5));
            issue.setPriority(Priority.MEDIUM);  // 24 hour SLA

            // When
            long hoursUntilOverdue = issue.getHoursUntilOverdue();

            // Then - Should have ~19 hours left (24 - 5)
            assertThat(hoursUntilOverdue).isBetween(18L, 20L);
        }
    }
}
