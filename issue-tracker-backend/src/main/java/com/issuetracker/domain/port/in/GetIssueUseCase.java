package com.issuetracker.domain.port.in;

import com.issuetracker.domain.model.Category;
import com.issuetracker.domain.model.Issue;
import com.issuetracker.domain.model.IssueStatus;
import com.issuetracker.domain.model.Priority;

import java.util.List;
import java.util.Optional;

/**
 * Use case interface for retrieving issues (single or filtered list).
 *
 * DESIGN PATTERN: Query Object Pattern (Enterprise Application Architecture)
 * - IssueQuery encapsulates search criteria as an object
 * - Allows flexible filtering without changing method signature
 *
 * DESIGN PATTERN: Port/Adapter Pattern (Hexagonal Architecture)
 * - This is an INPUT PORT defined by domain layer
 * - Implementation: IssueService (application layer)
 *
 * Business Rules:
 * 1. Citizens can only see their own reported issues
 * 2. STAFF can see all issues assigned to them + all unassigned issues
 * 3. ADMIN can see all issues
 * 4. Deleted issues are not returned in queries (unless specifically requested)
 */
public interface GetIssueUseCase {

    /**
     * Retrieves a single issue by ID.
     *
     * @param issueId ID of the issue to retrieve
     * @param requestingUserId ID of user requesting the issue (for authorization)
     * @return Optional containing the issue if found and accessible, empty otherwise
     */
    Optional<Issue> getIssueById(Long issueId, Long requestingUserId);

    /**
     * Retrieves a list of issues matching the query criteria.
     * Results are filtered based on user role and authorization.
     *
     * @param query the query criteria
     * @return list of matching issues (may be empty)
     */
    List<Issue> getIssues(IssueQuery query);

    /**
     * Query object encapsulating issue search criteria.
     *
     * DESIGN PATTERN: Query Object Pattern
     * - All fields are optional (null = no filter on that field)
     * - Multiple criteria are combined with AND logic
     *
     * @param requestingUserId ID of user performing the query (required for authorization)
     * @param status filter by status (null = all statuses)
     * @param priority filter by priority (null = all priorities)
     * @param category filter by category (null = all categories)
     * @param reportedByUserId filter by reporter (null = all reporters)
     * @param assignedToUserId filter by assignee (null = all assignees, including unassigned)
     * @param includeDeleted whether to include soft-deleted issues (default: false)
     * @param page page number for pagination (0-based, null = no pagination)
     * @param size page size for pagination (null = no pagination, max 100)
     */
    record IssueQuery(
            Long requestingUserId,
            IssueStatus status,
            Priority priority,
            Category category,
            Long reportedByUserId,
            Long assignedToUserId,
            Boolean includeDeleted,
            Integer page,
            Integer size
    ) {
        /**
         * Compact constructor for validation and defaults.
         */
        public IssueQuery {
            if (requestingUserId == null) {
                throw new IllegalArgumentException("Requesting user ID cannot be null");
            }

            // Default includeDeleted to false
            if (includeDeleted == null) {
                includeDeleted = false;
            }

            // Validate pagination parameters
            if (page != null && page < 0) {
                throw new IllegalArgumentException("Page number cannot be negative");
            }
            if (size != null) {
                if (size <= 0) {
                    throw new IllegalArgumentException("Page size must be positive");
                }
                if (size > 100) {
                    throw new IllegalArgumentException("Page size cannot exceed 100");
                }
            }
        }

        /**
         * Creates a query to get all issues for a user (based on their role).
         *
         * @param userId the user ID
         * @return query for all accessible issues
         */
        public static IssueQuery allIssuesForUser(Long userId) {
            return new IssueQuery(userId, null, null, null, null, null, false, null, null);
        }

        /**
         * Creates a query to get issues reported by a specific user.
         *
         * @param userId the user ID
         * @return query for user's reported issues
         */
        public static IssueQuery reportedByUser(Long userId) {
            return new IssueQuery(userId, null, null, null, userId, null, false, null, null);
        }

        /**
         * Creates a query to get issues assigned to a specific user.
         *
         * @param userId the user ID
         * @return query for user's assigned issues
         */
        public static IssueQuery assignedToUser(Long userId) {
            return new IssueQuery(userId, null, null, null, null, userId, false, null, null);
        }

        /**
         * Creates a query to get issues with a specific status.
         *
         * @param userId the requesting user ID
         * @param status the issue status
         * @return query for issues with the given status
         */
        public static IssueQuery withStatus(Long userId, IssueStatus status) {
            return new IssueQuery(userId, status, null, null, null, null, false, null, null);
        }
    }
}
