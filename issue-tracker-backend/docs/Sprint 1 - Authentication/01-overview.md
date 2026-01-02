# Sprint 1 - Authentication System

## Overview

**Completed:** January 1, 2026
**Architecture:** Hexagonal Architecture (Ports & Adapters)
**Technologies:** Spring Boot 4.0.1, Spring Security 6, JWT, PostgreSQL, BCrypt

---

## What We Built

A complete JWT-based authentication system with:
- ✅ User registration (email/password)
- ✅ User login (JWT token generation)
- ✅ Password hashing (BCrypt)
- ✅ JWT token validation on requests
- ✅ Role-based access control (ADMIN, STAFF, CITIZEN)
- ✅ Soft delete support (GDPR compliance)

---

## Files Created (19 Files)

### Domain Layer (5 files) - Pure Business Logic
```
domain/
├── model/
│   ├── Role.java                           # User roles enum
│   └── User.java                           # User domain model
└── port/
    ├── in/
    │   ├── RegisterUserUseCase.java        # Registration interface
    │   └── LoginUserUseCase.java           # Login interface
    └── out/
        └── UserRepository.java             # Repository interface
```

### Application Layer (1 file) - Orchestration
```
application/
└── service/
    └── UserService.java                    # Implements use cases
```

### Infrastructure Layer (13 files) - Technical Details
```
infrastructure/
├── adapter/
│   ├── in/web/
│   │   └── AuthController.java             # REST endpoints
│   └── out/persistence/
│       ├── UserEntity.java                 # JPA entity
│       ├── SpringDataUserRepository.java   # Spring Data JPA
│       └── JpaUserRepositoryAdapter.java   # Adapter pattern
├── config/
│   ├── SecurityBeans.java                  # Password encoder bean
│   └── SecurityConfig.java                 # Spring Security config
└── security/
    ├── JwtTokenProvider.java               # JWT generation/validation
    └── JwtAuthenticationFilter.java        # JWT validation filter
```

---

## Key Features

### 1. User Registration
- **Endpoint:** `POST /api/auth/register`
- **Flow:** Validate → Hash password → Save to DB
- **Security:** BCrypt password hashing (cost factor: 10)

### 2. User Login
- **Endpoint:** `POST /api/auth/login`
- **Flow:** Verify password → Generate JWT token
- **Token:** 24-hour expiry, contains userId, email, role

### 3. Protected Routes
- **Mechanism:** JWT validation on every request
- **Flow:** Extract token → Validate → Set authentication

### 4. Database
- **Table:** `users`
- **Soft Delete:** `deleted_at` column for GDPR compliance
- **Indexes:** email (unique), role, deleted_at

---

## Architecture Benefits

### Why Hexagonal Architecture?

**1. Testability**
- Domain tests run in milliseconds (no Spring Boot needed)
- Easy to mock dependencies (interfaces, not concrete classes)
- 85%+ test coverage achievable

**2. Maintainability**
- Clear separation: Domain → Application → Infrastructure
- Business logic isolated from framework code
- Easy to understand code organization

**3. Flexibility**
- Can swap PostgreSQL → MongoDB (change infrastructure only)
- Can swap JWT → OAuth (change infrastructure only)
- Domain remains unchanged

**4. SOLID Principles**
- **S**ingle Responsibility: Each class has one job
- **O**pen/Closed: Extend via interfaces, not modification
- **L**iskov Substitution: Interfaces are substitutable
- **I**nterface Segregation: Small, focused interfaces
- **D**ependency Inversion: Depend on abstractions

---

## Testing Status

| Component | Status | Coverage |
|-----------|--------|----------|
| Domain Model (User.java) | ⏳ Pending | 0% |
| UserService | ⏳ Pending | 0% |
| Auth Endpoints | ⏳ Pending | 0% |
| **Overall** | **⏳ Pending** | **0%** |

**Next Step:** Write tests to reach 85%+ coverage target.

---

## API Endpoints

### Register User
```bash
POST /api/auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "role": "CITIZEN"  # Optional, defaults to CITIZEN
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "role": "CITIZEN",
  "createdAt": "2026-01-01T12:00:00"
}
```

### Login User
```bash
POST /api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "role": "CITIZEN"
}
```

---

## Database Schema

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,  -- BCrypt hashed
    role VARCHAR(20) NOT NULL,       -- ADMIN, STAFF, CITIZEN
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP DEFAULT NULL  -- Soft delete
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_deleted_at ON users(deleted_at);
```

---

## Next Steps

1. **Write Tests** (Priority 1)
   - Unit tests for User domain model
   - Integration tests for auth endpoints
   - Target: 85%+ coverage

2. **Build Issue Domain** (Sprint 2)
   - Create Issue entity
   - CRUD operations
   - Timeline tracking

3. **Update Documentation**
   - Add sequence diagrams
   - Document error handling
   - Add troubleshooting guide

---

## Related Documentation

- [02-architecture-flow.md](./02-architecture-flow.md) - Request flow diagrams
- [03-domain-layer.md](./03-domain-layer.md) - Domain layer explained
- [04-application-layer.md](./04-application-layer.md) - Application layer explained
- [05-infrastructure-layer.md](./05-infrastructure-layer.md) - Infrastructure layer explained
- [06-how-it-works-together.md](./06-how-it-works-together.md) - Integration guide
- [07-api-reference.md](./07-api-reference.md) - Complete API documentation
