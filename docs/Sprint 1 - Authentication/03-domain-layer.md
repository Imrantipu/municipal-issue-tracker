# Domain Layer - Pure Business Logic

The domain layer is the **heart** of our application. It contains pure business logic with **ZERO framework dependencies**.

---

## Why Domain Layer First?

**Hexagonal Architecture Principle:**
- Domain = What the application **DOES** (business rules)
- Infrastructure = **HOW** it does it (technical details)

**Benefits:**
- ✅ Test business logic in milliseconds (no Spring Boot, no database)
- ✅ Business rules independent of framework changes
- ✅ Easy to understand (no @Annotations clutter)

---

## File 1: `Role.java` - User Roles Enum

### Location
```
domain/model/Role.java
```

### Code
```java
package com.issuetracker.domain.model;

public enum Role {
    ADMIN,    // System administrator
    STAFF,    // Municipal staff member
    CITIZEN   // Regular citizen
}
```

### Why This Code?

**Business Requirement:**
- System has 3 types of users
- Each has different permissions

**Why Enum (not String)?**
- ✅ Type-safe: Can't assign invalid role like "SUPERUSER"
- ✅ Compile-time check: Typos caught by compiler
- ✅ Easy to extend: Add new role = add enum value
- ✅ Performance: Enums are singleton objects

**SOLID Principle: Single Responsibility (S)**
- This enum has ONE job: Define valid user roles
- Permission logic is elsewhere (SecurityConfig)

---

## File 2: `User.java` - User Domain Model

### Location
```
domain/model/User.java
```

### Code Structure
```java
@Data
@Builder
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
    private LocalDateTime deletedAt;  // Soft delete

    // Business validation methods
    public void validate() { ... }
    private void validateName() { ... }
    private void validateEmail() { ... }
    private void validatePassword() { ... }

    // Business logic methods
    public boolean isDeleted() { ... }
    public boolean isAdmin() { ... }
    public void softDelete() { ... }
}
```

### Why This Code?

#### 1. **Lombok Annotations** (@Data, @Builder)
```java
@Data  // Generates: getters, setters, toString, equals, hashCode
@Builder  // Generates: User.builder().name("John").email("...").build()
```

**Why?**
- ✅ Reduces boilerplate (no manual getters/setters)
- ✅ Immutability option (can use @Value for immutable objects)
- ✅ Builder pattern makes object creation readable

#### 2. **No JPA Annotations** (@Entity, @Table)
```java
// ❌ BAD (in domain layer):
@Entity
@Table(name = "users")
public class User { ... }

// ✅ GOOD (domain layer):
public class User { ... }
```

**Why No @Entity?**
- Domain must be framework-independent
- JPA annotations belong in infrastructure (UserEntity.java)
- Can test User without database

#### 3. **Business Validation Methods**
```java
public void validate() {
    validateName();
    validateEmail();
    validatePassword();
    validateRole();
}

private void validateEmail() {
    if (email == null || email.trim().isEmpty()) {
        throw new IllegalArgumentException("Email cannot be empty");
    }
    if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
        throw new IllegalArgumentException("Email format is invalid");
    }
}
```

**Why Validation in Domain?**
- ✅ Business rules belong in domain
- ✅ Can validate User without Spring Boot
- ✅ Reusable (same validation for API, batch jobs, etc.)

**Why throw IllegalArgumentException?**
- Standard Java exception (no framework dependency)
- Caught by controller and converted to 400 Bad Request

#### 4. **Business Logic Methods**
```java
public boolean isDeleted() {
    return deletedAt != null;
}

public boolean isAdmin() {
    return role == Role.ADMIN;
}

public void softDelete() {
    this.deletedAt = LocalDateTime.now();
}
```

**Why These Methods?**
- ✅ **Encapsulation:** Business logic stays in domain object
- ✅ **Readability:** `user.isAdmin()` is clearer than `user.getRole() == Role.ADMIN`
- ✅ **Single Source of Truth:** Only one place defines "what is deleted"

#### 5. **Soft Delete (deletedAt)**
```java
private LocalDateTime deletedAt;  // null = active, not-null = deleted
```

**Why Soft Delete?**
- ✅ **GDPR Compliance:** Can restore deleted users if needed
- ✅ **Data Integrity:** Preserve foreign key relationships
- ✅ **Audit Trail:** Know when user was deleted
- ✅ **Analytics:** Can analyze deleted users

---

## File 3: `UserRepository.java` - Output Port

### Location
```
domain/port/out/UserRepository.java
```

### Code
```java
public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    void deleteById(Long id);
    List<User> findAllActive();
}
```

### Why This Code?

#### 1. **Interface (not Class)**
```java
// ✅ GOOD: Domain defines interface
public interface UserRepository { ... }

// ❌ BAD: Domain has concrete implementation
public class JpaUserRepository { ... }
```

**Why Interface?**
- **Dependency Inversion Principle (D)**
- Domain defines WHAT it needs
- Infrastructure provides HOW (JpaUserRepositoryAdapter)
- Can swap PostgreSQL → MongoDB by changing infrastructure only

#### 2. **Domain Types (not JPA Types)**
```java
// ✅ GOOD: Uses domain User
User save(User user);

// ❌ BAD: Uses JPA UserEntity
UserEntity save(UserEntity entity);
```

**Why Domain Types?**
- Domain never knows about JPA, PostgreSQL, etc.
- Adapter (infrastructure) converts User ↔ UserEntity

#### 3. **Method Names Match Business Needs**
```java
Optional<User> findByEmail(String email);  // For login
boolean existsByEmail(String email);       // For registration validation
List<User> findAllActive();                // Exclude soft-deleted users
```

**Why These Methods?**
- Designed for business use cases, not database operations
- `existsByEmail` is faster than `findByEmail` (no object creation)
- `findAllActive` enforces soft delete at repository level

---

## File 4: `RegisterUserUseCase.java` - Input Port

### Location
```
domain/port/in/RegisterUserUseCase.java
```

### Code
```java
public interface RegisterUserUseCase {
    User registerUser(RegisterUserCommand command);

    record RegisterUserCommand(
        String name,
        String email,
        String password,
        String role
    ) {}

    class EmailAlreadyExistsException extends RuntimeException {
        public EmailAlreadyExistsException(String email) {
            super("Email already exists: " + email);
        }
    }
}
```

### Why This Code?

#### 1. **Use Case Interface (not Service Class)**
```java
// ✅ GOOD: Interface defining capability
public interface RegisterUserUseCase { ... }

// ❌ BAD: Concrete service
public class UserService { ... }
```

**Why Interface?**
- **Interface Segregation Principle (I)**
- Controller depends on RegisterUserUseCase (not entire UserService)
- Can have multiple implementations (mock for testing, real for production)

#### 2. **Command Object (not Individual Parameters)**
```java
// ✅ GOOD: Command object
User registerUser(RegisterUserCommand command);

// ❌ BAD: Multiple parameters
User registerUser(String name, String email, String password, String role);
```

**Why Command Object?**
- ✅ **Extensibility:** Add new field = change command only
- ✅ **Readability:** One parameter instead of four
- ✅ **Type Safety:** Can't mix up parameter order

#### 3. **Record (Java 14+)**
```java
record RegisterUserCommand(String name, String email, String password, String role) {}
```

**Why Record?**
- ✅ Immutable by default (thread-safe)
- ✅ Auto-generates constructor, getters, equals, hashCode
- ✅ Perfect for DTOs/Commands (data carriers)

#### 4. **Custom Exception Inside Interface**
```java
class EmailAlreadyExistsException extends RuntimeException { ... }
```

**Why Inside Interface?**
- ✅ **Cohesion:** Exception tightly coupled to use case
- ✅ **Discoverability:** Easy to find (same file as use case)
- ✅ **Namespace:** `RegisterUserUseCase.EmailAlreadyExistsException`

---

## File 5: `LoginUserUseCase.java` - Input Port

### Location
```
domain/port/in/LoginUserUseCase.java
```

### Code
```java
public interface LoginUserUseCase {
    AuthResponse login(LoginCommand command);

    record LoginCommand(String email, String password) {}

    record AuthResponse(
        String token,
        Long userId,
        String name,
        String email,
        String role
    ) {}

    class InvalidCredentialsException extends RuntimeException { ... }
    class UserDeletedException extends RuntimeException { ... }
}
```

### Why Separate from RegisterUserUseCase?

**Interface Segregation Principle (I):**
```java
// ✅ GOOD: Separate interfaces
public interface RegisterUserUseCase { ... }
public interface LoginUserUseCase { ... }

// ❌ BAD: One giant interface
public interface UserUseCase {
    User register(...);
    AuthResponse login(...);
    void updateProfile(...);
    void changePassword(...);
    // ... 20 more methods
}
```

**Why Separate?**
- ✅ Controller depends only on what it needs
- ✅ Testing easier (mock only what you use)
- ✅ Changes to login don't affect registration

---

## Domain Layer Summary

### What We Achieved

| File | Purpose | SOLID Principle |
|------|---------|-----------------|
| `Role.java` | Define user roles | Single Responsibility |
| `User.java` | User entity + validation | Single Responsibility |
| `UserRepository.java` | Database contract | Dependency Inversion |
| `RegisterUserUseCase.java` | Registration contract | Interface Segregation |
| `LoginUserUseCase.java` | Login contract | Interface Segregation |

### Key Characteristics

✅ **Zero Framework Dependencies**
- No Spring, no JPA, no Jakarta
- Pure Java (+ Lombok for boilerplate)

✅ **Testable in Milliseconds**
```java
// Test without Spring Boot
@Test
void user_shouldThrowException_whenEmailInvalid() {
    User user = User.builder()
        .name("John")
        .email("invalid-email")  // No @ symbol
        .password("password123")
        .role(Role.CITIZEN)
        .build();

    assertThrows(IllegalArgumentException.class, () -> user.validate());
}
```

✅ **Business Logic Clarity**
- `user.isAdmin()` - Clear intent
- `user.softDelete()` - Encapsulated logic
- `user.validate()` - Single responsibility

✅ **Dependency Inversion**
- Domain defines interfaces (ports)
- Infrastructure implements them (adapters)

---

## Next: Application Layer

The domain layer defines **WHAT** the application does.
The application layer orchestrates **HOW** to do it.

Continue to: [04-application-layer.md](./04-application-layer.md)
