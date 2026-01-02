package com.issuetracker.application.service;

import com.issuetracker.domain.model.*;
import com.issuetracker.domain.port.in.*;
import com.issuetracker.domain.port.out.IssueRepository;
import com.issuetracker.domain.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive service layer tests for IssueService.
 *
 * Test Coverage:
 * - Create issue (8 tests)
 * - Update issue (10 tests)
 * - Assign issue (8 tests)
 * - Change status (10 tests)
 * - Get issue(s) (10 tests)
 * - Delete/Restore issue (6 tests)
 *
 * Total: 52+ tests for comprehensive service coverage
 *
 * Testing Approach:
 * - Use Mockito to mock dependencies (IssueRepository, UserRepository)
 * - Test business logic and authorization
 * - Verify repository interactions
 * - Test exception scenarios
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("IssueService Tests")
class IssueServiceTest {

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private IssueService issueService;

    // Test fixtures
    private User citizen;
    private User staff;
    private User admin;
    private Issue validIssue;

    @BeforeEach
    void setUp() {
        citizen = User.builder()
                .id(1L)
                .name("John Citizen")
                .email("citizen@test.com")
                .password("password123")
                .role(Role.CITIZEN)
                .createdAt(LocalDateTime.now())
                .build();

        staff = User.builder()
                .id(2L)
                .name("Jane Staff")
                .email("staff@test.com")
                .password("password123")
                .role(Role.STAFF)
                .createdAt(LocalDateTime.now())
                .build();

        admin = User.builder()
                .id(3L)
                .name("Admin User")
                .email("admin@test.com")
                .password("password123")
                .role(Role.ADMIN)
                .createdAt(LocalDateTime.now())
                .build();

        validIssue = Issue.builder()
                .id(100L)
                .title("Broken streetlight on Main St")
                .description("The streetlight at 123 Main St has been out for 3 days, creating safety concerns.")
                .status(IssueStatus.OPEN)
                .priority(Priority.MEDIUM)
                .category(Category.INFRASTRUCTURE)
                .location("123 Main St, City Center")
                .reportedBy(citizen)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ==================== CREATE ISSUE TESTS ====================

    @Nested
    @DisplayName("Create Issue Tests")
    class CreateIssueTests {

        @Test
        @DisplayName("should_CreateIssue_When_ValidDataProvided")
        void should_CreateIssue_When_ValidDataProvided() {
            // Given
            CreateIssueUseCase.CreateIssueCommand command = new CreateIssueUseCase.CreateIssueCommand(
                    "Broken streetlight on Main St",
                    "The streetlight at 123 Main St has been out for 3 days, creating safety concerns.",
                    Category.INFRASTRUCTURE,
                    Priority.MEDIUM,
                    "123 Main St, City Center",
                    citizen.getId()
            );

            when(userRepository.findById(citizen.getId())).thenReturn(Optional.of(citizen));
            when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> {
                Issue issue = invocation.getArgument(0);
                issue.setId(100L);
                return issue;
            });

            // When
            Issue created = issueService.createIssue(command);

            // Then
            assertThat(created).isNotNull();
            assertThat(created.getTitle()).isEqualTo(command.title());
            assertThat(created.getDescription()).isEqualTo(command.description());
            assertThat(created.getStatus()).isEqualTo(IssueStatus.OPEN);
            assertThat(created.getReportedBy()).isEqualTo(citizen);
            verify(issueRepository).save(any(Issue.class));
        }

        @Test
        @DisplayName("should_SetDefaultPriority_When_PriorityIsNull")
        void should_SetDefaultPriority_When_PriorityIsNull() {
            // Given
            CreateIssueUseCase.CreateIssueCommand command = new CreateIssueUseCase.CreateIssueCommand(
                    "Test issue title here",
                    "Test description with at least 20 characters.",
                    Category.SANITATION,
                    null,  // null priority
                    "Test location",
                    citizen.getId()
            );

            when(userRepository.findById(citizen.getId())).thenReturn(Optional.of(citizen));
            when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Issue created = issueService.createIssue(command);

            // Then
            assertThat(created.getPriority()).isEqualTo(Priority.MEDIUM);  // Default
        }

        @Test
        @DisplayName("should_ThrowException_When_UserNotFound")
        void should_ThrowException_When_UserNotFound() {
            // Given
            CreateIssueUseCase.CreateIssueCommand command = new CreateIssueUseCase.CreateIssueCommand(
                    "Test issue title here",
                    "Test description with at least 20 characters.",
                    Category.INFRASTRUCTURE,
                    Priority.HIGH,
                    "Test location",
                    999L  // Non-existent user
            );

            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> issueService.createIssue(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User with ID 999 not found");

            verify(issueRepository, never()).save(any());
        }

        @Test
        @DisplayName("should_ThrowException_When_TitleTooShort")
        void should_ThrowException_When_TitleTooShort() {
            // Given
            CreateIssueUseCase.CreateIssueCommand command = new CreateIssueUseCase.CreateIssueCommand(
                    "Short",  // Too short
                    "Valid description with at least 20 characters.",
                    Category.INFRASTRUCTURE,
                    Priority.HIGH,
                    "Test location",
                    citizen.getId()
            );

            when(userRepository.findById(citizen.getId())).thenReturn(Optional.of(citizen));

            // When/Then
            assertThatThrownBy(() -> issueService.createIssue(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Title must be at least 10 characters");

            verify(issueRepository, never()).save(any());
        }

        @Test
        @DisplayName("should_ThrowException_When_DescriptionTooShort")
        void should_ThrowException_When_DescriptionTooShort() {
            // Given
            CreateIssueUseCase.CreateIssueCommand command = new CreateIssueUseCase.CreateIssueCommand(
                    "Valid title with 10+ chars",
                    "Too short",  // Too short
                    Category.INFRASTRUCTURE,
                    Priority.HIGH,
                    "Test location",
                    citizen.getId()
            );

            when(userRepository.findById(citizen.getId())).thenReturn(Optional.of(citizen));

            // When/Then
            assertThatThrownBy(() -> issueService.createIssue(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Description must be at least 20 characters");
        }

        @Test
        @DisplayName("should_TrimWhitespace_When_CreatingIssue")
        void should_TrimWhitespace_When_CreatingIssue() {
            // Given
            CreateIssueUseCase.CreateIssueCommand command = new CreateIssueUseCase.CreateIssueCommand(
                    "  Padded title here  ",
                    "  Padded description with 20 characters  ",
                    Category.INFRASTRUCTURE,
                    Priority.HIGH,
                    "  Padded location  ",
                    citizen.getId()
            );

            when(userRepository.findById(citizen.getId())).thenReturn(Optional.of(citizen));
            when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Issue created = issueService.createIssue(command);

            // Then
            assertThat(created.getTitle()).isEqualTo("Padded title here");
            assertThat(created.getDescription()).isEqualTo("Padded description with 20 characters");
            assertThat(created.getLocation()).isEqualTo("Padded location");
        }

        @Test
        @DisplayName("should_CreateWithCriticalPriority_When_Specified")
        void should_CreateWithCriticalPriority_When_Specified() {
            // Given
            CreateIssueUseCase.CreateIssueCommand command = new CreateIssueUseCase.CreateIssueCommand(
                    "Gas leak emergency",
                    "Gas leak detected at 456 Oak St, immediate action required.",
                    Category.SAFETY,
                    Priority.CRITICAL,
                    "456 Oak St",
                    citizen.getId()
            );

            when(userRepository.findById(citizen.getId())).thenReturn(Optional.of(citizen));
            when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Issue created = issueService.createIssue(command);

            // Then
            assertThat(created.getPriority()).isEqualTo(Priority.CRITICAL);
        }

        @Test
        @DisplayName("should_SetCreatedAtAndUpdatedAt_When_CreatingIssue")
        void should_SetCreatedAtAndUpdatedAt_When_CreatingIssue() {
            // Given
            CreateIssueUseCase.CreateIssueCommand command = new CreateIssueUseCase.CreateIssueCommand(
                    "Test issue title here",
                    "Test description with at least 20 characters.",
                    Category.INFRASTRUCTURE,
                    Priority.MEDIUM,
                    "Test location",
                    citizen.getId()
            );

            when(userRepository.findById(citizen.getId())).thenReturn(Optional.of(citizen));
            when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Issue created = issueService.createIssue(command);

            // Then
            assertThat(created.getCreatedAt()).isNotNull();
            assertThat(created.getUpdatedAt()).isNotNull();
        }
    }

    // ==================== UPDATE ISSUE TESTS ====================

    @Nested
    @DisplayName("Update Issue Tests")
    class UpdateIssueTests {

        @Test
        @DisplayName("should_UpdateIssue_When_CitizenUpdatesOwnIssue")
        void should_UpdateIssue_When_CitizenUpdatesOwnIssue() {
            // Given
            UpdateIssueUseCase.UpdateIssueCommand command = new UpdateIssueUseCase.UpdateIssueCommand(
                    validIssue.getId(),
                    "Updated title with 10+ chars",
                    "Updated description with at least 20 characters.",
                    Priority.HIGH,
                    Category.SAFETY,
                    "Updated location",
                    citizen.getId()
            );

            when(issueRepository.findById(validIssue.getId())).thenReturn(Optional.of(validIssue));
            when(userRepository.findById(citizen.getId())).thenReturn(Optional.of(citizen));
            when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Issue updated = issueService.updateIssue(command);

            // Then
            assertThat(updated.getTitle()).isEqualTo(command.title());
            assertThat(updated.getDescription()).isEqualTo(command.description());
            assertThat(updated.getPriority()).isEqualTo(Priority.HIGH);
            assertThat(updated.getCategory()).isEqualTo(Category.SAFETY);
            verify(issueRepository).save(any(Issue.class));
        }

        @Test
        @DisplayName("should_UpdateIssue_When_StaffUpdatesAnyIssue")
        void should_UpdateIssue_When_StaffUpdatesAnyIssue() {
            // Given
            UpdateIssueUseCase.UpdateIssueCommand command = new UpdateIssueUseCase.UpdateIssueCommand(
                    validIssue.getId(),
                    "Staff updated title",
                    "Staff updated description with 20 chars.",
                    Priority.CRITICAL,
                    null,  // No category change
                    null,  // No location change
                    staff.getId()
            );

            when(issueRepository.findById(validIssue.getId())).thenReturn(Optional.of(validIssue));
            when(userRepository.findById(staff.getId())).thenReturn(Optional.of(staff));
            when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Issue updated = issueService.updateIssue(command);

            // Then
            assertThat(updated.getTitle()).isEqualTo(command.title());
            assertThat(updated.getPriority()).isEqualTo(Priority.CRITICAL);
            // Category and location should remain unchanged
            assertThat(updated.getCategory()).isEqualTo(Category.INFRASTRUCTURE);
        }

        @Test
        @DisplayName("should_ThrowException_When_CitizenUpdatesOthersIssue")
        void should_ThrowException_When_CitizenUpdatesOthersIssue() {
            // Given
            User otherCitizen = User.builder()
                    .id(99L)
                    .name("Other Citizen")
                    .email("other@test.com")
                    .password("password")
                    .role(Role.CITIZEN)
                    .build();

            UpdateIssueUseCase.UpdateIssueCommand command = new UpdateIssueUseCase.UpdateIssueCommand(
                    validIssue.getId(),
                    "Trying to update",
                    "Someone else's issue with 20 chars.",
                    Priority.HIGH,
                    null,
                    null,
                    otherCitizen.getId()
            );

            when(issueRepository.findById(validIssue.getId())).thenReturn(Optional.of(validIssue));
            when(userRepository.findById(otherCitizen.getId())).thenReturn(Optional.of(otherCitizen));

            // When/Then
            assertThatThrownBy(() -> issueService.updateIssue(command))
                    .isInstanceOf(SecurityException.class)
                    .hasMessageContaining("not authorized to update");

            verify(issueRepository, never()).save(any());
        }

        @Test
        @DisplayName("should_ThrowException_When_UpdatingClosedIssue")
        void should_ThrowException_When_UpdatingClosedIssue() {
            // Given
            validIssue.setStatus(IssueStatus.CLOSED);

            UpdateIssueUseCase.UpdateIssueCommand command = new UpdateIssueUseCase.UpdateIssueCommand(
                    validIssue.getId(),
                    "Trying to update",
                    "Closed issue with 20 chars.",
                    Priority.HIGH,
                    null,
                    null,
                    admin.getId()
            );

            when(issueRepository.findById(validIssue.getId())).thenReturn(Optional.of(validIssue));
            when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

            // When/Then
            assertThatThrownBy(() -> issueService.updateIssue(command))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Cannot update closed issue");
        }

        @Test
        @DisplayName("should_ThrowException_When_UpdatingDeletedIssue")
        void should_ThrowException_When_UpdatingDeletedIssue() {
            // Given
            validIssue.softDelete();

            UpdateIssueUseCase.UpdateIssueCommand command = new UpdateIssueUseCase.UpdateIssueCommand(
                    validIssue.getId(),
                    "Trying to update",
                    "Deleted issue with 20 chars.",
                    Priority.HIGH,
                    null,
                    null,
                    admin.getId()
            );

            when(issueRepository.findById(validIssue.getId())).thenReturn(Optional.of(validIssue));
            when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

            // When/Then
            assertThatThrownBy(() -> issueService.updateIssue(command))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Cannot update deleted issue");
        }

        @Test
        @DisplayName("should_ThrowException_When_IssueNotFound")
        void should_ThrowException_When_IssueNotFound() {
            // Given
            UpdateIssueUseCase.UpdateIssueCommand command = new UpdateIssueUseCase.UpdateIssueCommand(
                    999L,  // Non-existent
                    "Update title here",
                    "Update description with 20 chars.",
                    Priority.HIGH,
                    null,
                    null,
                    citizen.getId()
            );

            when(issueRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> issueService.updateIssue(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Issue with ID 999 not found");
        }

        @Test
        @DisplayName("should_UpdateOnlyProvidedFields_When_SomeFieldsNull")
        void should_UpdateOnlyProvidedFields_When_SomeFieldsNull() {
            // Given - Only update priority
            UpdateIssueUseCase.UpdateIssueCommand command = new UpdateIssueUseCase.UpdateIssueCommand(
                    validIssue.getId(),
                    null,  // Don't update title
                    null,  // Don't update description
                    Priority.CRITICAL,  // Update priority
                    null,  // Don't update category
                    null,  // Don't update location
                    staff.getId()
            );

            String originalTitle = validIssue.getTitle();
            String originalDescription = validIssue.getDescription();

            when(issueRepository.findById(validIssue.getId())).thenReturn(Optional.of(validIssue));
            when(userRepository.findById(staff.getId())).thenReturn(Optional.of(staff));
            when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Issue updated = issueService.updateIssue(command);

            // Then
            assertThat(updated.getPriority()).isEqualTo(Priority.CRITICAL);
            assertThat(updated.getTitle()).isEqualTo(originalTitle);  // Unchanged
            assertThat(updated.getDescription()).isEqualTo(originalDescription);  // Unchanged
        }

        @Test
        @DisplayName("should_AllowAdminToUpdateAnyIssue")
        void should_AllowAdminToUpdateAnyIssue() {
            // Given
            UpdateIssueUseCase.UpdateIssueCommand command = new UpdateIssueUseCase.UpdateIssueCommand(
                    validIssue.getId(),
                    "Admin updated title",
                    "Admin updated description with 20 chars.",
                    Priority.LOW,
                    Category.OTHER,
                    "Admin updated location",
                    admin.getId()
            );

            when(issueRepository.findById(validIssue.getId())).thenReturn(Optional.of(validIssue));
            when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
            when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Issue updated = issueService.updateIssue(command);

            // Then
            assertThat(updated.getTitle()).isEqualTo(command.title());
            assertThat(updated.getPriority()).isEqualTo(Priority.LOW);
            assertThat(updated.getCategory()).isEqualTo(Category.OTHER);
        }

        @Test
        @DisplayName("should_ThrowException_When_UserNotFound")
        void should_ThrowException_When_UserNotFound() {
            // Given
            UpdateIssueUseCase.UpdateIssueCommand command = new UpdateIssueUseCase.UpdateIssueCommand(
                    validIssue.getId(),
                    "Update title here",
                    "Update description with 20 chars.",
                    Priority.HIGH,
                    null,
                    null,
                    999L  // Non-existent user
            );

            when(issueRepository.findById(validIssue.getId())).thenReturn(Optional.of(validIssue));
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> issueService.updateIssue(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User with ID 999 not found");
        }

        @Test
        @DisplayName("should_ThrowException_When_UpdateWithInvalidTitle")
        void should_ThrowException_When_UpdateWithInvalidTitle() {
            // Given
            UpdateIssueUseCase.UpdateIssueCommand command = new UpdateIssueUseCase.UpdateIssueCommand(
                    validIssue.getId(),
                    "Short",  // Too short
                    "Valid description with 20 chars.",
                    Priority.HIGH,
                    null,
                    null,
                    staff.getId()
            );

            when(issueRepository.findById(validIssue.getId())).thenReturn(Optional.of(validIssue));
            when(userRepository.findById(staff.getId())).thenReturn(Optional.of(staff));

            // When/Then
            assertThatThrownBy(() -> issueService.updateIssue(command))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ==================== ASSIGN ISSUE TESTS ====================

    @Nested
    @DisplayName("Assign Issue Tests")
    class AssignIssueTests {

        @Test
        @DisplayName("should_AssignIssue_When_StaffAssignsToStaff")
        void should_AssignIssue_When_StaffAssignsToStaff() {
            // Given
            User anotherStaff = User.builder()
                    .id(10L)
                    .name("Another Staff")
                    .email("staff2@test.com")
                    .role(Role.STAFF)
                    .build();

            AssignIssueUseCase.AssignIssueCommand command = new AssignIssueUseCase.AssignIssueCommand(
                    validIssue.getId(),
                    anotherStaff.getId(),
                    staff.getId()
            );

            when(issueRepository.findById(validIssue.getId())).thenReturn(Optional.of(validIssue));
            when(userRepository.findById(staff.getId())).thenReturn(Optional.of(staff));
            when(userRepository.findById(anotherStaff.getId())).thenReturn(Optional.of(anotherStaff));
            when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Issue assigned = issueService.assignIssue(command);

            // Then
            assertThat(assigned.getAssignedTo()).isEqualTo(anotherStaff);
            assertThat(assigned.isAssigned()).isTrue();
            verify(issueRepository).save(any(Issue.class));
        }

        @Test
        @DisplayName("should_UnassignIssue_When_AssigneeIdIsNull")
        void should_UnassignIssue_When_AssigneeIdIsNull() {
            // Given
            validIssue.assignTo(staff);

            AssignIssueUseCase.AssignIssueCommand command = new AssignIssueUseCase.AssignIssueCommand(
                    validIssue.getId(),
                    null,  // Unassign
                    staff.getId()
            );

            when(issueRepository.findById(validIssue.getId())).thenReturn(Optional.of(validIssue));
            when(userRepository.findById(staff.getId())).thenReturn(Optional.of(staff));
            when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Issue unassigned = issueService.assignIssue(command);

            // Then
            assertThat(unassigned.isAssigned()).isFalse();
            assertThat(unassigned.getAssignedTo()).isNull();
        }

        @Test
        @DisplayName("should_ThrowException_When_CitizenTriesToAssign")
        void should_ThrowException_When_CitizenTriesToAssign() {
            // Given
            AssignIssueUseCase.AssignIssueCommand command = new AssignIssueUseCase.AssignIssueCommand(
                    validIssue.getId(),
                    staff.getId(),
                    citizen.getId()  // Citizen trying to assign
            );

            when(issueRepository.findById(validIssue.getId())).thenReturn(Optional.of(validIssue));
            when(userRepository.findById(citizen.getId())).thenReturn(Optional.of(citizen));

            // When/Then
            assertThatThrownBy(() -> issueService.assignIssue(command))
                    .isInstanceOf(SecurityException.class)
                    .hasMessage("Only STAFF or ADMIN can assign issues");

            verify(issueRepository, never()).save(any());
        }

        @Test
        @DisplayName("should_ThrowException_When_AssigningToCitizen")
        void should_ThrowException_When_AssigningToCitizen() {
            // Given
            AssignIssueUseCase.AssignIssueCommand command = new AssignIssueUseCase.AssignIssueCommand(
                    validIssue.getId(),
                    citizen.getId(),  // Trying to assign to citizen
                    staff.getId()
            );

            when(issueRepository.findById(validIssue.getId())).thenReturn(Optional.of(validIssue));
            when(userRepository.findById(staff.getId())).thenReturn(Optional.of(staff));
            when(userRepository.findById(citizen.getId())).thenReturn(Optional.of(citizen));

            // When/Then
            assertThatThrownBy(() -> issueService.assignIssue(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("can only be assigned to STAFF or ADMIN");
        }

        @Test
        @DisplayName("should_ThrowException_When_AssigningClosedIssue")
        void should_ThrowException_When_AssigningClosedIssue() {
            // Given
            validIssue.setStatus(IssueStatus.CLOSED);

            AssignIssueUseCase.AssignIssueCommand command = new AssignIssueUseCase.AssignIssueCommand(
                    validIssue.getId(),
                    staff.getId(),
                    admin.getId()
            );

            when(issueRepository.findById(validIssue.getId())).thenReturn(Optional.of(validIssue));
            when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
            when(userRepository.findById(staff.getId())).thenReturn(Optional.of(staff));

            // When/Then
            assertThatThrownBy(() -> issueService.assignIssue(command))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Cannot assign closed issue");
        }

        @Test
        @DisplayName("should_ThrowException_When_IssueNotFound")
        void should_ThrowException_When_IssueNotFound() {
            // Given
            AssignIssueUseCase.AssignIssueCommand command = new AssignIssueUseCase.AssignIssueCommand(
                    999L,
                    staff.getId(),
                    admin.getId()
            );

            when(issueRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> issueService.assignIssue(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Issue with ID 999 not found");
        }

        @Test
        @DisplayName("should_AllowAdminToAssignIssue")
        void should_AllowAdminToAssignIssue() {
            // Given
            AssignIssueUseCase.AssignIssueCommand command = new AssignIssueUseCase.AssignIssueCommand(
                    validIssue.getId(),
                    staff.getId(),
                    admin.getId()
            );

            when(issueRepository.findById(validIssue.getId())).thenReturn(Optional.of(validIssue));
            when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
            when(userRepository.findById(staff.getId())).thenReturn(Optional.of(staff));
            when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Issue assigned = issueService.assignIssue(command);

            // Then
            assertThat(assigned.getAssignedTo()).isEqualTo(staff);
        }

        @Test
        @DisplayName("should_ThrowException_When_AssigneeNotFound")
        void should_ThrowException_When_AssigneeNotFound() {
            // Given
            AssignIssueUseCase.AssignIssueCommand command = new AssignIssueUseCase.AssignIssueCommand(
                    validIssue.getId(),
                    999L,  // Non-existent assignee
                    staff.getId()
            );

            when(issueRepository.findById(validIssue.getId())).thenReturn(Optional.of(validIssue));
            when(userRepository.findById(staff.getId())).thenReturn(Optional.of(staff));
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> issueService.assignIssue(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User with ID 999 not found");
        }
    }

    // ==================== CHANGE STATUS TESTS ====================

    @Nested
    @DisplayName("Change Status Tests")
    class ChangeStatusTests {

        @Test
        @DisplayName("should_ChangeToInProgress_When_StaffChangesStatus")
        void should_ChangeToInProgress_When_StaffChangesStatus() {
            // Given
            ChangeStatusUseCase.ChangeStatusCommand command = new ChangeStatusUseCase.ChangeStatusCommand(
                    validIssue.getId(),
                    IssueStatus.IN_PROGRESS,
                    staff.getId()
            );

            when(issueRepository.findById(validIssue.getId())).thenReturn(Optional.of(validIssue));
            when(userRepository.findById(staff.getId())).thenReturn(Optional.of(staff));
            when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Issue updated = issueService.changeStatus(command);

            // Then
            assertThat(updated.getStatus()).isEqualTo(IssueStatus.IN_PROGRESS);
            verify(issueRepository).save(any(Issue.class));
        }

        @Test
        @DisplayName("should_ThrowException_When_CitizenChangesToInProgress")
        void should_ThrowException_When_CitizenChangesToInProgress() {
            // Given
            ChangeStatusUseCase.ChangeStatusCommand command = new ChangeStatusUseCase.ChangeStatusCommand(
                    validIssue.getId(),
                    IssueStatus.IN_PROGRESS,
                    citizen.getId()
            );

            when(issueRepository.findById(validIssue.getId())).thenReturn(Optional.of(validIssue));
            when(userRepository.findById(citizen.getId())).thenReturn(Optional.of(citizen));

            // When/Then
            assertThatThrownBy(() -> issueService.changeStatus(command))
                    .isInstanceOf(SecurityException.class)
                    .hasMessageContaining("not authorized to change status");
        }

        @Test
        @DisplayName("should_MarkAsResolved_When_AssignedStaffChangesStatus")
        void should_MarkAsResolved_When_AssignedStaffChangesStatus() {
            // Given
            validIssue.assignTo(staff);
            validIssue.changeStatus(IssueStatus.IN_PROGRESS);

            ChangeStatusUseCase.ChangeStatusCommand command = new ChangeStatusUseCase.ChangeStatusCommand(
                    validIssue.getId(),
                    IssueStatus.RESOLVED,
                    staff.getId()
            );

            when(issueRepository.findById(validIssue.getId())).thenReturn(Optional.of(validIssue));
            when(userRepository.findById(staff.getId())).thenReturn(Optional.of(staff));
            when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Issue resolved = issueService.changeStatus(command);

            // Then
            assertThat(resolved.getStatus()).isEqualTo(IssueStatus.RESOLVED);
            assertThat(resolved.getResolvedAt()).isNotNull();
        }

        @Test
        @DisplayName("should_ThrowException_When_UnassignedStaffMarksAsResolved")
        void should_ThrowException_When_UnassignedStaffMarksAsResolved() {
            // Given
            validIssue.changeStatus(IssueStatus.IN_PROGRESS);
            // Not assigned to anyone

            ChangeStatusUseCase.ChangeStatusCommand command = new ChangeStatusUseCase.ChangeStatusCommand(
                    validIssue.getId(),
                    IssueStatus.RESOLVED,
                    staff.getId()
            );

            when(issueRepository.findById(validIssue.getId())).thenReturn(Optional.of(validIssue));
            when(userRepository.findById(staff.getId())).thenReturn(Optional.of(staff));

            // When/Then
            assertThatThrownBy(() -> issueService.changeStatus(command))
                    .isInstanceOf(SecurityException.class)
                    .hasMessageContaining("not authorized");
        }

        @Test
        @DisplayName("should_ClosedIssue_When_AdminChangesToClosed")
        void should_ClosedIssue_When_AdminChangesToClosed() {
            // Given
            validIssue.changeStatus(IssueStatus.IN_PROGRESS);
            validIssue.changeStatus(IssueStatus.RESOLVED);

            ChangeStatusUseCase.ChangeStatusCommand command = new ChangeStatusUseCase.ChangeStatusCommand(
                    validIssue.getId(),
                    IssueStatus.CLOSED,
                    admin.getId()
            );

            when(issueRepository.findById(validIssue.getId())).thenReturn(Optional.of(validIssue));
            when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
            when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Issue closed = issueService.changeStatus(command);

            // Then
            assertThat(closed.getStatus()).isEqualTo(IssueStatus.CLOSED);
            assertThat(closed.getClosedAt()).isNotNull();
        }

        @Test
        @DisplayName("should_ThrowException_When_StaffTriesToClose")
        void should_ThrowException_When_StaffTriesToClose() {
            // Given
            validIssue.changeStatus(IssueStatus.IN_PROGRESS);
            validIssue.changeStatus(IssueStatus.RESOLVED);

            ChangeStatusUseCase.ChangeStatusCommand command = new ChangeStatusUseCase.ChangeStatusCommand(
                    validIssue.getId(),
                    IssueStatus.CLOSED,
                    staff.getId()
            );

            when(issueRepository.findById(validIssue.getId())).thenReturn(Optional.of(validIssue));
            when(userRepository.findById(staff.getId())).thenReturn(Optional.of(staff));

            // When/Then
            assertThatThrownBy(() -> issueService.changeStatus(command))
                    .isInstanceOf(SecurityException.class)
                    .hasMessageContaining("not authorized");
        }

        @Test
        @DisplayName("should_ThrowException_When_InvalidStatusTransition")
        void should_ThrowException_When_InvalidStatusTransition() {
            // Given - Try to go from OPEN to RESOLVED directly
            ChangeStatusUseCase.ChangeStatusCommand command = new ChangeStatusUseCase.ChangeStatusCommand(
                    validIssue.getId(),
                    IssueStatus.RESOLVED,
                    admin.getId()
            );

            when(issueRepository.findById(validIssue.getId())).thenReturn(Optional.of(validIssue));
            when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

            // When/Then
            assertThatThrownBy(() -> issueService.changeStatus(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid status transition");
        }

        @Test
        @DisplayName("should_ThrowException_When_ChangingStatusOfDeletedIssue")
        void should_ThrowException_When_ChangingStatusOfDeletedIssue() {
            // Given
            validIssue.softDelete();

            ChangeStatusUseCase.ChangeStatusCommand command = new ChangeStatusUseCase.ChangeStatusCommand(
                    validIssue.getId(),
                    IssueStatus.IN_PROGRESS,
                    staff.getId()
            );

            when(issueRepository.findById(validIssue.getId())).thenReturn(Optional.of(validIssue));
            when(userRepository.findById(staff.getId())).thenReturn(Optional.of(staff));

            // When/Then
            assertThatThrownBy(() -> issueService.changeStatus(command))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Cannot change status of deleted issue");
        }

        @Test
        @DisplayName("should_AllowAdminToMarkAsResolved_When_NotAssigned")
        void should_AllowAdminToMarkAsResolved_When_NotAssigned() {
            // Given
            validIssue.changeStatus(IssueStatus.IN_PROGRESS);

            ChangeStatusUseCase.ChangeStatusCommand command = new ChangeStatusUseCase.ChangeStatusCommand(
                    validIssue.getId(),
                    IssueStatus.RESOLVED,
                    admin.getId()
            );

            when(issueRepository.findById(validIssue.getId())).thenReturn(Optional.of(validIssue));
            when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
            when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Issue resolved = issueService.changeStatus(command);

            // Then
            assertThat(resolved.getStatus()).isEqualTo(IssueStatus.RESOLVED);
        }

        @Test
        @DisplayName("should_ThrowException_When_IssueNotFound")
        void should_ThrowException_When_IssueNotFound() {
            // Given
            ChangeStatusUseCase.ChangeStatusCommand command = new ChangeStatusUseCase.ChangeStatusCommand(
                    999L,
                    IssueStatus.IN_PROGRESS,
                    staff.getId()
            );

            when(issueRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> issueService.changeStatus(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Issue with ID 999 not found");
        }
    }

    // ==================== GET ISSUE TESTS ====================

    @Nested
    @DisplayName("Get Issue Tests")
    class GetIssueTests {

        @Test
        @DisplayName("should_GetIssueById_When_CitizenViewsOwnIssue")
        void should_GetIssueById_When_CitizenViewsOwnIssue() {
            // Given
            when(issueRepository.findById(validIssue.getId())).thenReturn(Optional.of(validIssue));
            when(userRepository.findById(citizen.getId())).thenReturn(Optional.of(citizen));

            // When
            Optional<Issue> found = issueService.getIssueById(validIssue.getId(), citizen.getId());

            // Then
            assertThat(found).isPresent();
            assertThat(found.get()).isEqualTo(validIssue);
        }

        @Test
        @DisplayName("should_ReturnEmpty_When_CitizenViewsOthersIssue")
        void should_ReturnEmpty_When_CitizenViewsOthersIssue() {
            // Given
            User otherCitizen = User.builder()
                    .id(99L)
                    .name("Other Citizen")
                    .role(Role.CITIZEN)
                    .build();

            when(issueRepository.findById(validIssue.getId())).thenReturn(Optional.of(validIssue));
            when(userRepository.findById(otherCitizen.getId())).thenReturn(Optional.of(otherCitizen));

            // When
            Optional<Issue> found = issueService.getIssueById(validIssue.getId(), otherCitizen.getId());

            // Then
            assertThat(found).isEmpty();  // Hidden due to authorization
        }

        @Test
        @DisplayName("should_GetIssueById_When_StaffViewsAssignedIssue")
        void should_GetIssueById_When_StaffViewsAssignedIssue() {
            // Given
            validIssue.assignTo(staff);

            when(issueRepository.findById(validIssue.getId())).thenReturn(Optional.of(validIssue));
            when(userRepository.findById(staff.getId())).thenReturn(Optional.of(staff));

            // When
            Optional<Issue> found = issueService.getIssueById(validIssue.getId(), staff.getId());

            // Then
            assertThat(found).isPresent();
        }

        @Test
        @DisplayName("should_GetIssueById_When_StaffViewsUnassignedIssue")
        void should_GetIssueById_When_StaffViewsUnassignedIssue() {
            // Given - Issue not assigned to anyone
            when(issueRepository.findById(validIssue.getId())).thenReturn(Optional.of(validIssue));
            when(userRepository.findById(staff.getId())).thenReturn(Optional.of(staff));

            // When
            Optional<Issue> found = issueService.getIssueById(validIssue.getId(), staff.getId());

            // Then
            assertThat(found).isPresent();
        }

        @Test
        @DisplayName("should_GetIssueById_When_AdminViewsAnyIssue")
        void should_GetIssueById_When_AdminViewsAnyIssue() {
            // Given
            when(issueRepository.findById(validIssue.getId())).thenReturn(Optional.of(validIssue));
            when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

            // When
            Optional<Issue> found = issueService.getIssueById(validIssue.getId(), admin.getId());

            // Then
            assertThat(found).isPresent();
        }

        @Test
        @DisplayName("should_ReturnEmpty_When_IssueNotFound")
        void should_ReturnEmpty_When_IssueNotFound() {
            // Given
            when(issueRepository.findById(999L)).thenReturn(Optional.empty());
            when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

            // When
            Optional<Issue> found = issueService.getIssueById(999L, admin.getId());

            // Then
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("should_GetIssues_When_CitizenQueriesOwnIssues")
        void should_GetIssues_When_CitizenQueriesOwnIssues() {
            // Given
            GetIssueUseCase.IssueQuery query = GetIssueUseCase.IssueQuery.reportedByUser(citizen.getId());

            List<Issue> allIssues = Arrays.asList(validIssue);

            when(userRepository.findById(citizen.getId())).thenReturn(Optional.of(citizen));
            when(issueRepository.findByCriteria(any(), any(), any(), any(), any(), anyBoolean()))
                    .thenReturn(allIssues);

            // When
            List<Issue> found = issueService.getIssues(query);

            // Then
            assertThat(found).hasSize(1);
            assertThat(found.get(0)).isEqualTo(validIssue);
        }

        @Test
        @DisplayName("should_GetAllIssues_When_AdminQueries")
        void should_GetAllIssues_When_AdminQueries() {
            // Given
            GetIssueUseCase.IssueQuery query = GetIssueUseCase.IssueQuery.allIssuesForUser(admin.getId());

            Issue issue1 = validIssue;
            Issue issue2 = Issue.builder()
                    .id(200L)
                    .title("Another issue title")
                    .description("Another description with 20 chars.")
                    .status(IssueStatus.OPEN)
                    .priority(Priority.HIGH)
                    .category(Category.SANITATION)
                    .location("456 Oak St")
                    .reportedBy(citizen)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            List<Issue> allIssues = Arrays.asList(issue1, issue2);

            when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
            when(issueRepository.findByCriteria(any(), any(), any(), any(), any(), anyBoolean()))
                    .thenReturn(allIssues);

            // When
            List<Issue> found = issueService.getIssues(query);

            // Then
            assertThat(found).hasSize(2);  // Admin sees all
        }

        @Test
        @DisplayName("should_FilterIssuesByCriteria_When_QueryingWithFilters")
        void should_FilterIssuesByCriteria_When_QueryingWithFilters() {
            // Given
            GetIssueUseCase.IssueQuery query = new GetIssueUseCase.IssueQuery(
                    admin.getId(),
                    IssueStatus.OPEN,
                    Priority.HIGH,
                    Category.INFRASTRUCTURE,
                    null,
                    null,
                    false,
                    null,
                    null
            );

            when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
            when(issueRepository.findByCriteria(
                    IssueStatus.OPEN,
                    Priority.HIGH,
                    Category.INFRASTRUCTURE,
                    null,
                    null,
                    false
            )).thenReturn(Arrays.asList(validIssue));

            // When
            List<Issue> found = issueService.getIssues(query);

            // Then
            assertThat(found).isNotEmpty();
            verify(issueRepository).findByCriteria(
                    IssueStatus.OPEN,
                    Priority.HIGH,
                    Category.INFRASTRUCTURE,
                    null,
                    null,
                    false
            );
        }
    }

    // ==================== DELETE/RESTORE ISSUE TESTS ====================

    @Nested
    @DisplayName("Delete and Restore Issue Tests")
    class DeleteAndRestoreIssueTests {

        @Test
        @DisplayName("should_SoftDeleteIssue_When_AdminDeletes")
        void should_SoftDeleteIssue_When_AdminDeletes() {
            // Given
            DeleteIssueUseCase.DeleteIssueCommand command = new DeleteIssueUseCase.DeleteIssueCommand(
                    validIssue.getId(),
                    admin.getId()
            );

            when(issueRepository.findById(validIssue.getId())).thenReturn(Optional.of(validIssue));
            when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
            when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Issue deleted = issueService.deleteIssue(command);

            // Then
            assertThat(deleted.isDeleted()).isTrue();
            assertThat(deleted.getDeletedAt()).isNotNull();
            verify(issueRepository).save(any(Issue.class));
        }

        @Test
        @DisplayName("should_ThrowException_When_StaffTriesToDelete")
        void should_ThrowException_When_StaffTriesToDelete() {
            // Given
            DeleteIssueUseCase.DeleteIssueCommand command = new DeleteIssueUseCase.DeleteIssueCommand(
                    validIssue.getId(),
                    staff.getId()
            );

            when(issueRepository.findById(validIssue.getId())).thenReturn(Optional.of(validIssue));
            when(userRepository.findById(staff.getId())).thenReturn(Optional.of(staff));

            // When/Then
            assertThatThrownBy(() -> issueService.deleteIssue(command))
                    .isInstanceOf(SecurityException.class)
                    .hasMessage("Only ADMIN can delete issues");

            verify(issueRepository, never()).save(any());
        }

        @Test
        @DisplayName("should_ThrowException_When_DeletingAlreadyDeletedIssue")
        void should_ThrowException_When_DeletingAlreadyDeletedIssue() {
            // Given
            validIssue.softDelete();

            DeleteIssueUseCase.DeleteIssueCommand command = new DeleteIssueUseCase.DeleteIssueCommand(
                    validIssue.getId(),
                    admin.getId()
            );

            when(issueRepository.findById(validIssue.getId())).thenReturn(Optional.of(validIssue));
            when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

            // When/Then
            assertThatThrownBy(() -> issueService.deleteIssue(command))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Issue is already deleted");
        }

        @Test
        @DisplayName("should_RestoreIssue_When_AdminRestores")
        void should_RestoreIssue_When_AdminRestores() {
            // Given
            validIssue.softDelete();

            DeleteIssueUseCase.RestoreIssueCommand command = new DeleteIssueUseCase.RestoreIssueCommand(
                    validIssue.getId(),
                    admin.getId()
            );

            when(issueRepository.findById(validIssue.getId())).thenReturn(Optional.of(validIssue));
            when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
            when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Issue restored = issueService.restoreIssue(command);

            // Then
            assertThat(restored.isDeleted()).isFalse();
            assertThat(restored.getDeletedAt()).isNull();
            verify(issueRepository).save(any(Issue.class));
        }

        @Test
        @DisplayName("should_ThrowException_When_StaffTriesToRestore")
        void should_ThrowException_When_StaffTriesToRestore() {
            // Given
            validIssue.softDelete();

            DeleteIssueUseCase.RestoreIssueCommand command = new DeleteIssueUseCase.RestoreIssueCommand(
                    validIssue.getId(),
                    staff.getId()
            );

            when(issueRepository.findById(validIssue.getId())).thenReturn(Optional.of(validIssue));
            when(userRepository.findById(staff.getId())).thenReturn(Optional.of(staff));

            // When/Then
            assertThatThrownBy(() -> issueService.restoreIssue(command))
                    .isInstanceOf(SecurityException.class)
                    .hasMessage("Only ADMIN can restore issues");
        }

        @Test
        @DisplayName("should_ThrowException_When_RestoringNonDeletedIssue")
        void should_ThrowException_When_RestoringNonDeletedIssue() {
            // Given - Issue not deleted
            DeleteIssueUseCase.RestoreIssueCommand command = new DeleteIssueUseCase.RestoreIssueCommand(
                    validIssue.getId(),
                    admin.getId()
            );

            when(issueRepository.findById(validIssue.getId())).thenReturn(Optional.of(validIssue));
            when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

            // When/Then
            assertThatThrownBy(() -> issueService.restoreIssue(command))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Issue is not deleted");
        }
    }
}
