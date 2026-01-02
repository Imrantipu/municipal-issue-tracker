# Architecture Decision Records (ADRs)

## What Are ADRs?

Architecture Decision Records document important architectural decisions made during the project. Each ADR captures:

- **Context**: What problem are we solving?
- **Decision**: What did we decide to do?
- **Consequences**: What are the trade-offs? (Good and Bad)

## Why ADRs Matter (Especially in Germany)

German engineering culture values:
- **Documentation**: Written reasoning shows systematic thinking
- **Transparency**: Clear explanation of trade-offs demonstrates maturity
- **Knowledge Transfer**: Future developers (or interviewers) understand your choices
- **Accountability**: You own your decisions and can defend them

## ADR Index

| ADR | Title | Status |
|-----|-------|--------|
| [0001](0001-use-hexagonal-architecture.md) | Use Hexagonal Architecture | Accepted |
| [0002](0002-choose-postgresql-over-mongodb.md) | Choose PostgreSQL over MongoDB | Accepted |
| [0003](0003-use-jwt-authentication.md) | Use JWT Authentication | Accepted |
| [0004](0004-choose-nextjs-app-router.md) | Choose Next.js App Router | Accepted |
| [0005](0005-use-tanstack-query-for-state.md) | Use TanStack Query for Server State | Accepted |
| [0006](0006-documentation-strategy.md) | Documentation Strategy for Portfolio | Accepted |

## How to Read ADRs

Start with the **Context** section to understand the problem. Then read the **Decision**. Finally, study the **Consequences** - this shows you understand trade-offs, not just benefits.

## Template

```markdown
# [Number]. [Title]

Date: YYYY-MM-DD

## Status

Accepted | Rejected | Deprecated | Superseded

## Context

What is the issue we're facing? What factors are driving this decision?

## Decision

What are we going to do? Be specific.

## Consequences

### Positive
- What benefits do we gain?

### Negative
- What are the downsides or risks?

### Neutral
- What changes but isn't clearly good or bad?
```
