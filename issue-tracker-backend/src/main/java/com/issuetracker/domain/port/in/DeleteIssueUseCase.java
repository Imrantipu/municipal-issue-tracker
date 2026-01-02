package com.issuetracker.domain.port.in;

import com.issuetracker.domain.model.Issue;

/**
 * Use case interface for deleting (soft delete) an issue.
 *
 * DESIGN PATTERN: Command Pattern (Behavioral)
 * - DeleteIssueCommand encapsulates the delete request as an object
 *
 * DESIGN PATTERN: Port/Adapter Pattern (Hexagonal Architecture)
 * - This is an INPUT PORT defined by domain layer
 * - Implementation: IssueService (application layer)
 *
 * Business Rules:
 * 1. Only ADMIN can delete issues (GDPR compliance - data controller role)
 * 2. Soft delete (set deletedAt timestamp, not physical deletion)
 * 3. Soft delete preserves audit trail and allows recovery
 * 4. Citizens can request deletion of their own issues (but ADMIN must approve)
 * 5. Deleted issues are hidden from queries (unless specifically requested)
 * 6. Issues can be restored by setting deletedAt to null
 */
public interface DeleteIssueUseCase {

    /**
     * Soft deletes an issue (sets deletedAt timestamp).
     *
     * @param command the delete command
     * @return the deleted issue
     * @throws IllegalArgumentException if issue not found
     * @throws IllegalStateException if issue is already deleted
     * @throws SecurityException if user is not authorized (not ADMIN)
     */
    Issue deleteIssue(DeleteIssueCommand command);

    /**
     * Restores a soft-deleted issue (sets deletedAt to null).
     *
     * @param command the restore command
     * @return the restored issue
     * @throws IllegalArgumentException if issue not found
     * @throws IllegalStateException if issue is not deleted
     * @throws SecurityException if user is not authorized (not ADMIN)
     */
    Issue restoreIssue(RestoreIssueCommand command);

    /**
     * Command object encapsulating issue delete request.
     *
     * DESIGN PATTERN: Command Pattern
     *
     * @param issueId ID of the issue to delete
     * @param deletedByUserId ID of user performing the deletion (must be ADMIN)
     */
    record DeleteIssueCommand(
            Long issueId,
            Long deletedByUserId
    ) {
        /**
         * Compact constructor for validation.
         */
        public DeleteIssueCommand {
            if (issueId == null) {
                throw new IllegalArgumentException("Issue ID cannot be null");
            }
            if (deletedByUserId == null) {
                throw new IllegalArgumentException("Deleted by user ID cannot be null");
            }
        }
    }

    /**
     * Command object encapsulating issue restore request.
     *
     * DESIGN PATTERN: Command Pattern
     *
     * @param issueId ID of the issue to restore
     * @param restoredByUserId ID of user performing the restoration (must be ADMIN)
     */
    record RestoreIssueCommand(
            Long issueId,
            Long restoredByUserId
    ) {
        /**
         * Compact constructor for validation.
         */
        public RestoreIssueCommand {
            if (issueId == null) {
                throw new IllegalArgumentException("Issue ID cannot be null");
            }
            if (restoredByUserId == null) {
                throw new IllegalArgumentException("Restored by user ID cannot be null");
            }
        }
    }
}
