# 2. Choose PostgreSQL over MongoDB

Date: 2025-12-26

## Status

Accepted

## Context

We need to choose a database for our Public Infrastructure Issue Reporting System.

**Our Data Characteristics:**
- Structured data with clear relationships (User → Issue → Timeline)
- Strong consistency requirements (financial transactions planned for future)
- Complex queries needed (filter by status, category, date ranges, assigned staff)
- GDPR compliance required (European data protection)
- Audit trail requirements (who created/updated what and when)

**Developer Background:**
- Strong experience with MongoDB from MERN stack
- Learning PostgreSQL as part of Spring Boot course
- Familiar with both SQL and NoSQL concepts

**Options Considered:**
1. **MongoDB** (familiar, NoSQL, flexible schema)
2. **PostgreSQL** (relational, ACID, strong consistency)
3. **MySQL** (relational, but PostgreSQL has better JSON support)

## Decision

We will use **PostgreSQL 15+** as our primary database.

### Configuration:
- **Local Development**: PostgreSQL in Docker container
- **Production**: Managed PostgreSQL (AWS RDS or similar)
- **ORM**: Spring Data JPA + Hibernate
- **Migration**: Flyway for version control

## Consequences

### Positive ✅

1. **Data Integrity**
   - Foreign key constraints prevent orphaned records
   - ACID transactions ensure data consistency
   - CASCADE/RESTRICT rules protect data relationships
   - Example: Cannot delete a citizen who has active issues (RESTRICT)

2. **Complex Queries**
   - JOIN operations for related data (Issue + Citizen + Staff in one query)
   - Aggregate functions (COUNT issues by status, AVG resolution time)
   - Window functions for analytics (rank issues by priority)
   - Prevent N+1 query problem with proper joins

3. **GDPR Compliance**
   - Soft delete with `deleted_at` timestamp (required for audit trail)
   - Can permanently delete data after retention period
   - Transaction support for "right to be forgotten" operations
   - Audit columns (created_at, updated_at, created_by, updated_by)

4. **German Market Standard**
   - PostgreSQL very popular in German companies
   - Enterprise-grade reliability expected in German engineering culture
   - Shows understanding of relational database design
   - Common in Spring Boot projects

5. **Future Features**
   - Payment system requires ACID transactions (money operations)
   - Reporting and analytics easier with SQL
   - Full-text search built-in (no need for Elasticsearch for MVP)
   - JSON support available if needed (JSONB column type)

6. **Performance**
   - Excellent query planner and optimizer
   - Index support (B-tree, Hash, GIN, GiST)
   - Materialized views for complex reports
   - Connection pooling with HikariCP

7. **Type Safety**
   - Schema enforces data types
   - CHECK constraints validate enums
   - NOT NULL constraints prevent missing critical data
   - Email format validation at database level

### Negative ❌

1. **Schema Rigidity**
   - Must define schema upfront (cannot just save any JSON)
   - Schema changes require migrations (Flyway scripts)
   - Adding new columns requires ALTER TABLE statements
   - **Mitigation**: We have clear requirements; schema unlikely to change drastically

2. **Learning Curve**
   - Need to learn SQL (though developer knows basics from course)
   - JPA/Hibernate mapping requires understanding
   - N+1 query problem and lazy loading concepts
   - **Mitigation**: Good documentation and course material available

3. **Slightly More Setup**
   - Need to define JPA entities carefully
   - Foreign key relationships must be mapped
   - Migrations must be written and tested
   - **Mitigation**: We use Flyway for automated migrations; worth the effort

4. **Horizontal Scaling Complexity**
   - Harder to scale horizontally than NoSQL
   - Sharding is complex (but we won't need this for MVP or even year 1)
   - **Mitigation**: Not a concern for our scale; vertical scaling sufficient

### Neutral 🔄

1. **Developer Comfort**
   - MongoDB is more familiar to developer
   - PostgreSQL requires learning SQL more deeply
   - BUT: This is actually GOOD for portfolio (shows growth)

2. **JSON Support**
   - PostgreSQL has JSONB (flexible like MongoDB if needed)
   - We don't need it for MVP, but nice to have
   - Can store unstructured data if required later

## Why PostgreSQL Fits Our Requirements

### 1. Data Relationships Are Clear
```
User (Citizen) ─── creates ──→ Issue
User (Staff) ─── assigned to ──→ Issue
Issue ─── has many ──→ Timeline entries
```
This is **relational data**. MongoDB would require manual denormalization or complex lookups.

### 2. Queries We Need to Support
```sql
-- Find all issues for a citizen with timeline (1 query with JOIN)
SELECT i.*, t.*
FROM issues i
LEFT JOIN timeline t ON i.id = t.issue_id
WHERE i.citizen_id = ? AND i.deleted_at IS NULL;

-- Dashboard: Count issues by status
SELECT status, COUNT(*)
FROM issues
WHERE deleted_at IS NULL
GROUP BY status;

-- Admin: Find overdue high-priority issues
SELECT i.*, u.name as citizen_name
FROM issues i
JOIN users u ON i.citizen_id = u.id
WHERE i.priority = 'HIGH'
  AND i.status NOT IN ('RESOLVED', 'CLOSED')
  AND i.created_at < NOW() - INTERVAL '7 days'
  AND i.deleted_at IS NULL;
```

These queries are natural in SQL, complex in MongoDB.

### 3. GDPR Soft Delete
```sql
-- Soft delete (GDPR compliant)
UPDATE users SET deleted_at = NOW() WHERE id = ?;

-- Permanent delete after retention period
DELETE FROM users WHERE deleted_at < NOW() - INTERVAL '2 years';
```

### 4. Future Payment System
When we add payments:
```sql
BEGIN TRANSACTION;
  UPDATE issues SET status = 'CLOSED' WHERE id = ?;
  INSERT INTO payments (issue_id, amount, status) VALUES (?, ?, 'COMPLETED');
COMMIT;
```
ACID guarantees both operations succeed or both fail. Critical for money.

## Comparison Table

| Feature | PostgreSQL | MongoDB |
|---------|-----------|---------|
| **Data Model** | Relational (tables) | Document (collections) |
| **Schema** | Strict (defined upfront) | Flexible (schema-less) |
| **Relationships** | Foreign keys, JOINs | Manual refs or denormalization |
| **Transactions** | Full ACID | Limited ACID (single doc) |
| **Complex Queries** | Excellent (SQL) | Limited (aggregation pipeline) |
| **Consistency** | Strong | Eventual (default) |
| **Scaling** | Vertical (easier) | Horizontal (easier) |
| **GDPR Compliance** | Excellent (audit logs) | Manual implementation |
| **German Market** | Very common | Less common for enterprise |
| **Spring Boot** | Perfect fit (JPA) | Requires custom code |
| **Our Use Case** | ✅ Perfect fit | ❌ Not ideal |

## Migration Path (If Needed)

If we later decide PostgreSQL is too rigid:
1. PostgreSQL has JSONB columns (can store JSON like MongoDB)
2. Can use PostgreSQL as primary, MongoDB for logs/analytics
3. Can migrate using tools like pgloader

But we DON'T expect to need this.

## Implementation Checklist

- [x] Create database schema with relationships (see `docs/database-schema.md`)
- [x] Define indexes for performance
- [x] Add soft delete columns
- [x] Add audit columns (created_at, updated_at)
- [x] Add CHECK constraints for enums
- [ ] Set up Flyway migration scripts
- [ ] Configure Docker Compose with PostgreSQL
- [ ] Set up JPA entities with proper mappings
- [ ] Test foreign key cascade behaviors
- [ ] Implement repository layer with @EntityGraph (prevent N+1)

## References

- [PostgreSQL Official Documentation](https://www.postgresql.org/docs/)
- [Spring Data JPA Documentation](https://spring.io/projects/spring-data-jpa)
- [Flyway Database Migrations](https://flywaydb.org/)
- [PostgreSQL vs MongoDB: When to Use](https://www.postgresql.org/about/)
