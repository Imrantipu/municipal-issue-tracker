package com.issuetracker.infrastructure.adapter.in.web;

import com.issuetracker.domain.model.Category;
import com.issuetracker.domain.model.Issue;
import com.issuetracker.domain.model.IssueStatus;
import com.issuetracker.domain.model.Priority;
import com.issuetracker.domain.port.in.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST API controller for Issue management.
 *
 * DESIGN PATTERN: Controller Pattern (MVC - Model-View-Controller)
 * - Handles HTTP requests and responses
 * - Delegates business logic to application services
 * - Converts between DTOs (Data Transfer Objects) and domain objects
 *
 * DESIGN PATTERN: DTO Pattern (Data Transfer Object)
 * - Request/Response DTOs separate API contract from domain model
 * - Prevents exposing internal domain structure to clients
 * - Allows API to evolve independently of domain
 *
 * DESIGN PATTERN: Dependency Injection Pattern
 * - @RequiredArgsConstructor generates constructor for dependency injection
 * - Injects all use case implementations (from IssueService)
 *
 * DESIGN PATTERN: Facade Pattern
 * - Controller provides simplified API facade over complex use cases
 * - Single endpoint may orchestrate multiple use cases
 *
 * REST API Endpoints:
 * - POST   /api/issues              - Create new issue
 * - GET    /api/issues              - List issues (with filters)
 * - GET    /api/issues/{id}         - Get issue details
 * - PUT    /api/issues/{id}         - Update issue
 * - PATCH  /api/issues/{id}/assign  - Assign/unassign issue
 * - PATCH  /api/issues/{id}/status  - Change status
 * - DELETE /api/issues/{id}         - Soft delete issue
 * - POST   /api/issues/{id}/restore - Restore deleted issue
 */
@RestController  // Spring MVC controller that returns JSON
@RequestMapping("/api/issues")  // Base path for all endpoints
@RequiredArgsConstructor  // DESIGN PATTERN: Dependency Injection
public class IssueController {  // DESIGN PATTERN: Controller Pattern

    // Inject all use case interfaces (implemented by IssueService)
    private final CreateIssueUseCase createIssueUseCase;
    private final UpdateIssueUseCase updateIssueUseCase;
    private final AssignIssueUseCase assignIssueUseCase;
    private final ChangeStatusUseCase changeStatusUseCase;
    private final GetIssueUseCase getIssueUseCase;
    private final DeleteIssueUseCase deleteIssueUseCase;

    // ==================== CREATE ISSUE ====================

    /**
     * POST /api/issues
     * Creates a new issue.
     *
     * Authentication: Required
     * Authorization: Any authenticated user (CITIZEN, STAFF, ADMIN)
     *
     * @param request the issue creation request
     * @param userDetails the authenticated user (injected by Spring Security)
     * @return 201 Created with issue details
     */
    @PostMapping
    public ResponseEntity<IssueResponse> createIssue(
            @RequestBody CreateIssueRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // Get user ID from authenticated user
        Long userId = getUserIdFromPrincipal(userDetails);

        // Create command
        CreateIssueUseCase.CreateIssueCommand command =
                new CreateIssueUseCase.CreateIssueCommand(
                        request.title(),
                        request.description(),
                        request.category(),
                        request.priority(),
                        request.location(),
                        userId
                );

        // Execute use case
        Issue created = createIssueUseCase.createIssue(command);

        // Convert to response DTO
        IssueResponse response = toResponse(created);

        // Return 201 Created
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ==================== GET ISSUES ====================

    /**
     * GET /api/issues
     * Lists issues with optional filtering.
     *
     * Query parameters:
     * - status: Filter by status (OPEN, IN_PROGRESS, RESOLVED, CLOSED)
     * - priority: Filter by priority (LOW, MEDIUM, HIGH, CRITICAL)
     * - category: Filter by category (INFRASTRUCTURE, SANITATION, SAFETY, ENVIRONMENT, OTHER)
     * - reportedBy: Filter by reporter user ID
     * - assignedTo: Filter by assignee user ID
     *
     * Authentication: Required
     * Authorization:
     * - CITIZEN: sees only their own reported issues
     * - STAFF: sees assigned + unassigned issues
     * - ADMIN: sees all issues
     *
     * @param status optional status filter
     * @param priority optional priority filter
     * @param category optional category filter
     * @param reportedBy optional reporter filter
     * @param assignedTo optional assignee filter
     * @param userDetails the authenticated user
     * @return 200 OK with list of issues
     */
    @GetMapping
    public ResponseEntity<List<IssueResponse>> getIssues(
            @RequestParam(required = false) IssueStatus status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) Long reportedBy,
            @RequestParam(required = false) Long assignedTo,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // Get user ID from authenticated user
        Long userId = getUserIdFromPrincipal(userDetails);

        // Create query
        GetIssueUseCase.IssueQuery query = new GetIssueUseCase.IssueQuery(
                userId,
                status,
                priority,
                category,
                reportedBy,
                assignedTo,
                false,  // Don't include deleted
                null,   // No pagination for now
                null
        );

        // Execute use case
        List<Issue> issues = getIssueUseCase.getIssues(query);

        // Convert to response DTOs
        List<IssueResponse> responses = issues.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        // Return 200 OK
        return ResponseEntity.ok(responses);
    }

    /**
     * GET /api/issues/{id}
     * Gets a single issue by ID.
     *
     * Authentication: Required
     * Authorization: Based on user role (see GetIssueUseCase)
     *
     * @param id the issue ID
     * @param userDetails the authenticated user
     * @return 200 OK with issue details, or 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<IssueResponse> getIssueById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // Get user ID from authenticated user
        Long userId = getUserIdFromPrincipal(userDetails);

        // Execute use case
        return getIssueUseCase.getIssueById(id, userId)
                .map(issue -> ResponseEntity.ok(toResponse(issue)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== UPDATE ISSUE ====================

    /**
     * PUT /api/issues/{id}
     * Updates an issue's details (title, description, priority, category, location).
     *
     * Authentication: Required
     * Authorization:
     * - CITIZEN: can only update their own issues
     * - STAFF/ADMIN: can update any issue
     *
     * @param id the issue ID
     * @param request the update request
     * @param userDetails the authenticated user
     * @return 200 OK with updated issue details
     */
    @PutMapping("/{id}")
    public ResponseEntity<IssueResponse> updateIssue(
            @PathVariable Long id,
            @RequestBody UpdateIssueRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // Get user ID from authenticated user
        Long userId = getUserIdFromPrincipal(userDetails);

        // Create command
        UpdateIssueUseCase.UpdateIssueCommand command =
                new UpdateIssueUseCase.UpdateIssueCommand(
                        id,
                        request.title(),
                        request.description(),
                        request.priority(),
                        request.category(),
                        request.location(),
                        userId
                );

        // Execute use case
        Issue updated = updateIssueUseCase.updateIssue(command);

        // Convert to response DTO
        IssueResponse response = toResponse(updated);

        // Return 200 OK
        return ResponseEntity.ok(response);
    }

    // ==================== ASSIGN ISSUE ====================

    /**
     * PATCH /api/issues/{id}/assign
     * Assigns or unassigns an issue.
     *
     * Authentication: Required
     * Authorization: Only STAFF/ADMIN can assign issues
     *
     * @param id the issue ID
     * @param request the assignment request (assigneeId = null to unassign)
     * @param userDetails the authenticated user
     * @return 200 OK with updated issue details
     */
    @PatchMapping("/{id}/assign")
    public ResponseEntity<IssueResponse> assignIssue(
            @PathVariable Long id,
            @RequestBody AssignIssueRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // Get user ID from authenticated user
        Long userId = getUserIdFromPrincipal(userDetails);

        // Create command
        AssignIssueUseCase.AssignIssueCommand command =
                new AssignIssueUseCase.AssignIssueCommand(
                        id,
                        request.assigneeId(),
                        userId
                );

        // Execute use case
        Issue assigned = assignIssueUseCase.assignIssue(command);

        // Convert to response DTO
        IssueResponse response = toResponse(assigned);

        // Return 200 OK
        return ResponseEntity.ok(response);
    }

    // ==================== CHANGE STATUS ====================

    /**
     * PATCH /api/issues/{id}/status
     * Changes the status of an issue.
     *
     * Authentication: Required
     * Authorization: Based on target status (see ChangeStatusUseCase)
     * - IN_PROGRESS: STAFF/ADMIN only
     * - RESOLVED: assigned staff or ADMIN
     * - CLOSED: ADMIN only
     *
     * @param id the issue ID
     * @param request the status change request
     * @param userDetails the authenticated user
     * @return 200 OK with updated issue details
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<IssueResponse> changeStatus(
            @PathVariable Long id,
            @RequestBody ChangeStatusRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // Get user ID from authenticated user
        Long userId = getUserIdFromPrincipal(userDetails);

        // Create command
        ChangeStatusUseCase.ChangeStatusCommand command =
                new ChangeStatusUseCase.ChangeStatusCommand(
                        id,
                        request.status(),
                        userId
                );

        // Execute use case
        Issue updated = changeStatusUseCase.changeStatus(command);

        // Convert to response DTO
        IssueResponse response = toResponse(updated);

        // Return 200 OK
        return ResponseEntity.ok(response);
    }

    // ==================== DELETE ISSUE ====================

    /**
     * DELETE /api/issues/{id}
     * Soft deletes an issue.
     *
     * Authentication: Required
     * Authorization: Only ADMIN can delete issues
     *
     * @param id the issue ID
     * @param userDetails the authenticated user
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIssue(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // Get user ID from authenticated user
        Long userId = getUserIdFromPrincipal(userDetails);

        // Create command
        DeleteIssueUseCase.DeleteIssueCommand command =
                new DeleteIssueUseCase.DeleteIssueCommand(id, userId);

        // Execute use case
        deleteIssueUseCase.deleteIssue(command);

        // Return 204 No Content
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/issues/{id}/restore
     * Restores a soft-deleted issue.
     *
     * Authentication: Required
     * Authorization: Only ADMIN can restore issues
     *
     * @param id the issue ID
     * @param userDetails the authenticated user
     * @return 200 OK with restored issue details
     */
    @PostMapping("/{id}/restore")
    public ResponseEntity<IssueResponse> restoreIssue(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // Get user ID from authenticated user
        Long userId = getUserIdFromPrincipal(userDetails);

        // Create command
        DeleteIssueUseCase.RestoreIssueCommand command =
                new DeleteIssueUseCase.RestoreIssueCommand(id, userId);

        // Execute use case
        Issue restored = deleteIssueUseCase.restoreIssue(command);

        // Convert to response DTO
        IssueResponse response = toResponse(restored);

        // Return 200 OK
        return ResponseEntity.ok(response);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Extracts user ID from Spring Security UserDetails.
     * Assumes username is the user ID (as set in JwtAuthenticationFilter).
     *
     * @param userDetails the authenticated user
     * @return the user ID
     */
    private Long getUserIdFromPrincipal(UserDetails userDetails) {
        // In our JWT implementation, the username is the user ID
        return Long.parseLong(userDetails.getUsername());
    }

    /**
     * Converts domain Issue to IssueResponse DTO.
     *
     * @param issue the domain Issue
     * @return the response DTO
     */
    private IssueResponse toResponse(Issue issue) {
        return new IssueResponse(
                issue.getId(),
                issue.getTitle(),
                issue.getDescription(),
                issue.getStatus(),
                issue.getPriority(),
                issue.getCategory(),
                issue.getLocation(),
                new UserSummary(
                        issue.getReportedBy().getId(),
                        issue.getReportedBy().getName(),
                        issue.getReportedBy().getEmail()
                ),
                issue.getAssignedTo() != null
                        ? new UserSummary(
                        issue.getAssignedTo().getId(),
                        issue.getAssignedTo().getName(),
                        issue.getAssignedTo().getEmail()
                )
                        : null,
                issue.getCreatedAt(),
                issue.getUpdatedAt(),
                issue.getResolvedAt(),
                issue.getClosedAt()
        );
    }

    // ==================== REQUEST/RESPONSE DTOs ====================

    /**
     * Request DTO for creating an issue.
     *
     * DESIGN PATTERN: DTO Pattern (Data Transfer Object)
     * - Immutable record (Java 16+ feature)
     * - Used to transfer data from client → controller
     * - Separate from domain model (Issue.java)
     */
    record CreateIssueRequest(
            String title,
            String description,
            Category category,
            Priority priority,
            String location
    ) {}

    /**
     * Request DTO for updating an issue.
     */
    record UpdateIssueRequest(
            String title,
            String description,
            Priority priority,
            Category category,
            String location
    ) {}

    /**
     * Request DTO for assigning an issue.
     */
    record AssignIssueRequest(
            Long assigneeId  // null = unassign
    ) {}

    /**
     * Request DTO for changing issue status.
     */
    record ChangeStatusRequest(
            IssueStatus status
    ) {}

    /**
     * Response DTO for issue data.
     *
     * DESIGN PATTERN: DTO Pattern
     * - Used to transfer data from controller → client
     * - Includes nested UserSummary DTOs (not full User objects)
     * - Excludes sensitive data (passwords, internal IDs)
     */
    record IssueResponse(
            Long id,
            String title,
            String description,
            IssueStatus status,
            Priority priority,
            Category category,
            String location,
            UserSummary reportedBy,
            UserSummary assignedTo,  // null if unassigned
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime resolvedAt,  // null if not resolved
            LocalDateTime closedAt     // null if not closed
    ) {}

    /**
     * Simplified user DTO for nested responses.
     * Prevents exposing full User object with sensitive data.
     */
    record UserSummary(
            Long id,
            String name,
            String email
    ) {}
}
