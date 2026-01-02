# Project Requirements Document
## Public Infrastructure Issue Reporting System

**Version:** 3.0 (Mid-Level Quality + German Market Standards)
**Last Updated:** December 25, 2024
**Target Market:** German Tech Companies (Mid-Level Portfolio)
**Development Approach:** Agile/Incremental with TDD + Clean Architecture
**Quality Focus:** Production-Grade MVP with Mid-Level Engineering Practices

---

## 1. Project Overview

**System Name:** Public Infrastructure Issue Reporting System

**Purpose:** A digital platform that enables citizens to report real-world public infrastructure issues (broken streetlights, potholes, water leakage, garbage overflow, damaged footpaths, etc.). Government staff and admins can manage, verify, assign, and resolve reported issues efficiently.

**Business Value:**
- Improves municipal response time
- Increases transparency in public service delivery
- Provides data-driven insights for infrastructure planning
- Reduces citizen frustration with unresolved issues

**Target Users:**
- **Primary:** Citizens (issue reporters)
- **Secondary:** Municipal Staff (issue resolvers)
- **Tertiary:** System Admins (platform managers)

---

## 2. MVP Strategy (German Market Approach)

### 2.1 MVP Philosophy

**German companies value:**
- ✅ Working software over comprehensive features
- ✅ Code quality over speed
- ✅ Test coverage over quick delivery
- ✅ Documentation over assumptions
- ✅ Security and privacy from day one

**MVP Goal:**
Build a **fully functional, well-tested, production-ready** core system that demonstrates professional software engineering practices. Better to have 5 features done perfectly than 20 features done poorly.

### 2.2 MVP Scope (Build THIS for Portfolio)

**Phase 1 - MVP Features (4-6 weeks):**

#### Must-Have (Core Value Proposition):
1. **Authentication System**
   - Email/Password registration & login
   - JWT-based authorization
   - Role-based access control (3 roles)
   - Session persistence
   - ❌ Skip Google OAuth (add later)

2. **Issue Reporting (Citizen)**
   - Create issue (title, description, category, location)
   - ❌ Skip image upload initially (add later)
   - View own issues
   - Edit own issues (if status=pending)
   - Delete own issues

3. **Issue Management (Admin/Staff)**
   - Admin: View all issues
   - Admin: Assign issues to staff
   - Staff: View assigned issues
   - Staff: Update issue status
   - Status flow: Pending → In-Progress → Resolved → Closed

4. **Basic Timeline Tracking**
   - Record status changes
   - Display timeline on issue details page
   - Show who made changes and when

5. **Dashboards (Basic)**
   - Citizen: Issue count statistics
   - Staff: Assigned issues list
   - Admin: System overview stats

6. **Core Infrastructure**
   - Responsive design (mobile-first)
   - Error handling
   - Loading states
   - Input validation
   - **80% test coverage minimum**

#### Explicitly EXCLUDE from MVP:
- ❌ Payment system (boost/subscription)
- ❌ Google OAuth
- ❌ Image upload (use placeholder images)
- ❌ Upvote system
- ❌ PDF generation
- ❌ User blocking
- ❌ Staff management CRUD
- ❌ Advanced filtering
- ❌ Charts/graphs

**Rationale:** MVP focuses on core workflow: Report → Assign → Resolve. **Quality over quantity** demonstrates mid-level engineering maturity.

---

### 2.3 Mid-Level Quality Touches (What Makes This Stand Out)

**These practices elevate your portfolio from entry-level to mid-level:**

**1. Clean Architecture (Hexagonal/Onion)**
- Domain logic separated from infrastructure
- Dependency inversion principle
- Easy to test, easy to change
- Demonstrates architectural thinking

**2. Comprehensive Testing (85%+ Coverage)**
- Unit tests for business logic
- Integration tests for API + DB
- E2E tests for critical flows
- Test-first development (TDD)

**3. Database Optimization**
- Proper indexing strategy
- Query performance analysis (EXPLAIN ANALYZE)
- N+1 query prevention
- Connection pooling configuration

**4. Monitoring & Observability**
- Spring Boot Actuator endpoints
- Structured logging (JSON format)
- Performance metrics
- Health checks

**5. CI/CD Pipeline (GitHub Actions)**
- Automated testing on every push
- Automated deployment to staging
- Code quality checks (SonarQube)
- Security scanning

**6. Docker (Local Development)**
- Docker Compose for full stack
- Consistent development environment
- Easy onboarding for reviewers
- Production-like setup locally

**7. API Documentation (Swagger/OpenAPI)**
- Interactive API documentation
- Request/Response examples
- Error codes documented
- Try-it-out feature

**8. Architecture Decision Records (ADRs)**
- Document WHY you made decisions
- Show thinking process
- Demonstrates maturity
- German companies love this

**9. Performance Benchmarks**
- Load testing results
- Response time percentiles (p50, p95, p99)
- Database query performance
- Frontend metrics (Lighthouse)

**10. Security Audit (OWASP Checklist)**
- OWASP Top 10 addressed
- Security headers configured
- Input validation comprehensive
- Dependency vulnerability scanning

**Impact:** These practices show you think like a mid-level engineer, not just code like a junior.

---

### 2.4 Post-MVP Enhancements (After Job Search)

**Phase 2 - Enhanced Features (Can add on the job):**
- Image upload (Cloudinary integration)
- Payment integration (boost/subscription)
- Google OAuth
- Upvote system
- Advanced search & filters
- PDF invoice generation
- User management (block/unblock)
- Charts and analytics
- Notifications (email/push)

---

## 3. German Market Quality Standards

### 3.1 Code Quality Requirements

**Industry Standard in Germany:**

| Metric | Minimum | Target (Mid-Level) | Tool |
|--------|---------|--------|------|
| Test Coverage | 80% | **85%+** | JaCoCo (Backend), Jest (Frontend) |
| Code Duplication | <5% | **<2%** | SonarQube |
| Cyclomatic Complexity | <10 per method | **<5** | SonarQube / CodeClimate |
| Code Review | 100% PRs | 100% | GitHub PR process |
| Documentation | All public APIs | **+ ADRs + diagrams** | JavaDoc, TSDoc, ADR |
| Maintainability Index | >65 | **>80** | SonarQube |
| Technical Debt Ratio | <5% | **<2%** | SonarQube |

**Mandatory Practices:**
- ✅ Every feature has tests BEFORE merge
- ✅ No direct commits to main branch
- ✅ Feature branch workflow
- ✅ Meaningful commit messages (Conventional Commits)
- ✅ Code formatted with standard tools (Prettier, Checkstyle)
- ✅ No compiler warnings
- ✅ No unused imports or variables

### 3.2 Testing Strategy (German Standard)

**Test Pyramid:**

```
        /\
       /E2E\      10% - End-to-End (Critical user flows)
      /------\
     /Integration\ 30% - Integration (API + DB)
    /------------\
   /   Unit Tests \ 60% - Unit (Business logic)
  /----------------\
```

**Backend Testing (Spring Boot):**
- ✅ Unit Tests: JUnit 5 + Mockito
- ✅ Integration Tests: @SpringBootTest + TestContainers (PostgreSQL)
- ✅ API Tests: MockMvc / RestAssured
- ✅ Security Tests: Test unauthorized access
- ✅ Repository Tests: Test database operations

**Frontend Testing (Next.js):**
- ✅ Component Tests: Jest + React Testing Library
- ✅ Integration Tests: Test user interactions
- ✅ E2E Tests: Playwright (critical flows only)
- ✅ Accessibility Tests: jest-axe

**Minimum Test Scenarios for MVP:**
1. User registration and login
2. Create issue (authenticated)
3. Assign issue (admin role)
4. Update status (staff role)
5. View timeline
6. Authorization failures (401/403)
7. Input validation failures (400)

**Target:** **85%+ coverage before deployment (Mid-Level Standard)**

**TDD Workflow (Test-Driven Development):**
1. Write failing test first (Red)
2. Write minimum code to pass (Green)
3. Refactor while keeping tests green (Refactor)
4. Repeat

**Test Quality Requirements:**
- ✅ Tests must be readable (Given-When-Then pattern)
- ✅ Tests must be isolated (no dependencies between tests)
- ✅ Tests must be fast (<5s for unit test suite)
- ✅ Integration tests use TestContainers (real PostgreSQL)
- ✅ Mocks used only when necessary
- ✅ Test names describe behavior, not implementation

### 3.3 GDPR Compliance (German Law)

**Required for German Market:**

1. **Data Privacy**
   - Store only necessary user data
   - Password hashing (BCrypt, min 10 rounds)
   - No logging of sensitive data (passwords, tokens)

2. **User Rights (Basic Compliance)**
   - Users can delete their account
   - Users can export their data (future)
   - Clear privacy policy (future)

3. **Security**
   - HTTPS only (production)
   - Secure cookie flags (httpOnly, secure, sameSite)
   - JWT token expiration (15min access, 7 day refresh)
   - Input sanitization against XSS
   - SQL injection prevention (JPA/Hibernate)

4. **Data Retention**
   - Soft delete for issues (audit trail)
   - Automatic data cleanup policy (document only for now)

**Note:** Full GDPR compliance is complex. For MVP/Portfolio, demonstrate awareness and basic implementation.

### 3.4 Performance Standards

**German Companies Expect:**

| Metric | Target | Measurement |
|--------|--------|-------------|
| API Response Time (p95) | <300ms | Spring Boot Actuator |
| Page Load Time (FCP) | <1.5s | Lighthouse |
| Time to Interactive (TTI) | <3s | Lighthouse |
| Lighthouse Score | >90 | Chrome DevTools |
| Database Query Time | <100ms | JPA query logs |

**Optimization Requirements:**
- ✅ Database indexing on foreign keys
- ✅ Lazy loading for relations
- ✅ Image optimization (if implemented)
- ✅ Code splitting (Next.js automatic)
- ✅ Caching strategy (TanStack Query)

### 3.5 Documentation Standards

**German companies require:**

1. **README.md (Mandatory)**
   - Project description
   - Tech stack with versions
   - Setup instructions (step-by-step)
   - Environment variables template
   - Running tests
   - Deployment guide
   - Admin credentials (for reviewers)
   - Known issues / limitations

2. **API Documentation**
   - Swagger/OpenAPI spec (recommended)
   - Or detailed API.md file
   - Request/Response examples
   - Error codes

3. **Code Comments**
   - JavaDoc for public methods (Backend)
   - TSDoc for exported functions (Frontend)
   - Complex logic explanation
   - "Why" not "what" in comments

4. **Architecture Documentation**
   - System architecture diagram
   - Database schema (ERD)
   - Authentication flow diagram
   - Folder structure explanation

**Example README structure in `/docs/README-template.md`**

---

## 4. User Roles & Permissions (MVP)

### 4.1 Admin
**Capabilities:**
- View all issues
- Assign issues to staff
- View system statistics
- ~~Manage staff accounts~~ (Post-MVP)
- ~~Block/unblock users~~ (Post-MVP)

### 4.2 Staff
**Capabilities:**
- View ONLY assigned issues
- Update issue status
- View assigned issues statistics

**Restrictions:**
- Cannot see unassigned issues
- Cannot see other staff's issues

### 4.3 Citizen
**Capabilities:**
- Submit issues (MVP: without image)
- View own issues
- Edit own issues (if status=pending)
- Delete own issues
- View issue details with timeline

**Restrictions (MVP):**
- ~~Limited to 3 issues~~ (No limit in MVP - simplify)
- ~~Cannot upvote~~ (Post-MVP feature)
- ~~No payment features~~ (Post-MVP)

---

## 5. Core Features - Detailed Requirements (MVP Only)

### 5.1 Authentication & Authorization

#### 5.1.1 Registration
**User Story:** As a new user, I want to register an account so that I can report issues.

**Acceptance Criteria:**
- [ ] Form fields: Name, Email, Password, Confirm Password
- [ ] Email validation (format check)
- [ ] Password strength: Min 8 chars, 1 uppercase, 1 number, 1 special char
- [ ] Unique email check (409 if duplicate)
- [ ] Password hashed with BCrypt (rounds=10)
- [ ] User created with role=CITIZEN by default
- [ ] Success: Redirect to login with toast message
- [ ] Errors: Display field-specific validation messages

**API Endpoint:**
```
POST /api/auth/register
Request: { name, email, password }
Response: { message: "Registration successful" }
Errors: 400 (validation), 409 (duplicate email)
```

**Tests Required:**
- ✅ Successful registration
- ✅ Duplicate email rejection
- ✅ Weak password rejection
- ✅ Invalid email format rejection
- ✅ Password is hashed in database

#### 5.1.2 Login
**User Story:** As a registered user, I want to log in so that I can access my account.

**Acceptance Criteria:**
- [ ] Form fields: Email, Password
- [ ] Credentials validated against database
- [ ] JWT token generated (15min expiry)
- [ ] Refresh token generated (7 days expiry)
- [ ] Tokens returned in response
- [ ] Frontend stores tokens (httpOnly cookie or localStorage)
- [ ] Success: Redirect based on role
  - Citizen → `/dashboard`
  - Staff → `/staff/dashboard`
  - Admin → `/admin/dashboard`
- [ ] Error: Invalid credentials (401)

**API Endpoint:**
```
POST /api/auth/login
Request: { email, password }
Response: {
  accessToken,
  refreshToken,
  user: { id, name, email, role }
}
Errors: 401 (invalid credentials)
```

**Tests Required:**
- ✅ Successful login (all roles)
- ✅ Invalid password rejection
- ✅ Non-existent email rejection
- ✅ Token generation and validation
- ✅ Token expiration handling

#### 5.1.3 Authorization Middleware
**Implementation:**
- JWT verification filter on all `/api/**` except auth endpoints
- Extract user ID and role from token
- Attach to request context
- Role-based access control annotations

**Example (Spring Boot):**
```java
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/issues/{id}/assign")
public ResponseEntity<?> assignIssue(...)
```

**Tests Required:**
- ✅ Unauthorized access returns 401
- ✅ Insufficient role returns 403
- ✅ Valid token allows access
- ✅ Expired token returns 401

### 5.2 Issue Management (MVP)

#### 5.2.1 Create Issue
**User Story:** As a citizen, I want to report an infrastructure issue so that it gets resolved.

**Acceptance Criteria:**
- [ ] Form fields: Title, Description, Category (dropdown), Location
- [ ] All fields required
- [ ] Title: Max 100 chars
- [ ] Description: Max 1000 chars
- [ ] Category: Enum (Roads, Streetlights, Water, Garbage, Footpaths, Other)
- [ ] Location: Text input (future: Google Maps integration)
- [ ] Initial status: PENDING
- [ ] Initial priority: NORMAL
- [ ] Created by: Logged-in user ID
- [ ] Timeline entry: "Issue reported by [Name]"
- [ ] Success: Redirect to My Issues with toast
- [ ] ~~No image upload in MVP~~ (Placeholder only)

**API Endpoint:**
```
POST /api/issues
Request: { title, description, category, location }
Response: { id, ...issue details }
Authorization: Bearer token (CITIZEN role)
```

**Tests Required:**
- ✅ Citizen can create issue
- ✅ Unauthenticated request fails (401)
- ✅ Non-citizen role cannot create (403)
- ✅ Missing required fields fail (400)
- ✅ Timeline entry created
- ✅ Issue saved in database

#### 5.2.2 View Issues
**User Stories:**
- As a citizen, I want to see all issues so that I know what problems exist
- As a citizen, I want to see my own issues so that I can track them

**All Issues Page (Public - anyone can view):**
- [ ] Display all issues (no auth required)
- [ ] Card layout: Title, Category, Status badge, Location, Created date
- [ ] Pagination: 12 issues per page (server-side)
- [ ] Click card → Navigate to issue details
- [ ] Basic sorting: Newest first

**My Issues Page (Private - Citizen only):**
- [ ] Display logged-in user's issues only
- [ ] Show Edit/Delete buttons (conditional on status)
- [ ] Filter by status (client-side for MVP)

**API Endpoints:**
```
GET /api/issues?page=0&size=12
Response: { content: [...], totalPages, totalElements }
Authorization: None (public)

GET /api/issues/my-issues
Response: { content: [...] }
Authorization: Bearer token (CITIZEN)
```

**Tests Required:**
- ✅ Public can view all issues
- ✅ Pagination works correctly
- ✅ Citizen sees only own issues in /my-issues
- ✅ Other user's issues not visible in /my-issues

#### 5.2.3 Issue Details
**User Story:** As any user, I want to view full issue details and timeline.

**Acceptance Criteria:**
- [ ] Display: Title, Description, Category, Location, Status, Priority, Created date
- [ ] Display: Reporter name
- [ ] Display: Assigned staff name (if assigned)
- [ ] Display: Timeline section (chronological, newest first)
- [ ] Conditional buttons:
  - Edit: If logged-in user is owner AND status=PENDING
  - Delete: If logged-in user is owner
  - ~~Boost: Not in MVP~~
- [ ] ~~Upvote: Not in MVP~~

**API Endpoint:**
```
GET /api/issues/{id}
Response: {
  id, title, description, ...,
  reporter: { id, name },
  assignedStaff: { id, name } | null,
  timeline: [{ status, message, updatedBy, createdAt }]
}
Authorization: Bearer token (any role)
```

**Tests Required:**
- ✅ Authenticated user can view details
- ✅ Timeline displays correctly
- ✅ Assigned staff info shown when assigned

#### 5.2.4 Edit Issue
**User Story:** As a citizen, I want to edit my issue if it hasn't been processed yet.

**Acceptance Criteria:**
- [ ] Only owner can edit
- [ ] Only if status = PENDING
- [ ] Modal form pre-filled with current data
- [ ] Can edit: Title, Description, Category, Location
- [ ] Validation same as create
- [ ] Success: Update in DB + UI refresh + toast
- [ ] Timeline entry: "Issue updated by [Name]"

**API Endpoint:**
```
PUT /api/issues/{id}
Request: { title, description, category, location }
Response: { ...updated issue }
Authorization: Bearer token (CITIZEN, must be owner)
Errors: 403 (not owner or status != PENDING), 404 (not found)
```

**Tests Required:**
- ✅ Owner can edit pending issue
- ✅ Owner cannot edit in-progress issue (403)
- ✅ Non-owner cannot edit (403)
- ✅ Timeline entry created

#### 5.2.5 Delete Issue
**User Story:** As a citizen, I want to delete my issue if I reported it incorrectly.

**Acceptance Criteria:**
- [ ] Only owner can delete
- [ ] Confirmation dialog ("Are you sure?")
- [ ] Soft delete recommended (set deletedAt timestamp)
- [ ] Or hard delete (remove from DB)
- [ ] Success: Remove from UI + toast
- [ ] Related timeline entries deleted/hidden

**API Endpoint:**
```
DELETE /api/issues/{id}
Response: { message: "Issue deleted" }
Authorization: Bearer token (CITIZEN, must be owner)
Errors: 403 (not owner), 404 (not found)
```

**Tests Required:**
- ✅ Owner can delete issue
- ✅ Non-owner cannot delete (403)
- ✅ Issue removed from database

#### 5.2.6 Assign Issue (Admin)
**User Story:** As an admin, I want to assign issues to staff so they can resolve them.

**Acceptance Criteria:**
- [ ] Only admin can assign
- [ ] Button visible only if issue has no assigned staff
- [ ] Click opens modal with staff dropdown
- [ ] Dropdown fetches all staff from DB
- [ ] Select staff + confirm
- [ ] Issue updated with assigned_staff_id
- [ ] Timeline entry: "Issue assigned to Staff: [Staff Name]"
- [ ] Button becomes disabled after assignment
- [ ] Staff sees issue in their dashboard immediately

**API Endpoint:**
```
POST /api/issues/{id}/assign
Request: { staffId }
Response: { ...updated issue }
Authorization: Bearer token (ADMIN)
Errors: 403 (not admin), 400 (already assigned), 404 (staff not found)
```

**Tests Required:**
- ✅ Admin can assign issue
- ✅ Non-admin cannot assign (403)
- ✅ Cannot assign if already assigned (400)
- ✅ Timeline entry created
- ✅ Staff sees issue in their list

#### 5.2.7 Update Status (Staff)
**User Story:** As staff, I want to update issue status so everyone knows the progress.

**Acceptance Criteria:**
- [ ] Only assigned staff can update status
- [ ] Dropdown with next valid statuses:
  - PENDING → IN_PROGRESS
  - IN_PROGRESS → WORKING
  - WORKING → RESOLVED
  - RESOLVED → CLOSED
- [ ] Select status + confirm
- [ ] Issue status updated in DB
- [ ] UI refreshes (TanStack Query invalidation)
- [ ] Timeline entry: "Status changed to [STATUS] by [Staff Name]"

**API Endpoint:**
```
PUT /api/issues/{id}/status
Request: { status: "IN_PROGRESS" }
Response: { ...updated issue }
Authorization: Bearer token (STAFF, must be assigned staff)
Errors: 403 (not assigned staff), 400 (invalid status transition)
```

**Tests Required:**
- ✅ Assigned staff can update status
- ✅ Non-assigned staff cannot update (403)
- ✅ Invalid status transition rejected (400)
- ✅ Timeline entry created

### 5.3 Timeline Tracking

**User Story:** As any user, I want to see the complete history of an issue so I understand its progress.

**Acceptance Criteria:**
- [ ] Timeline entry created for:
  - Issue creation
  - Issue update (by citizen)
  - Staff assignment
  - Status change
  - ~~Issue deletion~~ (optional)
- [ ] Each entry has: Status, Message, Updated by (name + role), Timestamp
- [ ] Display: Vertical timeline UI (latest first)
- [ ] Color-coded by status
- [ ] Read-only (cannot edit or delete)

**Database Schema:**
```sql
CREATE TABLE timeline (
  id BIGSERIAL PRIMARY KEY,
  issue_id BIGINT REFERENCES issues(id) ON DELETE CASCADE,
  status VARCHAR(50) NOT NULL,
  message TEXT NOT NULL,
  updated_by_id BIGINT REFERENCES users(id),
  updated_by_role VARCHAR(20) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Tests Required:**
- ✅ Timeline entry created on issue creation
- ✅ Timeline entry created on status change
- ✅ Timeline entries ordered correctly (newest first)
- ✅ Timeline entries include user info

### 5.4 Dashboards (Basic Stats)

#### 5.4.1 Citizen Dashboard
**Display:**
- Total issues submitted
- Pending issues count
- In-Progress issues count
- Resolved issues count

**API Endpoint:**
```
GET /api/dashboard/citizen
Response: {
  total: 12,
  pending: 3,
  inProgress: 5,
  resolved: 4
}
Authorization: Bearer token (CITIZEN)
```

#### 5.4.2 Staff Dashboard
**Display:**
- Assigned issues count
- Resolved issues count
- Today's assignments count

**API Endpoint:**
```
GET /api/dashboard/staff
Response: {
  assigned: 8,
  resolved: 15,
  todayAssigned: 2
}
Authorization: Bearer token (STAFF)
```

#### 5.4.3 Admin Dashboard
**Display:**
- Total issues
- Pending issues
- Resolved issues
- Total staff count
- Total citizen count

**API Endpoint:**
```
GET /api/dashboard/admin
Response: {
  totalIssues: 150,
  pending: 45,
  resolved: 80,
  totalStaff: 5,
  totalCitizens: 200
}
Authorization: Bearer token (ADMIN)
```

**Note:** Charts/graphs are POST-MVP. Simple card stats for MVP.

---

## 6. Technical Architecture

### 6.1 Tech Stack (Mid-Level Quality MVP)

**Frontend:**
- Framework: Next.js 14+ (App Router)
- Language: TypeScript 5+
- UI: Shadcn UI + Tailwind CSS 3+
- State Management: TanStack Query v5
- Form Handling: React Hook Form + Zod
- HTTP Client: Fetch API (built-in)
- Testing: Jest + React Testing Library + Playwright
- Performance: Web Vitals monitoring

**Backend:**
- Framework: Spring Boot 3.2+
- Language: Java 17
- Architecture: **Clean Architecture (Hexagonal)**
- Security: Spring Security 6 + JWT (jjwt 0.12+)
- Data Access: Spring Data JPA + Hibernate
- Validation: Jakarta Validation (Hibernate Validator)
- Testing: JUnit 5 + Mockito + TestContainers
- Database: PostgreSQL 15+
- Monitoring: Spring Boot Actuator + Micrometer
- API Docs: SpringDoc OpenAPI 3 (Swagger UI)
- Logging: Logback with JSON format (Logstash encoder)

**DevOps & Quality:**
- Version Control: Git + GitHub
- Local Development: **Docker + Docker Compose**
- CI/CD: **GitHub Actions (mandatory)**
- Code Quality: **SonarQube / SonarCloud**
- Frontend Hosting: Vercel
- Backend Hosting: Railway
- Database: Railway PostgreSQL
- Monitoring: Spring Boot Actuator (production)
- Load Testing: **Apache JMeter / k6**

### 6.2 Database Schema (MVP)

```sql
-- Users Table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL, -- BCrypt hashed
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'STAFF', 'CITIZEN')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Issues Table
CREATE TABLE issues (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(50) NOT NULL CHECK (category IN ('ROADS', 'STREETLIGHTS', 'WATER', 'GARBAGE', 'FOOTPATHS', 'OTHER')),
    location VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'IN_PROGRESS', 'WORKING', 'RESOLVED', 'CLOSED')),
    priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL' CHECK (priority IN ('NORMAL', 'HIGH')),
    citizen_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    assigned_staff_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Timeline Table
CREATE TABLE timeline (
    id BIGSERIAL PRIMARY KEY,
    issue_id BIGINT NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    updated_by_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    updated_by_role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for Performance
CREATE INDEX idx_issues_citizen_id ON issues(citizen_id);
CREATE INDEX idx_issues_assigned_staff_id ON issues(assigned_staff_id);
CREATE INDEX idx_issues_status ON issues(status);
CREATE INDEX idx_timeline_issue_id ON timeline(issue_id);
```

**Removed for MVP:**
- ❌ payments table
- ❌ upvotes table
- ❌ is_premium, is_blocked columns (add later)

### 6.3 API Contract (MVP Endpoints)

**Authentication:**
| Method | Endpoint | Auth | Role | Description |
|--------|----------|------|------|-------------|
| POST | /api/auth/register | No | - | Register new user |
| POST | /api/auth/login | No | - | Login user |
| POST | /api/auth/logout | Yes | All | Logout user |
| GET | /api/auth/me | Yes | All | Get current user |

**Issues:**
| Method | Endpoint | Auth | Role | Description |
|--------|----------|------|------|-------------|
| POST | /api/issues | Yes | CITIZEN | Create issue |
| GET | /api/issues | No | - | Get all issues (public, paginated) |
| GET | /api/issues/{id} | Yes | All | Get issue details |
| GET | /api/issues/my-issues | Yes | CITIZEN | Get user's issues |
| GET | /api/issues/assigned | Yes | STAFF | Get assigned issues |
| PUT | /api/issues/{id} | Yes | CITIZEN | Update own issue (owner only, pending only) |
| DELETE | /api/issues/{id} | Yes | CITIZEN | Delete own issue (owner only) |
| POST | /api/issues/{id}/assign | Yes | ADMIN | Assign staff to issue |
| PUT | /api/issues/{id}/status | Yes | STAFF | Update issue status (assigned staff only) |

**Dashboard:**
| Method | Endpoint | Auth | Role | Description |
|--------|----------|------|------|-------------|
| GET | /api/dashboard/citizen | Yes | CITIZEN | Get citizen stats |
| GET | /api/dashboard/staff | Yes | STAFF | Get staff stats |
| GET | /api/dashboard/admin | Yes | ADMIN | Get admin stats |

**Users:**
| Method | Endpoint | Auth | Role | Description |
|--------|----------|------|------|-------------|
| GET | /api/users/staff | Yes | ADMIN | Get all staff (for assignment dropdown) |

**Total MVP Endpoints:** 16 (manageable and testable)

### 6.4 Clean Architecture Implementation (Hexagonal Architecture)

**What is Clean Architecture?**
- **Domain** (core business logic) is independent of frameworks
- **Application** (use cases) orchestrates domain
- **Infrastructure** (database, web) depends on domain
- **Dependency Rule:** Inner layers don't know about outer layers

**Why for Mid-Level Portfolio?**
- Shows architectural maturity
- Easy to test (domain has no framework dependencies)
- Easy to change (swap database, swap framework)
- German companies love this approach

**Layer Structure:**

```
┌─────────────────────────────────────┐
│   Infrastructure (Adapters)        │  ← Controllers, Repositories, Config
│  ┌──────────────────────────────┐  │
│  │   Application (Use Cases)    │  │  ← Service interfaces, DTOs
│  │  ┌────────────────────────┐  │  │
│  │  │   Domain (Entities)    │  │  │  ← Pure business logic
│  │  └────────────────────────┘  │  │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘

Dependencies point inward →
```

**Benefits:**
- ✅ Domain logic is framework-agnostic (pure Java)
- ✅ 100% unit testable without mocks
- ✅ Easy to switch database (PostgreSQL → MongoDB)
- ✅ Easy to add new delivery mechanisms (REST → GraphQL)

### 6.5 Folder Structure (Clean Architecture)

**Backend (Spring Boot with Hexagonal Architecture):**
```
src/main/java/com/project/issuetracker/
├── domain/                              # Core Domain Layer (no framework dependencies)
│   ├── model/
│   │   ├── User.java                    # Domain entity (pure business logic)
│   │   ├── Issue.java
│   │   ├── Timeline.java
│   │   └── vo/                          # Value Objects
│   │       ├── Email.java
│   │       ├── IssueStatus.java
│   │       └── Priority.java
│   ├── port/                            # Ports (interfaces)
│   │   ├── in/                          # Input ports (use cases)
│   │   │   ├── CreateIssueUseCase.java
│   │   │   ├── AssignIssueUseCase.java
│   │   │   └── UpdateStatusUseCase.java
│   │   └── out/                         # Output ports (repositories)
│   │       ├── IssueRepository.java     # Interface (not Spring Data)
│   │       ├── UserRepository.java
│   │       └── TimelineRepository.java
│   ├── service/                         # Domain services (business rules)
│   │   ├── IssueAssignmentService.java
│   │   └── TimelineService.java
│   └── exception/                       # Domain exceptions
│       ├── IssueNotFoundException.java
│       └── IssueAlreadyAssignedException.java
│
├── application/                         # Application Layer (use case implementations)
│   ├── service/                         # Use case implementations
│   │   ├── IssueService.java            # Implements CreateIssueUseCase
│   │   ├── AuthService.java
│   │   └── DashboardService.java
│   ├── dto/                             # Data Transfer Objects
│   │   ├── request/
│   │   │   ├── CreateIssueRequest.java
│   │   │   └── LoginRequest.java
│   │   └── response/
│   │       ├── IssueResponse.java
│   │       └── AuthResponse.java
│   └── mapper/                          # DTO ↔ Domain mapping
│       ├── IssueMapper.java
│       └── UserMapper.java
│
├── infrastructure/                      # Infrastructure Layer (adapters)
│   ├── adapter/
│   │   ├── in/                          # Input adapters
│   │   │   └── web/                     # REST controllers
│   │   │       ├── IssueController.java
│   │   │       ├── AuthController.java
│   │   │       └── DashboardController.java
│   │   └── out/                         # Output adapters
│   │       └── persistence/             # Database adapters
│   │           ├── entity/              # JPA entities (separate from domain)
│   │           │   ├── IssueEntity.java
│   │           │   ├── UserEntity.java
│   │           │   └── TimelineEntity.java
│   │           ├── repository/          # Spring Data JPA repositories
│   │           │   ├── IssueJpaRepository.java
│   │           │   └── UserJpaRepository.java
│   │           └── adapter/             # Repository adapters
│   │               ├── IssueRepositoryAdapter.java  # Implements domain IssueRepository
│   │               └── UserRepositoryAdapter.java
│   ├── config/                          # Configuration
│   │   ├── SecurityConfig.java
│   │   ├── WebConfig.java
│   │   ├── OpenApiConfig.java           # Swagger configuration
│   │   └── ActuatorConfig.java          # Monitoring configuration
│   └── security/                        # Security infrastructure
│       ├── JwtTokenProvider.java
│       ├── JwtAuthenticationFilter.java
│       └── SecurityContext.java
│
└── shared/                              # Shared utilities
    ├── exception/
    │   └── GlobalExceptionHandler.java
    └── util/
        └── Constants.java

src/test/java/
├── domain/                              # Domain unit tests (no mocks needed)
│   ├── model/
│   └── service/
├── application/                         # Application unit tests
│   └── service/
├── infrastructure/
│   ├── adapter/
│   │   ├── in/web/                     # Controller tests (MockMvc)
│   │   └── out/persistence/            # Repository tests (TestContainers)
│   └── integration/                    # End-to-end integration tests
└── architecture/                       # Architecture tests (ArchUnit)
    └── HexagonalArchitectureTest.java  # Enforce layer dependencies
```

**Key Principles:**
1. **Domain** doesn't depend on anything (pure Java)
2. **Application** depends on Domain (orchestrates use cases)
3. **Infrastructure** depends on Application & Domain (implements adapters)
4. **Dependency Inversion:** Infrastructure implements ports defined in Domain

**Frontend (Next.js):**
```
src/
├── app/
│   ├── (auth)/
│   │   ├── login/
│   │   └── register/
│   ├── (public)/
│   │   ├── all-issues/
│   │   └── issue/[id]/
│   ├── dashboard/
│   │   ├── page.tsx (citizen dashboard)
│   │   ├── my-issues/
│   │   └── profile/
│   ├── staff/
│   │   ├── dashboard/
│   │   ├── assigned-issues/
│   │   └── profile/
│   ├── admin/
│   │   ├── dashboard/
│   │   ├── all-issues/
│   │   └── profile/
│   ├── layout.tsx
│   └── page.tsx
├── components/
│   ├── ui/ (shadcn components)
│   ├── layout/
│   │   ├── Navbar.tsx
│   │   └── Footer.tsx
│   ├── issues/
│   │   ├── IssueCard.tsx
│   │   ├── IssueForm.tsx
│   │   ├── IssueTimeline.tsx
│   │   └── StatusBadge.tsx
│   └── dashboard/
│       └── StatCard.tsx
├── lib/
│   ├── api.ts (fetch wrappers)
│   ├── auth.ts (JWT handling)
│   └── utils.ts
├── hooks/
│   ├── useAuth.ts
│   └── useIssues.ts
├── types/
│   └── index.ts
└── __tests__/
    ├── components/
    └── integration/
```

---

## 7. Development Workflow (German Best Practices)

### 7.1 Git Workflow

**Branch Strategy:**
```
main (production)
└── develop (integration)
    ├── feature/auth-login
    ├── feature/issue-create
    └── bugfix/timeline-sorting
```

**Commit Message Convention (Conventional Commits):**
```
feat(auth): implement JWT login endpoint
fix(issue): prevent unauthorized edit
test(issue): add integration tests for creation
docs(readme): update setup instructions
refactor(service): extract timeline logic
```

**Pull Request Process:**
1. Create feature branch from `develop`
2. Implement feature with tests (TDD preferred)
3. Run tests locally (must pass)
4. Push and create PR
5. Self-review checklist:
   - ✅ Tests pass
   - ✅ No console errors
   - ✅ Code formatted
   - ✅ Documentation updated
6. Merge to `develop`
7. Deploy `develop` to staging (optional)
8. Merge `develop` to `main` for production

### 7.2 Definition of Done (German Standard)

A feature is **DONE** when:

**Code:**
- ✅ Implementation complete per acceptance criteria
- ✅ Code formatted (Prettier/Checkstyle)
- ✅ No compiler warnings
- ✅ No unused imports/variables
- ✅ Code reviewed (self-review minimum)

**Testing:**
- ✅ Unit tests written and passing
- ✅ Integration tests written (where applicable)
- ✅ Coverage target met (80%+)
- ✅ Manual testing completed
- ✅ No regressions in existing features

**Documentation:**
- ✅ JavaDoc/TSDoc for public methods
- ✅ README updated (if needed)
- ✅ API documentation updated
- ✅ Comments for complex logic

**Deployment:**
- ✅ Merged to main branch
- ✅ Deployed to production
- ✅ Verified in production environment
- ✅ No errors in production logs

### 7.3 Sprint Planning (6-8 Week MVP)

**Updated Strategy:** Backend-first approach (build complete backend, then frontend)

---

#### **Sprint 1 (Week 1): Backend - Authentication** ✅ COMPLETED

**Completed Tasks:**
- [x] Project setup (Spring Boot + Maven + Docker)
- [x] Hexagonal Architecture folder structure (19 files)
- [x] Database schema design (users table)
- [x] Domain layer (User, Role, Use Cases, Repository interfaces)
- [x] Application layer (UserService with transaction management)
- [x] Infrastructure layer (JPA entities, JWT, Spring Security)
- [x] User registration API (POST /api/auth/register)
- [x] User login API (POST /api/auth/login)
- [x] JWT token generation & validation
- [x] Spring Security configuration (CORS, stateless sessions)
- [x] BCrypt password hashing
- [x] Soft delete support (GDPR compliance)
- [x] Comprehensive documentation (7 documents)

**Testing Status:**
- [x] Unit tests for User domain model (54 tests, 100% coverage) ✅
- [x] Integration tests for auth endpoints (100% coverage) ✅
- [x] Security tests (invalid tokens, expired tokens, etc.) ✅

**Documentation:**
- [x] Sprint 1 documentation folder (7 comprehensive docs)
- [x] Architecture flow diagrams
- [x] API reference guide

**Database:**
- [x] PostgreSQL running in Docker (port 5433)
- [x] Users table created by Hibernate
- [x] Soft delete column (deleted_at)

---

#### **Sprint 2 (Week 2-3): Backend - Issue Management** ✅ COMPLETED

**Focus:** Complete backend API for issue CRUD operations + Assignment + Status Management

**Completed Tasks:**
- [x] Issue domain layer ✅
  - [x] Issue.java entity (domain model with full validation)
  - [x] IssueStatus enum (OPEN, IN_PROGRESS, RESOLVED, CLOSED)
  - [x] Priority enum (LOW, MEDIUM, HIGH, CRITICAL)
  - [x] Category enum (INFRASTRUCTURE, SANITATION, SAFETY, ENVIRONMENT, OTHER)
  - [x] Issue validation (title 10-200, description 20-2000)
  - [x] IssueRepository interface (port/out)
  - [x] All 6 use case interfaces (Create, Update, Delete, Assign, ChangeStatus, Get)
- [x] Issue application layer ✅
  - [x] IssueService (implements all 6 use cases)
  - [x] Business rules enforcement (assignment, status transitions, role-based)
  - [x] Request/Response DTOs (using Java records)
  - [x] Domain ↔ DTO conversion
- [x] Issue infrastructure layer ✅
  - [x] IssueEntity (JPA entity with relationships)
  - [x] SpringDataIssueRepository (Spring Data JPA)
  - [x] JpaIssueRepositoryAdapter (adapter pattern)
  - [x] IssueController (REST API with 8 endpoints)
- [x] API endpoints (8 total) ✅
  - [x] POST /api/issues - Create issue
  - [x] GET /api/issues - List issues (with filters: status, priority, category, reportedBy, assignedTo)
  - [x] GET /api/issues/{id} - Get issue details
  - [x] PUT /api/issues/{id} - Update issue (owner or STAFF/ADMIN)
  - [x] PATCH /api/issues/{id}/assign - Assign/unassign issue to staff
  - [x] PATCH /api/issues/{id}/status - Change status (role-based rules)
  - [x] DELETE /api/issues/{id} - Soft delete issue (ADMIN only)
  - [x] POST /api/issues/{id}/restore - Restore deleted issue (ADMIN only)
- [x] Authorization ✅
  - [x] Role-based access control (CITIZEN sees own, STAFF sees assigned, ADMIN sees all)
  - [x] Citizens can only edit own issues
  - [x] Staff/Admin can edit all issues
  - [x] Only STAFF/ADMIN can assign issues
  - [x] Only STAFF role users can be assigned
  - [x] Status change permissions (IN_PROGRESS requires STAFF/ADMIN)
- [x] Tests ✅
  - [x] IssueTest.java - Domain tests (112 comprehensive tests)
  - [x] IssueServiceTest.java - Service tests (40+ tests)
  - [x] Integration tests for all API endpoints
  - [x] Authorization tests (role-based access)

**Testing Status:**
- [x] 112 domain tests (100% coverage) ✅
- [x] 40+ service tests (100% coverage) ✅
- [x] Integration tests passing ✅
- **Total Tests:** 166+ tests across Sprint 1 & 2

---

#### **Sprint 3 (Week 4): Backend - Timeline & Dashboards** 📍 NEXT

**Focus:** Add audit trail (Timeline) and Dashboard statistics

**NOTE:** Assignment and status update features were completed in Sprint 2 ✅

**Remaining Tasks:**
- [ ] Timeline domain layer
  - [ ] Timeline.java entity (audit trail)
  - [ ] TimelineEvent enum (CREATED, ASSIGNED, STATUS_CHANGED, UPDATED, DELETED, RESTORED)
  - [ ] TimelineRepository interface (port/out)
  - [ ] CreateTimelineEntryUseCase (port/in)
- [ ] Timeline application layer
  - [ ] TimelineService (create timeline entries)
  - [ ] Integrate with IssueService (auto-create entries on changes)
  - [ ] Timeline DTOs
- [ ] Timeline infrastructure layer
  - [ ] TimelineEntity (JPA entity)
  - [ ] SpringDataTimelineRepository
  - [ ] JpaTimelineRepositoryAdapter
  - [ ] GET /api/issues/{id}/timeline - Get timeline for issue
- [ ] Staff management (for frontend dropdown)
  - [ ] GET /api/users/staff - List all staff members (for assignment dropdown)
  - [ ] GET /api/users/{id} - Get user details (optional)
- [ ] Dashboard APIs
  - [ ] GET /api/dashboard/admin - Admin stats (total issues, by status, by priority, user counts)
  - [ ] GET /api/dashboard/staff - Staff stats (assigned issues, resolved count, today's assignments)
  - [ ] GET /api/dashboard/citizen - Citizen stats (own issues by status)
- [ ] Tests
  - [ ] Timeline entity tests
  - [ ] Timeline service tests
  - [ ] Timeline integration tests
  - [ ] Dashboard API tests
  - [ ] Infrastructure layer tests (reach 85%+ overall coverage)

**Already Completed in Sprint 2:**
- ✅ Assignment validation (only STAFF can be assigned)
- ✅ PATCH /api/issues/{id}/assign - Assign to staff
- ✅ PATCH /api/issues/{id}/status - Update status
- ✅ Role-based authorization tests

---

#### **Sprint 4 (Week 5): Frontend - Setup & Authentication**

**Focus:** Next.js project setup + login/register UI

**Tasks:**
- [ ] Next.js project setup
  - [ ] Create Next.js 14 app (App Router)
  - [ ] Install dependencies (TanStack Query, Tailwind, Shadcn UI)
  - [ ] Folder structure (app/, components/, lib/, hooks/)
  - [ ] Configure TypeScript + ESLint
- [ ] Authentication UI
  - [ ] /register page (sign up form)
  - [ ] /login page (login form)
  - [ ] JWT token storage (memory + httpOnly cookie approach)
  - [ ] Auth context/provider
  - [ ] Protected routes wrapper
  - [ ] Logout functionality
- [ ] API integration
  - [ ] Axios/fetch client setup
  - [ ] TanStack Query setup (mutations, queries)
  - [ ] Error handling (toast notifications)
  - [ ] Loading states
- [ ] Form validation
  - [ ] React Hook Form + Zod schemas
  - [ ] Client-side validation matching backend
- [ ] Tests
  - [ ] Component tests (Vitest + Testing Library)
  - [ ] E2E tests (Playwright - login/register flow)

---

#### **Sprint 5 (Week 6): Frontend - Issue Management**

**Focus:** Complete issue CRUD UI

**Tasks:**
- [ ] Issue list page
  - [ ] /issues - All issues (paginated table)
  - [ ] Filters (status, category, assigned to)
  - [ ] Search functionality
  - [ ] Role-based views (Citizen sees only own, Staff/Admin see all)
- [ ] Create issue page
  - [ ] /issues/new - Create issue form
  - [ ] Category dropdown
  - [ ] Location input (future: map integration)
  - [ ] Image upload placeholder
- [ ] Issue details page
  - [ ] /issues/[id] - Issue details
  - [ ] Timeline view (audit trail)
  - [ ] Edit/Delete buttons (if allowed)
  - [ ] Assign to staff (Admin/Staff only)
  - [ ] Update status (Staff only)
- [ ] My Issues page
  - [ ] /my-issues - Current user's issues
  - [ ] Filter by status
- [ ] API integration
  - [ ] TanStack Query for all issue endpoints
  - [ ] Optimistic updates
  - [ ] Cache invalidation
- [ ] Tests
  - [ ] Component tests (all pages)
  - [ ] E2E tests (create, edit, delete issue flow)

---

#### **Sprint 6 (Week 7): Frontend - Dashboards & Polish**

**Focus:** Dashboards, responsive design, UX polish

**Tasks:**
- [ ] Dashboards
  - [ ] /dashboard/admin - Admin dashboard (stats, charts)
  - [ ] /dashboard/staff - Staff dashboard (assigned issues)
  - [ ] /dashboard/citizen - Citizen dashboard (own issues stats)
- [ ] Responsive design
  - [ ] Mobile-first approach
  - [ ] Tablet breakpoints
  - [ ] Desktop optimization
  - [ ] Navigation menu (mobile drawer)
- [ ] UX improvements
  - [ ] Loading skeletons
  - [ ] Error boundaries
  - [ ] Toast notifications (success/error)
  - [ ] Confirmation dialogs (delete, logout)
  - [ ] Empty states (no issues, no results)
- [ ] Accessibility
  - [ ] Keyboard navigation
  - [ ] Screen reader support (ARIA labels)
  - [ ] Focus management
  - [ ] Color contrast (WCAG AA)
- [ ] Performance
  - [ ] Code splitting
  - [ ] Image optimization (Next.js Image component)
  - [ ] Lazy loading
- [ ] Tests
  - [ ] Accessibility tests (axe-core)
  - [ ] Performance tests (Lighthouse)
  - [ ] Cross-browser tests (Chrome, Firefox, Safari)

---

#### **Sprint 7 (Week 8): Quality, Documentation & Deployment**

**Focus:** Testing, documentation, deployment

**Tasks:**
- [ ] Backend quality
  - [ ] 85%+ test coverage (backend)
  - [ ] SonarQube analysis
  - [ ] Security audit (dependency check, OWASP)
  - [ ] Performance testing (JMeter - API load tests)
- [ ] Frontend quality
  - [ ] 80%+ test coverage (frontend)
  - [ ] E2E test suite (Playwright - all critical flows)
  - [ ] Bundle size optimization
  - [ ] Lighthouse score > 90
- [ ] Documentation
  - [ ] Update README.md (complete setup guide)
  - [ ] Swagger/OpenAPI docs (backend)
  - [ ] Component documentation (Storybook - optional)
  - [ ] Deployment guide
  - [ ] User manual (optional)
- [ ] Deployment
  - [ ] Backend → Railway/Render (Spring Boot)
  - [ ] Frontend → Vercel (Next.js)
  - [ ] Database → Railway PostgreSQL
  - [ ] Environment variables setup (production)
  - [ ] CI/CD pipeline (GitHub Actions)
  - [ ] Domain setup (optional)
- [ ] Production testing
  - [ ] Smoke tests (production environment)
  - [ ] Security headers (CSP, HSTS, etc.)
  - [ ] HTTPS enforcement
  - [ ] Error monitoring setup (Sentry - optional)
- [ ] Final polish
  - [ ] Code review (self-review with CodeRabbit)
  - [ ] Refactoring (remove dead code, optimize)
  - [ ] Performance optimization
  - [ ] Final bug fixes

---

### Updated Timeline Summary

| Sprint | Duration | Focus | Status |
|--------|----------|-------|--------|
| Sprint 1 | Week 1 | Backend - Authentication | ✅ **COMPLETED** (54 tests, 100% coverage) |
| Sprint 2 | Week 2-3 | Backend - Issue Management + Assignment + Status | ✅ **COMPLETED** (112 tests, 100% coverage) |
| Sprint 3 | Week 4 | Backend - Timeline & Dashboards | 📍 **NEXT** (Current Sprint) |
| Sprint 4 | Week 5 | Frontend - Auth UI | ⏳ Pending |
| Sprint 5 | Week 6 | Frontend - Issue UI | ⏳ Pending |
| Sprint 6 | Week 7 | Frontend - Dashboards & Polish | ⏳ Pending |
| Sprint 7 | Week 8 | Quality & Deployment | ⏳ Pending |

**Total:** 7-8 weeks to production-ready mid-level quality MVP

**Current Progress:**
- ✅ Sprint 1 complete - Authentication backend (54 tests)
- ✅ Sprint 2 complete - Issue Management with full CRUD, assignment, status management (112 tests)
- **Total Tests:** 166+ tests, 100% domain + application coverage

**Next Step:** Sprint 3 - Add Timeline tracking, Dashboard APIs, and reach 85%+ overall coverage

---

## 8. Mid-Level Quality Implementation Details

### 8.1 Docker Setup (Local Development)

**Why Docker for Local Development?**
- Consistent environment across team (German companies use this)
- Easy for reviewers/interviewers to run your project
- Production-like setup locally
- No "works on my machine" problems

**docker-compose.yml:**
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: issuetracker-db
    environment:
      POSTGRES_DB: issuetracker
      POSTGRES_USER: dev
      POSTGRES_PASSWORD: devpassword
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U dev"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: issuetracker-backend
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/issuetracker
      SPRING_DATASOURCE_USERNAME: dev
      SPRING_DATASOURCE_PASSWORD: devpassword
      JWT_SECRET: dev-secret-key-minimum-256-bits-long
    depends_on:
      postgres:
        condition: service_healthy
    volumes:
      - ./backend:/app
    command: ./mvnw spring-boot:run

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile.dev
    container_name: issuetracker-frontend
    ports:
      - "3000:3000"
    environment:
      NEXT_PUBLIC_API_URL: http://localhost:8080
    volumes:
      - ./frontend:/app
      - /app/node_modules
    command: npm run dev

volumes:
  postgres_data:
```

**Backend Dockerfile:**
```dockerfile
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY . .
RUN chmod +x mvnw
CMD ["./mvnw", "spring-boot:run"]
```

**Quick Start README section:**
```bash
# Clone repo
git clone <repo-url>

# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop
docker-compose down
```

**What Reviewers See:**
- ✅ Professional setup
- ✅ One command to run entire stack
- ✅ No environment setup needed

### 8.2 CI/CD Pipeline (GitHub Actions)

**Why CI/CD?**
- Automated testing on every push
- Code quality checks
- Prevents bugs from reaching main branch
- Shows professional workflow understanding

**.github/workflows/backend-ci.yml:**
```yaml
name: Backend CI/CD

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  test:
    runs-on: ubuntu-latest

    services:
      postgres:
        image: postgres:15-alpine
        env:
          POSTGRES_DB: test_db
          POSTGRES_USER: test
          POSTGRES_PASSWORD: test
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
        ports:
          - 5432:5432

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven

      - name: Run tests
        run: ./mvnw clean test
        env:
          SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/test_db
          SPRING_DATASOURCE_USERNAME: test
          SPRING_DATASOURCE_PASSWORD: test

      - name: Generate coverage report
        run: ./mvnw jacoco:report

      - name: Check coverage threshold
        run: |
          coverage=$(grep -oP 'Total.*?(\d+)%' target/site/jacoco/index.html | grep -oP '\d+')
          if [ "$coverage" -lt 85 ]; then
            echo "Coverage $coverage% is below 85% threshold"
            exit 1
          fi

      - name: SonarCloud Scan
        uses: SonarSource/sonarcloud-github-action@master
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}

      - name: Build
        run: ./mvnw clean package -DskipTests

      - name: Upload artifact
        uses: actions/upload-artifact@v4
        with:
          name: app-jar
          path: target/*.jar

  deploy:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'

    steps:
      - name: Deploy to Railway
        run: |
          # Railway auto-deploys from main branch
          echo "Deployment triggered"
```

**.github/workflows/frontend-ci.yml:**
```yaml
name: Frontend CI/CD

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  test:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'

      - name: Install dependencies
        run: npm ci

      - name: Run linter
        run: npm run lint

      - name: Run type check
        run: npm run type-check

      - name: Run unit tests
        run: npm test -- --coverage

      - name: Check coverage threshold
        run: |
          if ! npx istanbul check-coverage --lines 85; then
            echo "Coverage below 85% threshold"
            exit 1
          fi

      - name: Run E2E tests
        run: npm run test:e2e

      - name: Build
        run: npm run build

      - name: Lighthouse CI
        run: |
          npm install -g @lhci/cli
          lhci autorun
        env:
          LHCI_GITHUB_APP_TOKEN: ${{ secrets.LHCI_GITHUB_APP_TOKEN }}
```

**What This Achieves:**
- ✅ Automated testing on every commit
- ✅ Coverage enforcement (fails if <85%)
- ✅ Code quality checks (SonarCloud)
- ✅ Build verification
- ✅ Automated deployment to production

### 8.3 Monitoring & Observability

**Why Monitoring?**
- Production readiness demonstration
- Shows you think about operations, not just development
- German companies expect this from mid-level engineers

**Spring Boot Actuator Setup:**

**application.yml:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics, prometheus, loggers
      base-path: /actuator
  endpoint:
    health:
      show-details: always
      probes:
        enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}
      environment: ${spring.profiles.active}
```

**Available Endpoints (for portfolio demo):**
- `/actuator/health` - Health status (database, disk space)
- `/actuator/metrics` - Performance metrics
- `/actuator/info` - App info
- `/actuator/prometheus` - Metrics for monitoring tools

**Structured Logging (JSON format):**

**logback-spring.xml:**
```xml
<configuration>
    <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <includeMdcKeyName>traceId</includeMdcKeyName>
            <includeMdcKeyName>userId</includeMdcKeyName>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="JSON" />
    </root>
</configuration>
```

**Example Log Output:**
```json
{
  "timestamp": "2024-12-25T10:30:45.123Z",
  "level": "INFO",
  "message": "Issue assigned successfully",
  "logger": "com.project.issuetracker.application.service.IssueService",
  "thread": "http-nio-8080-exec-1",
  "traceId": "abc123",
  "userId": "user-456",
  "issueId": 789
}
```

**Frontend Performance Monitoring (Web Vitals):**

**src/lib/analytics.ts:**
```typescript
export function reportWebVitals(metric: any) {
  // Log to console in development
  console.log(metric);

  // Send to analytics in production
  if (process.env.NODE_ENV === 'production') {
    // Send to your analytics endpoint
    fetch('/api/analytics', {
      method: 'POST',
      body: JSON.stringify(metric),
    });
  }
}
```

**What Reviewers See:**
- ✅ Production-ready monitoring
- ✅ Observable system (can see what's happening)
- ✅ Structured logs (easy to parse and search)

### 8.4 Performance Benchmarks

**Why Performance Benchmarks?**
- Shows you care about performance
- Demonstrates testing beyond functional requirements
- Mid-level engineers understand performance implications

**Load Testing with k6:**

**k6/load-test.js:**
```javascript
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 20 },  // Ramp up to 20 users
    { duration: '1m', target: 20 },   // Stay at 20 users
    { duration: '30s', target: 0 },   // Ramp down to 0 users
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% of requests must complete below 500ms
    http_req_failed: ['rate<0.01'],   // Error rate must be below 1%
  },
};

export default function () {
  // Test GET all issues
  const res = http.get('http://localhost:8080/api/issues');

  check(res, {
    'status is 200': (r) => r.status === 200,
    'response time < 500ms': (r) => r.timings.duration < 500,
  });

  sleep(1);
}
```

**Run:**
```bash
k6 run k6/load-test.js
```

**Document Results in README:**
```markdown
## Performance Benchmarks

**Load Test Results** (20 concurrent users):
- Average response time: 124ms
- p95 response time: 287ms
- p99 response time: 412ms
- Requests per second: 156
- Error rate: 0%

**Database Query Performance:**
- Get all issues (paginated): 45ms
- Get issue details with timeline: 67ms
- Assign issue: 32ms
- Update status: 28ms

**Frontend Performance (Lighthouse):**
- Performance: 96/100
- Accessibility: 100/100
- Best Practices: 100/100
- SEO: 100/100
```

### 8.5 Security Audit (OWASP Top 10)

**Why Security Audit?**
- German companies take security seriously (GDPR culture)
- Shows professional awareness
- Demonstrates thoroughness

**OWASP Top 10 Checklist:**

```markdown
# Security Audit Checklist (OWASP Top 10 2021)

## A01: Broken Access Control
- [x] Role-based authorization on all endpoints
- [x] Users cannot access other users' data
- [x] Staff can only see assigned issues
- [x] Authorization tested in integration tests

## A02: Cryptographic Failures
- [x] Passwords hashed with BCrypt (10 rounds)
- [x] JWT secrets in environment variables
- [x] HTTPS enforced in production
- [x] Sensitive data not logged

## A03: Injection
- [x] JPA/Hibernate prevents SQL injection
- [x] Input validation with Jakarta Validation
- [x] Parameterized queries only
- [x] No dynamic SQL construction

## A04: Insecure Design
- [x] Authentication required for sensitive actions
- [x] Rate limiting implemented (future)
- [x] Business logic in domain layer
- [x] Security by design

## A05: Security Misconfiguration
- [x] Security headers configured
- [x] CORS properly configured
- [x] No default credentials
- [x] Error messages don't leak info

## A06: Vulnerable Components
- [x] Dependencies scanned (Dependabot)
- [x] Regular dependency updates
- [x] No known vulnerable dependencies

## A07: Authentication Failures
- [x] Strong password requirements enforced
- [x] JWT token expiration (15min)
- [x] Refresh token rotation
- [x] Account lockout (future)

## A08: Software & Data Integrity
- [x] CI/CD pipeline integrity
- [x] Code review required
- [x] Input validation
- [x] Data integrity constraints

## A09: Logging Failures
- [x] Security events logged
- [x] Failed login attempts logged
- [x] Audit trail (timeline)
- [x] No sensitive data in logs

## A10: Server-Side Request Forgery
- [x] No user-controlled URLs
- [x] Input validation on all external calls
- [x] Not applicable (no external requests)
```

**Security Headers Configuration:**

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .headers(headers -> headers
            .contentSecurityPolicy("default-src 'self'")
            .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
            .frameOptions(frame -> frame.deny())
            .httpStrictTransportSecurity(hsts -> hsts
                .includeSubDomains(true)
                .maxAgeInSeconds(31536000))
        );
    return http.build();
}
```

### 8.6 Architecture Decision Records (ADRs)

**Why ADRs?**
- Document WHY you made decisions
- Shows thinking process
- German companies LOVE this
- Demonstrates maturity

**ADR Template (`docs/adr/0001-use-hexagonal-architecture.md`):**
```markdown
# ADR 001: Use Hexagonal Architecture

**Date:** 2024-12-25
**Status:** Accepted
**Context:** Need to choose application architecture for issue tracking system
**Decision:** Implement Hexagonal (Ports & Adapters) Architecture

## Context

Building a full-stack issue tracking system that needs to:
- Be testable without framework dependencies
- Allow easy changes to database or delivery mechanism
- Demonstrate mid-level architectural thinking for German job market

## Decision

Implement Hexagonal Architecture with three layers:
1. Domain Layer (business logic, entities, ports)
2. Application Layer (use cases, DTOs, mappers)
3. Infrastructure Layer (adapters for web, database, external services)

## Consequences

### Positive
- Domain logic is 100% testable without mocks
- Easy to swap PostgreSQL for another database
- Easy to add GraphQL alongside REST
- Demonstrates architectural maturity to hiring managers
- Follows German market best practices

### Negative
- More files and folders (higher initial complexity)
- Requires discipline to maintain layer boundaries
- Slight learning curve for hexagonal concepts

### Mitigations
- Use ArchUnit tests to enforce layer dependencies automatically
- Document architecture clearly in README
- Provide examples in code comments

## Alternatives Considered

**1. Traditional Layered Architecture (MVC)**
- Simpler to implement
- Database entities used directly in controllers
- Rejected: Harder to test, tight coupling to framework

**2. Microservices**
- Better scalability
- Rejected: Overkill for portfolio project, increases complexity

## References
- https://netflixtechblog.com/ready-for-changes-with-hexagonal-architecture-b315ec967749
- Clean Architecture by Robert C. Martin
```

**Create ADRs for key decisions:**
- `/docs/adr/0001-use-hexagonal-architecture.md`
- `/docs/adr/0002-postgresql-over-mongodb.md`
- `/docs/adr/0003-jwt-authentication.md`
- `/docs/adr/0004-nextjs-app-router.md`
- `/docs/adr/0005-tanstack-query-state-management.md`

---

### 7.4 Testing Checklist

**Before Merge:**
- [ ] All unit tests pass (`./mvnw test`, `npm test`)
- [ ] All integration tests pass
- [ ] Code coverage ≥80%
- [ ] No failing E2E tests
- [ ] Manual testing in dev environment
- [ ] No console errors/warnings
- [ ] Lighthouse score >90 (frontend)

**Before Deployment:**
- [ ] All tests pass in CI/CD (if setup)
- [ ] Manual testing in staging
- [ ] Database migrations tested
- [ ] Environment variables verified
- [ ] Security headers checked
- [ ] Performance benchmarks met
- [ ] Rollback plan documented

---

## 8. Quality Gates (German Hiring Manager Expectations)

### 8.1 Code Review Checklist

When reviewing your own code (before considering it done):

**Functionality:**
- ✅ Feature works as specified
- ✅ Edge cases handled
- ✅ Error cases handled gracefully
- ✅ Input validation working

**Code Quality:**
- ✅ No code duplication (DRY principle)
- ✅ Functions are single-responsibility
- ✅ Clear variable/function names
- ✅ No magic numbers (use constants)
- ✅ Consistent formatting

**Security:**
- ✅ No sensitive data in logs
- ✅ SQL injection prevented (using JPA)
- ✅ XSS prevented (React escapes by default)
- ✅ Authorization checks in place
- ✅ Passwords never logged

**Performance:**
- ✅ No N+1 query problems
- ✅ Database queries optimized
- ✅ Proper indexing
- ✅ No unnecessary re-renders (React)
- ✅ Images optimized (when added)

**Testing:**
- ✅ Happy path tested
- ✅ Error paths tested
- ✅ Authorization tested
- ✅ Edge cases tested

### 8.2 Portfolio Presentation Checklist

**What German recruiters will check:**

**On GitHub:**
- ✅ Clean commit history (meaningful messages)
- ✅ 20+ frontend commits, 12+ backend commits
- ✅ README.md is comprehensive
- ✅ No secrets in code (use .env)
- ✅ .gitignore properly configured
- ✅ Tests are visible in repo

**On Live Site:**
- ✅ Site loads quickly (<3s)
- ✅ No broken links
- ✅ Responsive on mobile
- ✅ No console errors
- ✅ Error messages are user-friendly
- ✅ Form validation works
- ✅ Test accounts work (provided credentials)

**In Code:**
- ✅ Clean architecture (layers separated)
- ✅ Consistent code style
- ✅ Meaningful comments
- ✅ No commented-out code
- ✅ No TODOs in production code

**In Documentation:**
- ✅ Setup instructions are clear
- ✅ Tech stack listed with versions
- ✅ Environment variables documented
- ✅ API endpoints documented
- ✅ Known limitations mentioned (honest)

---

## 9. Deployment Strategy

### 9.1 Environment Variables

**Backend (.env):**
```
# Database
DATABASE_URL=postgresql://user:password@host:5432/dbname

# JWT
JWT_SECRET=your-secret-key-min-256-bits
JWT_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=604800000

# Application
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080

# CORS
CORS_ALLOWED_ORIGINS=https://your-frontend.vercel.app
```

**Frontend (.env.local):**
```
NEXT_PUBLIC_API_URL=https://your-backend.railway.app
NEXT_PUBLIC_APP_NAME=Infrastructure Issue Tracker
```

### 9.2 Deployment Steps

**Backend (Railway):**
1. Create Railway account
2. New Project → Deploy from GitHub
3. Select Spring Boot repo
4. Add PostgreSQL database (same project)
5. Configure environment variables
6. Deploy
7. Note the public URL

**Frontend (Vercel):**
1. Create Vercel account
2. Import GitHub repo
3. Framework: Next.js (auto-detected)
4. Add environment variables
5. Deploy
6. Note the public URL

**Post-Deployment:**
1. Test all features in production
2. Verify database connection
3. Check CORS configuration
4. Test with provided credentials
5. Run Lighthouse audit
6. Check for console errors

---

## 10. MVP Success Metrics

### 10.1 Feature Completeness

**MVP is complete when:**
- ✅ Users can register and login
- ✅ Citizens can create, view, edit, delete issues
- ✅ Admin can assign issues to staff
- ✅ Staff can update issue status
- ✅ Timeline tracks all changes
- ✅ Dashboards show basic stats
- ✅ All roles work correctly
- ✅ Authorization prevents unauthorized actions

### 10.2 Quality Metrics

**Minimum acceptable:**
- ✅ Test coverage: 80%+
- ✅ Lighthouse score: 90+
- ✅ No critical security vulnerabilities
- ✅ API response time <500ms (average)
- ✅ Page load time <3s
- ✅ Zero console errors in production
- ✅ Mobile responsive (all pages)

### 10.3 German Market Readiness

**Portfolio is job-application-ready when:**
- ✅ README is professional and complete
- ✅ Code quality passes self-review
- ✅ Deployed and accessible
- ✅ Test accounts work
- ✅ No embarrassing bugs
- ✅ GitHub shows consistent commit history
- ✅ You can explain all architectural decisions
- ✅ You can demo it confidently in interview

---

## 11. Post-MVP Roadmap

### Phase 2 Features (Add after getting job offers):
1. Image upload (Cloudinary)
2. Google OAuth
3. Payment system (Stripe)
4. Upvote system
5. Advanced search & filters
6. User management (block/unblock)
7. Staff CRUD operations
8. Email notifications
9. Charts and analytics
10. PDF generation

### Phase 3 Features (Long-term):
1. Real-time notifications (WebSocket)
2. Mobile app (React Native)
3. Advanced analytics (ML-powered)
4. Multi-language support
5. Offline support (PWA)

---

## 12. Resources

### Documentation Templates
- `/docs/API.md` - API endpoint documentation
- `/docs/ARCHITECTURE.md` - System architecture
- `/docs/SETUP.md` - Development setup guide
- `/docs/TESTING.md` - Testing strategy
- `/docs/DEPLOYMENT.md` - Deployment guide

### Learning Resources
- Spring Boot: https://spring.io/guides/gs/rest-service/
- Spring Security + JWT: https://www.bezkoder.com/spring-boot-jwt-authentication/
- Next.js App Router: https://nextjs.org/docs/app
- TanStack Query: https://tanstack.com/query/latest/docs/react/overview
- PostgreSQL: https://www.postgresql.org/docs/
- Testing: https://spring.io/guides/gs/testing-web/

### Tools
- Code Quality: SonarQube / SonarLint
- API Testing: Postman / Bruno
- Database Design: dbdiagram.io / draw.io
- Architecture Diagrams: excalidraw.com
- Performance: Lighthouse / WebPageTest

---

---

## 13. Summary: What Makes This Mid-Level Quality

### Features vs Quality Comparison

| Aspect | Entry-Level Approach | **This Project (Mid-Level)** |
|--------|---------------------|------------------------------|
| **Features** | Many features, basic quality | **Core features, exceptional quality** |
| **Architecture** | MVC layers | **Hexagonal Architecture** |
| **Testing** | Some tests, ~50% coverage | **TDD, 85%+ coverage** |
| **CI/CD** | Manual deployment | **GitHub Actions pipeline** |
| **Monitoring** | No monitoring | **Actuator + structured logging** |
| **Performance** | Not measured | **Load tested + benchmarked** |
| **Security** | Basic auth | **OWASP Top 10 addressed** |
| **Documentation** | Basic README | **README + ADRs + API docs** |
| **Local Dev** | Manual setup | **Docker Compose (1 command)** |
| **Code Quality** | Works | **SonarQube scanned, <2% duplication** |

### What German Hiring Managers Will See

**In Your GitHub:**
- ✅ Clean Architecture (not just MVC)
- ✅ 85%+ test coverage badge
- ✅ Green CI/CD pipeline
- ✅ Comprehensive README with benchmarks
- ✅ ADRs showing your thinking
- ✅ SonarQube quality gate passing

**In Interviews:**
- ✅ Can explain architectural decisions (ADRs)
- ✅ Can discuss performance (load test results)
- ✅ Can demonstrate monitoring (Actuator endpoints)
- ✅ Can show security awareness (OWASP checklist)
- ✅ Can explain testing strategy (TDD process)

**On Live Site:**
- ✅ Loads fast (<3s, Lighthouse 95+)
- ✅ No console errors
- ✅ Responsive design
- ✅ Docker Compose for easy local setup
- ✅ Swagger UI for API exploration

### Why This Stands Out

**Entry-level portfolios typically have:**
- Many features
- Low test coverage
- No CI/CD
- No monitoring
- No performance benchmarks
- Simple architecture

**This portfolio demonstrates:**
- ✅ **Production thinking** (monitoring, logging, performance)
- ✅ **Quality focus** (85% tests, SonarQube, security audit)
- ✅ **Architectural maturity** (hexagonal architecture, ADRs)
- ✅ **Professional workflow** (CI/CD, Docker, code quality gates)
- ✅ **Operational awareness** (observability, benchmarks)

**Result:** Hiring managers recognize this as **mid-level quality work**, not entry-level.

---

## 14. FINAL NOTE FOR GERMAN MARKET

**German companies value:**
1. **Quality over speed** - Take time to do it right
2. **Testing** - Not optional, it's mandatory (85%+)
3. **Documentation** - They WILL read your README, ADRs, and code
4. **Clean code** - They WILL review your GitHub architecture
5. **Honesty** - Mention limitations, don't hide them
6. **Production thinking** - Show you understand operations, not just development

**This MVP strategy:**
- ❌ NOT about having the most features
- ✅ About demonstrating **professional engineering practices**
- ✅ About showing **mid-level thinking** despite being entry-level experience
- ✅ About **quality that impresses** hiring managers

**Key Differentiator:**
> "Entry-level developers show they can code. Mid-level developers show they can engineer."

**This project positions you as an engineer, not just a coder.**

**Timeline:**
- **Weeks 1-6:** Core features with mid-level quality practices
- **Weeks 7-8:** Polish, documentation, benchmarks, deploy
- **Total:** 6-8 weeks to job-application-ready portfolio
- **Then:** Start applying while you continue learning

**Expected Outcome:**
A portfolio that makes German hiring managers think:
> "This candidate writes code like someone with 2-3 years experience, not a fresh graduate. Let's interview them."

---

**Document Version:** 3.0 (Mid-Level Quality Standards)
**Last Updated:** December 25, 2024
**Quality Level:** Mid-Level Portfolio (Entry-Level Experience)
**Target Timeline:** 6-8 weeks
**Next Document:** `/docs/database-schema.md`
**Next Action:** Review requirements → Create DB schema → Create ADR template → Start Sprint 1
