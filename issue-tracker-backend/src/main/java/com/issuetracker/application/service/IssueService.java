package com.issuetracker.application.service;

import com.issuetracker.domain.model.Issue;
import com.issuetracker.domain.model.IssueStatus;
import com.issuetracker.domain.model.Role;
import com.issuetracker.domain.model.User;
import com.issuetracker.domain.port.in.*;
import com.issuetracker.domain.port.out.IssueRepository;
import com.issuetracker.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Application service implementing all Issue use cases.
 *
 * DESIGN PATTERN: Facade Pattern (Structural)
 * - Provides simplified interface to complex subsystem
 * - Orchestrates: Domain validation + Authorization + Repository persistence
 * - Single entry point for all issue-related operations
 *
 * DESIGN PATTERN: Dependency Injection Pattern (Creational)
 * - @RequiredArgsConstructor generates constructor for dependency injection
 * - Dependencies injected via constructor (best practice)
 * - No field injection (@Autowired on fields)
 *
 * DESIGN PATTERN: Proxy Pattern (Structural)
 * - @Transactional creates dynamic proxy around this class
 * - Proxy intercepts method calls, opens DB transaction, commits/rolls back
 * - Ensures data consistency across repository operations
 *
 * DESIGN PATTERN: Service Layer Pattern (Application Architecture)
 * - Separates business logic (domain) from infrastructure concerns
 * - Orchestrates use cases and enforces authorization
 * - Transaction boundaries defined at service level
 *
 * Responsibilities:
 * 1. Implement all use case interfaces
 * 2. Enforce role-based authorization
 * 3. Orchestrate domain objects and repositories
 * 4. Manage transactions
 * 5. Handle cross-cutting concerns (logging, auditing)
 */
@Service  // DESIGN PATTERN: Singleton - Spring creates one instance
@RequiredArgsConstructor  // DESIGN PATTERN: Dependency Injection
@Transactional  // DESIGN PATTERN: Proxy - transaction management
public class IssueService implements
        CreateIssueUseCase,
        UpdateIssueUseCase,
        AssignIssueUseCase,
        ChangeStatusUseCase,
        GetIssueUseCase,
        DeleteIssueUseCase {

    private final IssueRepository issueRepository;
    private final UserRepository userRepository;

    // ==================== CREATE ISSUE ====================

    /**
     * Creates a new issue.
     *
     * Business Rules:
     * 1. Any authenticated user can create an issue
     * 2. Reporter must exist in database
     * 3. New issues always start with OPEN status
     * 4. Timestamps are set automatically
     *
     * @param command the create issue command
     * @return the created issue
     * @throws IllegalArgumentException if validation fails or user not found
     */
    @Override
    public Issue createIssue(CreateIssueCommand command) {
        // Find the reporting user
        User reporter = userRepository.findById(command.reportedByUserId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "User with ID " + command.reportedByUserId() + " not found"
                ));

        // Build the issue
        Issue issue = Issue.builder()
                .title(command.title())
                .description(command.description())
                .category(command.category())
                .priority(command.priority())
                .location(command.location())
                .status(IssueStatus.OPEN)  // Always start as OPEN
                .reportedBy(reporter)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Validate domain rules
        issue.validate();

        // Save to database
        return issueRepository.save(issue);
    }

    // ==================== UPDATE ISSUE ====================

    /**
     * Updates an existing issue.
     *
     * Business Rules:
     * 1. Citizens can only update their own issues
     * 2. STAFF/ADMIN can update any issue
     * 3. Cannot update closed issues
     * 4. Cannot update deleted issues
     *
     * @param command the update issue command
     * @return the updated issue
     * @throws IllegalArgumentException if issue not found or validation fails
     * @throws SecurityException if user not authorized
     * @throws IllegalStateException if issue is closed or deleted
     */
    @Override
    public Issue updateIssue(UpdateIssueCommand command) {
        // Find the issue
        Issue issue = issueRepository.findById(command.issueId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Issue with ID " + command.issueId() + " not found"
                ));

        // Find the user performing the update
        User updater = userRepository.findById(command.updatedByUserId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "User with ID " + command.updatedByUserId() + " not found"
                ));

        // Authorization check
        if (!canUpdateIssue(issue, updater)) {
            throw new SecurityException("You are not authorized to update this issue");
        }

        // State checks
        if (issue.isDeleted()) {
            throw new IllegalStateException("Cannot update deleted issue");
        }
        if (issue.isClosed()) {
            throw new IllegalStateException("Cannot update closed issue");
        }

        // Update fields (only if provided in command)
        if (command.title() != null && command.description() != null) {
            issue.updateDetails(command.title(), command.description());
        }
        if (command.priority() != null) {
            issue.changePriority(command.priority());
        }
        if (command.category() != null) {
            issue.setCategory(command.category());
            issue.setUpdatedAt(LocalDateTime.now());
        }
        if (command.location() != null) {
            issue.setLocation(command.location());
            issue.setUpdatedAt(LocalDateTime.now());
        }

        // Validate after updates
        issue.validate();

        // Save changes
        return issueRepository.save(issue);
    }

    /**
     * Checks if a user can update an issue.
     * CITIZEN: only their own issues
     * STAFF/ADMIN: any issue
     */
    private boolean canUpdateIssue(Issue issue, User user) {
        Role role = user.getRole();
        if (role == Role.ADMIN || role == Role.STAFF) {
            return true;  // STAFF and ADMIN can update any issue
        }
        return issue.isReportedBy(user);  // CITIZEN can only update their own
    }

    // ==================== ASSIGN ISSUE ====================

    /**
     * Assigns an issue to a staff member.
     *
     * Business Rules:
     * 1. Only STAFF/ADMIN can assign issues
     * 2. Issues can only be assigned to STAFF/ADMIN (not CITIZEN)
     * 3. Cannot assign closed or deleted issues
     * 4. assigneeId = null means unassign
     *
     * @param command the assign issue command
     * @return the assigned issue
     * @throws IllegalArgumentException if issue or users not found
     * @throws SecurityException if user not authorized (not STAFF/ADMIN)
     * @throws IllegalStateException if issue is closed or deleted
     */
    @Override
    public Issue assignIssue(AssignIssueCommand command) {
        // Find the issue
        Issue issue = issueRepository.findById(command.issueId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Issue with ID " + command.issueId() + " not found"
                ));

        // Find the user performing the assignment
        User assigner = userRepository.findById(command.assignedByUserId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "User with ID " + command.assignedByUserId() + " not found"
                ));

        // Authorization check - only STAFF/ADMIN can assign
        if (assigner.getRole() != Role.STAFF && assigner.getRole() != Role.ADMIN) {
            throw new SecurityException("Only STAFF or ADMIN can assign issues");
        }

        // State checks
        if (issue.isDeleted()) {
            throw new IllegalStateException("Cannot assign deleted issue");
        }
        if (issue.isClosed()) {
            throw new IllegalStateException("Cannot assign closed issue");
        }

        // Assign or unassign
        if (command.assigneeId() == null) {
            // Unassign
            issue.unassign();
        } else {
            // Find the assignee
            User assignee = userRepository.findById(command.assigneeId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "User with ID " + command.assigneeId() + " not found"
                    ));

            // Assign
            issue.assignTo(assignee);
        }

        // Save changes
        return issueRepository.save(issue);
    }

    // ==================== CHANGE STATUS ====================

    /**
     * Changes the status of an issue.
     *
     * Business Rules:
     * 1. Only STAFF/ADMIN can change status to IN_PROGRESS
     * 2. Only assigned staff can mark as RESOLVED
     * 3. Only ADMIN can mark as CLOSED
     * 4. Status transitions must be valid (enforced by domain)
     * 5. Cannot change status of deleted issues
     *
     * @param command the change status command
     * @return the updated issue
     * @throws IllegalArgumentException if transition invalid or issue not found
     * @throws SecurityException if user not authorized
     * @throws IllegalStateException if issue is deleted
     */
    @Override
    public Issue changeStatus(ChangeStatusCommand command) {
        // Find the issue
        Issue issue = issueRepository.findById(command.issueId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Issue with ID " + command.issueId() + " not found"
                ));

        // Find the user performing the status change
        User changer = userRepository.findById(command.changedByUserId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "User with ID " + command.changedByUserId() + " not found"
                ));

        // Authorization checks based on target status
        if (!canChangeStatus(issue, changer, command.newStatus())) {
            throw new SecurityException(
                    "You are not authorized to change status to " + command.newStatus()
            );
        }

        // State check
        if (issue.isDeleted()) {
            throw new IllegalStateException("Cannot change status of deleted issue");
        }

        // Change status (domain will validate transition rules)
        issue.changeStatus(command.newStatus());

        // Save changes
        return issueRepository.save(issue);
    }

    /**
     * Checks if a user can change an issue to a specific status.
     */
    private boolean canChangeStatus(Issue issue, User user, IssueStatus newStatus) {
        Role role = user.getRole();

        return switch (newStatus) {
            case OPEN -> true;  // Anyone can reopen (if transition is valid)

            case IN_PROGRESS -> role == Role.STAFF || role == Role.ADMIN;  // Only STAFF/ADMIN

            case RESOLVED -> {
                // Only assigned staff or ADMIN
                if (role == Role.ADMIN) {
                    yield true;
                }
                yield issue.isAssignedTo(user);
            }

            case CLOSED -> role == Role.ADMIN;  // Only ADMIN can close
        };
    }

    // ==================== GET ISSUE ====================

    /**
     * Retrieves a single issue by ID.
     *
     * Business Rules:
     * 1. Citizens can only see their own reported issues
     * 2. STAFF can see all issues assigned to them + unassigned
     * 3. ADMIN can see all issues
     *
     * @param issueId the issue ID
     * @param requestingUserId the user requesting the issue
     * @return Optional containing the issue if found and authorized
     */
    @Override
    public Optional<Issue> getIssueById(Long issueId, Long requestingUserId) {
        Optional<Issue> issueOpt = issueRepository.findById(issueId);
        if (issueOpt.isEmpty()) {
            return Optional.empty();
        }

        Issue issue = issueOpt.get();

        // Find requesting user
        User requester = userRepository.findById(requestingUserId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User with ID " + requestingUserId() + " not found"
                ));

        // Authorization check
        if (!canViewIssue(issue, requester)) {
            return Optional.empty();  // Hide unauthorized issues
        }

        return Optional.of(issue);
    }

    /**
     * Retrieves issues matching query criteria.
     *
     * Business Rules:
     * 1. Results are filtered based on user role
     * 2. Citizens only see their own issues
     * 3. STAFF see assigned + unassigned issues
     * 4. ADMIN sees all issues
     *
     * @param query the query criteria
     * @return list of matching issues
     */
    @Override
    public List<Issue> getIssues(IssueQuery query) {
        // Find requesting user
        User requester = userRepository.findById(query.requestingUserId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "User with ID " + query.requestingUserId() + " not found"
                ));

        // Get all matching issues from repository
        List<Issue> issues = issueRepository.findByCriteria(
                query.status(),
                query.priority(),
                query.category(),
                query.reportedByUserId(),
                query.assignedToUserId(),
                query.includeDeleted()
        );

        // Filter by authorization
        return issues.stream()
                .filter(issue -> canViewIssue(issue, requester))
                .collect(Collectors.toList());
    }

    /**
     * Checks if a user can view an issue.
     * CITIZEN: only their own issues
     * STAFF: assigned to them + unassigned
     * ADMIN: all issues
     */
    private boolean canViewIssue(Issue issue, User user) {
        Role role = user.getRole();

        return switch (role) {
            case ADMIN -> true;  // ADMIN can see everything

            case STAFF -> {
                // STAFF can see: assigned to them + unassigned + all open issues
                if (issue.isAssignedTo(user)) {
                    yield true;
                }
                if (!issue.isAssigned()) {
                    yield true;
                }
                yield false;
            }

            case CITIZEN -> issue.isReportedBy(user);  // CITIZEN can only see their own
        };
    }

    // ==================== DELETE ISSUE ====================

    /**
     * Soft deletes an issue.
     *
     * Business Rules:
     * 1. Only ADMIN can delete issues (GDPR data controller)
     * 2. Soft delete (set deletedAt timestamp)
     * 3. Cannot delete already deleted issues
     *
     * @param command the delete command
     * @return the deleted issue
     * @throws IllegalArgumentException if issue not found
     * @throws SecurityException if user is not ADMIN
     * @throws IllegalStateException if already deleted
     */
    @Override
    public Issue deleteIssue(DeleteIssueCommand command) {
        // Find the issue
        Issue issue = issueRepository.findById(command.issueId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Issue with ID " + command.issueId() + " not found"
                ));

        // Find the user performing deletion
        User deleter = userRepository.findById(command.deletedByUserId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "User with ID " + command.deletedByUserId() + " not found"
                ));

        // Authorization check - only ADMIN can delete
        if (deleter.getRole() != Role.ADMIN) {
            throw new SecurityException("Only ADMIN can delete issues");
        }

        // Soft delete
        issue.softDelete();

        // Save changes
        return issueRepository.save(issue);
    }

    /**
     * Restores a soft-deleted issue.
     *
     * Business Rules:
     * 1. Only ADMIN can restore issues
     * 2. Issue must be deleted to restore
     *
     * @param command the restore command
     * @return the restored issue
     * @throws IllegalArgumentException if issue not found
     * @throws SecurityException if user is not ADMIN
     * @throws IllegalStateException if not deleted
     */
    @Override
    public Issue restoreIssue(RestoreIssueCommand command) {
        // Find the issue
        Issue issue = issueRepository.findById(command.issueId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Issue with ID " + command.issueId() + " not found"
                ));

        // Find the user performing restoration
        User restorer = userRepository.findById(command.restoredByUserId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "User with ID " + command.restoredByUserId() + " not found"
                ));

        // Authorization check - only ADMIN can restore
        if (restorer.getRole() != Role.ADMIN) {
            throw new SecurityException("Only ADMIN can restore issues");
        }

        // Restore
        issue.restore();

        // Save changes
        return issueRepository.save(issue);
    }
}
