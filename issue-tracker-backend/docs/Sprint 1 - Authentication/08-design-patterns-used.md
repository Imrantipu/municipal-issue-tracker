# Design Patterns Used in Sprint 1 - Authentication

This document catalogs all 12 design patterns implemented in the authentication system, with code examples and explanations.

---

## Pattern Categories

**Creational Patterns:** Builder, Singleton
**Structural Patterns:** Adapter, Facade, Proxy, DTO
**Behavioral Patterns:** Strategy, Command, Template Method, Repository, Filter Chain
**Dependency Management:** Dependency Injection

---

## 1. Repository Pattern ⭐⭐⭐⭐⭐

### What It Is
Abstracts data access logic, providing a collection-like interface for accessing domain objects.

### Location
```
domain/port/out/UserRepository.java
infrastructure/adapter/out/persistence/JpaUserRepositoryAdapter.java
```

### Code Example
```java
// Domain defines interface (what it needs)
package com.issuetracker.domain.port.out;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}

// Infrastructure implements (how it's done)
package com.issuetracker.infrastructure.adapter.out.persistence;

@Component
public class JpaUserRepositoryAdapter implements UserRepository {
    private final SpringDataUserRepository springDataRepository;

    @Override
    public User save(User user) {
        UserEntity entity = toEntity(user);
        UserEntity saved = springDataRepository.save(entity);
        return toDomain(saved);
    }
}
```

### Why We Used It
- ✅ **Abstraction:** Domain doesn't know about JPA or SQL
- ✅ **Testability:** Can mock UserRepository in tests
- ✅ **Flexibility:** Can swap PostgreSQL → MongoDB by changing adapter only
- ✅ **Business Language:** Methods speak domain language (`findByEmail` not `selectByEmailColumn`)

### Benefits
- Domain layer stays pure (no framework dependencies)
- Easy to test without database
- Can implement caching, logging, etc. without changing domain

### Interview Tip
> "I used Repository pattern to abstract data access. Domain defines UserRepository interface, infrastructure implements it with JpaUserRepositoryAdapter. This allows swapping databases without touching business logic."

---

## 2. Adapter Pattern ⭐⭐⭐⭐⭐

### What It Is
Converts one interface to another, making incompatible interfaces work together.

### Location
```
infrastructure/adapter/out/persistence/JpaUserRepositoryAdapter.java
```

### Code Example
```java
@Component
@RequiredArgsConstructor
public class JpaUserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository springDataRepository;

    // Adapter converts between two representations:
    // User (domain) ↔ UserEntity (JPA)

    @Override
    public User save(User user) {
        // Domain → JPA
        UserEntity entity = toEntity(user);

        // Call JPA repository
        UserEntity saved = springDataRepository.save(entity);

        // JPA → Domain
        return toDomain(saved);
    }

    // Adapter method: Domain → JPA
    private UserEntity toEntity(User user) {
        return UserEntity.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .password(user.getPassword())
            .role(user.getRole())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .deletedAt(user.getDeletedAt())
            .build();
    }

    // Adapter method: JPA → Domain
    private User toDomain(UserEntity entity) {
        return User.builder()
            .id(entity.getId())
            .name(entity.getName())
            .email(entity.getEmail())
            .password(entity.getPassword())
            .role(entity.getRole())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .deletedAt(entity.getDeletedAt())
            .build();
    }
}
```

### Why We Used It
- ✅ **Separation:** User (domain) has no JPA annotations
- ✅ **Clean Architecture:** Domain independent of infrastructure
- ✅ **Hexagonal Architecture:** Ports (interfaces) and Adapters pattern

### Real-World Analogy
- User (domain) = European plug (2 pins)
- UserEntity (JPA) = American socket (3 pins)
- JpaUserRepositoryAdapter = Travel adapter that makes them work together

### Interview Tip
> "JpaUserRepositoryAdapter is an Adapter pattern. It converts between User domain object (no JPA) and UserEntity (with @Entity). This keeps domain layer framework-independent."

---

## 3. Strategy Pattern ⭐⭐⭐⭐

### What It Is
Defines a family of algorithms, encapsulates each one, and makes them interchangeable.

### Location
```
application/service/UserService.java (uses PasswordEncoder)
```

### Code Example
```java
// Strategy interface (from Spring Security)
public interface PasswordEncoder {
    String encode(CharSequence rawPassword);
    boolean matches(CharSequence rawPassword, String encodedPassword);
}

// Concrete strategies
class BCryptPasswordEncoder implements PasswordEncoder { ... }
class Argon2PasswordEncoder implements PasswordEncoder { ... }
class SCryptPasswordEncoder implements PasswordEncoder { ... }

// Client uses strategy
@Service
public class UserService {
    private final PasswordEncoder passwordEncoder;  // Strategy!

    public User registerUser(RegisterUserCommand command) {
        // Strategy in action: UserService doesn't know which algorithm
        String hashed = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashed);
        return userRepository.save(user);
    }
}

// Configuration (can swap strategies)
@Configuration
public class SecurityBeans {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // Can change to Argon2
    }
}
```

### Why We Used It
- ✅ **Flexibility:** Can swap BCrypt → Argon2 without changing UserService
- ✅ **Testability:** Can mock PasswordEncoder in tests
- ✅ **Encapsulation:** UserService doesn't know HOW password is hashed

### Other Strategies in Code
```java
// JwtTokenProvider is also a strategy
private final JwtTokenProvider jwtTokenProvider;

// Could swap with OAuthTokenProvider, Auth0Provider, etc.
```

### Interview Tip
> "PasswordEncoder is Strategy pattern. UserService depends on PasswordEncoder interface, not BCryptPasswordEncoder. I can swap BCrypt → Argon2 by changing one line in SecurityBeans configuration."

---

## 4. Builder Pattern ⭐⭐⭐⭐⭐

### What It Is
Constructs complex objects step by step, providing a fluent interface.

### Location
```
domain/model/User.java
```

### Code Example
```java
@Data
@Builder  // Lombok generates builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String name;
    private String email;
    private String password;
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}

// Usage
User user = User.builder()
    .name("John Doe")
    .email("john@example.com")
    .password("hashed-password")
    .role(Role.CITIZEN)
    .createdAt(LocalDateTime.now())
    .updatedAt(LocalDateTime.now())
    .build();
```

### Lombok Generated Code
```java
// Lombok generates this at compile time:
public static class UserBuilder {
    private Long id;
    private String name;
    private String email;
    // ...

    public UserBuilder name(String name) {
        this.name = name;
        return this;
    }

    public UserBuilder email(String email) {
        this.email = email;
        return this;
    }

    public User build() {
        return new User(id, name, email, password, role, ...);
    }
}
```

### Why We Used It
- ✅ **Readability:** `.name("John")` is clearer than positional parameters
- ✅ **Optional Fields:** Can omit `deletedAt`, `id` (null by default)
- ✅ **Immutability:** Build once, then object is complete
- ✅ **No Telescoping Constructors:** Avoid `User(name)`, `User(name, email)`, etc.

### Alternative Without Builder
```java
// ❌ BAD: Hard to read, easy to mix up parameters
User user = new User(
    null,                    // id
    "John Doe",              // name
    "john@example.com",      // email
    "hashed",                // password
    Role.CITIZEN,            // role
    LocalDateTime.now(),     // createdAt
    LocalDateTime.now(),     // updatedAt
    null                     // deletedAt
);
```

### Interview Tip
> "I used Builder pattern for User because it has 8 fields. Builder provides readable, fluent API with optional fields. Lombok @Builder annotation generates all boilerplate at compile time."

---

## 5. Dependency Injection Pattern ⭐⭐⭐⭐⭐

### What It Is
Dependencies are injected by framework, not created by the class itself.

### Location
```
application/service/UserService.java
infrastructure/adapter/in/web/AuthController.java
```

### Code Example
```java
@Service
@RequiredArgsConstructor  // Lombok generates constructor
public class UserService implements RegisterUserUseCase, LoginUserUseCase {

    // Dependencies declared as final fields
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // Lombok generates constructor:
    // public UserService(UserRepository repo,
    //                    PasswordEncoder encoder,
    //                    JwtTokenProvider jwt) {
    //     this.userRepository = repo;
    //     this.passwordEncoder = encoder;
    //     this.jwtTokenProvider = jwt;
    // }

    // UserService doesn't create dependencies!
    // Spring injects them via constructor
}
```

### Why Constructor Injection?
```java
// ✅ GOOD: Constructor injection (via @RequiredArgsConstructor)
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;  // Immutable!
}

// ❌ BAD: Field injection
public class UserService {
    @Autowired  // Don't use!
    private UserRepository userRepository;  // Mutable, hard to test
}
```

### Benefits
- ✅ **Testability:** Easy to pass mock dependencies
- ✅ **Immutability:** Fields are `final` (thread-safe)
- ✅ **Explicitness:** Dependencies visible in constructor
- ✅ **Compile-time Safety:** Won't compile if dependency missing

### Testing with DI
```java
@Test
void testRegisterUser() {
    // Easy to mock dependencies
    UserRepository mockRepo = mock(UserRepository.class);
    PasswordEncoder mockEncoder = mock(PasswordEncoder.class);
    JwtTokenProvider mockJwt = mock(JwtTokenProvider.class);

    // Inject mocks via constructor
    UserService service = new UserService(mockRepo, mockEncoder, mockJwt);

    // Test with full control over dependencies
}
```

### Interview Tip
> "I use constructor injection via @RequiredArgsConstructor. All dependencies are final (immutable), making the class thread-safe. In tests, I can easily inject mocks via constructor."

---

## 6. Command Pattern ⭐⭐⭐⭐

### What It Is
Encapsulates a request as an object, allowing parameterization and queuing.

### Location
```
domain/port/in/RegisterUserUseCase.java
domain/port/in/LoginUserUseCase.java
```

### Code Example
```java
public interface RegisterUserUseCase {
    User registerUser(RegisterUserCommand command);

    // Command object encapsulates request
    record RegisterUserCommand(
        String name,
        String email,
        String password,
        String role
    ) {}
}

// Usage
RegisterUserCommand command = new RegisterUserCommand(
    "John Doe",
    "john@example.com",
    "password123",
    "CITIZEN"
);

User user = registerUserUseCase.registerUser(command);
```

### Why Record?
```java
// Java 14+ Record
record RegisterUserCommand(String name, String email, String password, String role) {}

// Equivalent class (old way)
public final class RegisterUserCommand {
    private final String name;
    private final String email;
    private final String password;
    private final String role;

    public RegisterUserCommand(String name, String email, String password, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // + getters, equals, hashCode, toString
}
```

### Benefits
- ✅ **Single Parameter:** Method takes 1 object instead of 4 parameters
- ✅ **Extensibility:** Add new field without breaking method signature
- ✅ **Immutability:** Records are immutable (thread-safe)
- ✅ **Validation:** Can add validation to command object

### Alternative Without Command
```java
// ❌ BAD: Too many parameters
User registerUser(String name, String email, String password, String role);

// ✅ GOOD: One command object
User registerUser(RegisterUserCommand command);
```

### Interview Tip
> "I use Command pattern to encapsulate registration request. RegisterUserCommand is a Java record (immutable DTO). If I need to add a field like 'phoneNumber', I only change the command object, not method signatures."

---

## 7. Singleton Pattern ⭐⭐⭐

### What It Is
Ensures a class has only one instance and provides global access point.

### Location
```
All Spring beans (@Service, @Component, @RestController, @Configuration)
```

### Code Example
```java
// All Spring beans are singletons by default

@Service  // Singleton scope
public class UserService { ... }

@Component  // Singleton scope
public class JwtTokenProvider { ... }

@RestController  // Singleton scope
public class AuthController { ... }

@Configuration  // Singleton scope
public class SecurityConfig { ... }
```

### Spring Singleton Scope
```java
// Spring creates ONE instance per application context
ApplicationContext context = ...;

UserService service1 = context.getBean(UserService.class);
UserService service2 = context.getBean(UserService.class);

service1 == service2  // true (same instance)
```

### Why Spring Uses Singleton
- ✅ **Performance:** Create once, reuse many times
- ✅ **Memory Efficient:** One object for entire application
- ✅ **Stateless Services:** UserService has no mutable state
- ✅ **Thread-Safe:** If service is stateless

### Thread Safety
```java
// ✅ SAFE: No mutable state
@Service
public class UserService {
    private final UserRepository userRepository;  // Immutable reference

    public User registerUser(...) {
        User user = ...;  // Local variable (thread-safe)
        return userRepository.save(user);
    }
}

// ❌ UNSAFE: Mutable state in singleton
@Service
public class UnsafeService {
    private User currentUser;  // Shared between threads! Danger!
}
```

### Interview Tip
> "Spring beans are singletons by default. UserService is thread-safe because it's stateless - all dependencies are final, and methods use local variables only. No mutable instance state."

---

## 8. DTO Pattern (Data Transfer Object) ⭐⭐⭐⭐

### What It Is
Objects that carry data between processes, typically across layer boundaries.

### Location
```
infrastructure/adapter/in/web/AuthController.java
```

### Code Example
```java
@RestController
public class AuthController {

    // Request DTO (client → server)
    record RegisterRequest(
        String name,
        String email,
        String password,
        String role
    ) {}

    // Response DTO (server → client)
    record UserResponse(
        Long id,
        String name,
        String email,
        String role,
        LocalDateTime createdAt
    ) {}

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        // DTO → Command
        RegisterUserCommand command = new RegisterUserCommand(...);

        // Domain operation
        User user = registerUserUseCase.registerUser(command);

        // Domain → DTO (hide sensitive data)
        UserResponse response = new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole().name(),
            user.getCreatedAt()
            // NOTE: password, deletedAt NOT included!
        );

        return ResponseEntity.ok(response);
    }
}
```

### Data Flow
```
Client JSON
   ↓
RegisterRequest (DTO)  ← Only has fields from client
   ↓
RegisterUserCommand
   ↓
User (Domain)  ← Has all fields (password, deletedAt, etc.)
   ↓
UserEntity (JPA)
   ↓
Database

Database
   ↓
UserEntity (JPA)
   ↓
User (Domain)
   ↓
UserResponse (DTO)  ← Filtered fields (no password!)
   ↓
Client JSON
```

### Why We Used It
- ✅ **Security:** UserResponse doesn't include password or deletedAt
- ✅ **Encapsulation:** Domain User can change without breaking API
- ✅ **API Stability:** Client doesn't see internal domain structure
- ✅ **Validation:** Can validate DTO separately from domain

### Interview Tip
> "I use DTO pattern to decouple API from domain. UserResponse excludes password and deletedAt fields. If domain User changes, API remains stable because DTOs act as a contract with clients."

---

## 9. Facade Pattern ⭐⭐⭐

### What It Is
Provides simplified interface to complex subsystem.

### Location
```
application/service/UserService.java
```

### Code Example
```java
@Service
@Transactional
public class UserService {

    // Facade coordinates multiple subsystems
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // Simplified interface for client
    public User registerUser(RegisterUserCommand command) {
        // Coordinates 5 operations:

        // 1. Check uniqueness (repository)
        if (userRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyExistsException(command.email());
        }

        // 2. Create domain object
        User user = User.builder()...build();

        // 3. Validate (domain)
        user.validate();

        // 4. Hash password (password encoder)
        String hashed = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashed);

        // 5. Save (repository)
        return userRepository.save(user);
    }
}
```

### Without Facade
```java
// ❌ BAD: Controller knows too much
@RestController
public class AuthController {
    public UserResponse register(...) {
        // All this logic in controller!
        if (userRepository.existsByEmail(email)) throw ...;
        User user = new User(...);
        user.validate();
        String hashed = passwordEncoder.encode(password);
        user.setPassword(hashed);
        userRepository.save(user);
        // Controller is doing too much!
    }
}
```

### Why We Used It
- ✅ **Simplicity:** AuthController just calls one method
- ✅ **Orchestration:** UserService coordinates domain + infrastructure
- ✅ **Encapsulation:** Internal complexity hidden from client
- ✅ **Testability:** Can test orchestration logic independently

### Interview Tip
> "UserService is a Facade pattern. It provides simple interface (registerUser) that coordinates complex operations: validation, hashing, persistence. Controller doesn't know implementation details."

---

## 10. Filter Chain Pattern ⭐⭐⭐⭐

### What It Is
Processes request through chain of filters, each can modify or short-circuit.

### Location
```
infrastructure/security/JwtAuthenticationFilter.java
```

### Code Example
```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Extract JWT token
        String jwt = getJwtFromRequest(request);

        // 2. Validate and set authentication
        if (jwt != null && jwtTokenProvider.validateToken(jwt)) {
            Long userId = jwtTokenProvider.getUserIdFromToken(jwt);
            String email = jwtTokenProvider.getEmailFromToken(jwt);
            String role = jwtTokenProvider.getRoleFromToken(jwt);

            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    email, null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 3. Pass to next filter in chain
        filterChain.doFilter(request, response);
    }
}
```

### Spring Security Filter Chain
```
Request from Client
   ↓
CorsFilter (CORS headers)
   ↓
CsrfFilter (disabled for JWT)
   ↓
JwtAuthenticationFilter (your custom filter)
   ├─ Extract JWT
   ├─ Validate token
   └─ Set authentication
   ↓
FilterSecurityInterceptor (authorization check)
   ├─ Check if endpoint requires auth
   └─ Check if user has permission
   ↓
DispatcherServlet
   ↓
Controller
```

### Why We Used It
- ✅ **Separation of Concerns:** Each filter has one job
- ✅ **Extensibility:** Can add/remove filters easily
- ✅ **Order Matters:** JWT filter runs before authorization
- ✅ **Short-Circuit:** Filter can return 401 without calling controller

### Interview Tip
> "JwtAuthenticationFilter is part of Filter Chain pattern. It validates JWT and sets authentication before request reaches controller. Spring Security processes request through multiple filters in sequence."

---

## 11. Template Method Pattern ⭐⭐⭐

### What It Is
Defines skeleton of algorithm, letting subclasses override specific steps.

### Location
```
infrastructure/adapter/out/persistence/UserEntity.java
```

### Code Example
```java
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    // ... other fields

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Template Method hooks
    @PrePersist  // Called before INSERT
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate  // Called before UPDATE
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

### JPA Lifecycle (Template)
```
JPA Template Algorithm:

1. Validate entity
2. @PrePersist hook      ← You customize this
3. Generate SQL INSERT
4. Execute SQL
5. @PostPersist hook     ← You can customize this
6. Return entity
```

### Why JPA Uses It
- ✅ **Extensibility:** Add custom logic at specific points
- ✅ **Framework Control:** JPA controls overall lifecycle
- ✅ **Consistency:** Same lifecycle for all entities
- ✅ **Automatic Timestamps:** Timestamps set automatically

### Other Template Methods
```java
// Spring Boot also uses Template Method
@Override
protected void doFilterInternal(...) {
    // Your custom filter logic
    filterChain.doFilter(request, response);
}
```

### Interview Tip
> "@PrePersist and @PreUpdate are Template Method pattern. JPA defines lifecycle algorithm, I customize specific steps (setting timestamps). JPA controls when hooks are called."

---

## 12. Proxy Pattern ⭐⭐⭐

### What It Is
Provides surrogate/placeholder to control access to an object.

### Location
```
application/service/UserService.java (@Transactional)
```

### Code Example
```java
@Service
@Transactional  // Spring creates proxy for transaction management
public class UserService {

    public User registerUser(RegisterUserCommand command) {
        // Your business logic here
        // ...
        return userRepository.save(user);
    }
}
```

### What Spring Does (Behind the Scenes)
```java
// Spring creates proxy that wraps your UserService

public class UserServiceProxy extends UserService {

    private final UserService target;  // Your actual service
    private final PlatformTransactionManager txManager;

    @Override
    public User registerUser(RegisterUserCommand command) {
        TransactionStatus tx = null;
        try {
            // 1. BEGIN TRANSACTION
            tx = txManager.getTransaction(...);

            // 2. Call actual method
            User result = target.registerUser(command);

            // 3. COMMIT TRANSACTION
            txManager.commit(tx);

            return result;
        } catch (Exception e) {
            // 4. ROLLBACK on exception
            if (tx != null) {
                txManager.rollback(tx);
            }
            throw e;
        }
    }
}
```

### Proxy in Action
```
Controller calls: userService.registerUser()
                     ↓
                Spring Proxy
                ├─ Begin Transaction
                ├─ Call Real UserService
                │    ├─ Validate
                │    ├─ Hash password
                │    └─ Save to DB
                ├─ Commit Transaction
                └─ Return result
```

### Why Spring Uses Proxy
- ✅ **AOP (Aspect-Oriented Programming):** Add behavior without modifying code
- ✅ **Transaction Management:** Automatic commit/rollback
- ✅ **Separation:** Business logic separate from transaction logic
- ✅ **Declarative:** Just add @Transactional annotation

### Other Proxies in Spring
```java
// Spring Security also uses proxies
@PreAuthorize("hasRole('ADMIN')")  // Proxy checks authorization
public void deleteUser(Long id) { ... }

// Spring Cache uses proxies
@Cacheable("users")  // Proxy checks cache before calling method
public User findById(Long id) { ... }
```

### Interview Tip
> "@Transactional uses Proxy pattern. Spring wraps UserService in proxy that begins transaction before method call and commits/rollbacks after. My code focuses on business logic only."

---

## Pattern Combinations

### How Patterns Work Together

#### Example: User Registration Flow
```
1. Command Pattern
   RegisterUserCommand command = new RegisterUserCommand(...)

2. Dependency Injection
   Controller → RegisterUserUseCase (interface)

3. Facade Pattern
   UserService.registerUser(command) coordinates:

4. Strategy Pattern
   passwordEncoder.encode(password)

5. Repository Pattern
   userRepository.save(user)

6. Adapter Pattern
   JpaUserRepositoryAdapter converts User → UserEntity

7. Proxy Pattern
   @Transactional wraps method in transaction

8. DTO Pattern
   User (domain) → UserResponse (DTO) → Client
```

---

## Pattern Decisions: Why We Chose Each

| Pattern | Alternative | Why We Chose Pattern |
|---------|-------------|----------------------|
| Repository | DAO | Domain-centric, speaks business language |
| Adapter | Direct JPA | Keep domain framework-free |
| Strategy | Switch statements | Open for extension, closed for modification |
| Builder | Constructor | 8+ parameters, optional fields |
| DI | new keyword | Testability, loose coupling |
| Command | Multiple params | Extensibility, single parameter |
| Singleton | New instance | Performance, stateless services |
| DTO | Expose domain | Security, encapsulation |
| Facade | Controller logic | Simplicity, orchestration |
| Filter Chain | Single filter | Separation of concerns, extensibility |
| Template Method | Manual hooks | Framework consistency |
| Proxy | Manual tx code | Declarative, separation of concerns |

---

## Interview Preparation

### Pattern Questions You'll Get

**Q1: "What design patterns did you use?"**
**Answer:** "I used 12 patterns: Repository, Adapter, Strategy, Builder, Dependency Injection, Command, Singleton, DTO, Facade, Filter Chain, Template Method, and Proxy. The core architectural patterns are Repository and Adapter, which enable Hexagonal Architecture."

**Q2: "Explain Repository vs DAO"**
**Answer:** "Repository speaks domain language (findByEmail), DAO speaks database language (selectByEmailColumn). Repository is collection-like, DAO is table-centric. I use Repository for domain-driven design."

**Q3: "How does Adapter pattern help you?"**
**Answer:** "JpaUserRepositoryAdapter converts between User (domain, no JPA) and UserEntity (JPA annotations). This keeps domain layer framework-independent. I can swap PostgreSQL → MongoDB by changing adapter only."

**Q4: "Why Builder pattern for User?"**
**Answer:** "User has 8 fields. Builder provides readable fluent API (.name("John")), supports optional fields, and Lombok generates all boilerplate."

**Q5: "What's the benefit of Dependency Injection?"**
**Answer:** "Constructor injection via @RequiredArgsConstructor makes dependencies explicit, fields immutable (thread-safe), and testing easy (inject mocks via constructor)."

---

## Code Quality Metrics

### Pattern Usage Statistics

| Layer | Patterns Used | Files | Lines of Code |
|-------|---------------|-------|---------------|
| Domain | Repository, Command, Builder | 5 files | ~200 LOC |
| Application | DI, Strategy, Facade | 1 file | ~150 LOC |
| Infrastructure | Adapter, DTO, Singleton, Filter Chain, Template Method, Proxy | 13 files | ~800 LOC |

### Coverage by Pattern Type

- **Creational:** 2 patterns (Builder, Singleton)
- **Structural:** 4 patterns (Adapter, Facade, Proxy, DTO)
- **Behavioral:** 5 patterns (Strategy, Command, Template Method, Repository, Filter Chain)
- **Other:** 1 pattern (Dependency Injection)

---

## Learning Resources

### Books to Read
1. **"Head First Design Patterns"** - Visual learning (easiest)
2. **"Design Patterns" (GoF)** - The Bible (classic)
3. **"Clean Architecture"** - Understanding your architecture
4. **"Effective Java"** - Best practices

### Practice Exercises
1. Identify patterns in Spring Boot framework source code
2. Refactor code to use patterns
3. Implement new patterns (Observer, Decorator, etc.)

---

## Next Steps

### Patterns to Add in Sprint 2 (Issue Management)

1. **Observer Pattern**
   - Notify users when issue status changes
   - Event-driven architecture

2. **Specification Pattern**
   - Complex issue filtering (status + category + date)
   - Composable queries

3. **State Pattern**
   - Issue status transitions (PENDING → IN_PROGRESS → RESOLVED)
   - Validate state changes

4. **Factory Pattern**
   - Create different issue types
   - Create users with different roles

---

## Conclusion

Sprint 1 authentication system demonstrates **12 design patterns** working together to create:
- ✅ Clean, maintainable code
- ✅ Testable architecture
- ✅ Flexible, extensible design
- ✅ SOLID principles in action

**German Interview Ready:** You can explain each pattern with real code examples from your project! 🚀

---

**Document Version:** 1.0
**Last Updated:** January 2, 2026
**Author:** Sprint 1 - Authentication Team
