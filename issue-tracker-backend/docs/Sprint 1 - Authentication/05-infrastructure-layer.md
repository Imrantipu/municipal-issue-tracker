# Infrastructure Layer - Technical Implementation

The infrastructure layer contains all **framework-specific** and **technical** code. This is where Spring Boot, JPA, JWT, and security live.

---

## What is Infrastructure Layer?

**Purpose:** Implement technical concerns (database, HTTP, security, etc.)

**Responsibilities:**
- ✅ REST API endpoints (controllers)
- ✅ Database persistence (JPA entities, repositories)
- ✅ Security configuration (JWT, Spring Security)
- ✅ External integrations (if any)

**Key Principle:**
- Infrastructure **depends on** Domain (via interfaces)
- Domain **never depends on** Infrastructure

---

## Component 1: Persistence (Database)

### Files
```
infrastructure/adapter/out/persistence/
├── UserEntity.java               # JPA entity
├── SpringDataUserRepository.java # Spring Data JPA
└── JpaUserRepositoryAdapter.java # Adapter pattern
```

---

### File: `UserEntity.java` - JPA Entity

#### Code
```java
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

#### Why This Code?

**1. Why Separate from User.java (Domain)?**
```
User.java (Domain)        UserEntity.java (Infrastructure)
├─ Pure Java              ├─ JPA annotations
├─ Business logic         ├─ Database mapping
├─ Framework-free         ├─ PostgreSQL-specific
└─ Fast tests             └─ Requires Spring Boot
```

**Benefits:**
- ✅ Can test User without database
- ✅ Can swap PostgreSQL → MongoDB (only change UserEntity)
- ✅ Domain remains clean and focused

**2. JPA Annotations Explained:**
```java
@Entity  // Marks as JPA entity (maps to database table)
@Table(name = "users")  // Table name (default would be "user_entity")
```

**3. Column Mapping:**
```java
@Column(name = "email", nullable = false, unique = true, length = 255)
private String email;
```
- `name`: Column name in database
- `nullable = false`: NOT NULL constraint
- `unique = true`: UNIQUE constraint
- `length = 255`: VARCHAR(255)

**4. Enum Mapping:**
```java
@Enumerated(EnumType.STRING)  // Store as "CITIZEN" (not integer)
@Column(name = "role")
private Role role;
```

**Why STRING not ORDINAL?**
```java
// ✅ GOOD: EnumType.STRING
Database: "CITIZEN", "STAFF", "ADMIN"

// ❌ BAD: EnumType.ORDINAL
Database: 0, 1, 2
// Problem: If you reorder enum values, database breaks!
```

**5. Lifecycle Callbacks:**
```java
@PrePersist  // Before first INSERT
protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
}

@PreUpdate  // Before every UPDATE
protected void onUpdate() {
    updatedAt = LocalDateTime.now();
}
```

**Why Not Set in Constructor?**
- Timestamps should reflect **actual** database operation time
- Constructor runs before save (time might differ)
- JPA callbacks run right before SQL execution

---

### File: `SpringDataUserRepository.java` - Spring Data JPA

#### Code
```java
@Repository
public interface SpringDataUserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM UserEntity u WHERE u.deletedAt IS NULL")
    List<UserEntity> findAllActive();
}
```

#### Why This Code?

**1. Extends JpaRepository:**
```java
extends JpaRepository<UserEntity, Long>
                      ^           ^
                      |           |
                      Entity type ID type
```

**What You Get for Free:**
- `save(entity)`
- `findById(id)`
- `findAll()`
- `deleteById(id)`
- `count()`
- And 15+ more methods!

**2. Spring Data Method Name Queries:**
```java
Optional<UserEntity> findByEmail(String email);
//                   ^^^^^^^^^^^
//                   Spring generates SQL: SELECT * FROM users WHERE email = ?

boolean existsByEmail(String email);
//      ^^^^^^^^^^^^^
//      Spring generates: SELECT COUNT(*) > 0 FROM users WHERE email = ?
```

**How Does This Work?**
- Spring Data JPA parses method name
- Generates SQL query at startup
- No manual SQL writing needed!

**3. Custom @Query:**
```java
@Query("SELECT u FROM UserEntity u WHERE u.deletedAt IS NULL")
List<UserEntity> findAllActive();
```

**Why Custom Query?**
- Method name would be too long: `findByDeletedAtIsNull()`
- JPQL is clearer for complex queries
- Can use native SQL if needed: `@Query(value = "SELECT * FROM ...", nativeQuery = true)`

---

### File: `JpaUserRepositoryAdapter.java` - Adapter Pattern

#### Code Structure
```java
@Component
@RequiredArgsConstructor
public class JpaUserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository springDataRepository;

    @Override
    public User save(User user) {
        UserEntity entity = toEntity(user);
        UserEntity saved = springDataRepository.save(entity);
        return toDomain(saved);
    }

    private User toDomain(UserEntity entity) { ... }
    private UserEntity toEntity(User user) { ... }
}
```

#### Why Adapter Pattern?

**Problem Without Adapter:**
```java
// ❌ BAD: UserService uses JPA directly
public class UserService {
    private final JpaRepository<UserEntity, Long> repository;  // ← JPA dependency!
}
```

**Solution: Adapter Pattern**
```java
// ✅ GOOD: UserService uses domain interface
public class UserService {
    private final UserRepository repository;  // ← Domain interface!
}

// Adapter converts between domain and infrastructure
public class JpaUserRepositoryAdapter implements UserRepository {
    private User toDomain(UserEntity entity) { ... }  // JPA → Domain
    private UserEntity toEntity(User user) { ... }    // Domain → JPA
}
```

**Benefits:**
- ✅ UserService never knows about JPA
- ✅ Can swap PostgreSQL → MongoDB (change adapter only)
- ✅ Domain remains pure

**Conversion Example:**
```java
// Domain → JPA
User user = User.builder()
    .name("John")
    .email("john@example.com")
    .build();

UserEntity entity = toEntity(user);
// Now has @Entity, @Column, etc.

springDataRepository.save(entity);  // JPA save

// JPA → Domain
User savedUser = toDomain(entity);
// Pure domain object (no JPA)
```

---

## Component 2: Security

### Files
```
infrastructure/security/
├── JwtTokenProvider.java         # JWT generation/validation
└── JwtAuthenticationFilter.java  # JWT filter
```

---

### File: `JwtTokenProvider.java` - JWT Handling

#### Code Structure
```java
@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public String generateToken(User user) { ... }
    public boolean validateToken(String token) { ... }
    public Long getUserIdFromToken(String token) { ... }
    public String getEmailFromToken(String token) { ... }
}
```

#### Why This Code?

**1. @Value Injection:**
```java
@Value("${jwt.secret}")
private String jwtSecret;  // Reads from application.properties
```

**Why Externalize Configuration?**
- ✅ **Security:** Secret not hardcoded in code
- ✅ **Flexibility:** Different secrets for dev/prod
- ✅ **12-Factor App:** Configuration via environment variables

**2. JWT Generation:**
```java
public String generateToken(User user) {
    return Jwts.builder()
            .setSubject(user.getId().toString())
            .claim("email", user.getEmail())
            .claim("role", user.getRole().name())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
}
```

**JWT Structure:**
```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiZW1haWwiOiJqb2huQGV4YW1wbGUuY29tIn0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
^                      ^                                                          ^
|                      |                                                          |
Header                 Payload (claims)                                          Signature
```

**Decoded:**
```json
// Header
{
  "alg": "HS256",
  "typ": "JWT"
}

// Payload
{
  "sub": "1",
  "email": "john@example.com",
  "role": "CITIZEN",
  "iat": 1704067200,
  "exp": 1704153600
}

// Signature = HMAC-SHA256(header + payload, secret)
```

**3. Token Validation:**
```java
public boolean validateToken(String token) {
    try {
        Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token);
        return true;
    } catch (Exception e) {
        return false;  // Invalid signature, expired, malformed, etc.
    }
}
```

**What Gets Validated?**
- ✅ Signature (HMAC-SHA256 with secret key)
- ✅ Expiration (not expired)
- ✅ Structure (valid JWT format)

---

### File: `JwtAuthenticationFilter.java` - Request Filter

#### Code Structure
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
        String jwt = getJwtFromRequest(request);

        if (jwt != null && jwtTokenProvider.validateToken(jwt)) {
            Long userId = jwtTokenProvider.getUserIdFromToken(jwt);
            String email = jwtTokenProvider.getEmailFromToken(jwt);
            String role = jwtTokenProvider.getRoleFromToken(jwt);

            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    email, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

#### Why This Code?

**1. Extends OncePerRequestFilter:**
```java
extends OncePerRequestFilter
```

**Why OncePerRequestFilter?**
- ✅ **Guarantees:** Executes exactly once per request
- ✅ **Performance:** Prevents duplicate execution
- ✅ **CORS:** Handles OPTIONS preflight correctly

**2. Extract JWT from Header:**
```java
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
               ^      ^
               |      |
               |      JWT token
               Scheme
```

**3. Set Spring Security Authentication:**
```java
UsernamePasswordAuthenticationToken authentication =
    new UsernamePasswordAuthenticationToken(
        email,           // Principal (who)
        null,            // Credentials (not needed, already authenticated)
        List.of(new SimpleGrantedAuthority("ROLE_" + role))  // Authorities (what can do)
    );

SecurityContextHolder.getContext().setAuthentication(authentication);
```

**What This Does:**
- Tells Spring Security: "This request is authenticated"
- User can now access protected endpoints
- Can retrieve user info: `SecurityContextHolder.getContext().getAuthentication()`

**4. Continue Filter Chain:**
```java
filterChain.doFilter(request, response);
```

**Filter Chain:**
```
Request → CORS Filter → JWT Filter → Security Filter → Controller
```

---

## Component 3: Configuration

### Files
```
infrastructure/config/
├── SecurityBeans.java    # Password encoder bean
└── SecurityConfig.java   # Spring Security configuration
```

---

### File: `SecurityConfig.java` - Spring Security

#### Code
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

#### Why This Configuration?

**1. Disable CSRF:**
```java
.csrf(csrf -> csrf.disable())
```

**Why Disable?**
- ✅ **Stateless API:** JWT tokens (no cookies)
- ✅ **Not Vulnerable:** CSRF attacks target cookies
- ✅ **Industry Standard:** REST APIs don't need CSRF

**2. Stateless Session:**
```java
.sessionManagement(session ->
    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
)
```

**Why Stateless?**
- ✅ **JWT-Based:** Token contains all info
- ✅ **Scalable:** No session storage needed
- ✅ **No Cookies:** Pure REST API

**3. Authorization Rules:**
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/register").permitAll()  // Public
    .requestMatchers("/api/auth/login").permitAll()     // Public
    .anyRequest().authenticated()                       // Protected
)
```

**4. Add JWT Filter:**
```java
.addFilterBefore(
    jwtAuthenticationFilter,  // Our custom filter
    UsernamePasswordAuthenticationFilter.class  // Before this standard filter
)
```

---

## Component 4: REST API

### File: `AuthController.java` - REST Endpoints

#### Code Structure
```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) { ... }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) { ... }

    // DTOs
    record RegisterRequest(String name, String email, String password, String role) {}
    record LoginRequest(String email, String password) {}
    record UserResponse(Long id, String name, String email, String role, LocalDateTime createdAt) {}

    // Exception handlers
    @ResponseStatus(HttpStatus.CONFLICT)
    static class EmailAlreadyExistsException extends RuntimeException { ... }
}
```

#### Why This Code?

**1. Depends on Use Case Interfaces:**
```java
private final RegisterUserUseCase registerUserUseCase;  // ← Interface (not UserService!)
```

**Why Interface Dependency?**
- ✅ **Dependency Inversion:** Controller → Domain (via interface)
- ✅ **Testability:** Easy to mock RegisterUserUseCase
- ✅ **Flexibility:** Can swap implementation

**2. @ResponseStatus for Exceptions:**
```java
@ResponseStatus(HttpStatus.CONFLICT)  // 409
static class EmailAlreadyExistsException extends RuntimeException { ... }
```

**Automatic HTTP Status:**
- Spring catches exception
- Sees @ResponseStatus(409)
- Returns 409 Conflict to client

**3. Record for DTOs:**
```java
record RegisterRequest(String name, String email, String password, String role) {}
```

**Why Record?**
- ✅ **Immutable:** Thread-safe
- ✅ **Concise:** No boilerplate
- ✅ **JSON Mapping:** Jackson auto-serializes

---

## Infrastructure Layer Summary

### Component Overview

| Component | Purpose | Files |
|-----------|---------|-------|
| Persistence | Database operations | UserEntity, SpringDataUserRepository, JpaUserRepositoryAdapter |
| Security | JWT + Spring Security | JwtTokenProvider, JwtAuthenticationFilter, SecurityConfig |
| REST API | HTTP endpoints | AuthController |
| Configuration | Spring beans | SecurityBeans |

### Key Characteristics

✅ **Framework-Specific**
- All Spring Boot, JPA, JWT code here
- Domain remains framework-free

✅ **Adapter Pattern**
- JpaUserRepositoryAdapter converts domain ↔ JPA
- Domain never sees UserEntity

✅ **Dependency Inversion**
- Infrastructure depends on domain interfaces
- Not the other way around

---

## Next: Integration Guide

Now that you understand each layer, let's see how they work together!

Continue to: [06-how-it-works-together.md](./06-how-it-works-together.md)
