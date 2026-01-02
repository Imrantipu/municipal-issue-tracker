# Application Layer - Orchestration

The application layer **orchestrates** domain logic and infrastructure services. It's the **glue** between pure business logic (domain) and technical details (infrastructure).

---

## What is Application Layer?

**Purpose:** Implement use cases by coordinating domain objects and infrastructure services.

**Responsibilities:**
- ✅ Implement use case interfaces (port/in)
- ✅ Orchestrate domain logic
- ✅ Call infrastructure services (repositories, password encoder, JWT)
- ✅ Handle transactions
- ✅ Convert between domain and DTOs (if needed)

**NOT Responsible For:**
- ❌ Business validation (that's in domain)
- ❌ HTTP/REST concerns (that's in controllers)
- ❌ Database queries (that's in repositories)

---

## File: `UserService.java` - Use Case Implementation

### Location
```
application/service/UserService.java
```

### Code Structure
```java
@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements RegisterUserUseCase, LoginUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public User registerUser(RegisterUserCommand command) { ... }

    @Override
    public AuthResponse login(LoginCommand command) { ... }

    private Role parseRole(String roleString) { ... }
}
```

---

## Why This Code?

### 1. **@Service Annotation**
```java
@Service  // Spring component annotation
public class UserService { ... }
```

**Why @Service?**
- Tells Spring: "This is a service component, create a bean"
- Spring auto-discovers and manages lifecycle
- Enables dependency injection
- Semantic clarity (vs generic @Component)

### 2. **@RequiredArgsConstructor (Lombok)**
```java
@RequiredArgsConstructor  // Generates constructor for final fields
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // Lombok generates:
    // public UserService(UserRepository repo, PasswordEncoder encoder, JwtTokenProvider jwt) {
    //     this.userRepository = repo;
    //     this.passwordEncoder = encoder;
    //     this.jwtTokenProvider = jwt;
    // }
}
```

**Why Constructor Injection?**
- ✅ **Immutability:** Fields are `final` (cannot be changed)
- ✅ **Testability:** Easy to mock dependencies in tests
- ✅ **Compile-time Safety:** Lombok generates code at compile time
- ✅ **Spring Best Practice:** Recommended over @Autowired on fields

**Why Not @Autowired on Fields?**
```java
// ❌ BAD: Field injection
@Autowired
private UserRepository userRepository;

// ✅ GOOD: Constructor injection (via @RequiredArgsConstructor)
private final UserRepository userRepository;
```

Field injection:
- Cannot be final (mutable)
- Hard to test (need reflection to inject mocks)
- Hides dependencies (not visible in constructor)

### 3. **@Transactional Annotation**
```java
@Transactional  // All methods run in database transaction
public class UserService { ... }
```

**Why @Transactional?**
- ✅ **Atomicity:** All database operations succeed or fail together
- ✅ **Rollback on Exception:** If exception thrown, rollback changes
- ✅ **Connection Management:** Spring manages database connections

**Example Scenario:**
```java
public User registerUser(...) {
    user.validate();  // throws exception
    userRepository.save(user);  // ← This won't execute if validate() fails
}
```

Without @Transactional: Partial data might be saved
With @Transactional: All or nothing (rollback on exception)

### 4. **Implements Multiple Interfaces**
```java
public class UserService implements RegisterUserUseCase, LoginUserUseCase { ... }
```

**Why Implement Multiple Interfaces?**
- ✅ **Interface Segregation (I):** Clients depend on specific interfaces
- ✅ **Centralized Logic:** Related operations in one service
- ✅ **Dependency Injection:** Spring can inject UserService as either interface

**Can split into multiple services if needed:**
```java
// Option A: One service (current)
class UserService implements RegisterUserUseCase, LoginUserUseCase { ... }

// Option B: Split into multiple services (for larger systems)
class RegistrationService implements RegisterUserUseCase { ... }
class AuthenticationService implements LoginUserUseCase { ... }
```

---

## Method 1: `registerUser()` - Registration Logic

### Code
```java
@Override
public User registerUser(RegisterUserCommand command) {
    // 1. Business Rule: Email must be unique
    if (userRepository.existsByEmail(command.email())) {
        throw new EmailAlreadyExistsException(command.email());
    }

    // 2. Create domain object
    User user = User.builder()
            .name(command.name())
            .email(command.email().toLowerCase())  // Normalize
            .password(command.password())  // Plain text (will be hashed below)
            .role(parseRole(command.role()))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    // 3. Domain validation
    user.validate();

    // 4. Hash password (infrastructure concern)
    String hashedPassword = passwordEncoder.encode(user.getPassword());
    user.setPassword(hashedPassword);

    // 5. Save to database
    return userRepository.save(user);
}
```

### Why This Flow?

#### Step 1: Check Email Uniqueness
```java
if (userRepository.existsByEmail(command.email())) {
    throw new EmailAlreadyExistsException(command.email());
}
```

**Why Check First?**
- ✅ **Early Validation:** Fail fast before creating User object
- ✅ **Better Error Message:** "Email exists" vs generic database error
- ✅ **Performance:** `existsByEmail` is faster than `findByEmail`

**Why Not Database Constraint Only?**
- Database constraint is **backup** (catch race conditions)
- Application check gives **better UX** (clear error message)

#### Step 2: Normalize Email
```java
.email(command.email().toLowerCase())  // "JOHN@EXAMPLE.COM" → "john@example.com"
```

**Why Normalize?**
- ✅ **Case-Insensitive Login:** User can login with "John@Example.com"
- ✅ **Prevent Duplicates:** "john@ex.com" and "JOHN@ex.com" are same
- ✅ **Database Query:** Simpler (no LOWER() function needed)

#### Step 3: Domain Validation
```java
user.validate();  // Throws IllegalArgumentException if invalid
```

**Why After Object Creation?**
- User object exists → can call `user.validate()`
- All fields set → comprehensive validation
- Exception thrown → transaction rolls back

#### Step 4: Hash Password
```java
String hashedPassword = passwordEncoder.encode(user.getPassword());
user.setPassword(hashedPassword);
```

**Why in Service (not Domain)?**
- **Domain:** Business rules (what password is valid)
- **Application:** Orchestration (how to hash it)
- **Infrastructure:** Implementation (BCrypt algorithm)

**Why BCrypt?**
- ✅ **Adaptive:** Can increase cost factor over time
- ✅ **Salted:** Automatic unique salt per password
- ✅ **One-Way:** Cannot decrypt (only verify)
- ✅ **Industry Standard:** Used by GitHub, Dropbox, etc.

**BCrypt Example:**
```
Plain text: "password123"
BCrypt hash: "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
                ^   ^       ^                   ^
                |   |       |                   |
                |   |       |                   Hash (31 chars)
                |   |       Salt (22 chars)
                |   Cost factor (2^10 = 1024 iterations)
                Algorithm version (2a)
```

#### Step 5: Save to Database
```java
return userRepository.save(user);
```

**What Happens:**
1. UserService calls UserRepository interface
2. JpaUserRepositoryAdapter converts User → UserEntity
3. Spring Data JPA saves to PostgreSQL
4. Database generates ID and timestamps
5. Returns saved UserEntity
6. Adapter converts back to User
7. UserService returns User to controller

---

## Method 2: `login()` - Authentication Logic

### Code
```java
@Override
public AuthResponse login(LoginCommand command) {
    // 1. Find user by email
    User user = userRepository.findByEmail(command.email().toLowerCase())
            .orElseThrow(InvalidCredentialsException::new);

    // 2. Check if user is active
    if (user.isDeleted()) {
        throw new UserDeletedException();
    }

    // 3. Verify password
    if (!passwordEncoder.matches(command.password(), user.getPassword())) {
        throw new InvalidCredentialsException();
    }

    // 4. Generate JWT token
    String token = jwtTokenProvider.generateToken(user);

    // 5. Return auth response
    return new AuthResponse(
            token,
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole().name()
    );
}
```

### Why This Flow?

#### Step 1: Find User
```java
User user = userRepository.findByEmail(command.email().toLowerCase())
        .orElseThrow(InvalidCredentialsException::new);
```

**Why Optional.orElseThrow?**
- ✅ **Concise:** One line instead of if-else
- ✅ **Null-Safe:** No NullPointerException
- ✅ **Functional Style:** Modern Java best practice

**Why Same Exception for "Not Found" and "Wrong Password"?**
```java
// ✅ GOOD: Generic "Invalid credentials"
throw new InvalidCredentialsException();

// ❌ BAD: Specific error
throw new EmailNotFoundException();  // ← Security risk!
```

**Security Reason:**
- Prevents **user enumeration attack**
- Attacker can't tell if email exists or password is wrong
- Same error for both cases

#### Step 2: Check Soft Delete
```java
if (user.isDeleted()) {
    throw new UserDeletedException();
}
```

**Why Check After Finding?**
- User might exist but be soft-deleted
- Different error message for deleted vs wrong password
- Potential feature: "Restore account" button

#### Step 3: Verify Password
```java
if (!passwordEncoder.matches(command.password(), user.getPassword())) {
    throw new InvalidCredentialsException();
}
```

**Why matches() method?**
```java
// ✅ GOOD: BCrypt comparison
passwordEncoder.matches("password123", "$2a$10$...")  → true/false

// ❌ BAD: String comparison
"password123".equals("$2a$10$...")  → false (always!)
```

**How BCrypt Verification Works:**
1. Extract salt from stored hash
2. Hash input password with same salt
3. Compare hashes
4. Return true if match

**Time-Constant Comparison:**
- BCrypt uses time-constant comparison
- Prevents **timing attacks**
- Same time whether password is correct or not

#### Step 4: Generate JWT Token
```java
String token = jwtTokenProvider.generateToken(user);
```

**What's in the Token?**
```json
{
  "sub": "1",                    // User ID
  "email": "john@example.com",
  "role": "CITIZEN",
  "iat": 1704067200,             // Issued at (timestamp)
  "exp": 1704153600              // Expires (24 hours later)
}
```

**Why Include Role in Token?**
- ✅ **No Database Query:** Can check permissions without DB hit
- ✅ **Stateless:** Token contains all needed info
- ✅ **Fast Authorization:** JwtAuthenticationFilter extracts role

**Why NOT Include Password?**
- ❌ **Security Risk:** JWT is base64 (not encrypted!)
- ❌ **Unnecessary:** Only need it for login
- ❌ **Token Size:** Passwords are long (BCrypt hash = 60 chars)

---

## Method 3: `parseRole()` - Helper Method

### Code
```java
private Role parseRole(String roleString) {
    if (roleString == null || roleString.trim().isEmpty()) {
        return Role.CITIZEN;  // Default role
    }

    try {
        return Role.valueOf(roleString.toUpperCase());
    } catch (IllegalArgumentException e) {
        return Role.CITIZEN;  // Default if invalid
    }
}
```

### Why This Helper?

**Defensive Programming:**
- ✅ **Default Role:** If client doesn't specify, default to CITIZEN
- ✅ **Case-Insensitive:** Accepts "citizen", "CITIZEN", "Citizen"
- ✅ **Fail-Safe:** Invalid role → default to CITIZEN (not crash)

**Alternative: Fail Fast**
```java
// Option A: Lenient (current)
invalid role → CITIZEN

// Option B: Strict
invalid role → throw IllegalArgumentException
```

We chose **lenient** because:
- Most users should be CITIZEN
- Better UX (registration succeeds)
- Admin can manually promote users if needed

---

## Application Layer Summary

### What We Achieved

| Method | Purpose | Orchestrates |
|--------|---------|--------------|
| `registerUser()` | User registration | Domain validation + Password hashing + Repository |
| `login()` | User authentication | Repository lookup + Password verification + JWT generation |
| `parseRole()` | Role parsing | Defensive string → enum conversion |

### Key Characteristics

✅ **Orchestration, Not Implementation**
- Service doesn't validate (domain does)
- Service doesn't hash (encoder does)
- Service doesn't save (repository does)
- Service **coordinates** all of them

✅ **Transaction Management**
- @Transactional ensures atomicity
- Exception → rollback
- Success → commit

✅ **Dependency Inversion**
- Depends on interfaces (UserRepository, PasswordEncoder)
- Not concrete classes (JpaUserRepository, BCryptPasswordEncoder)

✅ **Single Responsibility**
- UserService = Orchestrate user operations
- Not: Validate, Hash, Store, Generate JWT
- Those are delegated to domain/infrastructure

---

## Next: Infrastructure Layer

The application layer orchestrates **WHAT** to do.
The infrastructure layer provides **HOW** to do it.

Continue to: [05-infrastructure-layer.md](./05-infrastructure-layer.md)
