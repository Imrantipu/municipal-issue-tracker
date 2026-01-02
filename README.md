# Municipal Issue Tracker

A full-stack web application for citizens to report and track municipal issues (e.g., broken streetlights, potholes, sanitation problems) with role-based access control and automated notifications.

## Project Overview

This is a **production-grade portfolio project** demonstrating enterprise software development practices, clean architecture, and comprehensive testing. Built as part of a 7-sprint development plan following Agile methodology.

### Key Features

- **Citizen Portal**: Report and track municipal issues
- **Staff Dashboard**: Manage and resolve issues
- **Admin Panel**: User management and system analytics
- **Real-time Notifications**: Email and SMS updates
- **SLA Tracking**: Automated priority-based escalation
- **Mobile-First Design**: Responsive UI for all devices

## Tech Stack

### Backend (Completed)
- **Framework**: Spring Boot 4.0.1
- **Language**: Java 17
- **Architecture**: Hexagonal (Ports & Adapters)
- **Database**: PostgreSQL 15
- **Security**: Spring Security + JWT
- **Testing**: JUnit 5, Mockito, TestContainers
- **Build Tool**: Maven

### Frontend (Planned - Sprint 4-6)
- **Framework**: Next.js 14 (App Router)
- **Language**: TypeScript
- **State Management**: TanStack Query
- **UI Library**: shadcn/ui + Tailwind CSS
- **Testing**: Vitest, React Testing Library

### DevOps
- **Containerization**: Docker + Docker Compose
- **CI/CD**: GitHub Actions (planned)
- **Code Review**: CodeRabbit AI

## Project Structure

```text
municipal-issue-tracker/
├── docs/                          # Comprehensive project documentation
│   ├── Sprint 1 - Authentication/
│   ├── Sprint 2 - Issue Management/
│   ├── adr/                       # Architecture Decision Records
│   └── ...
├── issue-tracker-backend/         # Spring Boot backend
│   ├── src/
│   │   ├── main/java/com/issuetracker/
│   │   │   ├── domain/            # Core business logic (framework-independent)
│   │   │   ├── application/       # Use cases & services
│   │   │   └── infrastructure/    # Adapters (web, persistence, security)
│   │   └── test/                  # 166+ comprehensive tests
│   └── pom.xml
├── issue-tracker-frontend/        # Next.js frontend (Sprint 4-6)
├── docker-compose.yml             # PostgreSQL setup
└── README.md
```

## Development Progress

### ✅ Sprint 1: Authentication System (Completed)
- JWT-based authentication
- Role-based access control (CITIZEN, STAFF, ADMIN)
- User registration and login
- **54 tests** - 100% coverage

### ✅ Sprint 2: Issue Management (Completed)
- Full CRUD operations for issues
- Status lifecycle management (OPEN → IN_PROGRESS → RESOLVED → CLOSED)
- Priority-based categorization
- SLA tracking with timestamps
- **112 tests** - 100% coverage

### 📅 Sprint 3: Timeline & Dashboards (In Progress)
- Timeline/audit trail tracking
- Dashboard statistics APIs
- Staff management endpoints
- Infrastructure layer testing (85%+ overall coverage goal)

### 📅 Sprint 4-6: Frontend Development (Planned)
- Next.js application
- Responsive UI components
- Real-time updates
- Dashboard analytics

### 📅 Sprint 7: Deployment (Planned)
- AWS/Render deployment
- CI/CD pipeline
- Production monitoring

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL 15 (or use Docker)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/Imrantipu/municipal-issue-tracker.git
   cd municipal-issue-tracker
   ```

2. **Start PostgreSQL** (using Docker)
   ```bash
   docker-compose up -d
   ```

3. **Configure environment**
   ```bash
   cd issue-tracker-backend
   cp .env.example .env
   # Edit .env with your database credentials
   ```

4. **Build and run the backend**
   ```bash
   cd issue-tracker-backend
   ./mvnw clean install
   ./mvnw spring-boot:run
   ```

5. **Access the API**
   - Backend API: `http://localhost:8080`
   - API Documentation: `http://localhost:8080/swagger-ui.html` (planned)

### Running Tests

```bash
cd issue-tracker-backend

# Run all tests
./mvnw test

# Run with coverage report
./mvnw test jacoco:report

# View coverage report (platform-specific)
# macOS:
open target/site/jacoco/index.html
# Linux:
xdg-open target/site/jacoco/index.html
# Windows:
start target/site/jacoco/index.html
# Or navigate to: file:///<project-path>/target/site/jacoco/index.html
```

**Current Test Coverage**: 166+ tests, 100% domain & application layers

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login

### Issues
- `POST /api/issues` - Create issue
- `GET /api/issues` - List issues (with filters)
- `GET /api/issues/{id}` - Get issue details
- `PUT /api/issues/{id}` - Update issue
- `PATCH /api/issues/{id}/assign` - Assign to staff
- `PATCH /api/issues/{id}/status` - Change status
- `DELETE /api/issues/{id}` - Soft delete

See [API Documentation](./docs/Sprint%202%20-%20Issue%20Management/03-api-endpoints.md) for detailed request/response examples.

## Architecture

This project follows **Hexagonal Architecture** (Ports & Adapters):

- **Domain Layer**: Pure business logic (no framework dependencies)
- **Application Layer**: Use cases and services
- **Infrastructure Layer**: Adapters for web, database, and external services

**Design Patterns Used**: 13 patterns including Repository, Adapter, Factory, Builder, Strategy, and more.

See [Architecture Documentation](./docs/architecture-explained.md) for details.

## Documentation

Comprehensive documentation available in [`/docs`](./docs):

- [Sprint 1 - Authentication System](./docs/Sprint%201%20-%20Authentication/)
- [Sprint 2 - Issue Management](./docs/Sprint%202%20-%20Issue%20Management/)
- [Architecture Decision Records (ADRs)](./docs/adr/)
- [Testing Strategy](./docs/Test-Strategy.md)
- [Database Schema](./docs/database-schema.md)
- [Interview Preparation Guide](./docs/interview-prep-guide.md)

## Contributing

This is a portfolio project, but feedback and suggestions are welcome!

### Development Workflow
1. Create feature branch: `git checkout -b feature/your-feature`
2. Make changes and write tests
3. Commit: `git commit -m "feat: your feature"`
4. Push: `git push origin feature/your-feature`
5. Create Pull Request
6. CodeRabbit will review automatically
7. Merge after approval

## Code Quality

- **Test Coverage**: 85%+ overall (100% domain/application layers)
- **Code Review**: Automated with CodeRabbit
- **Static Analysis**: SonarLint (planned)
- **Security**: OWASP dependency check (planned)

## License

This project is for educational and portfolio purposes.

## Author

**Imran** - [GitHub](https://github.com/Imrantipu)

Building this project to demonstrate enterprise Java development skills for German job market.

## Acknowledgments

- Hexagonal Architecture pattern by Alistair Cockburn
- Spring Boot documentation and community
- Clean Architecture principles by Robert C. Martin
