# 1. Use Hexagonal Architecture (Clean Architecture)

Date: 2025-12-26

## Status

Accepted

## Context

We need to choose an architecture pattern for our Spring Boot backend. The application has:
- Multiple user roles (Admin, Staff, Citizen) with different permissions
- Complex business logic (issue lifecycle, status transitions, timeline tracking)
- Need for high test coverage (85%+ target for German market)
- Future potential for API changes or database migrations

**Options Considered:**
1. **Traditional Layered Architecture** (Controller → Service → Repository)
2. **Hexagonal Architecture** (Domain-driven with Ports & Adapters)
3. **Microservices** (too complex for MVP)

## Decision

We will use **Hexagonal Architecture** (also called Ports and Adapters or Clean Architecture).

### Architecture Layers:

```
src/main/java/com/project/issuetracker/
├── domain/              # Core business logic (NO framework dependencies)
│   ├── model/           # Entities: User, Issue, Timeline
│   ├── port/in/         # Use case interfaces (what app can do)
│   ├── port/out/        # Repository interfaces (what app needs)
│   └── service/         # Business rules implementation
│
├── application/         # Application layer (orchestration)
│   ├── service/         # Use case implementations
│   ├── dto/             # Data Transfer Objects
│   └── mapper/          # DTO ↔ Domain mapping
│
└── infrastructure/      # External world (frameworks, DB, web)
    ├── adapter/in/web/  # REST controllers (Spring MVC)
    ├── adapter/out/persistence/  # JPA repositories
    ├── config/          # Spring configuration
    └── security/        # Spring Security setup
```

### Key Principles:

1. **Dependency Rule**: Dependencies point INWARD
   - Infrastructure → Application → Domain
   - Domain has ZERO dependencies on frameworks

2. **Ports (Interfaces)**:
   - Input Ports: Use cases (e.g., `CreateIssueUseCase`)
   - Output Ports: Repository contracts (e.g., `IssueRepository`)

3. **Adapters (Implementations)**:
   - Input Adapters: REST controllers
   - Output Adapters: JPA repositories

## Consequences

### Positive ✅

1. **Testability**
   - Domain logic can be tested WITHOUT Spring Boot
   - No database needed for business logic tests
   - Faster test execution → faster CI/CD
   - Easy to reach 85%+ coverage target

2. **Maintainability**
   - Clear separation of concerns
   - Business logic isolated from framework code
   - Easy to understand what the app DOES (just read domain layer)

3. **Flexibility**
   - Can swap databases (PostgreSQL → MySQL) without touching domain
   - Can add new APIs (REST → GraphQL) without changing business logic
   - Can replace Spring Boot with another framework (unlikely but possible)

4. **German Market Appeal**
   - Shows architectural thinking (not just coding)
   - Demonstrates understanding of SOLID principles
   - Common in enterprise Java projects in Germany
   - Interviewers will recognize this pattern

5. **Onboarding**
   - New developers can understand business logic by reading domain layer
   - Clear boundaries prevent "spaghetti code"

### Negative ❌

1. **More Boilerplate**
   - Need to create interfaces (ports) for everything
   - Need DTOs and mappers (more classes to maintain)
   - Simple CRUD operations become more verbose

2. **Learning Curve**
   - Team needs to understand the architecture
   - Requires discipline to follow the rules
   - Risk of "architectural astronaut" syndrome (over-engineering)

3. **Initial Setup Time**
   - Takes longer to set up compared to traditional layers
   - Need to create folder structure carefully

4. **Overkill for Very Simple Apps**
   - If app is just basic CRUD with no business logic, this might be too much
   - However, our app HAS business logic (status transitions, role-based access, timeline tracking)

### Neutral 🔄

1. **Code Volume**
   - More files and interfaces
   - But each file is smaller and focused
   - Total lines of code similar to traditional architecture

2. **Performance**
   - No performance impact (just different organization)
   - JVM optimizes away the extra abstraction layers

## Why This Fits Our Project

1. **Complex Business Rules**
   - Issue status transitions: PENDING → IN_PROGRESS → WORKING → RESOLVED → CLOSED
   - Role-based permissions: Admin vs Staff vs Citizen
   - Timeline tracking with validation
   - These deserve clean, testable domain logic

2. **High Test Coverage Requirement**
   - German companies expect 80%+ coverage
   - We target 85%
   - Hexagonal architecture makes this achievable without slow integration tests

3. **Portfolio Value**
   - Shows we understand architecture beyond "just coding"
   - Demonstrates mid-level engineering thinking
   - Sets us apart from junior developers

4. **Future-Proof**
   - If we add payment system later, it's just another adapter
   - If we add mobile app, it's just another input adapter
   - Architecture supports growth

## Implementation Plan

1. **Sprint 1**: Set up folder structure following the architecture
2. **Sprint 1**: Create domain models (User, Issue, Timeline) as POJOs
3. **Sprint 1**: Define port interfaces (use cases and repositories)
4. **Sprint 2+**: Implement adapters (controllers, JPA repositories)

## References

- [Alistair Cockburn - Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Clean Architecture by Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Netflix - Ready for changes with Hexagonal Architecture](https://netflixtechblog.com/ready-for-changes-with-hexagonal-architecture-b315ec967749)
