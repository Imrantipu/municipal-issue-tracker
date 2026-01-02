# Testing Guidelines - Issue Tracker Project

**Version:** 1.0
**Date:** January 2, 2026
**Status:** Active
**Target Coverage:** 85%+ for business logic

---

## Table of Contents

1. [Testing Strategy](#testing-strategy)
2. [Test Types and When to Use Them](#test-types-and-when-to-use-them)
3. [Testing Standards](#testing-standards)
4. [Naming Conventions](#naming-conventions)
5. [Test Structure](#test-structure)
6. [What to Test](#what-to-test)
7. [Mocking Guidelines](#mocking-guidelines)
8. [Code Coverage Requirements](#code-coverage-requirements)
9. [Running Tests](#running-tests)
10. [CI/CD Integration](#cicd-integration)
11. [Common Patterns](#common-patterns)
12. [Troubleshooting](#troubleshooting)

---

## Testing Strategy

### Test Pyramid

We follow the **Test Pyramid** approach:

```
                 /\
                /  \
               / E2E \ ← Few (5-10 tests)
              /------\
             /        \
            / Integration \ ← Some (20-30 tests)
           /--------------\
          /                \
         /   Unit Tests     \ ← Many (100+ tests)
        /--------------------\
```

**Ratios:**
- **70%** Unit Tests (fast, isolated, many)
- **20%** Integration Tests (medium speed, real dependencies)
- **10%** E2E Tests (slow, full system, critical paths only)

### Testing Layers

| Layer | Test Type | Tools | Coverage Target |
|-------|-----------|-------|-----------------|
| Domain | Unit Tests | JUnit 5 + AssertJ | 100% |
| Application | Unit Tests + Mocks | JUnit 5 + Mockito | 95%+ |
| Infrastructure | Integration Tests | Spring Boot Test + TestContainers | 80%+ |
| API | Integration Tests | MockMvc + Spring Security Test | 90%+ |
| E2E | End-to-End Tests | (Future: Playwright/Selenium) | Critical paths |

---

## Test Types and When to Use Them

### 1. Unit Tests

**Purpose:** Test individual components in isolation

**When to use:**
- Domain models (User, Issue, Comment)
- Value objects (Email, Password validation)
- Business logic methods
- Utility classes

**Characteristics:**
- ✅ Fast (<100ms per test)
- ✅ No external dependencies
- ✅ No Spring context
- ✅ Use mocks for dependencies

**Example:**
```java
@Test
@DisplayName("should_ThrowException_When_EmailIsInvalid")
void should_ThrowException_When_EmailIsInvalid() {
    // Given
    User user = User.builder()
        .email("invalid-email")
        .build();

    // When/Then
    assertThatThrownBy(user::validate)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Email format is invalid");
}
```

---

### 2. Service Tests (Unit with Mocks)

**Purpose:** Test application services with mocked dependencies

**When to use:**
- UserService, IssueService, CommentService
- Use case implementations
- Orchestration logic

**Characteristics:**
- ✅ Fast (<200ms per test)
- ✅ Mock repositories and external services
- ✅ No database, no HTTP
- ✅ Test business workflows

**Example:**
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("should_RegisterUser_When_ValidDataProvided")
    void should_RegisterUser_When_ValidDataProvided() {
        // Given
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any())).thenReturn(savedUser);

        // When
        User result = userService.registerUser(command);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).save(any(User.class));
    }
}
```

---

### 3. Integration Tests (API)

**Purpose:** Test REST endpoints with real Spring context

**When to use:**
- AuthController, IssueController
- Full HTTP request/response flow
- JSON serialization
- Security filters

**Characteristics:**
- ⚠️ Slower (1-5s per test)
- ✅ Full Spring context
- ✅ Real beans (no mocks)
- ✅ H2 in-memory database
- ✅ MockMvc for HTTP simulation

**Example:**
```java
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    @DisplayName("should_ReturnCreated_When_RegistrationIsValid")
    void should_ReturnCreated_When_RegistrationIsValid() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("john@example.com"));
    }
}
```

---

### 4. Repository Tests (Integration with TestContainers)

**Purpose:** Test database operations with real PostgreSQL

**When to use:**
- JPA repositories
- Custom queries
- Database constraints
- Data integrity

**Characteristics:**
- ⚠️ Slower (2-10s per test)
- ✅ Real PostgreSQL via Docker
- ✅ Test real SQL queries
- ✅ Transaction rollback

**Example:**
```java
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
class JpaUserRepositoryAdapterTest {

    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("should_SaveUser_When_UserIsValid")
    void should_SaveUser_When_UserIsValid() {
        // Given
        User user = createUser();

        // When
        User saved = userRepository.save(user);

        // Then
        assertThat(saved.getId()).isNotNull();
    }
}
```

---

## Testing Standards

### Code Standards

1. **One Test Class Per Production Class**
   - `User.java` → `UserTest.java`
   - `UserService.java` → `UserServiceTest.java`

2. **Test Class Naming**
   - Unit tests: `ClassNameTest.java`
   - Integration tests: `ClassNameIntegrationTest.java`
   - E2E tests: `FeatureNameE2ETest.java`

3. **Test Method Naming**
   - Format: `should_ExpectedBehavior_When_Condition()`
   - Example: `should_ThrowException_When_EmailIsNull()`
   - Use `@DisplayName` for readable test reports

4. **Package Structure**
   ```
   src/test/java/
   ├── com/issuetracker/
   │   ├── domain/
   │   │   └── model/
   │   │       ├── UserTest.java          (Unit)
   │   │       └── IssueTest.java         (Unit)
   │   ├── application/
   │   │   └── service/
   │   │       ├── UserServiceTest.java   (Unit + Mocks)
   │   │       └── IssueServiceTest.java  (Unit + Mocks)
   │   └── infrastructure/
   │       ├── adapter/
   │       │   ├── in/web/
   │       │   │   └── AuthControllerIntegrationTest.java
   │       │   └── out/persistence/
   │       │       └── JpaUserRepositoryAdapterTest.java
   ```

---

## Naming Conventions

### Test Method Names

Use the **should-when** pattern:

```java
// ✅ GOOD
should_ReturnUser_When_EmailExists()
should_ThrowException_When_PasswordIsTooShort()
should_SaveUser_When_DataIsValid()

// ❌ BAD
testLogin()
userRegistration()
test1()
```

### DisplayName

Always add `@DisplayName` for better test reports:

```java
@Test
@DisplayName("should_ThrowException_When_EmailAlreadyExists")
void should_ThrowException_When_EmailAlreadyExists() {
    // ...
}
```

### Nested Test Classes

Use `@Nested` for grouping related tests:

```java
@DisplayName("UserService Tests")
class UserServiceTest {

    @Nested
    @DisplayName("Registration Tests")
    class RegistrationTests {

        @Test
        @DisplayName("should_RegisterUser_When_ValidData")
        void should_RegisterUser_When_ValidData() {
            // ...
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {
        // ...
    }
}
```

---

## Test Structure

### AAA Pattern (Arrange-Act-Assert)

Also known as **Given-When-Then**:

```java
@Test
void should_RegisterUser_When_ValidDataProvided() {
    // Given (Arrange) - Set up test data and mocks
    User user = createValidUser();
    when(userRepository.save(any())).thenReturn(user);

    // When (Act) - Execute the method under test
    User result = userService.registerUser(command);

    // Then (Assert) - Verify the outcome
    assertThat(result).isNotNull();
    assertThat(result.getEmail()).isEqualTo("john@example.com");
    verify(userRepository).save(any(User.class));
}
```

### Test Data Builders

Use builder pattern or factory methods:

```java
// ✅ GOOD - Reusable and readable
private User createValidUser() {
    return User.builder()
        .name("John Doe")
        .email("john@example.com")
        .password("password123")
        .role(Role.CITIZEN)
        .createdAt(LocalDateTime.now())
        .build();
}

@Test
void testSomething() {
    User user = createValidUser();
    // ...
}
```

---

## What to Test

### Domain Layer (100% Coverage Required)

**✅ DO Test:**
- All validation methods
- Business logic methods
- State transitions
- Edge cases (null, empty, min, max)
- Boundary values

**Example checklist for User:**
- [ ] Name validation (null, empty, too short, too long, min, max)
- [ ] Email validation (null, empty, invalid format, too long)
- [ ] Password validation (null, empty, too short, too long)
- [ ] Role validation
- [ ] Business methods (isActive, isDeleted, isAdmin, etc.)
- [ ] Soft delete/restore
- [ ] Builder pattern

---

### Application Layer (95%+ Coverage Required)

**✅ DO Test:**
- Use case workflows
- Business rule enforcement
- Exception handling
- Data transformations
- Orchestration logic

**❌ DON'T Test:**
- Framework code (Spring's @Transactional)
- Simple getters/setters
- Lombok-generated code

---

### Infrastructure Layer (80%+ Coverage Required)

**✅ DO Test:**
- REST endpoint responses
- HTTP status codes
- JSON serialization
- Security filters
- Database queries
- Error responses

**❌ DON'T Test:**
- Spring Boot auto-configuration
- Third-party libraries
- Framework internals

---

## Mocking Guidelines

### When to Mock

**✅ DO Mock:**
- External dependencies (databases, APIs)
- Repositories in service tests
- Security services
- Slow operations

**❌ DON'T Mock:**
- Domain objects
- Value objects
- DTOs
- The class under test

### Mockito Best Practices

```java
// ✅ GOOD - Specific stubbing
when(userRepository.findByEmail("john@example.com"))
    .thenReturn(Optional.of(user));

// ✅ GOOD - Verify interactions
verify(userRepository).save(any(User.class));
verify(passwordEncoder).encode("password123");

// ✅ GOOD - Argument captors for complex verification
ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
verify(userRepository).save(captor.capture());
assertThat(captor.getValue().getEmail()).isEqualTo("john@example.com");

// ❌ BAD - Over-mocking
when(user.getName()).thenReturn("John"); // Don't mock domain objects!

// ❌ BAD - Verifying too much
verify(userRepository, times(1)).save(any()); // times(1) is default
```

---

## Code Coverage Requirements

### Minimum Coverage by Layer

| Layer | Min Coverage | Target | Actual |
|-------|--------------|--------|--------|
| Domain | 95% | 100% | 100% ✅ |
| Application | 90% | 95% | 100% ✅ |
| Infrastructure | 70% | 80% | 0% ⚠️ |
| **Overall** | **80%** | **85%** | **53%** ⚠️ |

### Coverage Tools

- **JaCoCo** - Code coverage measurement
- **SonarQube** - Code quality + coverage (optional)

### Viewing Coverage Reports

```bash
# Generate report
./mvnw test jacoco:report

# Open report
open target/site/jacoco/index.html
```

### What Counts Toward Coverage

**✅ Counted:**
- Instruction coverage (bytecode)
- Branch coverage (if/else, switch)
- Line coverage
- Method coverage

**❌ Not Counted:**
- Comments
- Blank lines
- Lombok-generated code
- Configuration files

---

## Running Tests

### Run All Tests

```bash
./mvnw test
```

### Run Specific Test Class

```bash
./mvnw test -Dtest=UserTest
./mvnw test -Dtest=UserServiceTest
```

### Run Specific Test Method

```bash
./mvnw test -Dtest=UserTest#should_ThrowException_When_EmailIsNull
```

### Run Tests by Pattern

```bash
# All unit tests
./mvnw test -Dtest="*Test"

# All integration tests
./mvnw test -Dtest="*IntegrationTest"

# Multiple test classes
./mvnw test -Dtest="UserTest,UserServiceTest"
```

### Run Tests with Coverage

```bash
./mvnw clean test jacoco:report
```

### Skip Tests (CI only)

```bash
./mvnw install -DskipTests
```

---

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest

    services:
      postgres:
        image: postgres:15-alpine
        env:
          POSTGRES_PASSWORD: test
        options: >-
          --health-cmd pg_isready
          --health-interval 10s

    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Run tests with coverage
        run: ./mvnw clean test jacoco:report

      - name: Check coverage threshold
        run: ./mvnw jacoco:check

      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v3
```

---

## Common Patterns

### 1. Testing Exceptions

```java
// ✅ GOOD - AssertJ
assertThatThrownBy(() -> user.validate())
    .isInstanceOf(IllegalArgumentException.class)
    .hasMessage("Email cannot be empty");

// ✅ GOOD - JUnit 5
assertThrows(IllegalArgumentException.class, () -> user.validate());

// ❌ BAD - Old JUnit 4 style
@Test(expected = IllegalArgumentException.class)
void test() {
    user.validate();
}
```

---

### 2. Testing Null/Empty Values

```java
@Test
void should_ThrowException_When_NameIsNull() {
    user.setName(null);
    assertThatThrownBy(user::validate)
        .isInstanceOf(IllegalArgumentException.class);
}

@Test
void should_ThrowException_When_NameIsEmpty() {
    user.setName("");
    assertThatThrownBy(user::validate)
        .isInstanceOf(IllegalArgumentException.class);
}

@Test
void should_ThrowException_When_NameIsBlank() {
    user.setName("   ");
    assertThatThrownBy(user::validate)
        .isInstanceOf(IllegalArgumentException.class);
}
```

---

### 3. Testing Boundary Values

```java
@Test
void should_ThrowException_When_NameIsTooShort() {
    user.setName("A"); // 1 character (min is 2)
    assertThatThrownBy(user::validate);
}

@Test
void should_PassValidation_When_NameIsMinimumLength() {
    user.setName("AB"); // 2 characters (exactly min)
    assertThatNoException().isThrownBy(user::validate);
}

@Test
void should_PassValidation_When_NameIsMaximumLength() {
    user.setName("A".repeat(100)); // 100 characters (exactly max)
    assertThatNoException().isThrownBy(user::validate);
}

@Test
void should_ThrowException_When_NameIsTooLong() {
    user.setName("A".repeat(101)); // 101 characters (over max)
    assertThatThrownBy(user::validate);
}
```

---

### 4. Testing Collections

```java
@Test
void should_ReturnActiveUsersOnly() {
    // Given
    List<User> users = createMixedUsers();

    // When
    List<User> active = userRepository.findAllActive();

    // Then
    assertThat(active)
        .hasSize(3)
        .extracting(User::getEmail)
        .containsExactlyInAnyOrder(
            "user1@example.com",
            "user2@example.com",
            "user3@example.com"
        );
    assertThat(active).allMatch(User::isActive);
}
```

---

### 5. Testing Async Operations (Future)

```java
@Test
void should_CompleteAsync() {
    CompletableFuture<User> future = userService.registerAsync(command);

    User result = future.get(5, TimeUnit.SECONDS);

    assertThat(result).isNotNull();
}
```

---

## Troubleshooting

### Common Issues

#### 1. Tests Pass Locally but Fail in CI

**Cause:** Time-dependent tests, environment differences

**Solution:**
```java
// ❌ BAD
LocalDateTime expected = LocalDateTime.now();
// ... test execution takes time
assertThat(user.getCreatedAt()).isEqualTo(expected); // Fails!

// ✅ GOOD
assertThat(user.getCreatedAt())
    .isBeforeOrEqualTo(LocalDateTime.now())
    .isAfter(LocalDateTime.now().minusSeconds(5));
```

---

#### 2. Flaky Tests

**Cause:** Test order dependency, shared state

**Solution:**
- Use `@BeforeEach` to reset state
- Don't rely on test execution order
- Use `@Transactional` for database rollback

---

#### 3. Slow Tests

**Cause:** Too many integration tests, no mocking

**Solution:**
- Prefer unit tests over integration tests
- Mock external dependencies
- Use in-memory databases (H2)
- Parallelize test execution:
  ```xml
  <plugin>
      <artifactId>maven-surefire-plugin</artifactId>
      <configuration>
          <parallel>classes</parallel>
          <threadCount>4</threadCount>
      </configuration>
  </plugin>
  ```

---

#### 4. Mock Not Working

**Check:**
1. Using `@ExtendWith(MockitoExtension.class)`?
2. Using `@Mock` on dependency?
3. Using `@InjectMocks` on class under test?
4. Stubbing before calling method?

```java
// ✅ CORRECT ORDER
@ExtendWith(MockitoExtension.class)
class MyTest {
    @Mock
    private UserRepository repo;

    @InjectMocks
    private UserService service;

    @Test
    void test() {
        when(repo.findById(1L)).thenReturn(Optional.of(user)); // Stub first
        service.getUser(1L); // Call method
        verify(repo).findById(1L); // Verify
    }
}
```

---

## Test Data Management

### Test Fixtures

Create reusable test data:

```java
public class UserFixtures {

    public static User validUser() {
        return User.builder()
            .name("John Doe")
            .email("john@example.com")
            .password("password123")
            .role(Role.CITIZEN)
            .createdAt(LocalDateTime.now())
            .build();
    }

    public static User adminUser() {
        return User.builder()
            .name("Admin User")
            .email("admin@example.com")
            .password("adminpass")
            .role(Role.ADMIN)
            .createdAt(LocalDateTime.now())
            .build();
    }
}

// Usage
@Test
void test() {
    User user = UserFixtures.validUser();
}
```

---

## Best Practices Summary

### ✅ DO

- Write tests first (TDD when possible)
- Test behavior, not implementation
- Use descriptive test names
- Test edge cases and boundaries
- One assertion concept per test
- Keep tests simple and readable
- Use Given-When-Then structure
- Clean up test data (use @Transactional)
- Run tests before committing
- Aim for 85%+ coverage

### ❌ DON'T

- Test framework code
- Test getters/setters
- Test Lombok-generated code
- Make tests depend on each other
- Use Thread.sleep() for timing
- Hardcode production URLs
- Skip failing tests
- Commit with failing tests
- Test implementation details
- Over-mock

---

## References

### Official Documentation

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/reference/testing/index.html)
- [TestContainers](https://www.testcontainers.org/)

### Books

- "Unit Testing Principles, Practices, and Patterns" - Vladimir Khorikov
- "Growing Object-Oriented Software, Guided by Tests" - Freeman & Pryce
- "Test Driven Development: By Example" - Kent Beck

---

## Changelog

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2026-01-02 | Initial testing guidelines created |

---

**Next Review Date:** Sprint 2 Completion
**Owner:** Development Team
**Approved By:** Tech Lead

