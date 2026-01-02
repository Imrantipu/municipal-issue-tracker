package com.issuetracker.domain.model;

/**
 * Issue status enumeration representing the lifecycle of an issue.
 *
 * DESIGN PATTERN: State Pattern (Behavioral)
 * - Each enum value represents a distinct state in the issue lifecycle
 * - State transitions are controlled by business rules (enforced in Issue.java)
 * - Valid transitions: OPEN → IN_PROGRESS → RESOLVED → CLOSED
 * - RESOLVED can transition back to OPEN (reopening), but CLOSED is final
 *
 * Benefits:
 * - Type-safe state representation (no invalid strings like "OPNE" or "open")
 * - Clear state transitions (prevents invalid workflows)
 * - Easy to add new states (just add enum value and update transition rules)
 *
 * Business Rules:
 * 1. New issues start as OPEN
 * 2. Only STAFF/ADMIN can change to IN_PROGRESS
 * 3. Only assigned staff can mark as RESOLVED
 * 4. Only ADMIN can mark as CLOSED
 * 5. CLOSED issues cannot be reopened
 * 6. RESOLVED issues can be reopened to OPEN
 */
public enum IssueStatus {

    /**
     * Initial state - issue has been reported but not yet assigned or worked on.
     * Any authenticated user can create an issue with OPEN status.
     */
    OPEN,

    /**
     * Issue is currently being worked on by assigned staff member.
     * Only STAFF/ADMIN can transition issue to this state.
     * Requires issue to be assigned to a staff member.
     */
    IN_PROGRESS,

    /**
     * Issue has been fixed/addressed and is awaiting verification.
     * Can be transitioned back to OPEN if reporter is not satisfied.
     * Only assigned staff can mark as RESOLVED.
     */
    RESOLVED,

    /**
     * Issue is permanently closed and cannot be reopened.
     * Final state in the lifecycle.
     * Only ADMIN can mark as CLOSED.
     */
    CLOSED;

    /**
     * Check if transition from this status to target status is allowed.
     *
     * Valid transitions:
     * - OPEN → IN_PROGRESS (staff starts work)
     * - IN_PROGRESS → RESOLVED (staff completes work)
     * - RESOLVED → CLOSED (admin closes)
     * - RESOLVED → OPEN (reopen if not satisfied)
     * - OPEN → CLOSED (admin closes without resolution)
     *
     * @param targetStatus the status to transition to
     * @return true if transition is allowed, false otherwise
     */
    public boolean canTransitionTo(IssueStatus targetStatus) {
        if (this == targetStatus) {
            return false; // No self-transitions
        }

        return switch (this) {
            case OPEN -> targetStatus == IN_PROGRESS || targetStatus == CLOSED;
            case IN_PROGRESS -> targetStatus == RESOLVED || targetStatus == OPEN;
            case RESOLVED -> targetStatus == CLOSED || targetStatus == OPEN;
            case CLOSED -> false; // CLOSED is final state
        };
    }

    /**
     * Check if this status represents a terminal state (cannot transition further).
     *
     * @return true if terminal state, false otherwise
     */
    public boolean isTerminal() {
        return this == CLOSED;
    }

    /**
     * Check if this status represents an active issue (still requires attention).
     *
     * @return true if active, false if resolved or closed
     */
    public boolean isActive() {
        return this == OPEN || this == IN_PROGRESS;
    }
}
