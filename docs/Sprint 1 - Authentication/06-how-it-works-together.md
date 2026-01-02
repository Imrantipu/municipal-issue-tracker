# How It All Works Together

This document shows how all components interact to handle user registration and login requests.

---

## Complete Registration Flow

### User Story
> "As a citizen, I want to register an account so I can report infrastructure issues."

### Step-by-Step Flow

```
┌──────────────────────────────────────────────────────────────────────────┐
│                     REGISTRATION: START TO FINISH                         │
└──────────────────────────────────────────────────────────────────────────┘

1. CLIENT (Frontend / Postman)
   │
   │  POST http://localhost:8080/api/auth/register
   │  Content-Type: application/json
   │
   │  {
   │    "name": "Alice Johnson",
   │    "email": "alice@example.com",
   │    "password": "SecurePass123",
   │    "role": "CITIZEN"
   │  }
   │
   ▼

2. SPRING MVC DISPATCHER SERVLET
   │
   │  ├─ Parse JSON → Java object (RegisterRequest)
   │  ├─ Find @PostMapping("/register") → AuthController.register()
   │  └─ Call controller method
   │
   ▼

3. SPRING SECURITY FILTER CHAIN
   │
   │  ├─ CorsFilter: Check origin (http://localhost:3000) → ✅ Allowed
   │  ├─ CsrfFilter: CSRF check → ⏭️ Disabled (stateless JWT)
   │  ├─ JwtAuthenticationFilter: Extract JWT → ⏭️ None (public endpoint)
   │  └─ FilterSecurityInterceptor:
   │      Check authorization: /api/auth/register → permitAll() ✅
   │
   ▼

4. INFRASTRUCTURE LAYER - AuthController.java
   │
   │  @PostMapping("/register")
   │  public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
   │
   │    // Create command
   │    RegisterUserCommand command = new RegisterUserCommand(
   │        request.name,
   │        request.email,
   │        request.password,
   │        request.role
   │    );
   │
   │    // Call use case
   │    User user = registerUserUseCase.registerUser(command);
   │
   │    // Convert to response DTO
   │    UserResponse response = new UserResponse(...);
   │    return ResponseEntity.status(201).body(response);
   │  }
   │
   ▼

5. APPLICATION LAYER - UserService.java (implements RegisterUserUseCase)
   │
   │  @Override
   │  public User registerUser(RegisterUserCommand command) {
   │
   │    // Step 5a: Check email uniqueness
   │    if (userRepository.existsByEmail(command.email())) {
   │        throw new EmailAlreadyExistsException(command.email());
   │    }
   │
   │    // Step 5b: Create User domain object
   │    User user = User.builder()
   │        .name(command.name())
   │        .email(command.email().toLowerCase())
   │        .password(command.password())
   │        .role(parseRole(command.role()))
   │        .createdAt(LocalDateTime.now())
   │        .build();
   │
   │    // Step 5c: Validate business rules
   │    user.validate();  // → Calls domain validation
   │
   │    // Step 5d: Hash password
   │    String hashed = passwordEncoder.encode(user.getPassword());
   │    user.setPassword(hashed);
   │
   │    // Step 5e: Save via repository interface
   │    return userRepository.save(user);
   │  }
   │
   ▼

6a. DOMAIN LAYER - User.java (validation)
   │
   │  public void validate() {
   │    validateName();    // Min 2 chars, max 100
   │    validateEmail();   // Valid email format
   │    validatePassword(); // Min 8 chars
   │    validateRole();    // Not null
   │  }
   │
   │  private void validateEmail() {
   │    if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
   │        throw new IllegalArgumentException("Email format is invalid");
   │    }
   │  }
   │
   ▼

6b. INFRASTRUCTURE LAYER - BCryptPasswordEncoder (password hashing)
   │
   │  public String encode(String rawPassword) {
   │    // BCrypt algorithm
   │    // - Generate random salt
   │    // - Hash password with salt
   │    // - Cost factor: 10 (2^10 = 1024 iterations)
   │
   │    return "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
   │  }
   │
   ▼

7. INFRASTRUCTURE LAYER - JpaUserRepositoryAdapter.java (Adapter Pattern)
   │
   │  @Override
   │  public User save(User user) {
   │
   │    // Convert: User (domain) → UserEntity (JPA)
   │    UserEntity entity = toEntity(user);
   │    // entity now has @Entity, @Id, @Column, etc.
   │
   │    // Save via Spring Data JPA
   │    UserEntity saved = springDataRepository.save(entity);
   │
   │    // Convert back: UserEntity (JPA) → User (domain)
   │    return toDomain(saved);
   │  }
   │
   │  private UserEntity toEntity(User user) {
   │    return UserEntity.builder()
   │        .id(user.getId())
   │        .name(user.getName())
   │        .email(user.getEmail())
   │        .password(user.getPassword())
   │        .role(user.getRole())
   │        .createdAt(user.getCreatedAt())
   │        .updatedAt(user.getUpdatedAt())
   │        .build();
   │  }
   │
   ▼

8. INFRASTRUCTURE LAYER - SpringDataUserRepository.java (Spring Data JPA)
   │
   │  // Spring generates implementation at runtime
   │  public interface SpringDataUserRepository extends JpaRepository<UserEntity, Long> {
   │      // save() method provided by JpaRepository
   │  }
   │
   │  // Spring executes:
   │  entityManager.persist(userEntity);
   │
   ▼

9. DATABASE (PostgreSQL)
   │
   │  Hibernate generates SQL:
   │
   │  INSERT INTO users (name, email, password, role, created_at, updated_at)
   │  VALUES ('Alice Johnson', 'alice@example.com', '$2a$10$...', 'CITIZEN', NOW(), NOW())
   │  RETURNING id;
   │
   │  Result: id = 1 (auto-generated by PostgreSQL SERIAL)
   │
   ▼

10. RESPONSE BACK UP THE CHAIN
   │
   │  PostgreSQL → Hibernate → SpringDataRepository → Adapter → UserService → AuthController
   │
   │  Each layer transforms data:
   │
   │  UserEntity (id=1) → User (id=1) → UserResponse (id=1, no password)
   │
   ▼

11. HTTP RESPONSE TO CLIENT
   │
   │  HTTP/1.1 201 Created
   │  Content-Type: application/json
   │
   │  {
   │    "id": 1,
   │    "name": "Alice Johnson",
   │    "email": "alice@example.com",
   │    "role": "CITIZEN",
   │    "createdAt": "2026-01-01T12:00:00"
   │  }
   │
   └─ Registration complete! ✅
```

---

## Complete Login Flow

### User Story
> "As a registered citizen, I want to login to access my issues."

### Step-by-Step Flow

```
┌──────────────────────────────────────────────────────────────────────────┐
│                       LOGIN: START TO FINISH                              │
└──────────────────────────────────────────────────────────────────────────┘

1. CLIENT
   │
   │  POST http://localhost:8080/api/auth/login
   │  Content-Type: application/json
   │
   │  {
   │    "email": "alice@example.com",
   │    "password": "SecurePass123"
   │  }
   │
   ▼

2-3. SPRING MVC + SECURITY FILTER CHAIN
   │
   │  (Same as registration - /api/auth/login is permitAll)
   │
   ▼

4. INFRASTRUCTURE LAYER - AuthController.java
   │
   │  @PostMapping("/login")
   │  public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
   │
   │    LoginCommand command = new LoginCommand(
   │        request.email,
   │        request.password
   │    );
   │
   │    AuthResponse response = loginUserUseCase.login(command);
   │    return ResponseEntity.ok(response);
   │  }
   │
   ▼

5. APPLICATION LAYER - UserService.java (implements LoginUserUseCase)
   │
   │  @Override
   │  public AuthResponse login(LoginCommand command) {
   │
   │    // Step 5a: Find user by email
   │    User user = userRepository.findByEmail(command.email().toLowerCase())
   │        .orElseThrow(InvalidCredentialsException::new);
   │
   │    // Step 5b: Check if user is deleted
   │    if (user.isDeleted()) {
   │        throw new UserDeletedException();
   │    }
   │
   │    // Step 5c: Verify password
   │    if (!passwordEncoder.matches(command.password(), user.getPassword())) {
   │        throw new InvalidCredentialsException();
   │    }
   │
   │    // Step 5d: Generate JWT token
   │    String token = jwtTokenProvider.generateToken(user);
   │
   │    // Step 5e: Return auth response
   │    return new AuthResponse(
   │        token,
   │        user.getId(),
   │        user.getName(),
   │        user.getEmail(),
   │        user.getRole().name()
   │    );
   │  }
   │
   ▼

6. INFRASTRUCTURE LAYER - JpaUserRepositoryAdapter.java
   │
   │  @Override
   │  public Optional<User> findByEmail(String email) {
   │
   │    // Call Spring Data JPA
   │    Optional<UserEntity> entityOpt = springDataRepository.findByEmail(email);
   │
   │    // Convert: UserEntity → User (if found)
   │    return entityOpt.map(this::toDomain);
   │  }
   │
   ▼

7. DATABASE (PostgreSQL)
   │
   │  SELECT * FROM users WHERE email = 'alice@example.com' AND deleted_at IS NULL;
   │
   │  Result:
   │  id=1, name='Alice Johnson', email='alice@example.com',
   │  password='$2a$10$...', role='CITIZEN', ...
   │
   ▼

8. INFRASTRUCTURE LAYER - BCryptPasswordEncoder (password verification)
   │
   │  public boolean matches(String rawPassword, String encodedPassword) {
   │
   │    // Input: "SecurePass123", "$2a$10$..."
   │
   │    // BCrypt verification:
   │    // 1. Extract salt from stored hash
   │    // 2. Hash input password with same salt
   │    // 3. Compare hashes (time-constant comparison)
   │
   │    return true;  // Password matches! ✅
   │  }
   │
   ▼

9. INFRASTRUCTURE LAYER - JwtTokenProvider.java (token generation)
   │
   │  public String generateToken(User user) {
   │
   │    Date now = new Date();
   │    Date expiryDate = new Date(now.getTime() + 86400000);  // +24 hours
   │
   │    return Jwts.builder()
   │        .setSubject(user.getId().toString())     // "1"
   │        .claim("email", user.getEmail())         // "alice@example.com"
   │        .claim("role", user.getRole().name())    // "CITIZEN"
   │        .setIssuedAt(now)
   │        .setExpiration(expiryDate)
   │        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
   │        .compact();
   │
   │    // Result:
   │    // "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiZW1haWwiOiJhbGljZUBleGFtcGxlLmNvbSIsInJvbGUiOiJDSVRJWkVOIn0.xyz..."
   │  }
   │
   ▼

10. HTTP RESPONSE TO CLIENT
   │
   │  HTTP/1.1 200 OK
   │  Content-Type: application/json
   │
   │  {
   │    "token": "eyJhbGciOiJIUzI1NiJ9...",
   │    "userId": 1,
   │    "name": "Alice Johnson",
   │    "email": "alice@example.com",
   │    "role": "CITIZEN"
   │  }
   │
   └─ Login complete! User has JWT token ✅
```

---

## Protected Endpoint Flow

### User Story
> "As a logged-in citizen, I want to access my issues list."

### Step-by-Step Flow

```
┌──────────────────────────────────────────────────────────────────────────┐
│                  PROTECTED ENDPOINT: JWT VALIDATION                       │
└──────────────────────────────────────────────────────────────────────────┘

1. CLIENT
   │
   │  GET http://localhost:8080/api/issues
   │  Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
   │
   ▼

2. SPRING SECURITY FILTER CHAIN
   │
   ▼

3. JWT AUTHENTICATION FILTER (JwtAuthenticationFilter.java)
   │
   │  protected void doFilterInternal(...) {
   │
   │    // Step 3a: Extract JWT from header
   │    String jwt = getJwtFromRequest(request);
   │    // jwt = "eyJhbGciOiJIUzI1NiJ9..."
   │
   │    // Step 3b: Validate token
   │    if (jwtTokenProvider.validateToken(jwt)) {
   │
   │        // Step 3c: Extract user info from token
   │        Long userId = jwtTokenProvider.getUserIdFromToken(jwt);    // 1
   │        String email = jwtTokenProvider.getEmailFromToken(jwt);     // "alice@example.com"
   │        String role = jwtTokenProvider.getRoleFromToken(jwt);       // "CITIZEN"
   │
   │        // Step 3d: Create Spring Security authentication
   │        UsernamePasswordAuthenticationToken authentication =
   │            new UsernamePasswordAuthenticationToken(
   │                email,  // Principal
   │                null,   // Credentials (not needed)
   │                List.of(new SimpleGrantedAuthority("ROLE_CITIZEN"))  // Authorities
   │            );
   │
   │        // Step 3e: Set in SecurityContext (marks user as authenticated)
   │        SecurityContextHolder.getContext().setAuthentication(authentication);
   │    }
   │
   │    // Step 3f: Continue to next filter
   │    filterChain.doFilter(request, response);
   │  }
   │
   ▼

4. JWT TOKEN PROVIDER (JwtTokenProvider.java)
   │
   │  public boolean validateToken(String token) {
   │
   │    try {
   │        Jwts.parser()
   │            .verifyWith(getSigningKey())  // Verify HMAC-SHA256 signature
   │            .build()
   │            .parseSignedClaims(token);    // Parse and verify
   │
   │        // Checks:
   │        // ✅ Signature valid (token not tampered with)
   │        // ✅ Not expired (exp claim < now)
   │        // ✅ Valid structure (header.payload.signature)
   │
   │        return true;
   │    } catch (Exception e) {
   │        return false;  // Invalid token
   │    }
   │  }
   │
   ▼

5. SECURITY FILTER INTERCEPTOR (Spring Security)
   │
   │  // Check authorization rules from SecurityConfig
   │
   │  .authorizeHttpRequests(auth -> auth
   │      .anyRequest().authenticated()  // ← /api/issues requires authentication
   │  )
   │
   │  // Is user authenticated?
   │  Authentication auth = SecurityContextHolder.getContext().getAuthentication();
   │  if (auth != null && auth.isAuthenticated()) {
   │      // ✅ User is authenticated (JWT was valid)
   │      // Allow request to proceed to controller
   │  } else {
   │      // ❌ User not authenticated
   │      // Return 401 Unauthorized
   │  }
   │
   ▼

6. CONTROLLER (IssuesController.java - example)
   │
   │  @GetMapping
   │  public ResponseEntity<List<IssueResponse>> getMyIssues() {
   │
   │    // Can access authenticated user info:
   │    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
   │    String email = auth.getName();  // "alice@example.com"
   │
   │    // Retrieve user's issues...
   │    return ResponseEntity.ok(issues);
   │  }
   │
   └─ Request successful! ✅
```

---

## Dependency Flow

### How Layers Depend on Each Other

```
┌─────────────────────────────────────────────────────────────────┐
│                      DEPENDENCY DIRECTION                        │
└─────────────────────────────────────────────────────────────────┘

                    ┌──────────────────┐
                    │   DOMAIN LAYER   │
                    │                  │
                    │  - User.java     │
                    │  - Role.java     │
                    │  - Interfaces    │
                    │    (ports)       │
                    └────────▲─────────┘
                             │
                             │ implements
                             │ (uses interfaces)
                    ┌────────┴─────────┐
                    │ APPLICATION      │
                    │ LAYER            │
                    │                  │
                    │ - UserService    │
                    └────────▲─────────┘
                             │
                             │ implements
                             │ (uses interfaces)
                    ┌────────┴─────────┐
                    │ INFRASTRUCTURE   │
                    │ LAYER            │
                    │                  │
                    │ - Controllers    │
                    │ - JPA Entities   │
                    │ - Security       │
                    └──────────────────┘

RULE: Arrows point UP (Infrastructure → Application → Domain)
- Infrastructure depends on application/domain
- Domain depends on NOTHING
```

### Example: UserRepository Dependency

```java
// ✅ CORRECT: Infrastructure depends on Domain

// DOMAIN: Defines interface
package com.issuetracker.domain.port.out;
public interface UserRepository {
    User save(User user);
}

// APPLICATION: Uses interface
package com.issuetracker.application.service;
public class UserService {
    private final UserRepository repository;  // ← Domain interface
}

// INFRASTRUCTURE: Implements interface
package com.issuetracker.infrastructure.adapter.out.persistence;
public class JpaUserRepositoryAdapter implements UserRepository {  // ← Implements domain interface
    @Override
    public User save(User user) { ... }
}
```

---

## Data Transformation Flow

### How Data Changes Between Layers

```
CLIENT JSON
   ↓
   {
     "name": "Alice",
     "email": "alice@example.com",
     "password": "SecurePass123"
   }
   ↓
CONTROLLER DTO (RegisterRequest)
   ↓
   RegisterRequest(
     name = "Alice",
     email = "alice@example.com",
     password = "SecurePass123",
     role = "CITIZEN"
   )
   ↓
DOMAIN COMMAND (RegisterUserCommand)
   ↓
   RegisterUserCommand(
     name = "Alice",
     email = "alice@example.com",
     password = "SecurePass123",
     role = "CITIZEN"
   )
   ↓
DOMAIN OBJECT (User)
   ↓
   User(
     id = null,
     name = "Alice",
     email = "alice@example.com",
     password = "$2a$10$...",  // ← Hashed!
     role = Role.CITIZEN,
     createdAt = 2026-01-01T12:00:00,
     updatedAt = 2026-01-01T12:00:00,
     deletedAt = null
   )
   ↓
JPA ENTITY (UserEntity)
   ↓
   UserEntity(
     @Id id = null → Generated by DB,
     @Column name = "Alice",
     @Column email = "alice@example.com",
     @Column password = "$2a$10$...",
     @Enumerated role = Role.CITIZEN,
     ...
   )
   ↓
DATABASE (SQL)
   ↓
   INSERT INTO users (name, email, password, role, ...)
   VALUES ('Alice', 'alice@example.com', '$2a$10$...', 'CITIZEN', ...);
   RETURNING id;  -- id = 1
   ↓
BACK UP THE CHAIN
   ↓
   UserEntity(id=1) → User(id=1) → UserResponse(id=1, no password) → JSON
```

---

## Transaction Management

### How @Transactional Works

```java
@Service
@Transactional  // ← All methods run in transaction
public class UserService {

    public User registerUser(RegisterUserCommand command) {
        // Transaction START

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException();  // ← Rollback!
        }

        User user = User.builder().build();
        user.validate();  // ← If throws exception → Rollback!

        userRepository.save(user);  // ← Database INSERT

        // Transaction COMMIT (if no exception)
        // Transaction ROLLBACK (if exception thrown)
    }
}
```

**What Happens:**

**Success Case:**
```
1. Transaction BEGIN
2. existsByEmail(email) → false ✅
3. user.validate() → success ✅
4. repository.save(user) → INSERT ✅
5. Transaction COMMIT → Data saved to DB ✅
```

**Failure Case (Exception):**
```
1. Transaction BEGIN
2. existsByEmail(email) → true ❌
3. throw EmailAlreadyExistsException ❌
4. Transaction ROLLBACK → Nothing saved to DB
5. Exception propagates to controller
6. Controller returns 409 Conflict to client
```

---

## Summary

### Key Patterns Used

| Pattern | Where | Why |
|---------|-------|-----|
| **Dependency Inversion** | All layers | Infrastructure depends on domain, not vice versa |
| **Adapter Pattern** | JpaUserRepositoryAdapter | Converts UserEntity ↔ User |
| **Strategy Pattern** | PasswordEncoder | Can swap BCrypt → Argon2 easily |
| **Command Pattern** | RegisterUserCommand | Encapsulates request data |
| **Filter Chain** | Spring Security | Process request through multiple filters |
| **Builder Pattern** | User.builder() | Fluent object creation |

### Complete Request Journey

**Registration:**
```
Client → Spring MVC → Security Filters → AuthController → UserService →
Domain Validation → Password Hashing → JpaAdapter → Spring Data JPA →
PostgreSQL → Back up the chain → JSON Response
```

**Login:**
```
Client → Spring MVC → Security Filters → AuthController → UserService →
Find User → Verify Password → Generate JWT → JSON Response
```

**Protected Endpoint:**
```
Client (with JWT) → Spring MVC → JWT Filter (validate & authenticate) →
Security Filter (check permissions) → Controller → Service → Response
```

---

## 🎯 Congratulations!

You now understand:
- ✅ How each layer works independently
- ✅ How layers communicate via interfaces
- ✅ How data flows through the system
- ✅ How Spring Security + JWT protect endpoints
- ✅ Why Hexagonal Architecture matters

**You've built a production-ready authentication system! 🚀**
