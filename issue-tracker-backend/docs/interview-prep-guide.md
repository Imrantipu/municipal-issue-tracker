# Technical Interview Preparation Guide
## Design Patterns + Data Structures + Algorithms

## Overview: What Interviewers Ask

German technical interviews typically have **3 parts**:

```
┌─────────────────────────────────────────────────────────────┐
│  1. DESIGN PATTERNS (Software Engineering)                  │
│     "How do you structure your code?"                       │
│     Focus: Clean, maintainable, reusable code              │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  2. DATA STRUCTURES (Computer Science Fundamentals)         │
│     "How do you organize and store data?"                   │
│     Focus: Choosing the right structure for the job        │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  3. ALGORITHMS (Problem Solving)                            │
│     "How do you solve computational problems efficiently?"  │
│     Focus: Complexity analysis (Big O), optimization        │
└─────────────────────────────────────────────────────────────┘
```

**Important**: German companies care MORE about Design Patterns and practical coding than LeetCode-style algorithm puzzles (unlike US FAANG interviews).

---

# Part 1: Design Patterns

## What Are Design Patterns?

**Simple explanation**: Proven solutions to common programming problems.

Think of them as "recipes" for code structure. Just like you don't invent how to make bread from scratch every time (you follow a recipe), you don't invent how to solve common coding problems (you use patterns).

## The 23 Gang of Four (GoF) Patterns

In 1994, four authors wrote a book with 23 patterns. These are THE classic patterns every developer should know.

**Grouped into 3 categories:**
1. **Creational Patterns** (How to create objects)
2. **Structural Patterns** (How to organize objects)
3. **Behavioral Patterns** (How objects communicate)

---

## ⭐ Top 10 Patterns for Your Interview (Prioritized)

### Must Know (5 patterns) - Learn First

These appear in your project and are asked in 80% of interviews.

#### 1. Singleton Pattern ⭐⭐⭐ (Creational)

**Problem**: You need exactly ONE instance of a class throughout your app.

**Example**: Database connection pool, configuration manager

**Your Project Use Case**: JWT Token Generator (only one instance needed)

```java
// BAD: Multiple instances
public class JwtTokenGenerator {
    private String secretKey;

    public JwtTokenGenerator() {
        this.secretKey = loadFromEnv(); // ❌ Loads multiple times
    }
}

// GOOD: Singleton
public class JwtTokenGenerator {
    private static JwtTokenGenerator instance;
    private String secretKey;

    // Private constructor prevents instantiation
    private JwtTokenGenerator() {
        this.secretKey = loadFromEnv();
    }

    // Thread-safe singleton
    public static synchronized JwtTokenGenerator getInstance() {
        if (instance == null) {
            instance = new JwtTokenGenerator();
        }
        return instance;
    }

    public String generateToken(User user) {
        // Generate JWT
    }
}

// Usage
JwtTokenGenerator generator = JwtTokenGenerator.getInstance();
String token = generator.generateToken(user);
```

**When interviewer asks**: "What if two threads call getInstance() simultaneously?"

**Your answer**: "I use synchronized keyword for thread safety, or use eager initialization (create instance at class loading), or use Bill Pugh Singleton (inner static helper class) for better performance."

**Spring Boot Note**: Spring's `@Bean` with default scope is a Singleton! Spring manages it for you.

---

#### 2. Factory Pattern ⭐⭐⭐ (Creational)

**Problem**: You need to create objects but don't want to specify exact class.

**Example**: Creating different types of users (Admin, Staff, Citizen)

**Your Project Use Case**: User Factory based on role

```java
// Without Factory - BAD
public User createUser(String role, String name, String email) {
    if (role.equals("ADMIN")) {
        return new AdminUser(name, email);
    } else if (role.equals("STAFF")) {
        return new StaffUser(name, email);
    } else if (role.equals("CITIZEN")) {
        return new CitizenUser(name, email);
    }
    // ❌ If/else everywhere in code
}

// With Factory - GOOD
public interface User {
    void performAction();
}

public class AdminUser implements User {
    public void performAction() { /* Admin logic */ }
}

public class StaffUser implements User {
    public void performAction() { /* Staff logic */ }
}

public class CitizenUser implements User {
    public void performAction() { /* Citizen logic */ }
}

// Factory
public class UserFactory {
    public static User createUser(String role, String name, String email) {
        return switch (role) {
            case "ADMIN" -> new AdminUser(name, email);
            case "STAFF" -> new StaffUser(name, email);
            case "CITIZEN" -> new CitizenUser(name, email);
            default -> throw new IllegalArgumentException("Invalid role");
        };
    }
}

// Usage - Clean!
User user = UserFactory.createUser("ADMIN", "John", "john@example.com");
user.performAction();
```

**Benefits**:
- Centralized object creation (one place to change)
- Reduces code duplication
- Easy to add new user types

---

#### 3. Repository Pattern ⭐⭐⭐ (Structural)

**Problem**: Separate data access logic from business logic.

**Your Project**: You're ALREADY using this! (JPA Repositories)

```java
// Repository interface (Port in Hexagonal Architecture)
public interface IssueRepository {
    Issue save(Issue issue);
    Optional<Issue> findById(Long id);
    List<Issue> findByCitizenId(Long citizenId);
    void delete(Long id);
}

// Implementation (Adapter)
@Repository
public class JpaIssueRepository implements IssueRepository {
    private final SpringDataIssueRepository springDataRepo;

    @Override
    public Issue save(Issue issue) {
        IssueEntity entity = mapper.toEntity(issue);
        IssueEntity saved = springDataRepo.save(entity);
        return mapper.toDomain(saved);
    }
}

// Business logic uses interface, not implementation
public class IssueService {
    private final IssueRepository repository; // ✅ Depends on interface

    public Issue createIssue(CreateIssueCommand cmd) {
        Issue issue = new Issue(...);
        return repository.save(issue); // ✅ Don't care about database details
    }
}
```

**Benefits**:
- Business logic doesn't know about database
- Easy to test (mock repository)
- Can swap databases without changing business code

**Interview Question**: "What's the difference between DAO and Repository?"

**Answer**: "DAO (Data Access Object) is lower-level, one DAO per table. Repository is domain-driven, works with aggregates (e.g., IssueRepository returns Issue with Timeline, not separate table access). Repository is more business-focused."

---

#### 4. Strategy Pattern ⭐⭐⭐ (Behavioral)

**Problem**: You have different algorithms for the same task and want to switch between them.

**Example**: Different notification strategies (Email, SMS, Push)

**Your Project Use Case**: Issue assignment strategies (Auto-assign vs Manual)

```java
// Strategy interface
public interface IssueAssignmentStrategy {
    User assignStaff(Issue issue, List<User> availableStaff);
}

// Strategy 1: Round-robin (fair distribution)
public class RoundRobinAssignment implements IssueAssignmentStrategy {
    private int currentIndex = 0;

    @Override
    public User assignStaff(Issue issue, List<User> availableStaff) {
        User assigned = availableStaff.get(currentIndex);
        currentIndex = (currentIndex + 1) % availableStaff.size();
        return assigned;
    }
}

// Strategy 2: Least-loaded (assign to staff with fewest issues)
public class LeastLoadedAssignment implements IssueAssignmentStrategy {
    @Override
    public User assignStaff(Issue issue, List<User> availableStaff) {
        return availableStaff.stream()
            .min(Comparator.comparingInt(User::getAssignedIssueCount))
            .orElseThrow();
    }
}

// Strategy 3: Priority-based (high priority → senior staff)
public class PriorityBasedAssignment implements IssueAssignmentStrategy {
    @Override
    public User assignStaff(Issue issue, List<User> availableStaff) {
        if (issue.getPriority() == Priority.HIGH) {
            return availableStaff.stream()
                .filter(User::isSeniorStaff)
                .findFirst()
                .orElse(availableStaff.get(0));
        }
        return availableStaff.get(0);
    }
}

// Context (uses strategy)
public class IssueService {
    private IssueAssignmentStrategy assignmentStrategy;

    public void setAssignmentStrategy(IssueAssignmentStrategy strategy) {
        this.assignmentStrategy = strategy;
    }

    public void autoAssignIssue(Issue issue) {
        List<User> availableStaff = userRepository.findByRole(UserRole.STAFF);
        User assignedStaff = assignmentStrategy.assignStaff(issue, availableStaff);
        issue.assignToStaff(assignedStaff);
    }
}

// Usage - Switch strategies easily!
IssueService service = new IssueService();

// Use round-robin for normal days
service.setAssignmentStrategy(new RoundRobinAssignment());

// Use priority-based during peak hours
service.setAssignmentStrategy(new PriorityBasedAssignment());
```

**Benefits**:
- No if/else chains (clean code)
- Easy to add new strategies (just implement interface)
- Business logic separated from algorithm

---

#### 5. Observer Pattern ⭐⭐⭐ (Behavioral)

**Problem**: When one object changes, you want other objects to be notified automatically.

**Example**: When issue status changes, notify citizen via email

**Your Project Use Case**: Issue status change notifications

```java
// Observer interface
public interface IssueObserver {
    void onIssueStatusChanged(Issue issue, IssueStatus oldStatus, IssueStatus newStatus);
}

// Concrete observers
public class EmailNotificationObserver implements IssueObserver {
    private final EmailService emailService;

    @Override
    public void onIssueStatusChanged(Issue issue, IssueStatus oldStatus, IssueStatus newStatus) {
        String message = String.format(
            "Your issue #%d status changed from %s to %s",
            issue.getId(), oldStatus, newStatus
        );
        emailService.sendEmail(issue.getCitizen().getEmail(), "Issue Update", message);
    }
}

public class TimelineObserver implements IssueObserver {
    private final TimelineRepository timelineRepository;

    @Override
    public void onIssueStatusChanged(Issue issue, IssueStatus oldStatus, IssueStatus newStatus) {
        Timeline entry = new Timeline(
            issue.getId(),
            newStatus,
            "Status changed from " + oldStatus + " to " + newStatus,
            LocalDateTime.now()
        );
        timelineRepository.save(entry);
    }
}

public class StatisticsObserver implements IssueObserver {
    @Override
    public void onIssueStatusChanged(Issue issue, IssueStatus oldStatus, IssueStatus newStatus) {
        // Update dashboard statistics
        metricsService.incrementStatusCount(newStatus);
        metricsService.decrementStatusCount(oldStatus);
    }
}

// Subject (Issue)
public class Issue {
    private List<IssueObserver> observers = new ArrayList<>();
    private IssueStatus status;

    public void addObserver(IssueObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(IssueObserver observer) {
        observers.remove(observer);
    }

    public void setStatus(IssueStatus newStatus) {
        IssueStatus oldStatus = this.status;
        this.status = newStatus;

        // Notify all observers
        notifyObservers(oldStatus, newStatus);
    }

    private void notifyObservers(IssueStatus oldStatus, IssueStatus newStatus) {
        for (IssueObserver observer : observers) {
            observer.onIssueStatusChanged(this, oldStatus, newStatus);
        }
    }
}

// Usage
Issue issue = issueRepository.findById(123);

// Attach observers
issue.addObserver(new EmailNotificationObserver(emailService));
issue.addObserver(new TimelineObserver(timelineRepository));
issue.addObserver(new StatisticsObserver(metricsService));

// Change status → all observers notified automatically!
issue.setStatus(IssueStatus.RESOLVED);
```

**Benefits**:
- Loose coupling (Issue doesn't know about email, timeline, etc.)
- Easy to add new observers (no code changes to Issue)
- Single Responsibility (each observer does ONE thing)

**Spring Boot Note**: Spring's `ApplicationEventPublisher` is Observer pattern!

```java
// Spring Boot way
@Service
public class IssueService {
    private final ApplicationEventPublisher eventPublisher;

    public void updateStatus(Issue issue, IssueStatus newStatus) {
        IssueStatus oldStatus = issue.getStatus();
        issue.setStatus(newStatus);

        // Publish event
        eventPublisher.publishEvent(
            new IssueStatusChangedEvent(issue, oldStatus, newStatus)
        );
    }
}

@Component
public class EmailNotificationListener {
    @EventListener
    public void handleStatusChange(IssueStatusChangedEvent event) {
        // Send email
    }
}
```

---

### Good to Know (5 more patterns) - Learn After First 5

#### 6. Adapter Pattern (Structural)

**Problem**: Two incompatible interfaces need to work together.

**Your Project**: Convert domain Issue to JPA IssueEntity

```java
// Domain model (what business logic uses)
public class Issue {
    private Long id;
    private String title;
    private IssueStatus status;
}

// JPA entity (what database uses)
@Entity
public class IssueEntity {
    @Id
    private Long id;
    private String title;

    @Enumerated(EnumType.STRING)
    private IssueStatus status;
}

// Adapter (converts between them)
public class IssueEntityMapper {
    public static IssueEntity toEntity(Issue domain) {
        IssueEntity entity = new IssueEntity();
        entity.setId(domain.getId());
        entity.setTitle(domain.getTitle());
        entity.setStatus(domain.getStatus());
        return entity;
    }

    public static Issue toDomain(IssueEntity entity) {
        return new Issue(
            entity.getId(),
            entity.getTitle(),
            entity.getStatus()
        );
    }
}
```

---

#### 7. Decorator Pattern (Structural)

**Problem**: Add behavior to objects without modifying their code.

**Example**: Add logging, caching, or validation to methods

```java
// Component interface
public interface IssueRepository {
    Issue save(Issue issue);
}

// Base implementation
public class JpaIssueRepository implements IssueRepository {
    public Issue save(Issue issue) {
        // Save to database
    }
}

// Decorator 1: Add logging
public class LoggingIssueRepository implements IssueRepository {
    private final IssueRepository wrapped;

    public LoggingIssueRepository(IssueRepository wrapped) {
        this.wrapped = wrapped;
    }

    public Issue save(Issue issue) {
        log.info("Saving issue: {}", issue.getId());
        Issue saved = wrapped.save(issue);
        log.info("Issue saved successfully");
        return saved;
    }
}

// Decorator 2: Add caching
public class CachingIssueRepository implements IssueRepository {
    private final IssueRepository wrapped;
    private final Cache cache;

    public Issue save(Issue issue) {
        Issue saved = wrapped.save(issue);
        cache.put(saved.getId(), saved);
        return saved;
    }
}

// Usage - Stack decorators!
IssueRepository repo = new JpaIssueRepository();
repo = new LoggingIssueRepository(repo);     // Add logging
repo = new CachingIssueRepository(repo);     // Add caching
```

**Spring Boot Note**: `@Transactional`, `@Cacheable` are decorators!

---

#### 8. Builder Pattern (Creational)

**Problem**: Creating objects with many optional parameters.

**Example**: Building complex Issue objects

```java
// Without Builder - BAD
Issue issue = new Issue(
    null,
    "Road pothole",
    "Large pothole on Main St",
    IssueCategory.ROADS,
    "Main Street",
    IssueStatus.PENDING,
    IssuePriority.NORMAL,
    citizen,
    null,
    LocalDateTime.now(),
    null
); // ❌ Hard to read, easy to mix up parameters

// With Builder - GOOD
Issue issue = Issue.builder()
    .title("Road pothole")
    .description("Large pothole on Main St")
    .category(IssueCategory.ROADS)
    .location("Main Street")
    .status(IssueStatus.PENDING)
    .priority(IssuePriority.NORMAL)
    .citizen(citizen)
    .createdAt(LocalDateTime.now())
    .build(); // ✅ Clear, readable, hard to make mistakes

// Builder implementation
public class Issue {
    private Long id;
    private String title;
    private String description;
    // ... other fields

    private Issue(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.description = builder.description;
        // ... copy other fields
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String title;
        private String description;
        // ... other fields

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        // ... other setters

        public Issue build() {
            // Validation
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("Title required");
            }
            return new Issue(this);
        }
    }
}
```

**Lombok shortcut**: `@Builder` annotation does this automatically!

```java
@Builder
public class Issue {
    private String title;
    private String description;
    // Lombok generates builder code
}

// Usage
Issue issue = Issue.builder()
    .title("Road pothole")
    .description("Large pothole")
    .build();
```

---

#### 9. Template Method Pattern (Behavioral)

**Problem**: Algorithm structure is same, but some steps differ.

**Example**: Issue validation (structure same, rules differ by role)

```java
// Abstract template
public abstract class IssueValidator {
    // Template method (defines algorithm structure)
    public final ValidationResult validate(Issue issue) {
        ValidationResult result = new ValidationResult();

        // Step 1: Common validation (all roles)
        validateCommonRules(issue, result);

        // Step 2: Role-specific validation (differs)
        validateRoleSpecificRules(issue, result);

        // Step 3: Business rules (common)
        validateBusinessRules(issue, result);

        return result;
    }

    // Common steps (implemented here)
    private void validateCommonRules(Issue issue, ValidationResult result) {
        if (issue.getTitle() == null || issue.getTitle().length() < 5) {
            result.addError("Title must be at least 5 characters");
        }
    }

    // Abstract step (subclasses implement)
    protected abstract void validateRoleSpecificRules(Issue issue, ValidationResult result);

    private void validateBusinessRules(Issue issue, ValidationResult result) {
        // Common business rules
    }
}

// Concrete implementation for Citizen
public class CitizenIssueValidator extends IssueValidator {
    @Override
    protected void validateRoleSpecificRules(Issue issue, ValidationResult result) {
        // Citizen-specific: Cannot set priority to HIGH
        if (issue.getPriority() == IssuePriority.HIGH) {
            result.addError("Citizens cannot create high-priority issues");
        }
    }
}

// Concrete implementation for Admin
public class AdminIssueValidator extends IssueValidator {
    @Override
    protected void validateRoleSpecificRules(Issue issue, ValidationResult result) {
        // Admin can do anything - no restrictions
    }
}

// Usage
IssueValidator validator = new CitizenIssueValidator();
ValidationResult result = validator.validate(issue);
```

---

#### 10. Dependency Injection (DI) Pattern (Structural)

**Problem**: Objects shouldn't create their dependencies (tight coupling).

**Your Project**: You're using this EVERYWHERE with Spring!

```java
// BAD: Hard-coded dependency
public class IssueService {
    private IssueRepository repository = new JpaIssueRepository(); // ❌ Tight coupling

    // Cannot test (always uses JPA), cannot swap implementation
}

// GOOD: Dependency Injection
public class IssueService {
    private final IssueRepository repository;

    // Constructor injection (Spring does this)
    public IssueService(IssueRepository repository) {
        this.repository = repository; // ✅ Injected from outside
    }
}

// Spring Boot configuration
@Configuration
public class AppConfig {
    @Bean
    public IssueRepository issueRepository() {
        return new JpaIssueRepository();
    }

    @Bean
    public IssueService issueService(IssueRepository repository) {
        return new IssueService(repository); // Spring injects
    }
}

// Testing - Easy to mock!
@Test
void testIssueService() {
    IssueRepository mockRepo = mock(IssueRepository.class);
    IssueService service = new IssueService(mockRepo); // Inject mock

    // Test with mock
}
```

**Interview Question**: "What are the types of dependency injection?"

**Answer**:
1. **Constructor Injection** (recommended) - Dependencies passed via constructor
2. **Setter Injection** - Dependencies set via setter methods
3. **Field Injection** - `@Autowired` on field (not recommended, hard to test)

---

## Design Pattern Cheat Sheet

| Pattern | Category | Problem It Solves | Your Project Usage |
|---------|----------|-------------------|-------------------|
| **Singleton** | Creational | Need exactly one instance | JWT Generator, Config |
| **Factory** | Creational | Create objects without specifying class | User creation by role |
| **Builder** | Creational | Complex object construction | Issue, User objects |
| **Repository** | Structural | Separate data access | IssueRepository, UserRepository |
| **Adapter** | Structural | Convert between interfaces | Domain ↔ JPA Entity mapping |
| **Decorator** | Structural | Add behavior without modifying | Logging, caching, transactions |
| **Strategy** | Behavioral | Swap algorithms at runtime | Issue assignment strategies |
| **Observer** | Behavioral | Notify multiple objects of changes | Status change notifications |
| **Template Method** | Behavioral | Define algorithm skeleton | Validation framework |
| **Dependency Injection** | Structural | Decouple object creation | Entire Spring Boot app |

---

# Part 2: Data Structures

## What Are Data Structures?

**Simple explanation**: Ways to organize and store data in memory for efficient access and modification.

Think of it like organizing your closet:
- **Array**: Shirts hung in order (fast to access by position, hard to insert)
- **Linked List**: Shirts connected by string (easy to insert, slow to find specific shirt)
- **Hash Map**: Shirts labeled with tags (very fast lookup by tag)

## Top 10 Data Structures for Interviews

### 1. Array / ArrayList ⭐⭐⭐

**When to use**: Store list of items, access by index

**Your Project**: List of issues

```java
List<Issue> issues = new ArrayList<>();
issues.add(issue1);
issues.add(issue2);
Issue first = issues.get(0); // O(1) - Fast access by index
```

**Time Complexity**:
- Access by index: O(1)
- Search: O(n)
- Insert at end: O(1) amortized
- Insert at beginning: O(n) (must shift all elements)

**Interview Question**: "ArrayList vs LinkedList?"

**Answer**: "ArrayList is faster for random access (O(1) vs O(n)) because it's backed by an array. LinkedList is faster for insertions/deletions in the middle (O(1) vs O(n)) because no shifting needed. For most use cases, ArrayList is better due to better cache locality."

---

### 2. Hash Map / Hash Table ⭐⭐⭐

**When to use**: Fast lookup by key (like a dictionary)

**Your Project**: Cache issues by ID, store user sessions

```java
Map<Long, Issue> issueCache = new HashMap<>();
issueCache.put(123L, issue); // O(1)
Issue cached = issueCache.get(123L); // O(1) - Fast!
```

**Time Complexity**:
- Get: O(1) average, O(n) worst case (hash collision)
- Put: O(1) average
- Remove: O(1) average

**Real example in your project**:
```java
// Cache user by email for fast authentication
Map<String, User> userCache = new HashMap<>();
userCache.put("john@example.com", user);

// Login - O(1) lookup!
User user = userCache.get(email);
if (user != null && user.getPassword().equals(hashedPassword)) {
    // Authenticated
}
```

**Interview Question**: "How does HashMap work internally?"

**Answer**: "HashMap uses an array of buckets. When you put(key, value), it calculates hash code of key to determine which bucket. If two keys hash to same bucket (collision), it stores them as a linked list (or tree if > 8 entries). Load factor 0.75 triggers resizing (doubles capacity) to maintain O(1) performance."

---

### 3. Stack ⭐⭐

**When to use**: Last In, First Out (LIFO) - undo functionality, parsing

**Your Project**: Issue timeline (most recent first)

```java
Stack<TimelineEntry> timeline = new Stack<>();
timeline.push(new TimelineEntry("Created", now));
timeline.push(new TimelineEntry("In Progress", now.plusHours(1)));
timeline.push(new TimelineEntry("Resolved", now.plusHours(2)));

TimelineEntry latest = timeline.pop(); // "Resolved" - most recent
```

**Operations**:
- Push: O(1)
- Pop: O(1)
- Peek: O(1)

**Real-world example**: Browser back button (stack of visited pages)

---

### 4. Queue ⭐⭐

**When to use**: First In, First Out (FIFO) - task processing, breadth-first search

**Your Project**: Issue processing queue (staff handles oldest first)

```java
Queue<Issue> pendingIssues = new LinkedList<>();
pendingIssues.offer(issue1); // Add to back
pendingIssues.offer(issue2);
pendingIssues.offer(issue3);

Issue next = pendingIssues.poll(); // Remove from front (issue1 - oldest)
```

**Real-world**: Print queue, customer service queue

---

### 5. Set (HashSet, TreeSet) ⭐⭐

**When to use**: Store unique items, no duplicates

**Your Project**: Assigned staff IDs (no duplicates)

```java
Set<Long> assignedStaffIds = new HashSet<>();
assignedStaffIds.add(101L);
assignedStaffIds.add(102L);
assignedStaffIds.add(101L); // Ignored - already exists

boolean hasStaff = assignedStaffIds.contains(101L); // O(1) - Fast!
```

**HashSet vs TreeSet**:
- **HashSet**: O(1) operations, no order
- **TreeSet**: O(log n) operations, sorted order

---

### 6. Tree (Binary Tree, BST) ⭐⭐

**When to use**: Hierarchical data, fast search in sorted data

**Example**: Organization structure, file system

```java
// Binary Search Tree
class TreeNode {
    int value;
    TreeNode left;
    TreeNode right;

    TreeNode(int value) {
        this.value = value;
    }
}

// Insert in BST - O(log n) average
void insert(TreeNode root, int value) {
    if (value < root.value) {
        if (root.left == null) root.left = new TreeNode(value);
        else insert(root.left, value);
    } else {
        if (root.right == null) root.right = new TreeNode(value);
        else insert(root.right, value);
    }
}
```

**Not directly used in your project**, but good to know for interviews.

---

### 7. Graph ⭐

**When to use**: Model relationships (social network, maps)

**Example**: User connections, road networks

```java
// Adjacency list representation
Map<User, List<User>> friendships = new HashMap<>();

void addFriendship(User user1, User user2) {
    friendships.computeIfAbsent(user1, k -> new ArrayList<>()).add(user2);
    friendships.computeIfAbsent(user2, k -> new ArrayList<>()).add(user1);
}
```

**Not in your MVP**, but could model issue dependencies later.

---

### 8. Priority Queue (Heap) ⭐⭐

**When to use**: Always need the min/max element (task scheduling)

**Your Project**: Prioritize high-priority issues

```java
// Min heap - smallest element first
PriorityQueue<Issue> issueQueue = new PriorityQueue<>(
    Comparator.comparing(Issue::getPriority).reversed() // HIGH priority first
);

issueQueue.offer(normalIssue);
issueQueue.offer(highPriorityIssue);

Issue next = issueQueue.poll(); // highPriorityIssue (highest priority)
```

**Operations**:
- Insert: O(log n)
- Get min/max: O(1)
- Remove min/max: O(log n)

---

### 9. Linked List ⭐

**When to use**: Frequent insertions/deletions

```java
class Node {
    Issue data;
    Node next;
}

// Doubly linked list
class DoublyNode {
    Issue data;
    DoublyNode prev;
    DoublyNode next;
}
```

**Java LinkedList** implements this internally.

---

### 10. Trie (Prefix Tree) ⭐

**When to use**: Autocomplete, spell checker, IP routing

**Example**: Issue search by title prefix

```java
class TrieNode {
    Map<Character, TrieNode> children = new HashMap<>();
    boolean isEndOfWord;
}

// Insert "road", "roadblock", "river"
// Search "roa" → suggests "road", "roadblock"
```

**Advanced topic** - not needed for MVP.

---

## Data Structure Selection Guide

| Use Case | Best Data Structure | Why |
|----------|-------------------|-----|
| Store list of issues | ArrayList | Fast random access |
| Cache by ID | HashMap | O(1) lookup |
| Recent timeline entries | Stack | LIFO access |
| Process issues in order | Queue | FIFO processing |
| Unique staff IDs | HashSet | No duplicates, O(1) contains |
| Prioritize by urgency | PriorityQueue | Always get highest priority |
| Sorted issues | TreeSet | Maintains sort order |
| Issue dependencies | Graph | Model relationships |

---

# Part 3: Algorithms

## What Are Algorithms?

**Simple explanation**: Step-by-step instructions to solve a problem.

Think of it like a recipe:
1. Find an issue in the list (Searching)
2. Arrange issues by date (Sorting)
3. Calculate shortest path (Graph algorithms)

## Big O Notation (Complexity Analysis)

**Most important concept for interviews!**

Big O describes how runtime/memory grows as input size increases.

```
O(1)        Constant    - Same time regardless of input size
O(log n)    Logarithmic - Halves input each step (Binary Search)
O(n)        Linear      - Loop through all elements once
O(n log n)  Linearithmic- Efficient sorting (Merge Sort, Quick Sort)
O(n²)       Quadratic   - Nested loops (Bubble Sort)
O(2ⁿ)       Exponential - Very slow (recursive Fibonacci)
```

**Visual ranking** (faster → slower):
```
O(1) < O(log n) < O(n) < O(n log n) < O(n²) < O(2ⁿ)
```

---

## Top 10 Algorithms for Interviews

### 1. Binary Search ⭐⭐⭐

**Problem**: Find element in **sorted** array

**Time**: O(log n)

```java
// Find issue by ID in sorted array
public int binarySearch(Issue[] issues, long targetId) {
    int left = 0;
    int right = issues.length - 1;

    while (left <= right) {
        int mid = left + (right - left) / 2;
        long midId = issues[mid].getId();

        if (midId == targetId) {
            return mid; // Found!
        } else if (midId < targetId) {
            left = mid + 1; // Search right half
        } else {
            right = mid - 1; // Search left half
        }
    }

    return -1; // Not found
}
```

**Example**:
```
Array: [1, 3, 5, 7, 9, 11, 13, 15]
Search for 7:

Step 1: mid = 4 → issues[4] = 9 (too big, search left)
Step 2: mid = 1 → issues[1] = 3 (too small, search right)
Step 3: mid = 3 → issues[3] = 7 (found!)

Only 3 comparisons instead of 4 (linear search would take 4)
```

**Interview tip**: Always mention "only works on sorted data"

---

### 2. Sorting Algorithms ⭐⭐⭐

#### Quick Sort (Most common, Java default)
**Time**: O(n log n) average, O(n²) worst
```java
Arrays.sort(issues); // Uses Quick Sort for primitives
Collections.sort(issueList); // Uses Merge Sort for objects
```

#### Merge Sort (Stable, predictable)
**Time**: O(n log n) guaranteed

#### Bubble Sort (Simple, slow)
**Time**: O(n²)
```java
// Don't use in production, but know for interviews
void bubbleSort(int[] arr) {
    for (int i = 0; i < arr.length - 1; i++) {
        for (int j = 0; j < arr.length - i - 1; j++) {
            if (arr[j] > arr[j + 1]) {
                // Swap
                int temp = arr[j];
                arr[j] = arr[j + 1];
                arr[j + 1] = temp;
            }
        }
    }
}
```

**Interview Question**: "Which sorting algorithm would you use for large datasets?"

**Answer**: "Merge Sort if stability matters (equal elements maintain order). Quick Sort if stability doesn't matter (faster in practice). Both are O(n log n), but Quick Sort has better cache locality."

---

### 3. Two Pointer Technique ⭐⭐

**Problem**: Find pair of issues with specific sum

```java
// Find two issues with IDs that sum to target
public int[] twoSum(int[] ids, int target) {
    Arrays.sort(ids); // O(n log n)
    int left = 0;
    int right = ids.length - 1;

    while (left < right) {
        int sum = ids[left] + ids[right];
        if (sum == target) {
            return new int[]{ids[left], ids[right]};
        } else if (sum < target) {
            left++; // Need bigger sum
        } else {
            right--; // Need smaller sum
        }
    }

    return new int[]{-1, -1}; // Not found
}
```

**Time**: O(n log n) for sort + O(n) for search = O(n log n)

---

### 4. Sliding Window ⭐⭐

**Problem**: Find max/min in subarrays (e.g., issues per day)

```java
// Find maximum number of issues in any 7-day window
public int maxIssuesInWeek(int[] issuesPerDay) {
    int maxIssues = 0;
    int windowSum = 0;
    int windowSize = 7;

    // First window
    for (int i = 0; i < windowSize; i++) {
        windowSum += issuesPerDay[i];
    }
    maxIssues = windowSum;

    // Slide window
    for (int i = windowSize; i < issuesPerDay.length; i++) {
        windowSum += issuesPerDay[i] - issuesPerDay[i - windowSize];
        maxIssues = Math.max(maxIssues, windowSum);
    }

    return maxIssues;
}
```

**Time**: O(n) - Much faster than recalculating sum for each window (O(n × k))

---

### 5. Depth-First Search (DFS) ⭐

**Problem**: Traverse tree/graph (e.g., issue dependencies)

```java
void dfs(TreeNode node) {
    if (node == null) return;

    // Process current node
    System.out.println(node.value);

    // Recurse on children
    dfs(node.left);
    dfs(node.right);
}
```

**Time**: O(n) - Visit each node once

---

### 6. Breadth-First Search (BFS) ⭐

**Problem**: Find shortest path, level-order traversal

```java
void bfs(TreeNode root) {
    Queue<TreeNode> queue = new LinkedList<>();
    queue.offer(root);

    while (!queue.isEmpty()) {
        TreeNode node = queue.poll();
        System.out.println(node.value); // Process

        if (node.left != null) queue.offer(node.left);
        if (node.right != null) queue.offer(node.right);
    }
}
```

**Time**: O(n)

---

### 7. Dynamic Programming (DP) ⭐

**Problem**: Optimization problems (Fibonacci, knapsack)

```java
// Fibonacci with memoization (DP)
Map<Integer, Long> memo = new HashMap<>();

long fibonacci(int n) {
    if (n <= 1) return n;
    if (memo.containsKey(n)) return memo.get(n);

    long result = fibonacci(n - 1) + fibonacci(n - 2);
    memo.put(n, result);
    return result;
}
```

**Without DP**: O(2ⁿ) - exponential, very slow
**With DP**: O(n) - linear, fast

---

### 8. Hash Table Algorithms ⭐⭐

**Problem**: Find duplicates, count occurrences

```java
// Count issue occurrences by category
public Map<IssueCategory, Integer> countByCategory(List<Issue> issues) {
    Map<IssueCategory, Integer> counts = new HashMap<>();

    for (Issue issue : issues) {
        counts.put(
            issue.getCategory(),
            counts.getOrDefault(issue.getCategory(), 0) + 1
        );
    }

    return counts;
}
```

**Time**: O(n)

---

### 9. Recursion ⭐⭐

**Problem**: Solve problem by breaking into smaller subproblems

```java
// Calculate factorial
int factorial(int n) {
    if (n <= 1) return 1; // Base case
    return n * factorial(n - 1); // Recursive case
}
```

---

### 10. Greedy Algorithms ⭐

**Problem**: Make locally optimal choice at each step

```java
// Assign issues to staff (greedy: assign to least loaded)
public void assignIssues(List<Issue> issues, List<User> staff) {
    PriorityQueue<User> staffQueue = new PriorityQueue<>(
        Comparator.comparingInt(User::getAssignedIssueCount)
    );
    staffQueue.addAll(staff);

    for (Issue issue : issues) {
        User leastLoaded = staffQueue.poll();
        issue.assignToStaff(leastLoaded);
        staffQueue.offer(leastLoaded); // Re-insert with updated count
    }
}
```

---

## Algorithm Complexity Cheat Sheet

| Algorithm | Best Case | Average Case | Worst Case | Space |
|-----------|-----------|--------------|------------|-------|
| Binary Search | O(1) | O(log n) | O(log n) | O(1) |
| Quick Sort | O(n log n) | O(n log n) | O(n²) | O(log n) |
| Merge Sort | O(n log n) | O(n log n) | O(n log n) | O(n) |
| Bubble Sort | O(n) | O(n²) | O(n²) | O(1) |
| Hash Table Get | O(1) | O(1) | O(n) | O(n) |
| DFS/BFS | O(V+E) | O(V+E) | O(V+E) | O(V) |

---

# Interview Preparation Priority

## For Mid-Level Full-Stack Position (6-8 weeks)

### Week 1-2: Design Patterns (High Priority)
- ✅ Study 5 must-know patterns (Singleton, Factory, Repository, Strategy, Observer)
- ✅ Implement them in your project
- ✅ Be able to explain with examples

### Week 3-4: Data Structures (Medium Priority)
- ✅ Master ArrayList, HashMap, HashSet (used daily)
- 🟡 Understand Stack, Queue, PriorityQueue
- 🟡 Know Tree, Graph concepts (less common but asked)

### Week 5-6: Algorithms (Medium Priority)
- ✅ Binary Search, Sorting (very common)
- ✅ Two Pointer, Sliding Window
- 🟡 DFS, BFS (good to know)
- ⏸️ DP, Advanced algorithms (not critical for mid-level)

### Week 7-8: Practice
- LeetCode Easy: 20 problems
- LeetCode Medium: 10 problems
- Focus on: Arrays, Hash Tables, Strings, Trees

---

# German Interview Tips

1. **Explain Trade-offs**: Don't just say "I used HashMap". Say "I used HashMap because O(1) lookup was critical, and I could afford O(n) space complexity."

2. **Connect to Project**: When asked about patterns, reference YOUR code: "In my Issue Tracker, I used Repository pattern to separate business logic from JPA."

3. **Know Big O**: German engineers care about performance. Always mention time/space complexity.

4. **Write Clean Code**: German culture values code quality. Use meaningful variable names, proper indentation.

5. **Testing Mindset**: When explaining patterns, mention how they improve testability.

---

# Sample Interview Questions & Answers

**Q: "Explain a design pattern you used in your project."**

**A**: "I used the Strategy pattern for issue assignment. The problem was: we needed different assignment algorithms - round-robin for normal days, priority-based during emergencies. Instead of if/else chains, I created an IssueAssignmentStrategy interface with multiple implementations. This made the code testable (I can test each strategy independently) and extensible (easy to add new strategies without changing existing code). The trade-off is more classes, but it follows Open/Closed Principle - open for extension, closed for modification."

---

**Q: "What's the difference between HashMap and TreeMap?"**

**A**: "HashMap uses hash table internally, giving O(1) average-case operations but no ordering. TreeMap uses Red-Black Tree, giving O(log n) operations but maintains sorted order by keys. I'd use HashMap when I just need fast lookup, like caching users by ID. I'd use TreeMap when I need sorted access, like displaying issues chronologically by ID."

---

**Q: "How would you find duplicates in an array?"**

**A**: "I'd use a HashSet. Iterate through the array, for each element: if HashSet contains it, it's a duplicate; otherwise, add it to HashSet. Time complexity O(n), space complexity O(n). Alternative: sort first (O(n log n)), then check adjacent elements - saves space (O(1)) but slower."

```java
public List<Integer> findDuplicates(int[] arr) {
    Set<Integer> seen = new HashSet<>();
    List<Integer> duplicates = new ArrayList<>();

    for (int num : arr) {
        if (!seen.add(num)) { // add() returns false if already present
            duplicates.add(num);
        }
    }

    return duplicates;
}
```

---

# Next Steps

You now have a comprehensive guide. Would you like to:

1. **Deep dive into specific patterns** (pick 2-3 to implement in your project)
2. **Practice algorithm problems** (I can give you problems matching your level)
3. **Move to project setup** (start coding with these patterns in mind)
4. **Mock interview** (I ask typical German interview questions)

Let me know what helps you most! 🚀
