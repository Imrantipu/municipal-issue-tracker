# Architecture Flow - How Requests Work

This document explains how user registration and login requests flow through the Hexagonal Architecture layers.

---

## Registration Flow

### Request Journey: Client → Database

```
┌─────────────────────────────────────────────────────────────────────┐
│                         REGISTRATION FLOW                            │
└─────────────────────────────────────────────────────────────────────┘

1. CLIENT (Postman/Frontend)
   ↓
   POST /api/auth/register
   {
     "name": "John Doe",
     "email": "john@example.com",
     "password": "password123",
     "role": "CITIZEN"
   }
   ↓
2. SPRING SECURITY (SecurityConfig.java)
   ├─ CORS check: Is origin allowed?
   ├─ CSRF disabled (stateless JWT)
   └─ Authorization: /api/auth/register is permitAll() ✅
   ↓
3. INFRASTRUCTURE - REST CONTROLLER (AuthController.java)
   ├─ @PostMapping("/register")
   ├─ Parse JSON → RegisterRequest DTO
   ├─ Create RegisterUserCommand
   └─ Call: registerUserUseCase.registerUser(command)
   ↓
4. APPLICATION - SERVICE (UserService.java)
   ├─ Check email exists? → userRepository.existsByEmail(email)
   ├─ Create User domain object
   ├─ Call: user.validate() → Domain validation
   ├─ Hash password: passwordEncoder.encode(password)
   └─ Save: userRepository.save(user)
   ↓
5. INFRASTRUCTURE - REPOSITORY ADAPTER (JpaUserRepositoryAdapter.java)
   ├─ Convert: User (domain) → UserEntity (JPA)
   ├─ Call: springDataRepository.save(entity)
   └─ Convert back: UserEntity → User (domain)
   ↓
6. DATABASE (PostgreSQL)
   ├─ INSERT INTO users (name, email, password, role, ...)
   └─ RETURN id=1, created_at=...
   ↓
7. RESPONSE BACK TO CLIENT
   ↓
   UserService returns User (domain)
   ↓
   AuthController converts to UserResponse (DTO)
   ↓
   Spring MVC serializes to JSON
   ↓
   CLIENT receives:
   {
     "id": 1,
     "name": "John Doe",
     "email": "john@example.com",
     "role": "CITIZEN",
     "createdAt": "2026-01-01T12:00:00"
   }
```

---

## Login Flow

### Request Journey: Client → JWT Token

```
┌─────────────────────────────────────────────────────────────────────┐
│                            LOGIN FLOW                                │
└─────────────────────────────────────────────────────────────────────┘

1. CLIENT
   ↓
   POST /api/auth/login
   {
     "email": "john@example.com",
     "password": "password123"
   }
   ↓
2. SPRING SECURITY
   └─ Authorization: /api/auth/login is permitAll() ✅
   ↓
3. INFRASTRUCTURE - REST CONTROLLER (AuthController.java)
   ├─ Parse JSON → LoginRequest DTO
   ├─ Create LoginCommand
   └─ Call: loginUserUseCase.login(command)
   ↓
4. APPLICATION - SERVICE (UserService.java)
   ├─ Find user: userRepository.findByEmail(email)
   ├─ Check if user exists? (throw InvalidCredentialsException if not)
   ├─ Check if user is deleted? (throw UserDeletedException if yes)
   ├─ Verify password: passwordEncoder.matches(plainPassword, hashedPassword)
   ├─ Generate JWT: jwtTokenProvider.generateToken(user)
   └─ Return AuthResponse(token, userId, name, email, role)
   ↓
5. JWT TOKEN PROVIDER (JwtTokenProvider.java)
   ├─ Create JWT Claims:
   │  - sub: userId
   │  - email: user.email
   │  - role: user.role
   │  - iat: now
   │  - exp: now + 24 hours
   ├─ Sign with HMAC-SHA256 + secret key
   └─ Return token: "eyJhbGciOiJIUzI1NiJ9..."
   ↓
6. RESPONSE TO CLIENT
   {
     "token": "eyJhbGciOiJIUzI1NiJ9...",
     "userId": 1,
     "name": "John Doe",
     "email": "john@example.com",
     "role": "CITIZEN"
   }
```

---

## Protected Endpoint Flow

### How JWT Authentication Works on Protected Routes

```
┌─────────────────────────────────────────────────────────────────────┐
│                    PROTECTED ENDPOINT FLOW                           │
└─────────────────────────────────────────────────────────────────────┘

1. CLIENT
   ↓
   GET /api/issues  (example protected endpoint)
   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
   ↓
2. SPRING SECURITY FILTER CHAIN
   ↓
3. JWT AUTHENTICATION FILTER (JwtAuthenticationFilter.java)
   ├─ Extract JWT from Authorization header
   ├─ Remove "Bearer " prefix
   ├─ Validate token: jwtTokenProvider.validateToken(token)
   │  ├─ Check signature (HMAC-SHA256)
   │  └─ Check expiration
   ├─ Extract claims:
   │  ├─ userId = jwtTokenProvider.getUserIdFromToken(token)
   │  ├─ email = jwtTokenProvider.getEmailFromToken(token)
   │  └─ role = jwtTokenProvider.getRoleFromToken(token)
   ├─ Create Spring Security Authentication:
   │  └─ UsernamePasswordAuthenticationToken(email, null, [ROLE_CITIZEN])
   └─ Set in SecurityContext: SecurityContextHolder.setAuthentication(...)
   ↓
4. SECURITY CONFIG (SecurityConfig.java)
   ├─ Check authorization rules
   ├─ Endpoint requires authentication? YES ✅
   └─ User is authenticated? YES (from JWT) ✅
   ↓
5. CONTROLLER
   └─ Request proceeds to controller method
   └─ Can access user via: SecurityContextHolder.getContext().getAuthentication()
```

---

## Error Flow Examples

### Scenario 1: Email Already Exists (Registration)

```
Client sends: POST /api/auth/register
↓
AuthController
↓
UserService.registerUser()
├─ userRepository.existsByEmail("john@example.com") → TRUE
└─ throw new EmailAlreadyExistsException("Email already exists: john@example.com")
↓
AuthController catches exception
↓
@ResponseStatus(HttpStatus.CONFLICT)  // 409
↓
Client receives:
{
  "status": 409,
  "error": "Conflict",
  "message": "Email already exists: john@example.com"
}
```

### Scenario 2: Invalid Password (Login)

```
Client sends: POST /api/auth/login
↓
AuthController
↓
UserService.login()
├─ userRepository.findByEmail("john@example.com") → Found
├─ user.isDeleted() → FALSE ✅
├─ passwordEncoder.matches("wrong", hashedPassword) → FALSE ❌
└─ throw new InvalidCredentialsException()
↓
AuthController catches exception
↓
@ResponseStatus(HttpStatus.UNAUTHORIZED)  // 401
↓
Client receives:
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password"
}
```

### Scenario 3: Expired JWT Token

```
Client sends: GET /api/issues
Authorization: Bearer <expired-token>
↓
JwtAuthenticationFilter
├─ Extract token
├─ jwtTokenProvider.validateToken(token)
│  └─ Token expired ❌
└─ Do NOT set authentication (SecurityContext remains empty)
↓
SecurityConfig
├─ Endpoint requires authentication? YES
├─ User authenticated? NO ❌
└─ Reject request
↓
Client receives:
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource"
}
```

---

## Data Flow: Domain vs Infrastructure

### User Object Transformations

```
┌─────────────────────────────────────────────────────────────────────┐
│                     DOMAIN ↔ INFRASTRUCTURE                          │
└─────────────────────────────────────────────────────────────────────┘

CLIENT (JSON)
   ↓
   {
     "name": "John Doe",
     "email": "john@example.com",
     "password": "password123"
   }
   ↓
CONTROLLER (DTO)
   ↓
   RegisterRequest(name, email, password, role)
   ↓
SERVICE (DOMAIN)
   ↓
   User {
     id: null,
     name: "John Doe",
     email: "john@example.com",
     password: "$2a$10$..." (hashed),
     role: Role.CITIZEN,
     createdAt: 2026-01-01T12:00:00,
     updatedAt: 2026-01-01T12:00:00,
     deletedAt: null
   }
   ↓
REPOSITORY ADAPTER (JPA)
   ↓
   UserEntity {
     @Id id: null → Generated by DB,
     @Column name: "John Doe",
     @Column email: "john@example.com",
     @Column password: "$2a$10$...",
     @Enumerated role: Role.CITIZEN,
     ...
   }
   ↓
DATABASE (PostgreSQL)
   ↓
   INSERT INTO users (name, email, password, role, ...)
   RETURNING id=1, created_at=...
   ↓
BACK UP THE CHAIN
   ↓
   UserEntity (with id=1)
   ↓
   User (domain, with id=1)
   ↓
   UserResponse (DTO, no password)
   ↓
   JSON {
     "id": 1,
     "name": "John Doe",
     "email": "john@example.com",
     "role": "CITIZEN",
     "createdAt": "2026-01-01T12:00:00"
   }
```

---

## Layer Dependencies

### Dependency Direction (Hexagonal Architecture)

```
┌─────────────────────────────────────────────────────────────────────┐
│                         DEPENDENCY FLOW                              │
└─────────────────────────────────────────────────────────────────────┘

                    ┌─────────────────┐
                    │     DOMAIN      │
                    │  (Pure Logic)   │
                    │                 │
                    │  - User.java    │
                    │  - Role.java    │
                    │  - Interfaces   │
                    └────────▲────────┘
                             │
                             │ implements
                             │
                    ┌────────┴────────┐
                    │  APPLICATION    │
                    │ (Orchestration) │
                    │                 │
                    │ - UserService   │
                    └────────▲────────┘
                             │
                             │ uses
                             │
                    ┌────────┴────────┐
                    │ INFRASTRUCTURE  │
                    │ (Tech Details)  │
                    │                 │
                    │ - Controllers   │
                    │ - JPA Entities  │
                    │ - Security      │
                    └─────────────────┘

KEY PRINCIPLE: Dependency Inversion
- Infrastructure DEPENDS ON Domain (via interfaces)
- Domain DOES NOT know about Infrastructure
- Can swap PostgreSQL → MongoDB without touching Domain
```

---

## Summary

### Key Takeaways

1. **Request Flow:** Client → Security → Controller → Service → Repository → Database
2. **Response Flow:** Database → Repository → Service → Controller → Client
3. **Dependency Direction:** Infrastructure → Application → Domain (never reverse)
4. **Data Transformation:** JSON → DTO → Domain → JPA Entity → SQL
5. **Error Handling:** Exceptions propagate up, caught by controller, converted to HTTP status

### Why This Architecture?

✅ **Clear separation** - Each layer has distinct responsibility
✅ **Testable** - Domain can be tested without Spring Boot
✅ **Maintainable** - Changes in one layer don't affect others
✅ **Flexible** - Easy to swap implementations (database, security, etc.)
✅ **SOLID** - Follows all 5 SOLID principles
