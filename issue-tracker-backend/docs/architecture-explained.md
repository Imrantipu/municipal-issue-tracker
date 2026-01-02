# Software Architecture Patterns Explained

## Hexagonal Architecture (Your Project)

### Visual Representation

```
                    ┌─────────────────────────────────────┐
                    │   INFRASTRUCTURE LAYER              │
                    │                                     │
  ┌─────────────┐   │  ┌────────────────────────────┐    │   ┌──────────────┐
  │   REST API  │───┼─>│   REST Controllers         │    │   │  PostgreSQL  │
  │  (Frontend) │   │  │   (Input Adapters)         │    │   │  Database    │
  └─────────────┘   │  └────────────┬───────────────┘    │   └──────┬───────┘
                    │               │                     │          │
                    │               ▼                     │          │
                    │  ┌────────────────────────────┐    │          │
                    │  │   APPLICATION LAYER        │    │          │
                    │  │                            │<───┼──────────┘
                    │  │  - Use Cases               │    │  (Output Adapters)
                    │  │  - DTOs                    │    │
                    │  │  - Mappers                 │    │
                    │  └────────────┬───────────────┘    │
                    │               │                     │
                    │               ▼                     │
                    │  ┌────────────────────────────┐    │
                    │  │   DOMAIN LAYER             │    │
                    │  │                            │    │
                    │  │  - Business Logic          │    │
                    │  │  - Domain Models           │    │
                    │  │  - Port Interfaces         │    │
                    │  │                            │    │
                    │  │  NO FRAMEWORK DEPENDENCIES │    │
                    │  └────────────────────────────┘    │
                    │                                     │
                    └─────────────────────────────────────┘
```

### Concrete Example: Creating an Issue

#### 1. Domain Layer (Core Business Logic)

**File: `domain/model/Issue.java`** (Pure Java, no Spring!)
```java
package com.issuetracker.domain.model;

import java.time.LocalDateTime;

// Pure domain entity - NO JPA annotations, NO framework dependencies
public class Issue {
    private Long id;
    private String title;
    private String description;
    private IssueStatus status;
    private IssuePriority priority;
    private User citizen;
    private User assignedStaff;
    private LocalDateTime createdAt;

    // Business logic lives here
    public void assignToStaff(User staff) {
        if (!staff.getRole().equals(UserRole.STAFF)) {
            throw new IllegalArgumentException("Only staff members can be assigned to issues");
        }
        if (this.status.equals(IssueStatus.CLOSED)) {
            throw new IllegalStateException("Cannot assign staff to closed issue");
        }
        this.assignedStaff = staff;
        this.status = IssueStatus.IN_PROGRESS;
    }

    public void resolve() {
        if (this.assignedStaff == null) {
            throw new IllegalStateException("Issue must be assigned before resolving");
        }
        this.status = IssueStatus.RESOLVED;
    }

    // Validation logic
    public void validate() {
        if (title == null || title.trim().length() < 5) {
            throw new IllegalArgumentException("Title must be at least 5 characters");
        }
        if (description == null || description.trim().length() < 20) {
            throw new IllegalArgumentException("Description must be at least 20 characters");
        }
    }
}
```

**File: `domain/port/out/IssueRepository.java`** (Interface - "Output Port")
```java
package com.issuetracker.domain.port.out;

import com.issuetracker.domain.model.Issue;
import java.util.List;
import java.util.Optional;

// Interface defined by domain, implemented by infrastructure
public interface IssueRepository {
    Issue save(Issue issue);
    Optional<Issue> findById(Long id);
    List<Issue> findByCitizenId(Long citizenId);
    List<Issue> findByStatus(IssueStatus status);
    void delete(Long id);
}
```

**File: `domain/port/in/CreateIssueUseCase.java`** (Interface - "Input Port")
```java
package com.issuetracker.domain.port.in;

import com.issuetracker.domain.model.Issue;

// What the application can DO (use case interface)
public interface CreateIssueUseCase {
    Issue createIssue(CreateIssueCommand command);
}
```

#### 2. Application Layer (Use Case Implementation)

**File: `application/service/IssueService.java`**
```java
package com.issuetracker.application.service;

import com.issuetracker.domain.model.Issue;
import com.issuetracker.domain.port.in.CreateIssueUseCase;
import com.issuetracker.domain.port.out.IssueRepository;
import com.issuetracker.domain.port.out.UserRepository;

// Implements the use case (business logic orchestration)
public class IssueService implements CreateIssueUseCase {
    private final IssueRepository issueRepository;
    private final UserRepository userRepository;

    // Dependency injection via constructor
    public IssueService(IssueRepository issueRepository, UserRepository userRepository) {
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Issue createIssue(CreateIssueCommand command) {
        // 1. Validate user exists
        User citizen = userRepository.findById(command.getCitizenId())
            .orElseThrow(() -> new UserNotFoundException("Citizen not found"));

        // 2. Create domain object
        Issue issue = new Issue(
            null, // ID assigned by database
            command.getTitle(),
            command.getDescription(),
            command.getCategory(),
            command.getLocation(),
            IssueStatus.PENDING,
            IssuePriority.NORMAL,
            citizen,
            null, // No staff assigned yet
            LocalDateTime.now()
        );

        // 3. Validate business rules
        issue.validate();

        // 4. Save via repository (port)
        return issueRepository.save(issue);
    }
}
```

#### 3. Infrastructure Layer (Adapters)

**Input Adapter: REST Controller**

**File: `infrastructure/adapter/in/web/IssueController.java`**
```java
package com.issuetracker.infrastructure.adapter.in.web;

import com.issuetracker.domain.port.in.CreateIssueUseCase;
import com.issuetracker.application.dto.CreateIssueRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/issues")
public class IssueController {
    private final CreateIssueUseCase createIssueUseCase;

    public IssueController(CreateIssueUseCase createIssueUseCase) {
        this.createIssueUseCase = createIssueUseCase;
    }

    @PostMapping
    public ResponseEntity<IssueResponse> createIssue(@RequestBody CreateIssueRequest request) {
        // 1. Map DTO to domain command
        CreateIssueCommand command = new CreateIssueCommand(
            request.getTitle(),
            request.getDescription(),
            request.getCategory(),
            request.getLocation(),
            getCurrentUserId() // From JWT token
        );

        // 2. Call use case (business logic)
        Issue issue = createIssueUseCase.createIssue(command);

        // 3. Map domain to DTO
        IssueResponse response = IssueMapper.toResponse(issue);

        return ResponseEntity.ok(response);
    }
}
```

**Output Adapter: JPA Repository**

**File: `infrastructure/adapter/out/persistence/JpaIssueRepository.java`**
```java
package com.issuetracker.infrastructure.adapter.out.persistence;

import com.issuetracker.domain.model.Issue;
import com.issuetracker.domain.port.out.IssueRepository;
import org.springframework.stereotype.Repository;

@Repository
public class JpaIssueRepository implements IssueRepository {
    private final SpringDataIssueRepository springDataRepo;

    public JpaIssueRepository(SpringDataIssueRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public Issue save(Issue issue) {
        // Convert domain Issue to JPA entity
        IssueEntity entity = IssueEntityMapper.toEntity(issue);

        // Save to database
        IssueEntity saved = springDataRepo.save(entity);

        // Convert back to domain
        return IssueEntityMapper.toDomain(saved);
    }

    @Override
    public Optional<Issue> findById(Long id) {
        return springDataRepo.findById(id)
            .map(IssueEntityMapper::toDomain);
    }
}
```

**JPA Entity (Infrastructure Detail)**

**File: `infrastructure/adapter/out/persistence/entity/IssueEntity.java`**
```java
package com.issuetracker.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "issues")
public class IssueEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private IssueStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citizen_id", nullable = false)
    private UserEntity citizen;

    // JPA-specific annotations and mappings
    // Domain model is completely separate!
}
```

### Key Benefits You Get

#### 1. Testability

**Test business logic WITHOUT Spring Boot or database:**

```java
// domain/service/IssueServiceTest.java
class IssueServiceTest {
    @Test
    void shouldNotAssignStaffToClosedIssue() {
        // No database, no Spring, just pure Java
        Issue issue = new Issue(...);
        issue.close();

        User staff = new User(..., UserRole.STAFF);

        // Test business rule
        assertThrows(IllegalStateException.class, () -> {
            issue.assignToStaff(staff);
        });
    }
}
```

This test runs in **milliseconds** (no database startup).

#### 2. Flexibility

Want to change from PostgreSQL to MongoDB? Just implement `IssueRepository` interface with MongoDB adapter:

```java
// New adapter - domain code unchanged!
@Repository
public class MongoIssueRepository implements IssueRepository {
    private final MongoTemplate mongoTemplate;

    @Override
    public Issue save(Issue issue) {
        // MongoDB implementation
    }
}
```

#### 3. Framework Independence

Your business logic (domain layer) has **ZERO** Spring dependencies. You could:
- Switch from Spring Boot to Quarkus
- Run same logic in AWS Lambda function
- Use in desktop app or CLI tool

Domain code doesn't care!

---

## All Software Architecture Patterns

Here are the **8 most common architecture patterns**:

### 1. Layered (N-Tier) Architecture ⭐ Most Common

**Structure:**
```
┌─────────────────────┐
│  Presentation       │ (Controllers, Views)
├─────────────────────┤
│  Business Logic     │ (Services)
├─────────────────────────┤
│  Data Access        │ (Repositories, DAOs)
├─────────────────────┤
│  Database           │
└─────────────────────┘
```

**When to use:**
- Simple CRUD applications
- Traditional enterprise apps
- Small to medium projects

**Pros:**
- Easy to understand
- Quick to implement
- Common pattern (everyone knows it)

**Cons:**
- Tight coupling between layers
- Hard to test (need database for tests)
- Business logic often leaks into controllers

**Example:** Traditional Spring Boot MVC app

---

### 2. Hexagonal Architecture (Ports & Adapters) ⭐ Your Project

**Structure:**
```
     ┌──────────┐
     │  Domain  │ (Core)
     └────┬─────┘
          │
  ┌───────┴───────┐
  │  Ports        │ (Interfaces)
  └───┬───────┬───┘
      │       │
  ┌───▼───┐ ┌▼──────┐
  │ REST  │ │  JPA  │ (Adapters)
  └───────┘ └───────┘
```

**When to use:**
- Need high test coverage (80%+)
- Complex business logic
- Multiple interfaces (REST API, CLI, GraphQL)
- Long-term maintainability important

**Pros:**
- Easy to test (mock ports)
- Flexible (swap adapters easily)
- Business logic isolated

**Cons:**
- More boilerplate
- Learning curve
- Overkill for simple CRUD

**Example:** Your Issue Tracker project

---

### 3. Clean Architecture (Uncle Bob) ⭐ Similar to Hexagonal

**Structure:**
```
┌─────────────────────────────┐
│  Frameworks & Drivers       │ (Spring, DB)
├─────────────────────────────┤
│  Interface Adapters         │ (Controllers, Presenters)
├─────────────────────────────┤
│  Use Cases                  │ (Business rules)
├─────────────────────────────┤
│  Entities                   │ (Domain models)
└─────────────────────────────┘
```

**When to use:**
- Same as Hexagonal (they're very similar)
- Robert C. Martin's book is popular, so good for interviews

**Difference from Hexagonal:**
- Clean Architecture has 4 layers (Hexagonal has 3)
- Otherwise, same principles

---

### 4. Microservices Architecture ⭐ For Large Systems

**Structure:**
```
┌──────────┐  ┌──────────┐  ┌──────────┐
│ User     │  │ Issue    │  │ Payment  │
│ Service  │  │ Service  │  │ Service  │
└────┬─────┘  └────┬─────┘  └────┬─────┘
     │             │             │
     └─────────┬───┴─────────────┘
               │
         ┌─────▼─────┐
         │ API       │
         │ Gateway   │
         └───────────┘
```

**When to use:**
- Large teams (10+ developers)
- Different parts scale independently
- Need different tech stacks

**Pros:**
- Independent deployment
- Technology flexibility
- Scalability

**Cons:**
- Complex (distributed system problems)
- Network latency
- Data consistency challenges
- NOT recommended for MVP

**Example:** Netflix, Amazon

---

### 5. Event-Driven Architecture

**Structure:**
```
┌─────────┐       ┌───────────┐       ┌─────────┐
│ Service │──────>│ Event Bus │──────>│ Service │
│    A    │ event │  (Kafka)  │ event │    B    │
└─────────┘       └───────────┘       └─────────┘
```

**When to use:**
- Asynchronous processing needed
- Real-time updates (chat, notifications)
- Decoupled services

**Pros:**
- Loose coupling
- Scalability
- Real-time processing

**Cons:**
- Debugging is hard (event flow complex)
- Eventually consistent (not immediately)
- Requires message broker (Kafka, RabbitMQ)

**Example:** Uber (ride events), Stock trading platforms

---

### 6. CQRS (Command Query Responsibility Segregation)

**Structure:**
```
Write Model              Read Model
┌──────────┐            ┌──────────┐
│ Commands │───┐    ┌──>│ Queries  │
└──────────┘   │    │   └──────────┘
               ▼    │
        ┌─────────────┐
        │  Event Store│
        └─────────────┘
```

**When to use:**
- Read/write patterns are very different
- High read load (separate read database)
- Complex domain

**Pros:**
- Optimize reads and writes separately
- Better performance

**Cons:**
- Complexity (two models)
- Eventual consistency
- Overkill for most apps

**Example:** Banking systems, E-commerce (product catalog)

---

### 7. Serverless Architecture

**Structure:**
```
API Gateway ──> Lambda Function 1
            ──> Lambda Function 2
            ──> Lambda Function 3
                      │
                      ▼
                  Database
```

**When to use:**
- Sporadic traffic (not always busy)
- Want to minimize infrastructure management
- Event-driven tasks

**Pros:**
- Pay per use (cost-effective)
- Auto-scaling
- No server management

**Cons:**
- Cold start latency
- Vendor lock-in (AWS Lambda)
- Debugging is harder

**Example:** AWS Lambda, Google Cloud Functions

---

### 8. Monolithic Architecture

**Structure:**
```
┌─────────────────────────────┐
│                             │
│  Single deployable unit     │
│                             │
│  - Controllers              │
│  - Services                 │
│  - Repositories             │
│  - Database                 │
│                             │
└─────────────────────────────┘
```

**When to use:**
- MVPs and startups
- Small teams (1-5 developers)
- Simple domain

**Pros:**
- Simple deployment
- Easy to develop
- No network overhead

**Cons:**
- Hard to scale (must scale everything)
- Tight coupling
- Deployment risk (one bug breaks everything)

**Example:** Most startups begin here (including your project!)

---

## Comparison Table

| Architecture | Complexity | Testability | Scalability | Best For |
|-------------|-----------|-------------|-------------|----------|
| **Layered** | Low | Medium | Medium | Simple CRUD apps |
| **Hexagonal** | Medium | High | High | Business logic heavy apps |
| **Clean** | Medium | High | High | Same as Hexagonal |
| **Microservices** | Very High | Medium | Very High | Large distributed systems |
| **Event-Driven** | High | Medium | Very High | Real-time, async processing |
| **CQRS** | High | Medium | Very High | Complex read/write patterns |
| **Serverless** | Low | Medium | Auto | Sporadic traffic |
| **Monolithic** | Low | Low | Low | MVPs, small apps |

---

## Which One for Your Project?

**Your Project = Hexagonal Architecture (Monolithic Deployment)**

Wait, what? Hexagonal + Monolithic?

Yes! Architecture pattern ≠ Deployment strategy:

- **Architecture**: Hexagonal (clean code organization)
- **Deployment**: Monolithic (single JAR file deployed together)

You get:
- ✅ Clean code (Hexagonal benefits)
- ✅ Easy deployment (Monolithic simplicity)
- ✅ Can split into microservices later (if needed)

**Perfect for:**
- MVP with professional quality
- Mid-level portfolio
- German job market expectations

---

## Interview Questions You Can Now Answer

**Q: "What architecture did you use and why?"**

A: "I used Hexagonal Architecture for my Issue Tracker. The business logic includes complex rules like role-based permissions, issue status transitions, and timeline tracking. Hexagonal Architecture let me isolate this logic in the domain layer with zero framework dependencies, which made it easy to reach 85% test coverage without slow integration tests. I can test business rules in milliseconds using pure Java. The trade-off is more boilerplate - I need interfaces for ports and mappers for DTOs - but for a portfolio project targeting German companies, demonstrating clean architecture is more valuable than saving a few lines of code."

**That's a senior-level answer** showing:
- Understanding of business requirements
- Testability focus
- Awareness of trade-offs
- German market awareness

---

## Learning Resources

**Books:**
- "Clean Architecture" by Robert C. Martin
- "Implementing Domain-Driven Design" by Vaughn Vernon

**Articles:**
- [Alistair Cockburn - Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [The Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)

**Videos:**
- "Hexagonal Architecture with Spring Boot" by Tom Hombergs

---

## Summary

**Hexagonal Architecture = Business Logic at the Center, Everything Else is Replaceable**

Your Issue Tracker uses this because:
- Complex business rules deserve clean, testable code
- 85% test coverage requirement (German market standard)
- Shows mid-level architectural thinking
- Future-proof (can add GraphQL, mobile app, etc. later)

You now understand 8 architecture patterns and can explain trade-offs. That's powerful knowledge for interviews! 🚀
