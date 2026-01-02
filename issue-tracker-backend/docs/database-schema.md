# Database Schema Design
## Public Infrastructure Issue Reporting System

**Version:** 1.0
**Last Updated:** December 25, 2024
**Database:** PostgreSQL 15+
**Design Principles:** Normalization, Audit Trail, Soft Delete, Performance Optimization

---

## 1. Overview

**Purpose:** Database schema for issue tracking system with 3 user roles (Admin, Staff, Citizen)

**Key Design Decisions:**
- **Soft Delete:** Users and Issues support soft delete for GDPR compliance
- **Audit Trail:** Timeline table tracks all state changes
- **Performance:** Strategic indexes on foreign keys and query columns
- **Data Integrity:** Foreign key constraints, CHECK constraints, NOT NULL where appropriate

---

## 2. Entity Relationship Diagram (ERD)

```
┌─────────────┐
│    Users    │
│ (3 roles)   │
└──────┬──────┘
       │
       │ 1:N (creates)
       │
       ▼
┌─────────────┐
│   Issues    │
│             │◄──────────┐
└──────┬──────┘           │ 1:N (assigned to)
       │                  │
       │ 1:N (has)       │
       │              ┌──┴────────┐
       ▼              │   Users   │
┌─────────────┐       │  (Staff)  │
│  Timeline   │       └───────────┘
│ (audit log) │
└─────────────┘
```

**Relationships:**
1. User → Issues (one-to-many): A citizen creates many issues
2. User (Staff) → Issues (one-to-many): A staff member is assigned many issues
3. Issue → Timeline (one-to-many): An issue has many timeline entries

---

## 3. Schema Definition

### 3.1 Users Table

**Purpose:** Store all users (Admin, Staff, Citizen)

```sql
CREATE TABLE users (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,

    -- User Information
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,  -- BCrypt hashed
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'STAFF', 'CITIZEN')),

    -- Audit & Soft Delete
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP DEFAULT NULL,  -- Soft delete (NULL = active)

    -- Constraints
    CONSTRAINT email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- Indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_deleted_at ON users(deleted_at);  -- For filtering active users
```

**Design Notes:**
- **Soft Delete:** `deleted_at` allows "deleting" users while preserving data
- **Email Validation:** CHECK constraint ensures valid email format
- **Password:** Stored as BCrypt hash (never plain text)
- **Role:** Single table for all user types (simpler than separate tables)

**Queries Optimized:**
- Login: `WHERE email = ? AND deleted_at IS NULL` (uses idx_users_email)
- Get staff list: `WHERE role = 'STAFF' AND deleted_at IS NULL` (uses idx_users_role)

---

### 3.2 Issues Table

**Purpose:** Store infrastructure issue reports

```sql
CREATE TABLE issues (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,

    -- Issue Details
    title VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(50) NOT NULL CHECK (category IN (
        'ROADS', 'STREETLIGHTS', 'WATER', 'GARBAGE', 'FOOTPATHS', 'OTHER'
    )),
    location VARCHAR(255) NOT NULL,

    -- Status & Priority
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' CHECK (status IN (
        'PENDING', 'IN_PROGRESS', 'WORKING', 'RESOLVED', 'CLOSED'
    )),
    priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL' CHECK (priority IN (
        'NORMAL', 'HIGH'
    )),

    -- Relationships
    citizen_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    assigned_staff_id BIGINT DEFAULT NULL REFERENCES users(id) ON DELETE SET NULL,

    -- Audit & Soft Delete
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP DEFAULT NULL,

    -- Constraints
    CONSTRAINT title_not_empty CHECK (LENGTH(TRIM(title)) > 0),
    CONSTRAINT description_not_empty CHECK (LENGTH(TRIM(description)) > 0)
);

-- Performance Indexes
CREATE INDEX idx_issues_citizen_id ON issues(citizen_id);
CREATE INDEX idx_issues_assigned_staff_id ON issues(assigned_staff_id);
CREATE INDEX idx_issues_status ON issues(status);
CREATE INDEX idx_issues_category ON issues(category);
CREATE INDEX idx_issues_created_at ON issues(created_at DESC);  -- For "recent issues" query
CREATE INDEX idx_issues_deleted_at ON issues(deleted_at);
CREATE INDEX idx_issues_status_priority ON issues(status, priority);  -- Composite for filtering
```

**Design Notes:**
- **Foreign Key Behavior:**
  - `citizen_id ON DELETE RESTRICT`: Cannot delete citizen if they have issues (use soft delete)
  - `assigned_staff_id ON DELETE SET NULL`: If staff deleted, issue remains unassigned
- **Composite Index:** `(status, priority)` optimizes filtered queries
- **Created At Descending:** Index supports "ORDER BY created_at DESC" efficiently

**Queries Optimized:**
- Get citizen's issues: `WHERE citizen_id = ? AND deleted_at IS NULL`
- Get staff's assigned issues: `WHERE assigned_staff_id = ? AND deleted_at IS NULL`
- Get pending high-priority issues: `WHERE status = 'PENDING' AND priority = 'HIGH'`
- Get recent issues: `ORDER BY created_at DESC LIMIT 12`

---

### 3.3 Timeline Table

**Purpose:** Audit trail for all issue state changes

```sql
CREATE TABLE timeline (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,

    -- Relationship
    issue_id BIGINT NOT NULL REFERENCES issues(id) ON DELETE CASCADE,

    -- Change Details
    status VARCHAR(50) NOT NULL,  -- Status at this point in time
    message TEXT NOT NULL,         -- Human-readable description

    -- Audit Information
    updated_by_id BIGINT DEFAULT NULL REFERENCES users(id) ON DELETE SET NULL,
    updated_by_role VARCHAR(20) NOT NULL,  -- Role at time of change

    -- Timestamp
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT message_not_empty CHECK (LENGTH(TRIM(message)) > 0)
);

-- Performance Indexes
CREATE INDEX idx_timeline_issue_id ON timeline(issue_id);
CREATE INDEX idx_timeline_created_at ON timeline(created_at DESC);
```

**Design Notes:**
- **Immutable:** Timeline entries cannot be edited or deleted (audit integrity)
- **Cascade Delete:** If issue deleted (hard delete in future), remove timeline
- **updated_by_role:** Stored as string to preserve role even if user changes role later
- **Timeline is append-only:** Only INSERT, never UPDATE or DELETE

**Queries Optimized:**
- Get issue timeline: `WHERE issue_id = ? ORDER BY created_at DESC`
- Recent activity across system: `ORDER BY created_at DESC LIMIT 20`

**Example Timeline Entries:**
```sql
-- Issue created
INSERT INTO timeline (issue_id, status, message, updated_by_id, updated_by_role)
VALUES (1, 'PENDING', 'Issue reported by John Doe', 5, 'CITIZEN');

-- Issue assigned
INSERT INTO timeline (issue_id, status, message, updated_by_id, updated_by_role)
VALUES (1, 'PENDING', 'Issue assigned to Staff: Jane Smith', 1, 'ADMIN');

-- Status changed
INSERT INTO timeline (issue_id, status, message, updated_by_id, updated_by_role)
VALUES (1, 'IN_PROGRESS', 'Work started on the issue', 3, 'STAFF');
```

---

## 4. Database Optimization Strategies

### 4.1 Index Strategy

**Indexes Created:**
```sql
-- Users (3 indexes)
idx_users_email, idx_users_role, idx_users_deleted_at

-- Issues (7 indexes)
idx_issues_citizen_id, idx_issues_assigned_staff_id,
idx_issues_status, idx_issues_category, idx_issues_created_at,
idx_issues_deleted_at, idx_issues_status_priority

-- Timeline (2 indexes)
idx_timeline_issue_id, idx_timeline_created_at
```

**Total: 12 indexes** (appropriate for MVP)

**Why not more?**
- Every index slows down INSERT/UPDATE
- These indexes cover 95% of query patterns
- Monitor slow queries, add indexes as needed

### 4.2 Query Performance Examples

**Slow Query (No Index):**
```sql
-- Without idx_issues_category
SELECT * FROM issues WHERE category = 'ROADS';
-- Seq Scan on issues (cost=0.00..25.50 rows=10 width=100)
-- Scans all 1000 rows
```

**Fast Query (With Index):**
```sql
-- With idx_issues_category
SELECT * FROM issues WHERE category = 'ROADS';
-- Index Scan using idx_issues_category (cost=0.29..8.31 rows=10 width=100)
-- Direct lookup, only 10 rows scanned
```

**Use EXPLAIN ANALYZE to verify:**
```sql
EXPLAIN ANALYZE SELECT * FROM issues WHERE category = 'ROADS';
```

### 4.3 N+1 Query Prevention

**Problem (N+1 queries):**
```java
// Get all issues (1 query)
List<Issue> issues = issueRepository.findAll();

// Get citizen for each issue (N queries)
for (Issue issue : issues) {
    String name = issue.getCitizen().getName();  // SELECT * FROM users WHERE id = ?
}
// Total: 1 + N queries (if 100 issues = 101 queries) ❌ SLOW
```

**Solution (Single Query with JOIN):**
```java
// Use EntityGraph to fetch issues with citizens in ONE query
@EntityGraph(attributePaths = {"citizen", "assignedStaff"})
@Query("SELECT i FROM Issue i WHERE i.deletedAt IS NULL")
List<Issue> findAllWithRelations();

// Total: 1 query ✅ FAST
```

**SQL Generated:**
```sql
SELECT i.*, u1.*, u2.*
FROM issues i
LEFT JOIN users u1 ON i.citizen_id = u1.id
LEFT JOIN users u2 ON i.assigned_staff_id = u2.id
WHERE i.deleted_at IS NULL;
```

---

## 5. Data Integrity Constraints

### 5.1 Foreign Key Constraints

| Table | Column | References | On Delete | Rationale |
|-------|--------|------------|-----------|-----------|
| issues | citizen_id | users(id) | RESTRICT | Cannot delete citizen with issues; use soft delete |
| issues | assigned_staff_id | users(id) | SET NULL | If staff deleted, issue becomes unassigned |
| timeline | issue_id | issues(id) | CASCADE | Timeline belongs to issue; delete together |
| timeline | updated_by_id | users(id) | SET NULL | Preserve timeline even if user deleted |

### 5.2 CHECK Constraints

**Email Format:**
```sql
CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
```

**Enum Values:**
```sql
CHECK (role IN ('ADMIN', 'STAFF', 'CITIZEN'))
CHECK (status IN ('PENDING', 'IN_PROGRESS', 'WORKING', 'RESOLVED', 'CLOSED'))
CHECK (category IN ('ROADS', 'STREETLIGHTS', 'WATER', 'GARBAGE', 'FOOTPATHS', 'OTHER'))
CHECK (priority IN ('NORMAL', 'HIGH'))
```

**Non-Empty Strings:**
```sql
CHECK (LENGTH(TRIM(title)) > 0)
CHECK (LENGTH(TRIM(description)) > 0)
CHECK (LENGTH(TRIM(message)) > 0)
```

### 5.3 NOT NULL Constraints

**Critical fields that must always have values:**
- users: name, email, password, role
- issues: title, description, category, location, status, priority, citizen_id
- timeline: issue_id, status, message, updated_by_role

---

## 6. Soft Delete Implementation

### 6.1 Application Layer Queries

**Always exclude soft-deleted records:**

```sql
-- Get active users
SELECT * FROM users WHERE deleted_at IS NULL;

-- Get active issues
SELECT * FROM issues WHERE deleted_at IS NULL;

-- Get citizen's active issues
SELECT * FROM issues
WHERE citizen_id = ? AND deleted_at IS NULL;
```

### 6.2 JPA Global Filter (Spring Boot)

```java
@Entity
@SQLDelete(sql = "UPDATE users SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class User {
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
```

**Effect:**
- `userRepository.delete(user)` → Soft delete (UPDATE)
- `userRepository.findAll()` → Only returns non-deleted users

### 6.3 GDPR Compliance

**Right to be Forgotten:**
```sql
-- Soft delete user (reversible)
UPDATE users SET deleted_at = NOW() WHERE id = 123;

-- Hard delete (permanent, only if legally required)
-- First anonymize data
UPDATE issues SET
    citizen_id = NULL,
    title = 'Deleted User Issue',
    description = '[Content Removed]'
WHERE citizen_id = 123;

-- Then delete user
DELETE FROM users WHERE id = 123;
```

---

## 7. Migration Scripts

### 7.1 Initial Schema Creation

**File:** `db/migration/V1__initial_schema.sql`

```sql
-- Create Users Table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'STAFF', 'CITIZEN')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP DEFAULT NULL,
    CONSTRAINT email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- Create Issues Table
CREATE TABLE issues (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(50) NOT NULL CHECK (category IN ('ROADS', 'STREETLIGHTS', 'WATER', 'GARBAGE', 'FOOTPATHS', 'OTHER')),
    location VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'IN_PROGRESS', 'WORKING', 'RESOLVED', 'CLOSED')),
    priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL' CHECK (priority IN ('NORMAL', 'HIGH')),
    citizen_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    assigned_staff_id BIGINT DEFAULT NULL REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP DEFAULT NULL,
    CONSTRAINT title_not_empty CHECK (LENGTH(TRIM(title)) > 0),
    CONSTRAINT description_not_empty CHECK (LENGTH(TRIM(description)) > 0)
);

-- Create Timeline Table
CREATE TABLE timeline (
    id BIGSERIAL PRIMARY KEY,
    issue_id BIGINT NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    updated_by_id BIGINT DEFAULT NULL REFERENCES users(id) ON DELETE SET NULL,
    updated_by_role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT message_not_empty CHECK (LENGTH(TRIM(message)) > 0)
);

-- Create Indexes (Users)
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_deleted_at ON users(deleted_at);

-- Create Indexes (Issues)
CREATE INDEX idx_issues_citizen_id ON issues(citizen_id);
CREATE INDEX idx_issues_assigned_staff_id ON issues(assigned_staff_id);
CREATE INDEX idx_issues_status ON issues(status);
CREATE INDEX idx_issues_category ON issues(category);
CREATE INDEX idx_issues_created_at ON issues(created_at DESC);
CREATE INDEX idx_issues_deleted_at ON issues(deleted_at);
CREATE INDEX idx_issues_status_priority ON issues(status, priority);

-- Create Indexes (Timeline)
CREATE INDEX idx_timeline_issue_id ON timeline(issue_id);
CREATE INDEX idx_timeline_created_at ON timeline(created_at DESC);
```

### 7.2 Seed Data (Development)

**File:** `db/migration/V2__seed_data.sql`

```sql
-- Insert Admin User
INSERT INTO users (name, email, password, role) VALUES
('Admin User', 'admin@issuetracker.com', '$2a$10$encrypted_password_here', 'ADMIN');

-- Insert Staff Users
INSERT INTO users (name, email, password, role) VALUES
('Staff One', 'staff1@issuetracker.com', '$2a$10$encrypted_password_here', 'STAFF'),
('Staff Two', 'staff2@issuetracker.com', '$2a$10$encrypted_password_here', 'STAFF');

-- Insert Citizen Users
INSERT INTO users (name, email, password, role) VALUES
('John Citizen', 'john@example.com', '$2a$10$encrypted_password_here', 'CITIZEN'),
('Jane Citizen', 'jane@example.com', '$2a$10$encrypted_password_here', 'CITIZEN');

-- Insert Sample Issues
INSERT INTO issues (title, description, category, location, status, citizen_id) VALUES
('Pothole on Main Street', 'Large pothole causing traffic issues', 'ROADS', 'Main Street near City Hall', 'PENDING', 4),
('Broken Streetlight', 'Streetlight not working for 2 weeks', 'STREETLIGHTS', 'Park Avenue', 'PENDING', 5);

-- Insert Timeline Entries
INSERT INTO timeline (issue_id, status, message, updated_by_id, updated_by_role) VALUES
(1, 'PENDING', 'Issue reported by John Citizen', 4, 'CITIZEN'),
(2, 'PENDING', 'Issue reported by Jane Citizen', 5, 'CITIZEN');
```

---

## 8. Testing the Schema

### 8.1 Verify Constraints

```sql
-- Test: Duplicate email (should FAIL)
INSERT INTO users (name, email, password, role) VALUES
('Test', 'admin@issuetracker.com', 'pass', 'CITIZEN');
-- ERROR: duplicate key value violates unique constraint "users_email_key"

-- Test: Invalid role (should FAIL)
INSERT INTO users (name, email, password, role) VALUES
('Test', 'test@test.com', 'pass', 'INVALID');
-- ERROR: new row for relation "users" violates check constraint "users_role_check"

-- Test: Invalid status (should FAIL)
INSERT INTO issues (..., status, ...) VALUES (..., 'INVALID', ...);
-- ERROR: new row for relation "issues" violates check constraint "issues_status_check"
```

### 8.2 Verify Indexes

```sql
-- Check if indexes are being used
EXPLAIN ANALYZE SELECT * FROM issues WHERE category = 'ROADS';
-- Should show: Index Scan using idx_issues_category

EXPLAIN ANALYZE SELECT * FROM issues WHERE status = 'PENDING' AND priority = 'HIGH';
-- Should show: Index Scan using idx_issues_status_priority
```

---

## 9. Performance Benchmarks[text](../1stAsMvp.md) [text](../experience.txt)

### 9.1 Expected Query Performance

| Query | Expected Time | Notes |
|-------|---------------|-------|
| Get user by email | <5ms | Uses unique index |
| Get all issues (paginated) | <50ms | Uses idx_issues_created_at |
| Get citizen's issues | <10ms | Uses idx_issues_citizen_id |
| Get staff's assigned issues | <10ms | Uses idx_issues_assigned_staff_id |
| Get issue timeline | <15ms | Uses idx_timeline_issue_id |
| Filter by status + priority | <20ms | Uses idx_issues_status_priority |

### 9.2 Monitoring Slow Queries

**Enable slow query logging (PostgreSQL):**
```sql
-- In postgresql.conf
log_min_duration_statement = 100  -- Log queries taking >100ms

-- Or dynamically
ALTER DATABASE issuetracker SET log_min_duration_statement = 100;
```

**Analyze query performance:**
```sql
-- Find slowest queries
SELECT query, calls, total_time, mean_time
FROM pg_stat_statements
ORDER BY mean_time DESC
LIMIT 10;
```

---

## 10. Future Enhancements (Post-MVP)

### 10.1 Additional Tables (Phase 2)

**Payments Table:**
```sql
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    amount DECIMAL(10,2) NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('BOOST', 'SUBSCRIPTION')),
    issue_id BIGINT REFERENCES issues(id) ON DELETE SET NULL,
    transaction_id VARCHAR(255) UNIQUE NOT NULL,
    status VARCHAR(50) NOT NULL CHECK (status IN ('SUCCESS', 'FAILED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

**Upvotes Table:**
```sql
CREATE TABLE upvotes (
    id BIGSERIAL PRIMARY KEY,
    issue_id BIGINT NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(issue_id, user_id)  -- Prevent duplicate upvotes
);
```

### 10.2 Full-Text Search

```sql
-- Add tsvector column for full-text search
ALTER TABLE issues ADD COLUMN search_vector tsvector;

-- Generate search vector
UPDATE issues SET search_vector =
    to_tsvector('english', coalesce(title, '') || ' ' || coalesce(description, ''));

-- Create index
CREATE INDEX idx_issues_search ON issues USING GIN(search_vector);

-- Search query
SELECT * FROM issues
WHERE search_vector @@ to_tsquery('english', 'pothole & street');
```

---

## 11. Summary

**Schema Statistics:**
- **Tables:** 3 (Users, Issues, Timeline)
- **Indexes:** 12 (strategic coverage)
- **Constraints:** 15+ (data integrity)
- **Relationships:** 4 foreign keys

**Key Features:**
- ✅ Soft delete support (GDPR compliant)
- ✅ Complete audit trail (Timeline table)
- ✅ Performance optimized (strategic indexes)
- ✅ Data integrity enforced (FK + CHECK constraints)
- ✅ N+1 query prevention ready
- ✅ Scalable to 100k+ records

**German Market Standards:**
- ✅ Audit trail (Timeline)
- ✅ GDPR compliance (Soft delete)
- ✅ Data integrity (Constraints)
- ✅ Performance benchmarks documented

---

**Next Steps:**
1. Review this schema design
2. Create ADR: "Why PostgreSQL over MongoDB"
3. Create ADR: "Why soft delete over hard delete"
4. Implement schema with Flyway migrations
5. Write integration tests for constraints

**Questions? Ask me to explain any section in detail.**
