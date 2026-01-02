package com.issuetracker.domain.model;

/**
 * User roles in the issue tracking system.
 *
 * SOLID Principle: Single Responsibility
 * - This enum has ONE job: Define valid user roles
 * - Role-specific permissions are handled in SecurityConfig
 */
public enum Role {
    /**
     * System administrator - full access to all features
     * Can manage users, view all issues, assign issues
     */
    ADMIN,

    /**
     * Municipal staff member - resolves assigned issues
     * Can view assigned issues, update status, add comments
     */
    STAFF,

    /**
     * Regular citizen - reports issues
     * Can create, view, edit, and delete own issues
     */
    CITIZEN
}
