package com.issuetracker.domain.model;

/**
 * Categories for classifying municipal issues by type.
 *
 * DESIGN PATTERN: Value Object Pattern (Domain-Driven Design)
 * - Category is an immutable enum representing a business classification
 * - Helps route issues to appropriate departments
 * - Enables filtering and reporting by category
 *
 * Benefits:
 * - Type-safe categorization (prevents typos like "INFRASTRUCUTRE")
 * - Self-documenting (clear what each category covers)
 * - Easy to add category-specific behavior (default assignee, SLA, etc.)
 * - Enables departmental reporting and workload distribution
 *
 * Business Rules:
 * 1. Category is required when creating an issue
 * 2. Category can be changed by STAFF/ADMIN
 * 3. Category determines default assignment department
 * 4. Issues are routed based on category
 */
public enum Category {

    /**
     * Infrastructure issues - roads, bridges, sidewalks, streetlights, traffic signals.
     * Assigned to: Public Works Department
     * Examples:
     * - Potholes in roads
     * - Broken streetlights
     * - Damaged sidewalks
     * - Traffic light malfunctions
     * - Fallen street signs
     * - Bridge damage
     */
    INFRASTRUCTURE("Public Works", "Infrastructure-related issues: roads, bridges, sidewalks, streetlights"),

    /**
     * Sanitation issues - trash collection, illegal dumping, graffiti, cleanliness.
     * Assigned to: Sanitation Department
     * Examples:
     * - Missed trash collection
     * - Overflowing garbage bins
     * - Illegal dumping
     * - Graffiti on public property
     * - Littered public areas
     * - Dead animal removal
     */
    SANITATION("Sanitation", "Sanitation-related issues: trash, cleanliness, graffiti"),

    /**
     * Safety issues - hazards, dangerous conditions, emergency situations.
     * Assigned to: Public Safety Department
     * Examples:
     * - Downed power lines
     * - Gas leaks
     * - Broken fire hydrants
     * - Unsafe buildings
     * - Fallen trees blocking roads
     * - Exposed manholes
     */
    SAFETY("Public Safety", "Safety-related issues: hazards, dangerous conditions"),

    /**
     * Environment issues - parks, trees, water quality, noise, air quality.
     * Assigned to: Environmental Services
     * Examples:
     * - Dead or diseased trees
     * - Water pollution
     * - Excessive noise complaints
     * - Park maintenance issues
     * - Pest control needs
     * - Air quality concerns
     */
    ENVIRONMENT("Environmental Services", "Environment-related issues: parks, trees, water, noise"),

    /**
     * Other issues - anything not fitting above categories.
     * Assigned to: General Administration
     * Examples:
     * - Unclear or miscategorized issues
     * - Multiple-category issues
     * - New issue types
     * - Administrative questions
     */
    OTHER("General Administration", "Other issues not fitting above categories");

    private final String departmentName;  // Which department handles this category
    private final String description;     // Human-readable description

    /**
     * Constructor for Category enum.
     *
     * @param departmentName the department responsible for this category
     * @param description human-readable description of what this category covers
     */
    Category(String departmentName, String description) {
        this.departmentName = departmentName;
        this.description = description;
    }

    /**
     * Get the department name responsible for this category.
     * Used for routing issues to appropriate staff members.
     *
     * @return department name
     */
    public String getDepartmentName() {
        return departmentName;
    }

    /**
     * Get the human-readable description of this category.
     *
     * @return category description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Check if this category requires immediate attention (safety-related).
     *
     * @return true if SAFETY category, false otherwise
     */
    public boolean requiresImmediateAttention() {
        return this == SAFETY;
    }

    /**
     * Get the default category for issues when category is unclear.
     *
     * @return OTHER category
     */
    public static Category getDefault() {
        return OTHER;
    }

    /**
     * Find category by department name (case-insensitive).
     *
     * @param departmentName the department name to search for
     * @return matching Category or null if not found
     */
    public static Category findByDepartment(String departmentName) {
        if (departmentName == null || departmentName.isBlank()) {
            return null;
        }

        for (Category category : values()) {
            if (category.departmentName.equalsIgnoreCase(departmentName.trim())) {
                return category;
            }
        }
        return null;
    }
}
