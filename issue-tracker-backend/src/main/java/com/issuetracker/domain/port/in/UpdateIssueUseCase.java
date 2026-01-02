package com.issuetracker.domain.port.in;

import com.issuetracker.domain.model.Category;
import com.issuetracker.domain.model.Issue;
import com.issuetracker.domain.model.Priority;

/**
 * Use case interface for updating an existing issue.
 *
 * DESIGN PATTERN: Command Pattern (Behavioral)
 * - UpdateIssueCommand encapsulates the update request as an object
 *
 * DESIGN PATTERN: Port/Adapter Pattern (Hexagonal Architecture)
 * - This is an INPUT PORT defined by domain layer
 * - Implementation: IssueService (application layer)
 *
 * Business Rules:
 * 1. Citizens can only update their own issues
 * 2. STAFF/ADMIN can update any issue
 * 3. Cannot update closed issues
 * 4. Cannot update deleted issues
 * 5. Only title, description, priority, and category can be updated via this use case
 * 6. Status changes use ChangeStatusUseCase
 * 7. Assignment changes use AssignIssueUseCase
 */
public interface UpdateIssueUseCase {

    /**
     * Updates an existing issue.
     *
     * @param command the update command containing issue ID and new values
     * @return the updated issue
     * @throws IllegalArgumentException if validation fails
     * @throws IllegalStateException if issue is closed or deleted
     * @throws SecurityException if user is not authorized to update this issue
     */
    Issue updateIssue(UpdateIssueCommand command);

    /**
     * Command object encapsulating issue update request.
     *
     * DESIGN PATTERN: Command Pattern
     *
     * @param issueId ID of the issue to update
     * @param title new title (null = no change)
     * @param description new description (null = no change)
     * @param priority new priority (null = no change)
     * @param category new category (null = no change)
     * @param location new location (null = no change)
     * @param updatedByUserId ID of user performing the update (for authorization)
     */
    record UpdateIssueCommand(
            Long issueId,
            String title,
            String description,
            Priority priority,
            Category category,
            String location,
            Long updatedByUserId
    ) {
        /**
         * Compact constructor for validation and normalization.
         */
        public UpdateIssueCommand {
            if (issueId == null) {
                throw new IllegalArgumentException("Issue ID cannot be null");
            }
            if (updatedByUserId == null) {
                throw new IllegalArgumentException("Updated by user ID cannot be null");
            }

            // Normalize strings
            if (title != null) {
                title = title.trim();
            }
            if (description != null) {
                description = description.trim();
            }
            if (location != null) {
                location = location.trim();
            }
        }
    }
}
