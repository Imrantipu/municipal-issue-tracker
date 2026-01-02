# Sprint 2 - Design Patterns Catalog

**Document Version:** 1.0
**Last Updated:** January 2, 2026
**Sprint:** 2 of 7 (Issue Management)
**Total Patterns Used:** 13

---

## Table of Contents

1. [Overview](#overview)
2. [Creational Patterns (3)](#creational-patterns)
3. [Structural Patterns (4)](#structural-patterns)
4. [Behavioral Patterns (5)](#behavioral-patterns)
5. [Architectural Patterns (1)](#architectural-patterns)
6. [Pattern Summary Table](#pattern-summary-table)
7. [Interview Preparation](#interview-preparation)

---

## Overview

This document catalogs all design patterns used in Sprint 2 (Issue Management). Each pattern includes:
- **What it is:** Pattern definition
- **Where used:** File location with line numbers
- **Why used:** Business justification
- **Benefits:** Advantages gained
- **Trade-offs:** Considerations
- **Interview tips:** How to discuss in interviews

---

## Creational Patterns

Creational patterns deal with object creation mechanisms.

### 1. Builder Pattern

**What:** Separates object construction from its representation, allowing step-by-step object creation with optional parameters.

**Where Used:**
1. **Issue.java** - Domain model construction
   ```java
   Issue issue = Issue.builder()
       .title("Broken streetlight")
       .description("The streetlight...")
       .status(IssueStatus.OPEN)
       .priority(Priority.MEDIUM)
       .category(Category.INFRASTRUCTURE)
       .location("123 Main St")
       .reportedBy(user)
       .build();
   ```

2. **IssueEntity.java** - JPA entity construction (for testing)
   ```java
   @Builder  // Lombok generates builder class
   public class IssueEntity { ... }
   ```

**Why Used:**
- Issue has 13 fields - constructor with 13 parameters would be unreadable
- Many fields are optional (assignedTo, resolvedAt, closedAt, deletedAt)
- Provides fluent, readable API for object creation
- Lombok `@Builder` generates boilerplate automatically

**Benefits:**
- ✅ Readable code: `Issue.builder().title("...").build()`
- ✅ Optional parameters without constructor overloading
- ✅ Immutability after construction
- ✅ Self-documenting (clear what each field is)
- ✅ Compile-time safety (type checking)

**Trade-offs:**
- ⚠️ More verbose than simple constructor (but Lombok eliminates this)
- ⚠️ Additional class generated (but Lombok handles this)

**Interview Tip:**
*"We used Builder Pattern for the Issue domain model because it has 13 fields. Instead of a constructor with 13 parameters (which is unreadable), we used Lombok's @Builder to generate a fluent API. This makes object creation readable and maintainable."*

---

### 2. Dependency Injection Pattern

**What:** Objects receive their dependencies from external source rather than creating them internally.

**Where Used:**

1. **IssueService.java** - Constructor injection
   ```java
   @RequiredArgsConstructor  // Lombok generates constructor
   public class IssueService {
       private final IssueRepository issueRepository;
       private final UserRepository userRepository;
       // Dependencies injected by Spring
   }
   ```

2. **IssueController.java** - Use case injection
   ```java
   @RequiredArgsConstructor
   public class IssueController {
       private final CreateIssueUseCase createIssueUseCase;
       private final UpdateIssueUseCase updateIssueUseCase;
       // ... 6 use cases injected
   }
   ```

**Why Used:**
- Follows Dependency Inversion Principle (SOLID)
- Dependencies are interfaces (not concrete classes)
- Enables easy testing with mocks
- Spring manages object lifecycle

**Benefits:**
- ✅ Loose coupling (depend on abstractions)
- ✅ Easy to test (inject mocks)
- ✅ Single instance management (Spring handles)
- ✅ Configuration externalized

**Interview Tip:**
*"We use constructor injection everywhere. IssueService depends on IssueRepository interface (not the JPA implementation). This allows us to mock the repository in tests and swap implementations without changing service code."*

---

### 3. Singleton Pattern

**What:** Ensures a class has only one instance and provides global access to it.

**Where Used:**

1. **Spring Beans** - All `@Service`, `@Component`, `@Controller` beans
   ```java
   @Service  // Spring creates single instance
   public class IssueService { ... }

   @Component  // Single instance per application
   public class JpaIssueRepositoryAdapter { ... }
   ```

**Why Used:**
- Spring manages singleton lifecycle automatically
- Services are stateless (safe to share single instance)
- Performance: No need to create multiple instances

**Benefits:**
- ✅ Memory efficient (one instance shared)
- ✅ Thread-safe (Spring handles synchronization)
- ✅ Global access point

**Interview Tip:**
*"Spring beans are singletons by default. IssueService is stateless, so one instance can handle all requests. Spring manages thread safety and lifecycle."*

---

## Structural Patterns

Structural patterns deal with object composition and relationships.

### 4. Adapter Pattern

**What:** Converts interface of a class into another interface clients expect.

**Where Used:**

**JpaIssueRepositoryAdapter.java** - Adapts Spring Data JPA to domain interface
```java
@Component
public class JpaIssueRepositoryAdapter implements IssueRepository {

    private final SpringDataIssueRepository jpaRepository;

    @Override
    public Issue save(Issue issue) {
        IssueEntity entity = toEntity(issue);  // Convert domain → JPA
        IssueEntity saved = jpaRepository.save(entity);
        return toDomain(saved);  // Convert JPA → domain
    }

    private IssueEntity toEntity(Issue domain) { ... }
    private Issue toDomain(IssueEntity entity) { ... }
}
```

**Why Used:**
- Domain defines `IssueRepository` interface (what it needs)
- Infrastructure implements with JPA (how it's done)
- Domain never sees JPA annotations or entities
- Can swap PostgreSQL → MongoDB by changing only adapter

**Benefits:**
- ✅ Domain stays pure (no framework dependencies)
- ✅ Easy to swap persistence technology
- ✅ Testable without database (use in-memory adapter)
- ✅ Separation of concerns

**Trade-offs:**
- ⚠️ Conversion overhead (Issue ↔ IssueEntity)
- ⚠️ More code (but worth it for flexibility)

**Interview Tip:**
*"The Adapter Pattern keeps our domain layer clean. Issue.java has zero JPA annotations. JpaIssueRepositoryAdapter converts between domain models and JPA entities. If we wanted to switch to MongoDB, we'd only change the adapter—domain stays untouched."*

---

### 5. Facade Pattern

**What:** Provides simplified interface to complex subsystem.

**Where Used:**

**IssueService.java** - Simplifies complex use case orchestration
```java
@Service
public class IssueService implements
        CreateIssueUseCase, UpdateIssueUseCase,
        AssignIssueUseCase, ChangeStatusUseCase,
        GetIssueUseCase, DeleteIssueUseCase {

    // Single service orchestrates 6 use cases
    // Hides complexity of:
    // - Domain validation
    // - Authorization checks
    // - Repository operations
    // - Transaction management
}
```

**Why Used:**
- Controller doesn't need to know about repositories, validation, authorization
- Service provides one unified interface for all issue operations
- Hides complexity of domain interactions

**Benefits:**
- ✅ Simplified client code (controller just calls service)
- ✅ Centralized business logic
- ✅ Easy to add cross-cutting concerns (logging, auditing)

**Interview Tip:**
*"IssueService is a facade over complex domain operations. The controller doesn't deal with validation, authorization, or repository details—it just calls the service. This keeps the controller thin and testable."*

---

### 6. Proxy Pattern

**What:** Provides surrogate or placeholder to control access to an object.

**Where Used:**

1. **@Transactional** - Spring creates proxy for transaction management
   ```java
   @Service
   @Transactional  // Spring creates proxy
   public class IssueService { ... }
   ```

   **How it works:**
   ```
   Controller → TransactionProxy → IssueService
                      ↓
                 1. Begin transaction
                 2. Call real service
                 3. Commit/rollback
   ```

2. **Spring Data JPA** - Repository interface is proxied at runtime
   ```java
   public interface SpringDataIssueRepository
           extends JpaRepository<IssueEntity, Long> {
       // Spring creates proxy implementation at runtime
   }
   ```

**Why Used:**
- Adds behavior (transactions, query generation) without modifying original class
- Declarative programming (annotations vs imperative code)

**Benefits:**
- ✅ Automatic transaction management
- ✅ No boilerplate transaction code
- ✅ Rollback on exceptions

**Interview Tip:**
*"@Transactional uses Proxy Pattern. Spring wraps IssueService in a proxy that opens a transaction before each method and commits/rolls back after. This eliminates manual transaction handling code."*

---

### 7. DTO Pattern (Data Transfer Object)

**What:** Object that carries data between processes, used to reduce number of method calls.

**Where Used:**

**IssueController.java** - Request/Response DTOs
```java
// Request DTO
record CreateIssueRequest(
    String title,
    String description,
    Category category,
    Priority priority,
    String location
) {}

// Response DTO
record IssueResponse(
    Long id,
    String title,
    IssueStatus status,
    UserSummary reportedBy,  // Nested DTO
    LocalDateTime createdAt
    // ... only expose what clients need
) {}

// Nested DTO
record UserSummary(
    Long id,
    String name,
    String email
    // No password field!
) {}
```

**Why Used:**
- Separate API contract from domain model
- Prevent exposing internal domain structure
- Can change domain without breaking API
- Security: Don't expose password or internal IDs

**Benefits:**
- ✅ API stability (domain changes don't affect clients)
- ✅ Security (control what's exposed)
- ✅ Versioning (can support multiple DTO versions)
- ✅ Immutability (records are immutable)

**Interview Tip:**
*"We use DTOs to decouple the API from domain. IssueResponse only exposes safe fields—no internal IDs, passwords, or deletedAt timestamps. If we refactor the domain, the API stays stable."*

---

## Behavioral Patterns

Behavioral patterns deal with object interaction and responsibility distribution.

### 8. Command Pattern

**What:** Encapsulates request as an object, allowing parameterization and queuing of requests.

**Where Used:**

**All Use Case Commands:**
```java
// CreateIssueUseCase.java
record CreateIssueCommand(
    String title,
    String description,
    Category category,
    Priority priority,
    String location,
    Long reportedByUserId
) {}

// UpdateIssueUseCase.java
record UpdateIssueCommand(
    Long issueId,
    String title,
    String description,
    Priority priority,
    Category category,
    String location,
    Long updatedByUserId
) {}

// 6 total command objects
```

**Why Used:**
- Encapsulates all data needed for operation in single object
- Method signature stays stable (one parameter instead of 6)
- Can log, queue, or undo commands
- Validates data at construction time (in record constructor)

**Benefits:**
- ✅ Extensibility (add fields without breaking method signature)
- ✅ Validation at construction (fail fast)
- ✅ Can be serialized (for message queues)
- ✅ Self-documenting (command name explains intent)

**Interview Tip:**
*"Instead of passing 6 parameters to createIssue(), we use CreateIssueCommand. This makes the code more maintainable—if we add a field, we just update the command, not every method call. The record also validates data automatically."*

---

### 9. Repository Pattern

**What:** Mediates between domain and data mapping layers, acting like an in-memory collection.

**Where Used:**

**IssueRepository.java** - Domain's data access interface
```java
public interface IssueRepository {
    Issue save(Issue issue);
    Optional<Issue> findById(Long id);
    List<Issue> findAll();
    List<Issue> findByStatus(IssueStatus status);
    // ... 15 methods total
}
```

**Why Used:**
- Domain defines WHAT it needs (interface)
- Infrastructure defines HOW (JPA implementation)
- Hides SQL, JPQL, and persistence details from domain
- Provides collection-like interface

**Benefits:**
- ✅ Domain doesn't know about JPA, SQL, or database
- ✅ Easy to test (mock the interface)
- ✅ Can swap persistence (PostgreSQL → MongoDB)
- ✅ Centralized query logic

**Interview Tip:**
*"Repository Pattern abstracts persistence. IssueService calls methods like findById() without knowing it's JPA underneath. In tests, we mock the repository. In production, JpaIssueRepositoryAdapter uses Spring Data JPA."*

---

### 10. State Pattern

**What:** Allows object to alter behavior when internal state changes.

**Where Used:**

**IssueStatus.java** - Status transitions with validation
```java
public enum IssueStatus {
    OPEN, IN_PROGRESS, RESOLVED, CLOSED;

    public boolean canTransitionTo(IssueStatus targetStatus) {
        return switch (this) {
            case OPEN -> targetStatus == IN_PROGRESS || targetStatus == CLOSED;
            case IN_PROGRESS -> targetStatus == RESOLVED || targetStatus == OPEN;
            case RESOLVED -> targetStatus == CLOSED || targetStatus == OPEN;
            case CLOSED -> false;  // Terminal state
        };
    }
}
```

**Why Used:**
- Status transitions follow business rules
- CLOSED is final state (can't transition out)
- Prevents invalid workflows (OPEN → RESOLVED not allowed)

**Benefits:**
- ✅ Business rules enforced in one place
- ✅ Clear state machine
- ✅ Easy to understand allowed transitions

**Interview Tip:**
*"IssueStatus uses State Pattern. Each status knows which transitions are valid. This prevents bugs like marking an issue RESOLVED without going through IN_PROGRESS first."*

---

### 11. Template Method Pattern

**What:** Defines skeleton of algorithm in base class, letting subclasses override specific steps.

**Where Used:**

**IssueEntity.java** - JPA lifecycle callbacks
```java
@Entity
public class IssueEntity {

    @PrePersist  // Hook method called by JPA
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate  // Hook method called by JPA
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

**How JPA uses it:**
```
JPA save() algorithm:
1. Validate entity
2. Call @PrePersist hook     ← We define this step
3. INSERT into database
4. Call @PostPersist hook
```

**Why Used:**
- JPA framework calls our hooks at specific lifecycle points
- We don't control the algorithm (JPA does)
- We just fill in the steps (setting timestamps)

**Interview Tip:**
*"JPA lifecycle callbacks are Template Method Pattern. JPA defines the save algorithm, but calls our @PrePersist method to set timestamps. We fill in the details without modifying JPA's core logic."*

---

### 12. Query Object Pattern

**What:** Encapsulates database query as an object with parameters.

**Where Used:**

**GetIssueUseCase.IssueQuery** - Search criteria encapsulation
```java
record IssueQuery(
    Long requestingUserId,
    IssueStatus status,           // null = all
    Priority priority,             // null = all
    Category category,             // null = all
    Long reportedByUserId,         // null = all
    Long assignedToUserId,         // null = all
    Boolean includeDeleted,        // default false
    Integer page,                  // pagination
    Integer size                   // pagination
) {
    // Factory methods for common queries
    public static IssueQuery allIssuesForUser(Long userId) { ... }
    public static IssueQuery reportedByUser(Long userId) { ... }
    public static IssueQuery assignedToUser(Long userId) { ... }
}
```

**Usage:**
```java
// Simple query
IssueQuery query = IssueQuery.allIssuesForUser(userId);

// Complex query
IssueQuery query = new IssueQuery(
    userId,
    IssueStatus.OPEN,
    Priority.HIGH,
    Category.SAFETY,
    null, null, false, null, null
);

List<Issue> issues = issueService.getIssues(query);
```

**Why Used:**
- Flexible filtering without changing method signature
- All parameters are optional
- Easy to add new filters (just add field to record)
- Type-safe (compile-time checking)

**Benefits:**
- ✅ No "parameter explosion" (method with 10 parameters)
- ✅ Self-documenting (field names are clear)
- ✅ Can add filters without breaking existing code

**Interview Tip:**
*"Query Object Pattern prevents method signature explosion. Instead of findIssues(status, priority, category, ...) with 8 parameters, we use IssueQuery. Adding a new filter is just adding a field to the record."*

---

## Architectural Patterns

### 13. Ports and Adapters (Hexagonal Architecture)

**What:** Application core (domain) defines interfaces (ports), infrastructure provides implementations (adapters).

**Where Used:** **Entire Sprint 2 architecture**

```
┌──────────────────────────────────────────┐
│         APPLICATION CORE (DOMAIN)        │
│                                          │
│  Input Ports (Use Cases):               │
│  - CreateIssueUseCase                   │
│  - UpdateIssueUseCase                   │
│  - ...                                  │
│                                          │
│  Output Ports (Repository):             │
│  - IssueRepository (interface)          │
│                                          │
│  Domain Models:                          │
│  - Issue, IssueStatus, Priority         │
└──────────────────────────────────────────┘
           ↑                    ↓
    Input Adapter        Output Adapter
           ↑                    ↓
┌──────────────────┐   ┌──────────────────┐
│  IssueController │   │JpaIssueRepository│
│   (Web/REST)     │   │    Adapter       │
└──────────────────┘   └──────────────────┘
```

**Why Used:**
- Domain has ZERO framework dependencies
- Can swap web framework (REST → GraphQL) by changing input adapter
- Can swap database (PostgreSQL → MongoDB) by changing output adapter
- Domain testable without infrastructure

**Benefits:**
- ✅ Framework independence
- ✅ Database independence
- ✅ Easy to test
- ✅ Technology agnostic domain

**Interview Tip:**
*"We use Hexagonal Architecture. The domain defines what it needs (ports), infrastructure provides how (adapters). Issue.java has no Spring, JPA, or HTTP annotations. This makes our business logic portable and testable."*

---

## Pattern Summary Table

| # | Pattern | Type | Location | Primary Benefit |
|---|---------|------|----------|-----------------|
| 1 | **Builder** | Creational | Issue.java | Readable object construction |
| 2 | **Dependency Injection** | Creational | All services | Loose coupling, testability |
| 3 | **Singleton** | Creational | Spring beans | Memory efficiency |
| 4 | **Adapter** | Structural | JpaIssueRepositoryAdapter | Framework independence |
| 5 | **Facade** | Structural | IssueService | Simplified interface |
| 6 | **Proxy** | Structural | @Transactional | Automatic transactions |
| 7 | **DTO** | Structural | Controller DTOs | API/domain separation |
| 8 | **Command** | Behavioral | Use case commands | Encapsulation, extensibility |
| 9 | **Repository** | Behavioral | IssueRepository | Data access abstraction |
| 10 | **State** | Behavioral | IssueStatus | Valid state transitions |
| 11 | **Template Method** | Behavioral | JPA lifecycle | Framework extension |
| 12 | **Query Object** | Behavioral | IssueQuery | Flexible filtering |
| 13 | **Hexagonal Architecture** | Architectural | Entire system | Clean architecture |

---

## Interview Preparation

### Top 5 Patterns to Master for Interviews

1. **Hexagonal Architecture (Ports & Adapters)**
   - *Question:* "How do you keep business logic independent of frameworks?"
   - *Answer:* "We use Hexagonal Architecture. Domain defines ports (interfaces), infrastructure provides adapters. Our Issue domain has zero Spring/JPA dependencies."

2. **Repository Pattern**
   - *Question:* "How do you abstract database access?"
   - *Answer:* "Repository Pattern. IssueRepository interface is defined in domain, implemented in infrastructure. Domain calls findById() without knowing it's JPA."

3. **Builder Pattern**
   - *Question:* "How do you handle classes with many optional parameters?"
   - *Answer:* "Builder Pattern. Issue has 13 fields, so we use `Issue.builder().title(...).build()` instead of constructors."

4. **Command Pattern**
   - *Question:* "How do you avoid parameter explosion in methods?"
   - *Answer:* "Command Pattern. CreateIssueCommand encapsulates 6 parameters in one object. Easy to extend without breaking method signature."

5. **Adapter Pattern**
   - *Question:* "How do you convert between domain models and database entities?"
   - *Answer:* "Adapter Pattern. JpaIssueRepositoryAdapter converts Issue ↔ IssueEntity, keeping domain clean of JPA annotations."

### Common Interview Questions

**Q: "Why not put business logic in the controller?"**
**A:** "We follow Hexagonal Architecture. Controller is an input adapter—just transforms DTOs and calls services. Business logic lives in IssueService and Issue domain model. This keeps logic testable and framework-independent."

**Q: "What's the difference between Service and Repository?"**
**A:** "Service orchestrates use cases (business logic, authorization, transactions). Repository abstracts data access (saves, finds, queries). Service USES repository, but repository knows nothing about business rules."

**Q: "Why use DTOs instead of returning domain objects?"**
**A:** "Security and stability. DTOs prevent exposing sensitive fields (like deletedAt, password). They also decouple API from domain—we can refactor domain without breaking client contracts."

---

**Document Status:** Complete
**Pattern Count:** 13
**Code Examples:** Inline throughout
**Interview Prep:** Included
**Next Review:** After Sprint 3
