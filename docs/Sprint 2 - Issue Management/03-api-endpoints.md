# Sprint 2 - API Endpoints Documentation

**Version:** 1.0
**Last Updated:** January 2, 2026
**Base URL:** `http://localhost:8080/api/issues`
**Authentication:** JWT Bearer Token (required for all endpoints)

---

## Table of Contents

1. [Authentication](#authentication)
2. [Create Issue](#1-create-issue)
3. [List Issues](#2-list-issues)
4. [Get Issue by ID](#3-get-issue-by-id)
5. [Update Issue](#4-update-issue)
6. [Assign Issue](#5-assign-issue)
7. [Change Issue Status](#6-change-issue-status)
8. [Delete Issue](#7-delete-issue)
9. [Restore Issue](#8-restore-issue)
10. [Error Responses](#error-responses)
11. [Status Codes](#status-codes)

---

## Authentication

All endpoints require JWT authentication via Bearer token.

**Header:**
```
Authorization: Bearer <JWT_TOKEN>
```

**How to get token:**
1. Login via `/api/auth/login` (Sprint 1)
2. Extract `token` from response
3. Include in all subsequent requests

---

## 1. Create Issue

**Endpoint:** `POST /api/issues`

**Description:** Creates a new issue. Any authenticated user can create an issue.

**Authorization:**
- ✅ CITIZEN
- ✅ STAFF
- ✅ ADMIN

### Request

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>
```

**Body:**
```json
{
  "title": "Broken streetlight on Main St",
  "description": "The streetlight at 123 Main St has been out for 3 days, creating safety concerns for pedestrians.",
  "category": "INFRASTRUCTURE",
  "priority": "MEDIUM",
  "location": "123 Main St, City Center"
}
```

**Field Validation:**
| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| title | String | ✅ Yes | 10-200 characters |
| description | String | ✅ Yes | 20-2000 characters |
| category | Enum | ✅ Yes | INFRASTRUCTURE, SANITATION, SAFETY, ENVIRONMENT, OTHER |
| priority | Enum | ❌ No | LOW, MEDIUM, HIGH, CRITICAL (defaults to MEDIUM) |
| location | String | ✅ Yes | Max 500 characters |

### Response

**Status Code:** `201 Created`

**Body:**
```json
{
  "id": 1,
  "title": "Broken streetlight on Main St",
  "description": "The streetlight at 123 Main St has been out for 3 days, creating safety concerns for pedestrians.",
  "status": "OPEN",
  "priority": "MEDIUM",
  "category": "INFRASTRUCTURE",
  "location": "123 Main St, City Center",
  "reportedBy": {
    "id": 10,
    "name": "John Doe",
    "email": "john@example.com"
  },
  "assignedTo": null,
  "createdAt": "2026-01-02T10:30:00",
  "updatedAt": "2026-01-02T10:30:00",
  "resolvedAt": null,
  "closedAt": null
}
```

### Example cURL

```bash
curl -X POST http://localhost:8080/api/issues \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "title": "Broken streetlight on Main St",
    "description": "The streetlight at 123 Main St has been out for 3 days, creating safety concerns for pedestrians.",
    "category": "INFRASTRUCTURE",
    "priority": "MEDIUM",
    "location": "123 Main St, City Center"
  }'
```

### Error Scenarios

| Error | Status | Condition |
|-------|--------|-----------|
| Title too short | 400 | title < 10 characters |
| Description too short | 400 | description < 20 characters |
| Invalid category | 400 | category not in enum |
| Missing location | 400 | location is null/empty |
| Unauthorized | 401 | Missing/invalid JWT token |

---

## 2. List Issues

**Endpoint:** `GET /api/issues`

**Description:** Lists issues with optional filtering. Results are filtered based on user role.

**Authorization:**
- **CITIZEN:** Sees only their own reported issues
- **STAFF:** Sees assigned + unassigned issues
- **ADMIN:** Sees all issues

### Request

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Query Parameters (all optional):**
| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| status | Enum | Filter by status | `?status=OPEN` |
| priority | Enum | Filter by priority | `?priority=HIGH` |
| category | Enum | Filter by category | `?category=INFRASTRUCTURE` |
| reportedBy | Long | Filter by reporter ID | `?reportedBy=10` |
| assignedTo | Long | Filter by assignee ID | `?assignedTo=5` |

**Example URLs:**
```
GET /api/issues                              # All accessible issues
GET /api/issues?status=OPEN                  # Only OPEN issues
GET /api/issues?priority=CRITICAL            # Only CRITICAL priority
GET /api/issues?category=SAFETY              # Only SAFETY category
GET /api/issues?status=OPEN&priority=HIGH    # OPEN + HIGH priority
GET /api/issues?reportedBy=10                # Issues reported by user 10
GET /api/issues?assignedTo=5                 # Issues assigned to user 5
```

### Response

**Status Code:** `200 OK`

**Body:**
```json
[
  {
    "id": 1,
    "title": "Broken streetlight on Main St",
    "description": "The streetlight at 123 Main St...",
    "status": "OPEN",
    "priority": "MEDIUM",
    "category": "INFRASTRUCTURE",
    "location": "123 Main St, City Center",
    "reportedBy": {
      "id": 10,
      "name": "John Doe",
      "email": "john@example.com"
    },
    "assignedTo": null,
    "createdAt": "2026-01-02T10:30:00",
    "updatedAt": "2026-01-02T10:30:00",
    "resolvedAt": null,
    "closedAt": null
  },
  {
    "id": 2,
    "title": "Overflowing trash bin at Park Ave",
    "description": "Trash bin at Park Ave is overflowing...",
    "status": "IN_PROGRESS",
    "priority": "LOW",
    "category": "SANITATION",
    "location": "Park Ave, Near Fountain",
    "reportedBy": {
      "id": 11,
      "name": "Jane Smith",
      "email": "jane@example.com"
    },
    "assignedTo": {
      "id": 5,
      "name": "Staff Member",
      "email": "staff@example.com"
    },
    "createdAt": "2026-01-01T14:20:00",
    "updatedAt": "2026-01-02T09:00:00",
    "resolvedAt": null,
    "closedAt": null
  }
]
```

**Empty Result:**
```json
[]
```

### Example cURL

```bash
# Get all accessible issues
curl -X GET http://localhost:8080/api/issues \
  -H "Authorization: Bearer <JWT_TOKEN>"

# Get only CRITICAL priority issues
curl -X GET "http://localhost:8080/api/issues?priority=CRITICAL" \
  -H "Authorization: Bearer <JWT_TOKEN>"

# Get OPEN issues in SAFETY category
curl -X GET "http://localhost:8080/api/issues?status=OPEN&category=SAFETY" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

---

## 3. Get Issue by ID

**Endpoint:** `GET /api/issues/{id}`

**Description:** Retrieves a single issue by ID. Authorization applies based on user role.

**Authorization:**
- **CITIZEN:** Can view only their own issues
- **STAFF:** Can view assigned + unassigned issues
- **ADMIN:** Can view all issues

### Request

**Path Parameters:**
- `id` (Long) - Issue ID

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

### Response

**Status Code:** `200 OK`

**Body:**
```json
{
  "id": 1,
  "title": "Broken streetlight on Main St",
  "description": "The streetlight at 123 Main St has been out for 3 days, creating safety concerns for pedestrians.",
  "status": "OPEN",
  "priority": "MEDIUM",
  "category": "INFRASTRUCTURE",
  "location": "123 Main St, City Center",
  "reportedBy": {
    "id": 10,
    "name": "John Doe",
    "email": "john@example.com"
  },
  "assignedTo": null,
  "createdAt": "2026-01-02T10:30:00",
  "updatedAt": "2026-01-02T10:30:00",
  "resolvedAt": null,
  "closedAt": null
}
```

### Example cURL

```bash
curl -X GET http://localhost:8080/api/issues/1 \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### Error Scenarios

| Error | Status | Condition |
|-------|--------|-----------|
| Not Found | 404 | Issue doesn't exist OR user not authorized |
| Unauthorized | 401 | Missing/invalid JWT token |

---

## 4. Update Issue

**Endpoint:** `PUT /api/issues/{id}`

**Description:** Updates issue details (title, description, priority, category, location). Does NOT change status or assignment.

**Authorization:**
- **CITIZEN:** Can update only their own issues
- **STAFF:** Can update any issue
- **ADMIN:** Can update any issue

**Business Rules:**
- ❌ Cannot update CLOSED issues
- ❌ Cannot update DELETED issues

### Request

**Path Parameters:**
- `id` (Long) - Issue ID

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>
```

**Body (all fields optional):**
```json
{
  "title": "Updated: Broken streetlight on Main St",
  "description": "Updated description with more details about the streetlight issue. It has been out for 5 days now.",
  "priority": "HIGH",
  "category": "SAFETY",
  "location": "123 Main St, City Center (Near Post Office)"
}
```

**Partial Update Supported:**
```json
{
  "priority": "CRITICAL"
}
```

### Response

**Status Code:** `200 OK`

**Body:**
```json
{
  "id": 1,
  "title": "Updated: Broken streetlight on Main St",
  "description": "Updated description with more details...",
  "status": "OPEN",
  "priority": "HIGH",
  "category": "SAFETY",
  "location": "123 Main St, City Center (Near Post Office)",
  "reportedBy": {
    "id": 10,
    "name": "John Doe",
    "email": "john@example.com"
  },
  "assignedTo": null,
  "createdAt": "2026-01-02T10:30:00",
  "updatedAt": "2026-01-02T11:45:00",
  "resolvedAt": null,
  "closedAt": null
}
```

### Example cURL

```bash
curl -X PUT http://localhost:8080/api/issues/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -d '{
    "priority": "HIGH",
    "category": "SAFETY"
  }'
```

### Error Scenarios

| Error | Status | Condition |
|-------|--------|-----------|
| Unauthorized | 403 | CITIZEN trying to update others' issue |
| Cannot update closed | 400 | Issue status is CLOSED |
| Cannot update deleted | 400 | Issue is soft-deleted |
| Not Found | 404 | Issue doesn't exist |
| Validation error | 400 | Title/description doesn't meet constraints |

---

## 5. Assign Issue

**Endpoint:** `PATCH /api/issues/{id}/assign`

**Description:** Assigns issue to a staff member or unassigns if assigneeId is null.

**Authorization:**
- **CITIZEN:** ❌ Cannot assign
- **STAFF:** ✅ Can assign
- **ADMIN:** ✅ Can assign

**Business Rules:**
- ✅ Can only assign to STAFF or ADMIN users (not CITIZEN)
- ❌ Cannot assign CLOSED issues
- ❌ Cannot assign DELETED issues

### Request

**Path Parameters:**
- `id` (Long) - Issue ID

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>
```

**Body:**
```json
{
  "assigneeId": 5
}
```

**Unassign (set to null):**
```json
{
  "assigneeId": null
}
```

### Response

**Status Code:** `200 OK`

**Body:**
```json
{
  "id": 1,
  "title": "Broken streetlight on Main St",
  "description": "The streetlight at 123 Main St...",
  "status": "OPEN",
  "priority": "MEDIUM",
  "category": "INFRASTRUCTURE",
  "location": "123 Main St, City Center",
  "reportedBy": {
    "id": 10,
    "name": "John Doe",
    "email": "john@example.com"
  },
  "assignedTo": {
    "id": 5,
    "name": "Staff Member",
    "email": "staff@example.com"
  },
  "createdAt": "2026-01-02T10:30:00",
  "updatedAt": "2026-01-02T12:00:00",
  "resolvedAt": null,
  "closedAt": null
}
```

### Example cURL

```bash
# Assign to user 5
curl -X PATCH http://localhost:8080/api/issues/1/assign \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -d '{"assigneeId": 5}'

# Unassign
curl -X PATCH http://localhost:8080/api/issues/1/assign \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -d '{"assigneeId": null}'
```

### Error Scenarios

| Error | Status | Condition |
|-------|--------|-----------|
| Forbidden | 403 | CITIZEN attempting to assign |
| Cannot assign to CITIZEN | 400 | Trying to assign to CITIZEN user |
| Cannot assign closed issue | 400 | Issue is CLOSED |
| User not found | 404 | assigneeId doesn't exist |

---

## 6. Change Issue Status

**Endpoint:** `PATCH /api/issues/{id}/status`

**Description:** Changes the status of an issue following valid state transitions.

**Authorization (depends on target status):**

| Target Status | Who Can Change |
|---------------|----------------|
| OPEN | Anyone (if valid transition) |
| IN_PROGRESS | STAFF, ADMIN |
| RESOLVED | Assigned STAFF, ADMIN |
| CLOSED | ADMIN only |

**Valid Status Transitions:**
```
OPEN → IN_PROGRESS → RESOLVED → CLOSED
  ↓                      ↓
  └──────────────────────┘ (Reopen)

OPEN → CLOSED (Admin only, skip resolution)
```

### Request

**Path Parameters:**
- `id` (Long) - Issue ID

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>
```

**Body:**
```json
{
  "status": "IN_PROGRESS"
}
```

### Response

**Status Code:** `200 OK`

**Body:**
```json
{
  "id": 1,
  "title": "Broken streetlight on Main St",
  "description": "The streetlight at 123 Main St...",
  "status": "IN_PROGRESS",
  "priority": "MEDIUM",
  "category": "INFRASTRUCTURE",
  "location": "123 Main St, City Center",
  "reportedBy": {
    "id": 10,
    "name": "John Doe",
    "email": "john@example.com"
  },
  "assignedTo": {
    "id": 5,
    "name": "Staff Member",
    "email": "staff@example.com"
  },
  "createdAt": "2026-01-02T10:30:00",
  "updatedAt": "2026-01-02T12:30:00",
  "resolvedAt": null,
  "closedAt": null
}
```

**When status becomes RESOLVED:**
```json
{
  ...
  "status": "RESOLVED",
  "resolvedAt": "2026-01-02T14:00:00",  // Automatically set
  ...
}
```

**When status becomes CLOSED:**
```json
{
  ...
  "status": "CLOSED",
  "closedAt": "2026-01-02T15:00:00",  // Automatically set
  ...
}
```

### Example cURL

```bash
# Change to IN_PROGRESS
curl -X PATCH http://localhost:8080/api/issues/1/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -d '{"status": "IN_PROGRESS"}'

# Mark as RESOLVED
curl -X PATCH http://localhost:8080/api/issues/1/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -d '{"status": "RESOLVED"}'
```

### Error Scenarios

| Error | Status | Condition |
|-------|--------|-----------|
| Invalid transition | 400 | e.g., OPEN → RESOLVED (must go through IN_PROGRESS) |
| Unauthorized | 403 | e.g., CITIZEN trying to mark IN_PROGRESS |
| Not assigned | 403 | STAFF trying to mark RESOLVED when not assigned |
| Only ADMIN | 403 | Non-admin trying to CLOSE |
| Cannot change deleted | 400 | Issue is soft-deleted |

---

## 7. Delete Issue

**Endpoint:** `DELETE /api/issues/{id}`

**Description:** Soft deletes an issue (sets deletedAt timestamp). Does not physically delete from database.

**Authorization:**
- **CITIZEN:** ❌ Cannot delete
- **STAFF:** ❌ Cannot delete
- **ADMIN:** ✅ Can delete

**Business Rule:** Soft delete for GDPR compliance and audit trail.

### Request

**Path Parameters:**
- `id` (Long) - Issue ID

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

### Response

**Status Code:** `204 No Content`

**Body:** Empty

### Example cURL

```bash
curl -X DELETE http://localhost:8080/api/issues/1 \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### Error Scenarios

| Error | Status | Condition |
|-------|--------|-----------|
| Forbidden | 403 | Non-admin attempting to delete |
| Not Found | 404 | Issue doesn't exist |
| Already deleted | 400 | Issue is already soft-deleted |

---

## 8. Restore Issue

**Endpoint:** `POST /api/issues/{id}/restore`

**Description:** Restores a soft-deleted issue (sets deletedAt to null).

**Authorization:**
- **CITIZEN:** ❌ Cannot restore
- **STAFF:** ❌ Cannot restore
- **ADMIN:** ✅ Can restore

### Request

**Path Parameters:**
- `id` (Long) - Issue ID

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

### Response

**Status Code:** `200 OK`

**Body:**
```json
{
  "id": 1,
  "title": "Broken streetlight on Main St",
  "description": "The streetlight at 123 Main St...",
  "status": "OPEN",
  "priority": "MEDIUM",
  "category": "INFRASTRUCTURE",
  "location": "123 Main St, City Center",
  "reportedBy": {
    "id": 10,
    "name": "John Doe",
    "email": "john@example.com"
  },
  "assignedTo": null,
  "createdAt": "2026-01-02T10:30:00",
  "updatedAt": "2026-01-02T16:00:00",
  "resolvedAt": null,
  "closedAt": null
}
```

### Example cURL

```bash
curl -X POST http://localhost:8080/api/issues/1/restore \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### Error Scenarios

| Error | Status | Condition |
|-------|--------|-----------|
| Forbidden | 403 | Non-admin attempting to restore |
| Not Found | 404 | Issue doesn't exist |
| Not deleted | 400 | Issue is not deleted |

---

## Error Responses

### Standard Error Format

All errors follow consistent JSON structure:

```json
{
  "timestamp": "2026-01-02T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Title must be at least 10 characters long",
  "path": "/api/issues"
}
```

### Common Error Messages

| HTTP Status | Error Type | Example Message |
|-------------|------------|-----------------|
| 400 Bad Request | Validation Error | "Title must be at least 10 characters long" |
| 401 Unauthorized | Auth Error | "JWT token is invalid or expired" |
| 403 Forbidden | Authorization Error | "You are not authorized to update this issue" |
| 404 Not Found | Resource Error | "Issue with ID 999 not found" |
| 500 Internal Server Error | Server Error | "An unexpected error occurred" |

---

## Status Codes

| Code | Meaning | When Used |
|------|---------|-----------|
| 200 OK | Success | GET, PUT, PATCH operations successful |
| 201 Created | Resource Created | POST creates new issue |
| 204 No Content | Success (no body) | DELETE successful |
| 400 Bad Request | Validation Failed | Invalid input data |
| 401 Unauthorized | Not Authenticated | Missing/invalid JWT |
| 403 Forbidden | Not Authorized | User lacks permission |
| 404 Not Found | Resource Missing | Issue doesn't exist |
| 500 Internal Server Error | Server Error | Unexpected server error |

---

## Testing the API

### Postman Collection

Import this collection into Postman for testing:

1. Set environment variable: `baseUrl = http://localhost:8080`
2. Set environment variable: `token = <your_jwt_token>`
3. Use `{{baseUrl}}` and `{{token}}` in requests

### Example Workflow

```bash
# 1. Login (Sprint 1)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@test.com", "password": "password123"}'

# Extract token from response: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

# 2. Create Issue
curl -X POST http://localhost:8080/api/issues \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "title": "Test issue with 10+ chars",
    "description": "Test description with at least 20 characters.",
    "category": "INFRASTRUCTURE",
    "priority": "HIGH",
    "location": "Test location"
  }'

# 3. List Issues
curl -X GET http://localhost:8080/api/issues \
  -H "Authorization: Bearer <TOKEN>"

# 4. Assign Issue (as STAFF/ADMIN)
curl -X PATCH http://localhost:8080/api/issues/1/assign \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{"assigneeId": 5}'

# 5. Change Status
curl -X PATCH http://localhost:8080/api/issues/1/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{"status": "IN_PROGRESS"}'
```

---

**Document Version:** 1.0
**Last Updated:** January 2, 2026
**Maintained By:** Development Team
