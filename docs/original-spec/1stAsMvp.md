# Project Requirements Document
## Public Infrastructure Issue Reporting System

**Version:** 2.0 (German Market Standards + MVP Focus)
**Last Updated:** December 25, 2024
**Target Market:** German Tech Companies
**Development Approach:** Agile/Incremental with TDD

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

**Rationale:** MVP focuses on core workflow: Report → Assign → Resolve. This is sufficient for portfolio demonstration and job applications.

---

### 2.3 Post-MVP Enhancements (After Job Search)

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

| Metric | Minimum | Target | Tool |
|--------|---------|--------|------|
| Test Coverage | 70% | 80%+ | JaCoCo (Backend), Jest (Frontend) |
| Code Duplication | <5% | <3% | SonarQube |
| Cyclomatic Complexity | <10 per method | <7 | SonarQube |
| Code Review | 100% PRs | 100% | GitHub PR process |
| Documentation | All public APIs | + internal complexity | JavaDoc, TSDoc |

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

**Target:** 80% coverage before deployment

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

### 6.1 Tech Stack (MVP)

**Frontend:**
- Framework: Next.js 14+ (App Router)
- Language: TypeScript 5+
- UI: Shadcn UI + Tailwind CSS 3+
- State Management: TanStack Query v5
- Form Handling: React Hook Form + Zod
- HTTP Client: Fetch API (built-in)
- Testing: Jest + React Testing Library + Playwright

**Backend:**
- Framework: Spring Boot 3.2+
- Language: Java 17
- Security: Spring Security 6 + JWT (jjwt 0.12+)
- Data Access: Spring Data JPA + Hibernate
- Validation: Jakarta Validation (Hibernate Validator)
- Testing: JUnit 5 + Mockito + TestContainers
- Database: PostgreSQL 15+

**DevOps:**
- Version Control: Git + GitHub
- Frontend Hosting: Vercel
- Backend Hosting: Railway
- Database: Railway PostgreSQL
- CI/CD: GitHub Actions (optional for MVP)

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

### 6.4 Folder Structure

**Backend (Spring Boot):**
```
src/main/java/com/project/issuetracker/
├── config/
│   ├── SecurityConfig.java
│   └── WebConfig.java
├── controller/
│   ├── AuthController.java
│   ├── IssueController.java
│   ├── DashboardController.java
│   └── UserController.java
├── service/
│   ├── AuthService.java
│   ├── IssueService.java
│   ├── TimelineService.java
│   └── UserService.java
├── repository/
│   ├── UserRepository.java
│   ├── IssueRepository.java
│   └── TimelineRepository.java
├── model/
│   ├── User.java
│   ├── Issue.java
│   └── Timeline.java
├── dto/
│   ├── request/
│   └── response/
├── security/
│   ├── JwtTokenProvider.java
│   └── JwtAuthenticationFilter.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   └── CustomExceptions.java
└── util/
    └── Constants.java

src/test/java/
├── controller/
├── service/
└── integration/
```

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

### 7.3 Sprint Planning (4-6 Week MVP)

**Sprint 1 (Week 1): Foundation & Auth**
- [ ] Project setup (frontend + backend)
- [ ] Database schema implementation
- [ ] User registration (API + UI)
- [ ] User login (API + UI)
- [ ] JWT middleware
- [ ] Protected routes
- [ ] Unit tests for auth
- [ ] Integration tests for auth

**Sprint 2 (Week 2-3): Core Issue Management**
- [ ] Create issue (API + UI)
- [ ] View all issues (API + UI with pagination)
- [ ] Issue details page
- [ ] Timeline implementation
- [ ] My Issues page (Citizen)
- [ ] Edit issue (API + UI)
- [ ] Delete issue (API + UI)
- [ ] Unit + integration tests

**Sprint 3 (Week 4): Admin & Staff Features**
- [ ] Admin dashboard (stats API + UI)
- [ ] Get staff list API
- [ ] Assign issue (API + UI)
- [ ] Staff dashboard (stats API + UI)
- [ ] Assigned issues page (Staff)
- [ ] Update status (API + UI)
- [ ] Timeline entries for all actions
- [ ] Unit + integration tests

**Sprint 4 (Week 5): Polish & Quality**
- [ ] Responsive design (mobile/tablet)
- [ ] Error handling (global + specific)
- [ ] Loading states
- [ ] Input validation (frontend + backend)
- [ ] E2E tests (Playwright - critical flows)
- [ ] Performance optimization
- [ ] Security audit
- [ ] Code quality review (SonarQube)

**Sprint 5 (Week 6): Documentation & Deployment**
- [ ] Comprehensive README.md
- [ ] API documentation (Swagger)
- [ ] Architecture diagrams
- [ ] Deploy to Vercel (frontend)
- [ ] Deploy to Railway (backend + DB)
- [ ] Environment variables setup
- [ ] Production testing
- [ ] Final code review

**Total:** 6 weeks to production-ready MVP

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

**FINAL NOTE FOR GERMAN MARKET:**

German companies value:
1. **Quality over speed** - Take time to do it right
2. **Testing** - Not optional, it's mandatory
3. **Documentation** - They will read your README
4. **Clean code** - They will review your GitHub
5. **Honesty** - Mention limitations, don't hide them

**This MVP focuses on demonstrating professional engineering practices over feature bloat. Better to have 10 features working perfectly than 30 features with bugs.**

**Target: 6 weeks to a portfolio project that will impress German hiring managers.**

---

**Document Version:** 2.0 (German Market Standards + MVP Focus)
**Last Updated:** December 25, 2024
**Next Document to Create:** `/docs/database-schema.md`
**Next Action:** Review this requirements doc → Create database schema → Start Sprint 1
