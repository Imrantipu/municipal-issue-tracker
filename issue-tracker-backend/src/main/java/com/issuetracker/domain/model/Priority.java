package com.issuetracker.domain.model;

/**
 * Priority levels for issues, indicating urgency and importance.
 *
 * DESIGN PATTERN: Value Object Pattern (Domain-Driven Design)
 * - Priority is an immutable enum representing a business concept
 * - Encapsulates priority-related business logic (SLA days, ordering)
 * - Used as part of the Issue aggregate
 *
 * Benefits:
 * - Type-safe priority representation
 * - Self-documenting code (CRITICAL is clearer than integer 1)
 * - Easy to add priority-specific behavior (SLA days, color coding, etc.)
 *
 * Business Rules:
 * 1. Default priority is MEDIUM for new issues
 * 2. CRITICAL issues must be assigned within 1 hour
 * 3. HIGH priority issues must be assigned within 4 hours
 * 4. MEDIUM priority issues must be assigned within 24 hours
 * 5. LOW priority issues must be assigned within 72 hours
 * 6. Only STAFF/ADMIN can change priority
 */
public enum Priority {

    /**
     * Low priority - minor issues, cosmetic problems, nice-to-have improvements.
     * SLA: 72 hours for assignment, 7 days for resolution.
     * Examples: Faded street sign, minor pothole, trash can needs painting.
     */
    LOW(4, 72, 168),

    /**
     * Medium priority - standard issues requiring attention.
     * SLA: 24 hours for assignment, 3 days for resolution.
     * Default priority for new issues.
     * Examples: Broken streetlight, graffiti, sidewalk crack.
     */
    MEDIUM(3, 24, 72),

    /**
     * High priority - important issues affecting public services.
     * SLA: 4 hours for assignment, 24 hours for resolution.
     * Examples: Traffic light malfunction, fallen tree blocking road, water leak.
     */
    HIGH(2, 4, 24),

    /**
     * Critical priority - urgent issues posing safety risks or major disruptions.
     * SLA: 1 hour for assignment, 4 hours for resolution.
     * Examples: Gas leak, downed power line, major road hazard, bridge damage.
     */
    CRITICAL(1, 1, 4);

    private final int level;  // Numeric representation (1 = highest priority)
    private final int assignmentSlaHours;  // Hours until issue must be assigned
    private final int resolutionSlaHours;  // Hours until issue must be resolved

    /**
     * Constructor for Priority enum.
     *
     * @param level numeric priority (1 = highest, 4 = lowest)
     * @param assignmentSlaHours hours until issue must be assigned
     * @param resolutionSlaHours hours until issue must be resolved
     */
    Priority(int level, int assignmentSlaHours, int resolutionSlaHours) {
        this.level = level;
        this.assignmentSlaHours = assignmentSlaHours;
        this.resolutionSlaHours = resolutionSlaHours;
    }

    /**
     * Get the numeric priority level (1 = highest, 4 = lowest).
     * Useful for sorting and comparison.
     *
     * @return priority level as integer
     */
    public int getLevel() {
        return level;
    }

    /**
     * Get the SLA (Service Level Agreement) hours for assignment.
     * Issues must be assigned to staff within this timeframe.
     *
     * @return assignment SLA in hours
     */
    public int getAssignmentSlaHours() {
        return assignmentSlaHours;
    }

    /**
     * Get the SLA (Service Level Agreement) hours for resolution.
     * Issues must be resolved within this timeframe after assignment.
     *
     * @return resolution SLA in hours
     */
    public int getResolutionSlaHours() {
        return resolutionSlaHours;
    }

    /**
     * Check if this priority is higher than another priority.
     *
     * @param other the priority to compare with
     * @return true if this priority is higher (more urgent)
     */
    public boolean isHigherThan(Priority other) {
        return this.level < other.level;
    }

    /**
     * Check if this priority is critical level.
     *
     * @return true if CRITICAL, false otherwise
     */
    public boolean isCritical() {
        return this == CRITICAL;
    }

    /**
     * Get the default priority for new issues.
     *
     * @return MEDIUM priority
     */
    public static Priority getDefault() {
        return MEDIUM;
    }
}
