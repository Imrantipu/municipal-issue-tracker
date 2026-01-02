package com.issuetracker.domain.port.in;

import com.issuetracker.domain.model.Issue;

/**
 * Use case interface for assigning/unassigning issues to staff members.
 *
 * DESIGN PATTERN: Command Pattern (Behavioral)
 * - AssignIssueCommand encapsulates the assignment request as an object
 *
 * DESIGN PATTERN: Port/Adapter Pattern (Hexagonal Architecture)
 * - This is an INPUT PORT defined by domain layer
 * - Implementation: IssueService (application layer)
 *
 * Business Rules:
 * 1. Only STAFF/ADMIN can assign issues
 * 2. Issues can only be assigned to STAFF/ADMIN users (not CITIZEN)
 * 3. Cannot assign closed issues
 * 4. Cannot assign deleted issues
 * 5. Assigning null assigneeId unassigns the issue
 * 6. Issue can be reassigned to different staff member
 */
public interface AssignIssueUseCase {

    /**
     * Assigns an issue to a staff member, or unassigns if assigneeId is null.
     *
     * @param command the assignment command
     * @return the updated issue
     * @throws IllegalArgumentException if validation fails
     * @throws IllegalStateException if issue is closed or deleted
     * @throws SecurityException if user is not authorized (not STAFF/ADMIN)
     */
    Issue assignIssue(AssignIssueCommand command);

    /**
     * Command object encapsulating issue assignment request.
     *
     * DESIGN PATTERN: Command Pattern
     *
     * @param issueId ID of the issue to assign
     * @param assigneeId ID of user to assign to (null = unassign)
     * @param assignedByUserId ID of user performing the assignment (for authorization)
     */
    record AssignIssueCommand(
            Long issueId,
            Long assigneeId,
            Long assignedByUserId
    ) {
        /**
         * Compact constructor for validation.
         */
        public AssignIssueCommand {
            if (issueId == null) {
                throw new IllegalArgumentException("Issue ID cannot be null");
            }
            if (assignedByUserId == null) {
                throw new IllegalArgumentException("Assigned by user ID cannot be null");
            }
            // assigneeId can be null (means unassign)
        }
    }
}
