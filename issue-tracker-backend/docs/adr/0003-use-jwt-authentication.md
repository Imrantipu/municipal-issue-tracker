# 3. Use JWT Authentication

Date: 2025-12-26

## Status

Accepted

## Context

We need to implement authentication and authorization for three user roles:
- **Admin**: Full system access (manage users, view all issues, system configuration)
- **Staff**: Assigned issues, update status, view citizen details
- **Citizen**: Create issues, view own issues, update own profile

**Requirements from Specification:**
- Login: Email/Password (+ Google Sign-in for future)
- Registration: Name, Email, Password, Photo upload
- No email verification needed (MVP simplification)
- Role-based access control (different dashboards per role)

**Options Considered:**
1. **Session-based Authentication** (cookies, server-side sessions)
2. **JWT (JSON Web Tokens)** (stateless, token-based)
3. **OAuth 2.0** (third-party, complex for MVP)

## Decision

We will use **JWT (JSON Web Tokens)** with Spring Security 6.

### Authentication Flow:

```
┌──────────┐                    ┌──────────┐                    ┌──────────┐
│  Client  │                    │  Server  │                    │ Database │
└────┬─────┘                    └────┬─────┘                    └────┬─────┘
     │                               │                               │
     │ 1. POST /api/auth/login       │                               │
     │    { email, password }        │                               │
     ├──────────────────────────────>│                               │
     │                               │                               │
     │                               │ 2. Find user by email         │
     │                               ├──────────────────────────────>│
     │                               │                               │
     │                               │ 3. User record                │
     │                               │<──────────────────────────────┤
     │                               │                               │
     │                               │ 4. Verify password (BCrypt)   │
     │                               │                               │
     │                               │ 5. Generate JWT token         │
     │                               │    (contains: id, email, role)│
     │                               │                               │
     │ 6. { token: "eyJhbG...", }    │                               │
     │<──────────────────────────────┤                               │
     │                               │                               │
     │ 7. Store token in localStorage│                               │
     │                               │                               │
     │ 8. GET /api/issues            │                               │
     │    Header: Authorization:     │                               │
     │    Bearer eyJhbG...           │                               │
     ├──────────────────────────────>│                               │
     │                               │                               │
     │                               │ 9. Verify JWT signature       │
     │                               │    Extract user info from token│
     │                               │    Check role permissions     │
     │                               │                               │
     │                               │ 10. Fetch issues (if authorized)
     │                               ├──────────────────────────────>│
     │                               │                               │
     │                               │ 11. Issue data                │
     │                               │<──────────────────────────────┤
     │                               │                               │
     │ 12. Issue data                │                               │
     │<──────────────────────────────┤                               │
     │                               │                               │
```

### JWT Token Structure:

```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "123",           // User ID
    "email": "user@example.com",
    "role": "CITIZEN",
    "iat": 1703600000,      // Issued at timestamp
    "exp": 1703686400       // Expiration timestamp (24 hours)
  },
  "signature": "HMACSHA256(base64UrlEncode(header) + '.' + base64UrlEncode(payload), secret)"
}
```

### Implementation:
- **Library**: Spring Security 6 + `jjwt` (Java JWT library)
- **Secret Key**: Stored in environment variable (NOT in code)
- **Token Expiry**: 24 hours for access token
- **Password Hashing**: BCrypt (Spring Security default)
- **CORS**: Configured for Next.js frontend (http://localhost:3000)

## Consequences

### Positive ✅

1. **Stateless Server**
   - No session storage on server (scales horizontally easily)
   - Server doesn't need to remember who is logged in
   - Each request is self-contained (token has all info needed)
   - Perfect for REST API architecture

2. **Mobile-Friendly**
   - Works seamlessly with mobile apps (just send token in header)
   - No cookie issues with native apps
   - Future-proof if we build Android/iOS app

3. **Microservices-Ready**
   - Token can be verified by multiple services independently
   - No shared session store needed
   - Each microservice validates token with same secret key
   - (Though we're not using microservices for MVP)

4. **Performance**
   - No database lookup on every request to check session
   - Token validation is fast (just verify signature)
   - Reduces database load significantly

5. **Standard in Modern Apps**
   - German companies use JWT widely for REST APIs
   - Shows understanding of modern authentication
   - Common in Spring Boot + React/Next.js stacks

6. **Developer Experience**
   - Easy to test (just include token in Postman/Insomnia)
   - Easy to debug (token payload is readable - base64 encoded, not encrypted)
   - Frontend can decode token to show user info (name, role) without API call

7. **Role-Based Access Control (RBAC)**
   - Token contains role (ADMIN, STAFF, CITIZEN)
   - Spring Security checks role before method execution
   - Example: `@PreAuthorize("hasRole('ADMIN')")` on controller methods

### Negative ❌

1. **Token Revocation is Hard**
   - Once issued, token is valid until expiry
   - Cannot "logout" user server-side (token still works until it expires)
   - **Mitigation**: Short expiry time (24 hours), client-side token deletion

2. **Secret Key Management**
   - Must keep secret key VERY secure
   - If leaked, attacker can forge tokens
   - **Mitigation**: Environment variable, rotate key periodically, use strong random key

3. **Token Size**
   - JWT tokens are larger than session IDs (~200-500 bytes vs 32 bytes)
   - Sent with every request (slight bandwidth overhead)
   - **Mitigation**: Not a problem for modern networks; keep payload minimal

4. **No Server-Side User State**
   - Cannot track "who is currently online"
   - Cannot force logout (must wait for token expiry)
   - **Mitigation**: Acceptable for MVP; can add Redis blacklist later if needed

5. **XSS Vulnerability**
   - If token stored in localStorage, vulnerable to XSS attacks
   - Malicious JavaScript can steal token
   - **Mitigation**:
     - Sanitize all user input (prevent XSS)
     - Use `httpOnly` cookies (but harder with Next.js SSR)
     - Content Security Policy headers

### Neutral 🔄

1. **Refresh Token Strategy**
   - For MVP: Single 24-hour access token (simple)
   - For Production: Access token (15 min) + Refresh token (7 days)
   - We start simple, can add refresh tokens later

2. **Storage Location**
   - localStorage: Easy, but XSS vulnerable
   - httpOnly cookie: More secure, but CSRF considerations
   - For MVP: localStorage with XSS prevention measures

## Why JWT Fits Our Requirements

### 1. Three User Roles
JWT payload includes role:
```java
// Spring Security automatically checks role from token
@GetMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")  // Only ADMIN can access
public ResponseEntity<List<User>> getAllUsers() {
    // ...
}

@GetMapping("/api/staff/assigned-issues")
@PreAuthorize("hasRole('STAFF')")  // Only STAFF can access
public ResponseEntity<List<Issue>> getAssignedIssues() {
    Long staffId = getCurrentUserId(); // from JWT token
    // ...
}

@GetMapping("/api/citizen/my-issues")
@PreAuthorize("hasRole('CITIZEN')")  // Only CITIZEN can access
public ResponseEntity<List<Issue>> getMyIssues() {
    Long citizenId = getCurrentUserId(); // from JWT token
    // ...
}
```

### 2. Frontend Integration (Next.js)
```typescript
// Login - store token
const login = async (email: string, password: string) => {
  const response = await fetch('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  });
  const { token } = await response.json();
  localStorage.setItem('token', token);
};

// Subsequent requests - send token
const fetchIssues = async () => {
  const token = localStorage.getItem('token');
  const response = await fetch('/api/issues', {
    headers: {
      'Authorization': `Bearer ${token}`,
    },
  });
  return response.json();
};
```

### 3. Password Security
- Passwords hashed with BCrypt (Spring Security default)
- Cost factor: 12 (good balance of security and performance)
- Salted automatically (each password has unique salt)

## Security Considerations

### 1. Token Secret
```properties
# application.properties (NOT in Git)
jwt.secret=${JWT_SECRET}  # 256-bit random key from environment

# Generate strong secret:
# openssl rand -base64 32
```

### 2. HTTPS Only (Production)
- JWT must ONLY be sent over HTTPS
- HTTP exposes token to network sniffing
- Enforce HTTPS in production deployment

### 3. Token Expiry
- Access token: 24 hours (MVP)
- Expired tokens automatically rejected by Spring Security
- Client redirects to login on 401 Unauthorized

### 4. Input Validation
- Email format validation (prevent injection)
- Password strength requirements (min 8 characters)
- Sanitize all inputs (prevent XSS)

## Session-Based vs JWT Comparison

| Feature | Session-Based | JWT |
|---------|---------------|-----|
| **Storage** | Server (Redis/DB) | Client (localStorage) |
| **Scalability** | Need shared session store | Stateless, scales easily |
| **Logout** | Easy (delete session) | Hard (token valid until expiry) |
| **Mobile Apps** | Difficult (cookies) | Easy (Authorization header) |
| **Microservices** | Need shared session | Independent verification |
| **Token Size** | Small (32 bytes) | Larger (200-500 bytes) |
| **Database Load** | Check session on each request | No DB lookup |
| **German Market** | Older apps | Modern REST APIs ✅ |
| **Spring Boot** | Supported | Perfect fit ✅ |

## Implementation Checklist

Sprint 1 (Week 1):
- [ ] Add Spring Security 6 dependency
- [ ] Add `jjwt` library (JWT creation/validation)
- [ ] Create JWT utility class (generate, validate, extract claims)
- [ ] Configure Spring Security (disable session, enable JWT filter)
- [ ] Create AuthController (login, register endpoints)
- [ ] Hash passwords with BCrypt
- [ ] Add role-based method security (@PreAuthorize)
- [ ] Configure CORS for Next.js frontend
- [ ] Write unit tests for JWT utility
- [ ] Write integration tests for auth endpoints
- [ ] Document API endpoints (Swagger)

## Future Enhancements (Post-MVP)

1. **Refresh Token**
   - Add refresh token with 7-day expiry
   - Rotate tokens for better security

2. **Token Blacklist** (if needed)
   - Redis cache for invalidated tokens
   - Enable server-side logout

3. **OAuth 2.0 / Google Sign-In**
   - Add social login (already mentioned in spec)
   - Use Spring Security OAuth 2.0 Client

4. **Two-Factor Authentication (2FA)**
   - For Admin accounts (high security)

## References

- [Spring Security 6 Documentation](https://spring.io/projects/spring-security)
- [JWT.io - Introduction to JWT](https://jwt.io/introduction)
- [jjwt - Java JWT Library](https://github.com/jwtk/jjwt)
- [OWASP - JWT Security Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_for_Java_Cheat_Sheet.html)
