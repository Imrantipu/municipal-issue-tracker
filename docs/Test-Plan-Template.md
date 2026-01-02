# Test Plan Template

**Use this template for each sprint to plan testing activities.**

---

## Sprint Information

- **Sprint Number:** [e.g., Sprint 2]
- **Sprint Name:** [e.g., Issue Management]
- **Sprint Duration:** [e.g., 2 weeks]
- **Test Plan Author:** [Your Name]
- **Date Created:** [YYYY-MM-DD]
- **Last Updated:** [YYYY-MM-DD]

---

## 1. Test Objectives

**What are we testing in this sprint?**

- [ ] Objective 1: [e.g., Verify Issue creation functionality]
- [ ] Objective 2: [e.g., Validate Issue assignment workflow]
- [ ] Objective 3: [e.g., Test Issue search and filtering]

---

## 2. Scope

### In Scope

**Features to be tested:**

1. [Feature 1]
2. [Feature 2]
3. [Feature 3]

### Out of Scope

**Features NOT tested in this sprint:**

1. [Feature X - will be tested in Sprint 3]
2. [Feature Y - not implemented yet]

---

## 3. Test Types

| Test Type | Count | Tools | Owner | Status |
|-----------|-------|-------|-------|--------|
| Unit Tests (Domain) | [e.g., 40] | JUnit 5 | [Name] | ⏳ Pending |
| Unit Tests (Service) | [e.g., 20] | JUnit 5 + Mockito | [Name] | ⏳ Pending |
| Integration Tests (API) | [e.g., 15] | MockMvc | [Name] | ⏳ Pending |
| Integration Tests (DB) | [e.g., 10] | TestContainers | [Name] | ⏳ Pending |
| **Total** | **[85]** | - | - | - |

---

## 4. Test Cases

### 4.1 Domain Layer Tests

**Class:** [e.g., Issue.java]

| Test ID | Test Name | Priority | Status |
|---------|-----------|----------|--------|
| DM-01 | should_CreateIssue_When_ValidData | High | ⏳ Pending |
| DM-02 | should_ThrowException_When_TitleIsNull | High | ⏳ Pending |
| DM-03 | should_ThrowException_When_TitleTooShort | Medium | ⏳ Pending |
| DM-04 | should_AssignIssue_When_StaffUser | High | ⏳ Pending |

### 4.2 Service Layer Tests

**Class:** [e.g., IssueService.java]

| Test ID | Test Name | Priority | Status |
|---------|-----------|----------|--------|
| SV-01 | should_CreateIssue_When_CitizenSubmits | High | ⏳ Pending |
| SV-02 | should_ThrowException_When_Unauthorized | High | ⏳ Pending |

### 4.3 API Integration Tests

**Endpoint:** [e.g., POST /api/issues]

| Test ID | Test Name | Expected Result | Status |
|---------|-----------|-----------------|--------|
| API-01 | should_Return201_When_IssueCreated | 201 Created + Issue JSON | ⏳ Pending |
| API-02 | should_Return401_When_NotAuthenticated | 401 Unauthorized | ⏳ Pending |
| API-03 | should_Return400_When_InvalidData | 400 Bad Request | ⏳ Pending |

---

## 5. Coverage Goals

| Component | Current Coverage | Target Coverage | Gap |
|-----------|------------------|-----------------|-----|
| Domain Layer | 0% | 100% | +100% |
| Application Layer | 0% | 95% | +95% |
| Infrastructure Layer | 0% | 80% | +80% |
| **Overall** | **0%** | **85%** | **+85%** |

---

## 6. Test Environment

### Required Tools

- [ ] JDK 17
- [ ] Maven 3.9+
- [ ] Docker (for TestContainers)
- [ ] PostgreSQL 15 (Docker image)
- [ ] IntelliJ IDEA / VS Code

### Test Database

- **Type:** PostgreSQL 15 (via TestContainers)
- **Port:** Dynamic (assigned by TestContainers)
- **Data:** Test fixtures only

### Test Configuration

```properties
# src/test/resources/application-test.properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
jwt.secret=test-secret-key-256-bits
```

---

## 7. Test Data

### Test Users

| Username | Email | Role | Password | Purpose |
|----------|-------|------|----------|---------|
| admin_user | admin@test.com | ADMIN | test123 | Admin operations |
| staff_user | staff@test.com | STAFF | test123 | Staff operations |
| citizen_user | citizen@test.com | CITIZEN | test123 | Citizen operations |

### Test Issues

Create test fixtures for:
- [ ] Open issue
- [ ] In Progress issue
- [ ] Closed issue
- [ ] High priority issue
- [ ] Low priority issue

---

## 8. Entry Criteria

**Before starting testing:**

- [ ] All features implemented
- [ ] Code review completed
- [ ] Unit tests written
- [ ] Integration tests written
- [ ] Test environment set up
- [ ] Test data prepared

---

## 9. Exit Criteria

**Testing is complete when:**

- [ ] All planned tests executed
- [ ] Coverage target achieved (85%+)
- [ ] All critical bugs fixed
- [ ] No high-priority bugs remaining
- [ ] Test report generated
- [ ] Code coverage report reviewed

---

## 10. Schedule

| Activity | Start Date | End Date | Duration | Owner |
|----------|-----------|----------|----------|-------|
| Write unit tests | [Date] | [Date] | 2 days | [Name] |
| Write integration tests | [Date] | [Date] | 2 days | [Name] |
| Execute all tests | [Date] | [Date] | 1 day | [Name] |
| Fix bugs | [Date] | [Date] | 1 day | [Name] |
| Generate reports | [Date] | [Date] | 0.5 days | [Name] |
| **Total** | - | - | **5.5 days** | - |

---

## 11. Risks and Mitigation

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| TestContainers setup issues | Medium | High | Use H2 as fallback |
| Time constraints | High | Medium | Prioritize critical tests |
| Flaky tests | Medium | Medium | Fix immediately when found |
| Low coverage | Low | High | Set JaCoCo threshold |

---

## 12. Test Execution

### How to Run Tests

```bash
# All tests
./mvnw clean test

# Unit tests only
./mvnw test -Dtest="*Test"

# Integration tests only
./mvnw test -Dtest="*IntegrationTest"

# With coverage
./mvnw clean test jacoco:report
```

### Success Criteria

| Metric | Target | Actual | Status |
|--------|--------|--------|--------|
| Tests Passing | 100% | [%] | ⏳ |
| Code Coverage | 85% | [%] | ⏳ |
| Build Time | < 5 min | [time] | ⏳ |
| Test Execution Time | < 2 min | [time] | ⏳ |

---

## 13. Defect Management

### Bug Tracking

| Bug ID | Severity | Description | Status | Fixed By |
|--------|----------|-------------|--------|----------|
| BUG-001 | Critical | [Description] | Open | - |
| BUG-002 | High | [Description] | Fixed | [Name] |

### Severity Levels

- **Critical:** System crash, data loss, security vulnerability
- **High:** Major feature broken, workaround exists
- **Medium:** Minor feature issue, cosmetic problem
- **Low:** Enhancement, nice-to-have

---

## 14. Deliverables

- [ ] Test code (all test classes)
- [ ] Test coverage report (JaCoCo HTML)
- [ ] Test execution report
- [ ] Bug report (if any)
- [ ] Test summary document

---

## 15. Approval

| Role | Name | Signature | Date |
|------|------|-----------|------|
| Test Plan Author | [Your Name] | | |
| Tech Lead | [Name] | | |
| Product Owner | [Name] | | |

---

## Appendix A: Test Case Details

### TC-001: Register User with Valid Data

**Objective:** Verify user can register with valid credentials

**Preconditions:**
- Application is running
- Database is empty

**Test Steps:**
1. Send POST /api/auth/register with valid data
2. Verify response status is 201 Created
3. Verify response contains user ID
4. Verify password is hashed in database

**Expected Result:**
- User created successfully
- Password is BCrypt hashed
- Email is normalized to lowercase

**Actual Result:** [To be filled]

**Status:** ⏳ Pending

---

## Appendix B: Coverage Report Template

```
===========================================
Code Coverage Report - Sprint [X]
===========================================
Date: [YYYY-MM-DD]
Coverage Tool: JaCoCo

Package                                  Coverage
-------------------------------------------------
com.issuetracker.domain.model              100%
com.issuetracker.domain.port.in            100%
com.issuetracker.application.service        95%
com.issuetracker.infrastructure             80%
-------------------------------------------------
TOTAL                                        85%

Branch Coverage:                             85%
Line Coverage:                               85%
Method Coverage:                             90%

Tests Executed:                             120
Tests Passed:                               120
Tests Failed:                                 0
Test Execution Time:                     1m 45s
```

---

**End of Test Plan Template**

