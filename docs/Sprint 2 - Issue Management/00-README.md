# Sprint 2 - Issue Management (Backend)

**Sprint:** 2 of 7
**Status:** 🚧 In Progress
**Started:** January 2, 2026
**Duration:** 1-2 weeks

---

## 🎯 Sprint Goals

Build the core Issue Management functionality (backend only):

1. **Domain Model:** Issue entity with validation
2. **CRUD Operations:** Create, Read, Update, Delete issues
3. **Issue Lifecycle:** Open → In Progress → Resolved → Closed
4. **Assignment:** Assign issues to staff members
5. **Priority & Category:** Categorize and prioritize issues
6. **Search & Filter:** Find issues by status, priority, assignee

---

## 📋 Features Planned

### Core Features

- [ ] Issue domain model (title, description, status, priority, category)
- [ ] Create issue (citizens can report issues)
- [ ] View issue details
- [ ] List all issues (with filtering)
- [ ] Update issue details
- [ ] Assign issue to staff
- [ ] Change issue status
- [ ] Change priority
- [ ] Soft delete issue

### Business Rules

- ✅ Only authenticated users can create issues
- ✅ Citizens can only view/edit their own issues
- ✅ Staff can view/edit all issues
- ✅ Admin can view/edit/delete all issues
- ✅ Only staff/admin can assign issues
- ✅ Only staff/admin can change status to "In Progress"
- ✅ Title required (10-200 characters)
- ✅ Description required (20-2000 characters)
- ✅ Location required
- ✅ Status transition rules enforced

---

## 📚 Documentation (This Folder)

**Sprint 2 Specific Documents:**

1. **00-README.md** - This file (navigation)
2. **01-sprint-plan.md** - Detailed sprint planning
3. **02-issue-domain.md** - Issue domain model documentation
4. **03-api-endpoints.md** - REST API specification
5. **04-test-results.md** - Test results and coverage
6. (More files will be added as sprint progresses)

---

## 🧪 Testing Documentation (Project-Wide)

📄 **[../Test-Strategy.md](../Test-Strategy.md)** - Overall testing approach
📄 **[../Testing-Guidelines.md](../Testing-Guidelines.md)** - How to write tests
📄 **[../Test-Plan-Template.md](../Test-Plan-Template.md)** - Reusable template

**Sprint 2 Test Plan:** Will be created based on template

---

## 🏗️ Architecture

Following **Hexagonal Architecture** (same as Sprint 1):

```
domain/
├── model/
│   ├── Issue.java          ← Domain model
│   ├── IssueStatus.java    ← Enum
│   ├── Priority.java       ← Enum
│   └── Category.java       ← Enum
├── port/
│   ├── in/
│   │   ├── CreateIssueUseCase.java
│   │   ├── UpdateIssueUseCase.java
│   │   ├── AssignIssueUseCase.java
│   │   └── CloseIssueUseCase.java
│   └── out/
│       └── IssueRepository.java

application/
└── service/
    └── IssueService.java

infrastructure/
├── adapter/
│   ├── in/web/
│   │   └── IssueController.java
│   └── out/persistence/
│       ├── IssueEntity.java
│       ├── SpringDataIssueRepository.java
│       └── JpaIssueRepositoryAdapter.java
```

---

## 📊 Success Criteria

- [ ] All issue CRUD operations working
- [ ] Business rules enforced
- [ ] Role-based access control implemented
- [ ] 85%+ test coverage
- [ ] API documented
- [ ] Design patterns documented
- [ ] All tests passing

---

## 🔗 Dependencies

**Requires from Sprint 1:**
- ✅ User authentication (JWT)
- ✅ User roles (CITIZEN, STAFF, ADMIN)
- ✅ Security configuration

**New Dependencies:**
- None (using existing Spring Boot stack)

---

## 📅 Timeline

| Day | Tasks |
|-----|-------|
| **Day 1-2** | Domain model + Use cases + Tests |
| **Day 3-4** | Service layer + Repository + Tests |
| **Day 5-6** | REST API + Integration tests |
| **Day 7** | Documentation + Coverage review |

---

**Previous Sprint:** [Sprint 1 - Authentication](../Sprint%201%20-%20Authentication/)
**Next Sprint:** Sprint 3 - Comments & Attachments
