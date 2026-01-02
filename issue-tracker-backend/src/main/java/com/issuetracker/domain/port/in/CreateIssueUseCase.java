package com.issuetracker.domain.port.in;

import com.issuetracker.domain.model.Category;
import com.issuetracker.domain.model.Issue;
import com.issuetracker.domain.model.Priority;

/**
 * Use case interface for creating a new issue.
 *
 * DESIGN PATTERN: Command Pattern (Behavioral)
 * - CreateIssueCommand encapsulates the issue creation request as an object
 * - Method takes single command parameter instead of multiple individual parameters
 * - Benefits: Extensibility (add fields without breaking method signature), validation, logging
 *
 * DESIGN PATTERN: Port/Adapter Pattern (Hexagonal Architecture)
 * - This is an INPUT PORT (use case interface defined by domain layer)
 * - Implementation: IssueService (application layer)
 * - Caller: IssueController (infrastructure/web adapter)
 * - Direction: Infrastructure → Application → Domain
 *
 * Business Rules:
 * 1. Any authenticated user can create an issue
 * 2. Title must be 10-200 characters
 * 3. Description must be 20-2000 characters
 * 4. Location is required
 * 5. Category is required
 * 6. Priority defaults to MEDIUM if not provided
 * 7. Status is always OPEN for new issues
 * 8. CreatedBy is the authenticated user
 */
public interface CreateIssueUseCase {

    /**
     * Creates a new issue in the system.
     *
     * @param command the issue creation command containing all required data
     * @return the created issue with generated ID and timestamps
     * @throws IllegalArgumentException if validation fails
     */
    Issue createIssue(CreateIssueCommand command);

    /**
     * Command object encapsulating issue creation request.
     *
     * DESIGN PATTERN: Command Pattern
     * - Immutable record (Java 16+ feature)
     * - Encapsulates all data needed to create an issue
     * - Can be serialized, logged, queued, etc.
     *
     * @param title issue title (10-200 characters)
     * @param description detailed description (20-2000 characters)
     * @param category issue category (required)
     * @param priority issue priority (defaults to MEDIUM if null)
     * @param location physical location (required)
     * @param reportedByUserId ID of user reporting the issue (authenticated user)
     */
    record CreateIssueCommand(
            String title,
            String description,
            Category category,
            Priority priority,
            String location,
            Long reportedByUserId
    ) {
        /**
         * Compact constructor for validation and normalization.
         */
        public CreateIssueCommand {
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

            // Use default priority if not provided
            if (priority == null) {
                priority = Priority.MEDIUM;
            }
        }
    }
}
