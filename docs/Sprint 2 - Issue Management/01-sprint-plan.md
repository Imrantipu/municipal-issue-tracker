# Sprint 2 - Detailed Sprint Plan

**Sprint:** 2 of 7
**Focus:** Issue Management Backend
**Duration:** 1-2 weeks
**Target Coverage:** 85%+

---

## Sprint Backlog

### Domain Layer (Day 1-2)

#### Issue Domain Model
- [ ] **Issue.java** - Core domain entity
  - Fields: id, title, description, status, priority, category, location, reportedBy, assignedTo, createdAt, updatedAt, resolvedAt, closedAt, deletedAt
  - Validation: title (10-200), description (20-2000), required fields
  - Business logic: status transitions, assignment validation

- [ ] **IssueStatus.java** - Enum
  - Values: OPEN, IN_PROGRESS, RESOLVED, CLOSED

- [ ] **Priority.java** - Enum
  - Values: LOW, MEDIUM, HIGH, CRITICAL

- [ ] **Category.java** - Enum
  - Values: INFRASTRUCTURE, SANITATION, SAFETY, ENVIRONMENT, OTHER

#### Use Case Interfaces
- [ ] **CreateIssueUseCase.java** - Create new issue
- [ ] **UpdateIssueUseCase.java** - Update issue details
- [ ] **AssignIssueUseCase.java** - Assign to staff
- [ ] **ChangeStatusUseCase.java** - Update status
- [ ] **GetIssueUseCase.java** - Retrieve issue(s)
- [ ] **DeleteIssueUseCase.java** - Soft delete

#### Repository Interface
- [ ] **IssueRepository.java** - Output port
  - Methods: save, findById, findAll, findByReporter, findByAssignee, findByStatus, delete

---

### Application Layer (Day 3-4)

#### Service Implementation
- [ ] **IssueService.java**
  - Implements all use cases
  - Business rules enforcement
  - Role-based authorization
  - Transaction management

**Business Rules to Implement:**
1. Only authenticated users can create issues
2. Citizens can only edit their own issues
3. Staff/Admin can edit all issues
4. Only Staff/Admin can assign issues
5. Only Staff/Admin can change status to IN_PROGRESS
6. Status transitions: OPEN → IN_PROGRESS → RESOLVED → CLOSED
7. Cannot skip status steps
8. Resolved issues can be reopened
9. Closed issues cannot be reopened

---

### Infrastructure Layer (Day 5-6)

#### Persistence Adapter
- [ ] **IssueEntity.java** - JPA entity
  - Mappings, relationships, indexes

- [ ] **SpringDataIssueRepository.java** - Spring Data JPA
  - Query methods

- [ ] **JpaIssueRepositoryAdapter.java** - Adapter
  - Domain ↔ Entity conversion

#### Web Adapter
- [ ] **IssueController.java** - REST API
  - POST /api/issues - Create issue
  - GET /api/issues - List issues (with filters)
  - GET /api/issues/{id} - Get issue details
  - PUT /api/issues/{id} - Update issue
  - PATCH /api/issues/{id}/assign - Assign issue
  - PATCH /api/issues/{id}/status - Change status
  - DELETE /api/issues/{id} - Delete issue

- [ ] **Request/Response DTOs**
  - CreateIssueRequest
  - UpdateIssueRequest
  - AssignIssueRequest
  - IssueResponse
  - IssueListResponse

---

### Testing (Throughout Sprint)

#### Unit Tests (Domain)
- [ ] **IssueTest.java** (50+ tests)
  - Title validation (5 tests)
  - Description validation (5 tests)
  - Status validation (5 tests)
  - Priority validation (3 tests)
  - Category validation (3 tests)
  - Status transition logic (10 tests)
  - Assignment validation (5 tests)
  - Business logic methods (15+ tests)

#### Service Tests
- [ ] **IssueServiceTest.java** (40+ tests)
  - Create issue (10 tests)
  - Update issue (8 tests)
  - Assign issue (8 tests)
  - Change status (8 tests)
  - Authorization checks (10 tests)
  - Exception handling (5 tests)

#### Integration Tests
- [ ] **IssueControllerIntegrationTest.java** (30+ tests)
  - POST /api/issues (8 tests)
  - GET /api/issues (6 tests)
  - GET /api/issues/{id} (4 tests)
  - PUT /api/issues/{id} (6 tests)
  - PATCH endpoints (4 tests)
  - DELETE /api/issues/{id} (2 tests)

#### Repository Tests
- [ ] **JpaIssueRepositoryAdapterTest.java** (20+ tests)
  - CRUD operations
  - Query methods
  - Filtering
  - Sorting

**Total Tests Target:** 140+ tests

---

## Database Schema

### issues Table

```sql
CREATE TABLE issues (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    category VARCHAR(50) NOT NULL,
    location VARCHAR(500) NOT NULL,

    reported_by_id BIGINT NOT NULL,
    assigned_to_id BIGINT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP,
    closed_at TIMESTAMP,
    deleted_at TIMESTAMP,

    FOREIGN KEY (reported_by_id) REFERENCES users(id),
    FOREIGN KEY (assigned_to_id) REFERENCES users(id)
);

CREATE INDEX idx_issues_status ON issues(status);
CREATE INDEX idx_issues_reporter ON issues(reported_by_id);
CREATE INDEX idx_issues_assignee ON issues(assigned_to_id);
CREATE INDEX idx_issues_created_at ON issues(created_at DESC);
```

---

## API Endpoints

### POST /api/issues
**Create new issue**

Request:
```json
{
  "title": "Broken streetlight on Main St",
  "description": "The streetlight at 123 Main St has been out for 3 days...",
  "category": "INFRASTRUCTURE",
  "priority": "MEDIUM",
  "location": "123 Main St, City Center"
}
```

Response: 201 Created
```json
{
  "id": 1,
  "title": "Broken streetlight on Main St",
  "description": "...",
  "status": "OPEN",
  "priority": "MEDIUM",
  "category": "INFRASTRUCTURE",
  "location": "123 Main St, City Center",
  "reportedBy": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com"
  },
  "createdAt": "2026-01-02T10:00:00"
}
```

### GET /api/issues
**List issues with filters**

Query params:
- status: OPEN | IN_PROGRESS | RESOLVED | CLOSED
- priority: LOW | MEDIUM | HIGH | CRITICAL
- category: INFRASTRUCTURE | SANITATION | ...
- reportedBy: userId
- assignedTo: userId
- page: 0
- size: 20
- sort: createdAt,desc

Response: 200 OK
```json
{
  "content": [ /* array of issues */ ],
  "page": 0,
  "size": 20,
  "totalElements": 45,
  "totalPages": 3
}
```

### GET /api/issues/{id}
**Get issue details**

Response: 200 OK
```json
{
  "id": 1,
  "title": "...",
  "description": "...",
  "status": "OPEN",
  "reportedBy": { /* user */ },
  "assignedTo": { /* user or null */ },
  "createdAt": "...",
  "updatedAt": "..."
}
```

### PUT /api/issues/{id}
**Update issue details**

Request:
```json
{
  "title": "Updated title",
  "description": "Updated description",
  "priority": "HIGH",
  "category": "SAFETY"
}
```

Response: 200 OK

### PATCH /api/issues/{id}/assign
**Assign issue to staff**

Request:
```json
{
  "assigneeId": 5
}
```

Response: 200 OK

### PATCH /api/issues/{id}/status
**Change issue status**

Request:
```json
{
  "status": "IN_PROGRESS"
}
```

Response: 200 OK

### DELETE /api/issues/{id}
**Soft delete issue**

Response: 204 No Content

---

## Design Patterns (To Document)

Expected patterns in Sprint 2:

1. **Repository Pattern** - IssueRepository
2. **Adapter Pattern** - JpaIssueRepositoryAdapter
3. **Command Pattern** - Use case commands
4. **DTO Pattern** - Request/Response objects
5. **Builder Pattern** - Issue.builder()
6. **Strategy Pattern** - Status transition strategies
7. **Facade Pattern** - IssueService
8. **Dependency Injection** - Constructor injection
9. **Singleton Pattern** - Spring beans

---

## Test Coverage Goals

| Layer | Target | Sprint 1 Actual |
|-------|--------|-----------------|
| Domain | 100% | 100% ✅ |
| Application | 95% | 100% ✅ |
| Infrastructure | 80% | 0% ⚠️ |
| **Overall** | **85%** | **53%** |

**Sprint 2 Goal:** Achieve 85%+ overall coverage including infrastructure

---

## Risk Management

| Risk | Mitigation |
|------|------------|
| Complex status transitions | Create state machine, document all transitions |
| Authorization logic complexity | Use Spring Security @PreAuthorize |
| N+1 query problems | Use @EntityGraph or JOIN FETCH |
| TestContainers issues | Use H2 as fallback |
| Time constraints | Focus on core features first |

---

## Definition of Done

- [ ] All features implemented
- [ ] All tests passing
- [ ] 85%+ code coverage
- [ ] API documented
- [ ] Design patterns documented
- [ ] Code reviewed
- [ ] Integration tests working
- [ ] Manual testing complete

---

**Start Date:** January 2, 2026
**Target End Date:** January 14, 2026 (2 weeks)
