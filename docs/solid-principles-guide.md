# SOLID Principles & Best Practices Guide
## How They Apply to Our Hexagonal Architecture

**Purpose**: Understand SOLID principles and see WHERE we use them in our code

**Date**: 2025-12-26

---

## Table of Contents

1. [What is SOLID?](#what-is-solid)
2. [S - Single Responsibility Principle](#s---single-responsibility-principle)
3. [O - Open/Closed Principle](#o---openclosed-principle)
4. [L - Liskov Substitution Principle](#l---liskov-substitution-principle)
5. [I - Interface Segregation Principle](#i---interface-segregation-principle)
6. [D - Dependency Inversion Principle](#d---dependency-inversion-principle)
7. [How SOLID Shapes Our Folder Structure](#how-solid-shapes-our-folder-structure)
8. [Best Practices Summary](#best-practices-summary)

---

# What is SOLID?

**SOLID** = 5 principles for writing clean, maintainable object-oriented code

**Invented by**: Robert C. Martin (Uncle Bob) in early 2000s

**Why it matters**:
- ✅ Makes code easier to understand
- ✅ Makes code easier to change
- ✅ Makes code easier to test
- ✅ Reduces bugs
- ✅ **German companies ask about SOLID in interviews!**

**The acronym**:
- **S** - Single Responsibility Principle
- **O** - Open/Closed Principle
- **L** - Liskov Substitution Principle
- **I** - Interface Segregation Principle
- **D** - Dependency Inversion Principle

Let's learn each one with examples from YOUR project!

---

# S - Single Responsibility Principle

## Definition

**"A class should have ONE reason to change"**

OR

**"A class should do ONE thing and do it well"**

## Bad Example (Violates SRP)

```java
// ❌ BAD: This class has MULTIPLE responsibilities
public class IssueController {

    @PostMapping("/issues")
    public Issue createIssue(@RequestBody IssueRequest request) {
        // Responsibility 1: Handle HTTP request
        if (request == null) {
            return ResponseEntity.badRequest().build();
        }

        // Responsibility 2: Validate business rules
        if (request.getTitle().length() < 5) {
            throw new ValidationException("Title too short");
        }

        // Responsibility 3: Database access
        jdbcTemplate.update(
            "INSERT INTO issues (title, description) VALUES (?, ?)",
            request.getTitle(),
            request.getDescription()
        );

        // Responsibility 4: Send email notification
        emailService.sendEmail(
            request.getCitizenEmail(),
            "Issue created",
            "Your issue was created successfully"
        );

        return new Issue(...);
    }
}
```

**Problems**:
- If HTTP request format changes → must change this class
- If validation rules change → must change this class
- If database changes → must change this class
- If email service changes → must change this class

**4 reasons to change = violates SRP!**

## Good Example (Follows SRP)

```java
// ✅ GOOD: Each class has ONE responsibility

// Responsibility 1: Handle HTTP requests
@RestController
@RequestMapping("/api/issues")
public class IssueController {
    private final CreateIssueUseCase createIssueUseCase; // Delegate to use case

    @PostMapping
    public ResponseEntity<IssueResponse> createIssue(@RequestBody IssueRequest request) {
        // ONLY handles HTTP concerns (validation, mapping, response)
        Issue issue = createIssueUseCase.createIssue(request);
        return ResponseEntity.ok(IssueMapper.toResponse(issue));
    }
}

// Responsibility 2: Business logic
@Service
public class IssueService implements CreateIssueUseCase {
    private final IssueRepository repository;
    private final NotificationService notificationService;

    @Override
    public Issue createIssue(CreateIssueCommand command) {
        // ONLY handles business logic
        Issue issue = new Issue(command.getTitle(), command.getDescription());
        issue.validate(); // Validation logic in domain model

        Issue saved = repository.save(issue);
        notificationService.notifyIssueCreated(saved); // Delegate notification

        return saved;
    }
}

// Responsibility 3: Database access
@Repository
public class JpaIssueRepository implements IssueRepository {
    private final SpringDataIssueRepository springDataRepo;

    @Override
    public Issue save(Issue issue) {
        // ONLY handles database operations
        IssueEntity entity = IssueMapper.toEntity(issue);
        IssueEntity saved = springDataRepo.save(entity);
        return IssueMapper.toDomain(saved);
    }
}

// Responsibility 4: Email notifications
@Service
public class EmailNotificationService implements NotificationService {
    private final EmailSender emailSender;

    @Override
    public void notifyIssueCreated(Issue issue) {
        // ONLY handles email sending
        String message = "Your issue #" + issue.getId() + " was created";
        emailSender.send(issue.getCitizen().getEmail(), "Issue Created", message);
    }
}
```

**Benefits**:
- Change HTTP format? → Only touch IssueController
- Change validation? → Only touch Issue domain model
- Change database? → Only touch JpaIssueRepository
- Change email? → Only touch EmailNotificationService

**1 class = 1 reason to change!**

## How SRP Shapes Our Folder Structure

```
src/main/java/com/issuetracker/
├── infrastructure/adapter/in/web/
│   └── IssueController.java          # ONLY HTTP concerns
│
├── application/service/
│   └── IssueService.java              # ONLY orchestration
│
├── domain/model/
│   └── Issue.java                     # ONLY business rules
│
└── infrastructure/adapter/out/persistence/
    └── JpaIssueRepository.java        # ONLY database
```

**Each folder = ONE responsibility!**

---

# O - Open/Closed Principle

## Definition

**"Classes should be OPEN for extension, but CLOSED for modification"**

OR

**"You should be able to add new features WITHOUT changing existing code"**

## Bad Example (Violates OCP)

```java
// ❌ BAD: Must modify this class to add new assignment strategies
public class IssueService {

    public void assignIssue(Issue issue, String strategy) {
        if (strategy.equals("ROUND_ROBIN")) {
            // Round-robin assignment logic
            User staff = getNextStaffRoundRobin();
            issue.assignToStaff(staff);

        } else if (strategy.equals("LEAST_LOADED")) {
            // Least-loaded assignment logic
            User staff = getStaffWithFewestIssues();
            issue.assignToStaff(staff);

        } else if (strategy.equals("PRIORITY_BASED")) {
            // Priority-based assignment logic
            User staff = getSeniorStaffForHighPriority(issue);
            issue.assignToStaff(staff);
        }
        // Want to add new strategy? Must modify this class! ❌
    }
}
```

**Problem**: Every new strategy requires changing IssueService!

## Good Example (Follows OCP)

```java
// ✅ GOOD: Can add new strategies WITHOUT modifying existing code

// Strategy interface (in domain/port/out/)
public interface IssueAssignmentStrategy {
    User selectStaff(Issue issue, List<User> availableStaff);
}

// Strategy 1: Round-robin
public class RoundRobinAssignmentStrategy implements IssueAssignmentStrategy {
    private int currentIndex = 0;

    @Override
    public User selectStaff(Issue issue, List<User> availableStaff) {
        User selected = availableStaff.get(currentIndex);
        currentIndex = (currentIndex + 1) % availableStaff.size();
        return selected;
    }
}

// Strategy 2: Least-loaded
public class LeastLoadedAssignmentStrategy implements IssueAssignmentStrategy {
    @Override
    public User selectStaff(Issue issue, List<User> availableStaff) {
        return availableStaff.stream()
            .min(Comparator.comparingInt(User::getActiveIssueCount))
            .orElseThrow();
    }
}

// Strategy 3: Priority-based
public class PriorityBasedAssignmentStrategy implements IssueAssignmentStrategy {
    @Override
    public User selectStaff(Issue issue, List<User> availableStaff) {
        if (issue.getPriority() == Priority.HIGH) {
            return availableStaff.stream()
                .filter(User::isSenior)
                .findFirst()
                .orElse(availableStaff.get(0));
        }
        return availableStaff.get(0);
    }
}

// Service (CLOSED for modification, OPEN for extension)
public class IssueService {
    private final IssueAssignmentStrategy strategy; // Can inject any strategy!

    public IssueService(IssueAssignmentStrategy strategy) {
        this.strategy = strategy;
    }

    public void assignIssue(Issue issue) {
        List<User> availableStaff = userRepository.findByRole(UserRole.STAFF);
        User selectedStaff = strategy.selectStaff(issue, availableStaff);
        issue.assignToStaff(selectedStaff);
    }
}

// Configuration: Choose strategy
@Configuration
public class AssignmentConfig {
    @Bean
    public IssueAssignmentStrategy assignmentStrategy() {
        // Can switch strategies here without touching IssueService!
        return new LeastLoadedAssignmentStrategy();

        // Want to use round-robin? Just change this line:
        // return new RoundRobinAssignmentStrategy();

        // Want to add a NEW strategy? Create new class implementing interface!
        // NO need to modify IssueService! ✅
    }
}
```

**Benefits**:
- Add new strategy? → Create new class implementing interface
- IssueService never changes!
- No risk of breaking existing code

## How OCP Shapes Our Folder Structure

```
domain/port/in/          # Interfaces (OPEN for new implementations)
domain/port/out/         # Interfaces (OPEN for new implementations)

infrastructure/adapter/  # New adapters added without changing domain
```

**Example**: Want to add GraphQL API alongside REST?

```
infrastructure/adapter/in/
├── web/                 # REST (existing)
│   └── IssueController.java
└── graphql/             # NEW! (added without changing existing code)
    └── IssueResolver.java
```

---

# L - Liskov Substitution Principle

## Definition

**"Subclasses should be replaceable with their parent class"**

OR

**"If you have a Bird class, a Penguin (subclass) should work wherever Bird works"**

## Bad Example (Violates LSP)

```java
// ❌ BAD: Penguin breaks the "fly()" contract
public class Bird {
    public void fly() {
        System.out.println("Flying...");
    }
}

public class Penguin extends Bird {
    @Override
    public void fly() {
        throw new UnsupportedOperationException("Penguins can't fly!");
        // ❌ Violates LSP! Penguin can't replace Bird everywhere
    }
}

// Code that uses Bird
public void makeBirdFly(Bird bird) {
    bird.fly(); // Works for most birds, CRASHES for Penguin!
}
```

## Good Example (Follows LSP)

```java
// ✅ GOOD: Separate interfaces for different capabilities
public interface Animal {
    void eat();
}

public interface Flyable {
    void fly();
}

public class Sparrow implements Animal, Flyable {
    public void eat() { /* eating logic */ }
    public void fly() { /* flying logic */ }
}

public class Penguin implements Animal {
    public void eat() { /* eating logic */ }
    // No fly() method - Penguin doesn't promise to fly!
}

// Code that needs flying
public void makeFly(Flyable flyable) {
    flyable.fly(); // Only accepts things that CAN fly
}
```

## LSP in Our Project

```java
// ✅ GOOD: Repository interface
public interface IssueRepository {
    Issue save(Issue issue);
    Optional<Issue> findById(Long id);
}

// Implementation 1: PostgreSQL
public class JpaIssueRepository implements IssueRepository {
    @Override
    public Issue save(Issue issue) {
        // PostgreSQL save logic
    }

    @Override
    public Optional<Issue> findById(Long id) {
        // PostgreSQL find logic
    }
}

// Implementation 2: MongoDB (future)
public class MongoIssueRepository implements IssueRepository {
    @Override
    public Issue save(Issue issue) {
        // MongoDB save logic
    }

    @Override
    public Optional<Issue> findById(Long id) {
        // MongoDB find logic
    }
}

// Service doesn't care which implementation!
public class IssueService {
    private final IssueRepository repository; // Can be JPA or Mongo!

    public Issue createIssue(CreateIssueCommand command) {
        Issue issue = new Issue(...);
        return repository.save(issue); // Works with BOTH implementations!
    }
}
```

**LSP guarantees**: If it implements `IssueRepository`, it MUST work like a repository!

---

# I - Interface Segregation Principle

## Definition

**"Don't force clients to depend on methods they don't use"**

OR

**"Many small, specific interfaces are better than one large interface"**

## Bad Example (Violates ISP)

```java
// ❌ BAD: One large interface with methods not all implementations need
public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long id);
    List<User> findAll();

    // Admin-specific methods
    void deleteUser(Long id);
    void banUser(Long id);
    void unbanUser(Long id);

    // Report-specific methods
    List<User> generateMonthlyReport();
    byte[] exportToPdf();

    // Analytics-specific methods
    int countActiveUsers();
    Map<String, Integer> getUsersByCountry();
}

// Problem: Simple UserService must implement ALL methods!
public class SimpleUserService implements UserRepository {
    @Override
    public void generateMonthlyReport() {
        throw new UnsupportedOperationException("Not needed in this service!");
        // ❌ Forced to implement methods we don't use!
    }

    @Override
    public byte[] exportToPdf() {
        throw new UnsupportedOperationException("Not needed!");
    }
    // ... many more unused methods
}
```

## Good Example (Follows ISP)

```java
// ✅ GOOD: Separate interfaces for different needs

// Basic CRUD (everyone needs this)
public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long id);
    List<User> findAll();
}

// Admin operations (only admin service needs this)
public interface UserAdminOperations {
    void deleteUser(Long id);
    void banUser(Long id);
    void unbanUser(Long id);
}

// Reporting (only report service needs this)
public interface UserReportOperations {
    List<User> generateMonthlyReport();
    byte[] exportToPdf();
}

// Analytics (only analytics service needs this)
public interface UserAnalyticsOperations {
    int countActiveUsers();
    Map<String, Integer> getUsersByCountry();
}

// Implementations can choose what to implement
public class JpaUserRepository implements UserRepository, UserAdminOperations {
    // Implements ONLY what it needs
}

public class UserReportService implements UserReportOperations {
    // Implements ONLY reporting methods
}
```

## ISP in Our Project

```java
// ✅ Separate interfaces in domain/port/in/ (use cases)

// Create issue use case (one specific operation)
public interface CreateIssueUseCase {
    Issue createIssue(CreateIssueCommand command);
}

// Assign issue use case (separate operation)
public interface AssignIssueUseCase {
    void assignIssue(Long issueId, Long staffId);
}

// Update issue status use case (separate operation)
public interface UpdateIssueStatusUseCase {
    void updateStatus(Long issueId, IssueStatus newStatus);
}

// Controller can depend on ONLY what it needs
@RestController
public class IssueController {
    private final CreateIssueUseCase createIssueUseCase;
    // Only depends on create, NOT on assign or update!

    @PostMapping
    public IssueResponse createIssue(@RequestBody IssueRequest request) {
        Issue issue = createIssueUseCase.createIssue(...);
        return IssueMapper.toResponse(issue);
    }
}
```

**Benefit**: Controller doesn't depend on methods it doesn't use!

---

# D - Dependency Inversion Principle

## Definition

**"High-level modules should NOT depend on low-level modules. Both should depend on abstractions (interfaces)"**

OR

**"Depend on interfaces, not concrete classes"**

**This is THE MOST IMPORTANT principle for Hexagonal Architecture!**

## Bad Example (Violates DIP)

```java
// ❌ BAD: High-level (Service) depends on low-level (JpaRepository)
public class IssueService {  // HIGH-LEVEL (business logic)

    // Direct dependency on concrete class!
    private final JpaIssueRepository repository; // LOW-LEVEL (database detail)

    public IssueService() {
        this.repository = new JpaIssueRepository(); // Creates concrete class!
    }

    public Issue createIssue(CreateIssueCommand command) {
        Issue issue = new Issue(...);
        return repository.save(issue);
    }
}

// Problems:
// 1. Can't test IssueService without real database
// 2. Can't swap JpaRepository for MongoRepository
// 3. Business logic tied to database implementation
```

## Good Example (Follows DIP)

```java
// ✅ GOOD: Both depend on abstraction (interface)

// Abstraction (defined by DOMAIN, not infrastructure!)
public interface IssueRepository {  // ABSTRACTION
    Issue save(Issue issue);
    Optional<Issue> findById(Long id);
}

// High-level module depends on INTERFACE
public class IssueService {  // HIGH-LEVEL
    private final IssueRepository repository; // Depends on INTERFACE!

    // Dependency injected (Spring does this)
    public IssueService(IssueRepository repository) {
        this.repository = repository;
    }

    public Issue createIssue(CreateIssueCommand command) {
        Issue issue = new Issue(...);
        return repository.save(issue); // Doesn't care about implementation!
    }
}

// Low-level module implements INTERFACE
@Repository
public class JpaIssueRepository implements IssueRepository {  // LOW-LEVEL
    @Override
    public Issue save(Issue issue) {
        // PostgreSQL implementation
    }
}

// Can swap implementations easily!
@Repository
public class MongoIssueRepository implements IssueRepository {  // LOW-LEVEL
    @Override
    public Issue save(Issue issue) {
        // MongoDB implementation
    }
}
```

**Benefits**:
- ✅ IssueService doesn't know about JPA or Mongo
- ✅ Can test IssueService with mock repository (no database!)
- ✅ Can swap database without touching business logic

## How DIP Creates Our Folder Structure

```
┌─────────────────────────────────────────────────┐
│  Infrastructure (LOW-LEVEL - depends on domain) │
│  ├── JpaIssueRepository implements IssueRepository
│  └── MongoIssueRepository implements IssueRepository
└─────────────┬───────────────────────────────────┘
              │ depends on ↓
┌─────────────▼───────────────────────────────────┐
│  Domain (HIGH-LEVEL - defines interface)        │
│  └── IssueRepository interface                  │
└──────────────────────────────────────────────────┘
```

**Dependency direction**: Infrastructure → Domain (NOT domain → infrastructure!)

---

# How SOLID Shapes Our Folder Structure

Let's see how ALL 5 principles create our Hexagonal Architecture:

```
src/main/java/com/issuetracker/

├── domain/                          # Dependency Inversion (D)
│   │                                # Domain defines interfaces
│   │                                # Infrastructure implements them
│   │
│   ├── model/                       # Single Responsibility (S)
│   │   ├── Issue.java               # ONLY business entity
│   │   ├── User.java                # ONLY user business logic
│   │   └── Timeline.java            # ONLY timeline entity
│   │
│   ├── port/                        # Interface Segregation (I)
│   │   │                            # Small, focused interfaces
│   │   │
│   │   ├── in/                      # Use cases (what app can DO)
│   │   │   ├── CreateIssueUseCase.java      # S: One operation
│   │   │   ├── AssignIssueUseCase.java      # S: One operation
│   │   │   └── UpdateStatusUseCase.java     # S: One operation
│   │   │
│   │   └── out/                     # What app NEEDS
│   │       ├── IssueRepository.java         # I: Only persistence
│   │       └── NotificationService.java     # I: Only notifications
│   │
│   └── service/                     # Open/Closed (O)
│       └── IssueAssignmentStrategy.java     # O: New strategies
│                                              #    without modifying
│
├── application/                     # Single Responsibility (S)
│   ├── service/                     # ONLY orchestration
│   │   └── IssueService.java
│   │
│   ├── dto/                         # ONLY data transfer
│   │   ├── IssueRequest.java
│   │   └── IssueResponse.java
│   │
│   └── mapper/                      # ONLY mapping
│       └── IssueMapper.java
│
└── infrastructure/                  # Dependency Inversion (D)
    │                                # Implements domain interfaces
    │
    ├── adapter/
    │   ├── in/web/                  # Liskov Substitution (L)
    │   │   └── IssueController.java # L: Can replace with GraphQL
    │   │
    │   └── out/persistence/         # Liskov Substitution (L)
    │       ├── JpaIssueRepository.java   # L: Implements IssueRepository
    │       └── MongoIssueRepository.java # L: Can replace JPA
    │
    ├── config/                      # Single Responsibility (S)
    │   └── DatabaseConfig.java      # ONLY configuration
    │
    └── security/                    # Single Responsibility (S)
        └── JwtTokenProvider.java    # ONLY JWT logic
```

---

# Best Practices Summary

## 1. Single Responsibility (S)

**Rule**: One class, one job

**In your code**:
```java
// ✅ DO: Separate concerns
@RestController
public class IssueController { /* ONLY HTTP */ }

@Service
public class IssueService { /* ONLY business logic */ }

@Repository
public class JpaIssueRepository { /* ONLY database */ }

// ❌ DON'T: Mix concerns
@RestController
public class IssueController {
    // Don't put database code here!
    // Don't put business logic here!
}
```

**Comments to write**:
```java
/**
 * Handles HTTP requests for issue management.
 *
 * RESPONSIBILITY: REST API endpoints only
 * - Maps HTTP requests to domain commands
 * - Maps domain objects to HTTP responses
 * - Handles HTTP status codes (200, 400, 404)
 *
 * Does NOT contain:
 * - Business logic (that's in IssueService)
 * - Database access (that's in IssueRepository)
 * - Validation rules (that's in domain model)
 */
@RestController
@RequestMapping("/api/issues")
public class IssueController {
    // ...
}
```

---

## 2. Open/Closed (O)

**Rule**: Open for extension, closed for modification

**In your code**:
```java
// ✅ DO: Use interfaces for strategies
public interface IssueAssignmentStrategy {
    User selectStaff(Issue issue, List<User> available);
}

// Add new strategies without modifying existing code
public class NewStrategy implements IssueAssignmentStrategy {
    // ...
}

// ❌ DON'T: Use if/else chains
public void assign(String strategy) {
    if (strategy.equals("A")) { /* ... */ }
    else if (strategy.equals("B")) { /* ... */ }
    // Adding "C" requires modifying this method!
}
```

**Comments to write**:
```java
/**
 * Strategy interface for issue assignment algorithms.
 *
 * EXTENSIBILITY: Implement this interface to add new assignment strategies
 * without modifying existing code (Open/Closed Principle).
 *
 * Examples:
 * - RoundRobinAssignmentStrategy
 * - LeastLoadedAssignmentStrategy
 * - PriorityBasedAssignmentStrategy
 *
 * To add a new strategy:
 * 1. Create class implementing this interface
 * 2. Configure as Spring Bean
 * 3. No changes to IssueService needed!
 */
public interface IssueAssignmentStrategy {
    User selectStaff(Issue issue, List<User> availableStaff);
}
```

---

## 3. Liskov Substitution (L)

**Rule**: Subtypes must be replaceable

**In your code**:
```java
// ✅ DO: Ensure implementations work correctly
public interface IssueRepository {
    Issue save(Issue issue);
}

// Both implementations MUST work correctly
public class JpaIssueRepository implements IssueRepository {
    public Issue save(Issue issue) { /* returns saved issue */ }
}

public class MongoIssueRepository implements IssueRepository {
    public Issue save(Issue issue) { /* returns saved issue */ }
}

// ❌ DON'T: Break contracts
public class BrokenRepository implements IssueRepository {
    public Issue save(Issue issue) {
        return null; // ❌ Violates contract! Should return saved issue
    }
}
```

---

## 4. Interface Segregation (I)

**Rule**: Small, focused interfaces

**In your code**:
```java
// ✅ DO: Separate interfaces
public interface CreateIssueUseCase {
    Issue createIssue(CreateIssueCommand command);
}

public interface UpdateIssueUseCase {
    Issue updateIssue(UpdateIssueCommand command);
}

// ❌ DON'T: One large interface
public interface IssueOperations {
    Issue create(...);
    Issue update(...);
    Issue delete(...);
    Issue assign(...);
    // 20 more methods...
}
```

**Comments to write**:
```java
/**
 * Use case: Create a new issue in the system.
 *
 * SCOPE: Single operation (Interface Segregation Principle)
 * - Clients depend only on what they need
 * - Easy to test (mock single method)
 * - Clear contract (one responsibility)
 *
 * Separate from UpdateIssueUseCase, DeleteIssueUseCase, etc.
 * to avoid forcing clients to depend on unused methods.
 */
public interface CreateIssueUseCase {
    Issue createIssue(CreateIssueCommand command);
}
```

---

## 5. Dependency Inversion (D)

**Rule**: Depend on abstractions (interfaces)

**In your code**:
```java
// ✅ DO: Depend on interface
public class IssueService {
    private final IssueRepository repository; // Interface!

    public IssueService(IssueRepository repository) {
        this.repository = repository;
    }
}

// ❌ DON'T: Depend on concrete class
public class IssueService {
    private final JpaIssueRepository repository; // Concrete class!

    public IssueService() {
        this.repository = new JpaIssueRepository(); // ❌ Tight coupling!
    }
}
```

**Comments to write**:
```java
/**
 * Business service for issue management.
 *
 * DEPENDENCIES: Depends on abstractions, not implementations
 * (Dependency Inversion Principle)
 *
 * - IssueRepository interface (NOT JpaIssueRepository)
 * - NotificationService interface (NOT EmailNotificationService)
 *
 * Benefits:
 * - Easy to test (inject mocks)
 * - Easy to swap implementations (JPA → Mongo)
 * - Domain logic independent of frameworks
 */
@Service
public class IssueService implements CreateIssueUseCase {
    private final IssueRepository repository; // ✅ Interface
    private final NotificationService notifier; // ✅ Interface

    // Constructor injection (Spring provides implementations)
    public IssueService(IssueRepository repository,
                       NotificationService notifier) {
        this.repository = repository;
        this.notifier = notifier;
    }
}
```

---

# Interview Questions You Can Answer

## Q: "What SOLID principles do you know?"

**Answer**:
"I use all 5 SOLID principles in my Issue Tracker project:

**S - Single Responsibility**: Each class has one job. My IssueController only handles HTTP, IssueService only has business logic, and JpaIssueRepository only accesses the database.

**O - Open/Closed**: I use the Strategy pattern for issue assignment. I can add new assignment strategies by creating new classes implementing IssueAssignmentStrategy, without modifying existing code.

**L - Liskov Substitution**: My JpaIssueRepository and potential MongoIssueRepository both implement IssueRepository interface and can be swapped without breaking IssueService.

**I - Interface Segregation**: I have small, focused use case interfaces like CreateIssueUseCase, AssignIssueUseCase instead of one large interface, so controllers only depend on methods they actually use.

**D - Dependency Inversion**: My IssueService depends on IssueRepository interface (abstraction), not on JpaIssueRepository (concrete class). This is the foundation of my Hexagonal Architecture."

---

## Q: "Why is Dependency Inversion important?"

**Answer**:
"Dependency Inversion is crucial because it decouples high-level business logic from low-level technical details.

In my project, the domain layer defines the IssueRepository interface (what it needs), and the infrastructure layer implements it (how it works). This means:

1. I can test business logic without a database (mock the repository)
2. I can swap PostgreSQL for MongoDB without touching domain code
3. My business rules are framework-independent

This is the core principle behind Hexagonal Architecture - business logic at the center, everything else is replaceable."

---

# Code Comments Best Practices

## 1. Explain WHY, not WHAT

```java
// ❌ BAD: Obvious from code
// Save issue to repository
Issue saved = repository.save(issue);

// ✅ GOOD: Explains reasoning
// Must save BEFORE sending notification to ensure issue has ID for email link
Issue saved = repository.save(issue);
notificationService.notifyCreated(saved);
```

## 2. Document Decisions

```java
// ✅ GOOD: Explains architectural decision
/**
 * Uses RESTRICT instead of CASCADE on citizen_id foreign key.
 *
 * WHY: Citizens should not be deleted if they have active issues.
 * This protects historical data and prevents accidental data loss.
 * If citizen requests deletion, we first close their issues,
 * then delete the citizen record.
 */
@ManyToOne
@JoinColumn(name = "citizen_id",
            foreignKey = @ForeignKey(
                name = "fk_issue_citizen",
                foreignKeyDefinition = "FOREIGN KEY (citizen_id) REFERENCES users(id) ON DELETE RESTRICT"
            ))
private User citizen;
```

## 3. Mark SOLID Principles

```java
// ✅ GOOD: Documents which principle and why
/**
 * Repository interface for Issue persistence.
 *
 * SOLID PRINCIPLE: Dependency Inversion (D)
 * - Domain defines this interface
 * - Infrastructure implements it (JpaIssueRepository)
 * - IssueService depends on THIS interface, not implementation
 *
 * BENEFIT: Can swap database (PostgreSQL → MongoDB) without
 * changing business logic in IssueService.
 */
public interface IssueRepository {
    Issue save(Issue issue);
    Optional<Issue> findById(Long id);
}
```

---

# Summary: SOLID + Hexagonal Architecture

**Why our folder structure exists**:

1. **S (Single Responsibility)** → Each folder has ONE job
   - `domain/` = Business logic
   - `application/` = Orchestration
   - `infrastructure/` = Technical details

2. **O (Open/Closed)** → Can add features without modifying
   - Add new API? → New folder in `infrastructure/adapter/in/`
   - Add new database? → New folder in `infrastructure/adapter/out/`

3. **L (Liskov Substitution)** → Implementations interchangeable
   - JPA ↔ Mongo
   - REST ↔ GraphQL

4. **I (Interface Segregation)** → Small, focused interfaces
   - `domain/port/in/` = Separate use case per file
   - `domain/port/out/` = Separate repository per aggregate

5. **D (Dependency Inversion)** → Depend on abstractions
   - Domain defines interfaces
   - Infrastructure implements them
   - **This creates the Hexagonal Architecture!**

---

**Ready to create folders with SOLID principles in mind?** 🚀
