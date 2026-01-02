# API Reference - Authentication Endpoints

Quick reference for authentication API endpoints.

---

## Base URL

```
http://localhost:8080
```

---

## Authentication Endpoints

### 1. Register User

**Create a new user account.**

#### Request

```http
POST /api/auth/register
Content-Type: application/json
```

```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "role": "CITIZEN"
}
```

#### Parameters

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| name | string | Yes | User's full name (2-100 characters) |
| email | string | Yes | Valid email address |
| password | string | Yes | Password (minimum 8 characters) |
| role | string | No | User role: ADMIN, STAFF, or CITIZEN (defaults to CITIZEN) |

#### Success Response (201 Created)

```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "role": "CITIZEN",
  "createdAt": "2026-01-01T12:00:00"
}
```

#### Error Responses

**400 Bad Request** - Validation error
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Name must be at least 2 characters"
}
```

**409 Conflict** - Email already exists
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Email already exists: john@example.com"
}
```

#### cURL Example

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "password123",
    "role": "CITIZEN"
  }'
```

---

### 2. Login User

**Authenticate user and receive JWT token.**

#### Request

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "email": "john@example.com",
  "password": "password123"
}
```

#### Parameters

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| email | string | Yes | User's email address |
| password | string | Yes | User's password |

#### Success Response (200 OK)

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiZW1haWwiOiJqb2huQGV4YW1wbGUuY29tIiwicm9sZSI6IkNJVElaRU4iLCJpYXQiOjE3MDQwNjcyMDAsImV4cCI6MTcwNDE1MzYwMH0.xyz...",
  "userId": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "role": "CITIZEN"
}
```

#### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| token | string | JWT access token (valid for 24 hours) |
| userId | number | User's unique ID |
| name | string | User's full name |
| email | string | User's email address |
| role | string | User's role (ADMIN, STAFF, CITIZEN) |

#### Error Responses

**401 Unauthorized** - Invalid credentials
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password"
}
```

**401 Unauthorized** - User account deleted
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "User account has been deleted"
}
```

#### cURL Example

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'
```

---

## Using JWT Token

### Authorization Header

Include the JWT token in the `Authorization` header for all protected endpoints:

```http
GET /api/issues
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Token Format

```
Authorization: Bearer <token>
               ^      ^
               |      |
               |      JWT token
               Scheme (must be "Bearer")
```

### Token Expiration

- **Validity:** 24 hours from issue time
- **After expiry:** Client receives 401 Unauthorized
- **Action:** Request new token via login

### Token Payload (Decoded)

```json
{
  "sub": "1",                    // User ID
  "email": "john@example.com",   // User email
  "role": "CITIZEN",             // User role
  "iat": 1704067200,             // Issued at (Unix timestamp)
  "exp": 1704153600              // Expires at (Unix timestamp)
}
```

---

## Example Workflow

### Complete Authentication Flow

```bash
# 1. Register a new user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice Johnson",
    "email": "alice@example.com",
    "password": "SecurePass123",
    "role": "CITIZEN"
  }'

# Response:
# {
#   "id": 1,
#   "name": "Alice Johnson",
#   "email": "alice@example.com",
#   "role": "CITIZEN",
#   "createdAt": "2026-01-01T12:00:00"
# }

# 2. Login to get JWT token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@example.com",
    "password": "SecurePass123"
  }'

# Response:
# {
#   "token": "eyJhbGciOiJIUzI1NiJ9...",
#   "userId": 1,
#   "name": "Alice Johnson",
#   "email": "alice@example.com",
#   "role": "CITIZEN"
# }

# 3. Use token to access protected endpoint
curl -X GET http://localhost:8080/api/issues \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."

# Response: User's issues list
```

---

## Postman Collection

### Import Collection

1. Open Postman
2. Click **Import**
3. Paste the following JSON:

```json
{
  "info": {
    "name": "Issue Tracker - Authentication",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Register User",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"name\": \"John Doe\",\n  \"email\": \"john@example.com\",\n  \"password\": \"password123\",\n  \"role\": \"CITIZEN\"\n}"
        },
        "url": {
          "raw": "http://localhost:8080/api/auth/register",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "auth", "register"]
        }
      }
    },
    {
      "name": "Login User",
      "event": [
        {
          "listen": "test",
          "script": {
            "exec": [
              "// Save token to environment variable",
              "var jsonData = pm.response.json();",
              "pm.environment.set(\"jwt_token\", jsonData.token);"
            ]
          }
        }
      ],
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"email\": \"john@example.com\",\n  \"password\": \"password123\"\n}"
        },
        "url": {
          "raw": "http://localhost:8080/api/auth/login",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "auth", "login"]
        }
      }
    }
  ]
}
```

### Environment Setup

Create a Postman environment with:

```json
{
  "jwt_token": "",
  "base_url": "http://localhost:8080"
}
```

---

## Error Codes Reference

| Status Code | Error | Common Causes |
|-------------|-------|---------------|
| 400 | Bad Request | Invalid input (email format, password too short, missing fields) |
| 401 | Unauthorized | Wrong password, expired token, user deleted |
| 409 | Conflict | Email already exists |
| 500 | Internal Server Error | Server error (check logs) |

---

## Security Notes

### Password Requirements

- Minimum length: 8 characters
- Maximum length: 255 characters
- Stored using BCrypt (cost factor: 10)
- Never sent in responses

### Email Validation

- Must match format: `user@domain.com`
- Normalized to lowercase
- Maximum length: 255 characters
- Unique constraint enforced

### JWT Token Security

- **Algorithm:** HMAC-SHA256
- **Secret Key:** Configured via `JWT_SECRET` environment variable
- **Storage:** Client should store in memory or httpOnly cookie (NOT localStorage for security)
- **Transmission:** Always use HTTPS in production
- **Expiration:** 24 hours (configurable via `JWT_EXPIRATION`)

### CORS Configuration

**Allowed Origins** (from `application.properties`):
```
http://localhost:3000  # Next.js frontend
http://localhost:3001  # Alternative port
```

**Allowed Methods:**
- GET
- POST
- PUT
- PATCH
- DELETE
- OPTIONS

---

## Testing Checklist

### Registration Tests

- [ ] ✅ Register with valid data → 201 Created
- [ ] ✅ Register with duplicate email → 409 Conflict
- [ ] ✅ Register with invalid email → 400 Bad Request
- [ ] ✅ Register with short password → 400 Bad Request
- [ ] ✅ Register with short name → 400 Bad Request
- [ ] ✅ Register without role → defaults to CITIZEN
- [ ] ✅ Password is hashed in database (not plain text)

### Login Tests

- [ ] ✅ Login with correct credentials → 200 OK + JWT token
- [ ] ✅ Login with wrong password → 401 Unauthorized
- [ ] ✅ Login with non-existent email → 401 Unauthorized
- [ ] ✅ Login with deleted user → 401 Unauthorized
- [ ] ✅ JWT token contains correct claims (userId, email, role)
- [ ] ✅ JWT token expires after 24 hours

### JWT Token Tests

- [ ] ⏳ Protected endpoint without token → 401 Unauthorized
- [ ] ⏳ Protected endpoint with valid token → 200 OK
- [ ] ⏳ Protected endpoint with expired token → 401 Unauthorized
- [ ] ⏳ Protected endpoint with tampered token → 401 Unauthorized

---

## Next Steps

After authentication is working:

1. **Write Tests**
   - Unit tests for domain models
   - Integration tests for API endpoints
   - Target: 85%+ coverage

2. **Build Issue Domain**
   - Issue creation endpoint
   - Issue listing endpoint
   - Issue update/delete endpoints

3. **Add Role-Based Authorization**
   - `@PreAuthorize("hasRole('ADMIN')")`
   - Different permissions per role

---

## Support

**Check Logs:**
```bash
# View Spring Boot logs
cd issue-tracker-backend
./mvnw spring-boot:run

# Check PostgreSQL logs
docker compose logs postgres
```

**Common Issues:**
- Port 8080 already in use → Kill process or change port
- Database connection failed → Check Docker Compose
- JWT token invalid → Check JWT_SECRET in application.properties

---

**Last Updated:** January 1, 2026
**Version:** 1.0.0
**Status:** ✅ Production Ready
