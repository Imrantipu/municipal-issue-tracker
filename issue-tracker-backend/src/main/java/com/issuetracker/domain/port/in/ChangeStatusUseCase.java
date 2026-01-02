package com.issuetracker.domain.port.in;

import com.issuetracker.domain.model.Issue;
import com.issuetracker.domain.model.IssueStatus;

/**
 * Use case interface for changing the status of an issue.
 *
 * DESIGN PATTERN: Command Pattern (Behavioral)
 * - ChangeStatusCommand encapsulates the status change request as an object
 *
 * DESIGN PATTERN: Port/Adapter Pattern (Hexagonal Architecture)
 * - This is an INPUT PORT defined by domain layer
 * - Implementation: IssueService (application layer)
 *
 * Business Rules:
 * 1. Status transitions must follow valid workflow (enforced by IssueStatus enum)
 *    - OPEN → IN_PROGRESS (staff starts work)
 *    - IN_PROGRESS → RESOLVED (staff completes work)
 *    - RESOLVED → CLOSED (admin closes)
 *    - RESOLVED → OPEN (reopen if not satisfied)
 *    - OPEN → CLOSED (admin closes without resolution)
 * 2. Only STAFF/ADMIN can change status to IN_PROGRESS
 * 3. Only assigned staff can mark as RESOLVED
 * 4. Only ADMIN can mark as CLOSED
 * 5. CLOSED is final state (cannot be changed)
 * 6. Cannot change status of deleted issues
 */
public interface ChangeStatusUseCase {

    /**
     * Changes the status of an issue.
     *
     * @param command the status change command
     * @return the updated issue
     * @throws IllegalArgumentException if status transition is not allowed
     * @throws IllegalStateException if issue is deleted
     * @throws SecurityException if user is not authorized for this status change
     */
    Issue changeStatus(ChangeStatusCommand command);

    /**
     * Command object encapsulating status change request.
     *
     * DESIGN PATTERN: Command Pattern
     *
     * @param issueId ID of the issue to update
     * @param newStatus the new status to set
     * @param changedByUserId ID of user performing the status change (for authorization)
     */
    record ChangeStatusCommand(
            Long issueId,
            IssueStatus newStatus,
            Long changedByUserId
    ) {
        /**
         * Compact constructor for validation.
         */
        public ChangeStatusCommand {
            if (issueId == null) {
                throw new IllegalArgumentException("Issue ID cannot be null");
            }
            if (newStatus == null) {
                throw new IllegalArgumentException("New status cannot be null");
            }
            if (changedByUserId == null) {
                throw new IllegalArgumentException("Changed by user ID cannot be null");
            }
        }
    }
}
