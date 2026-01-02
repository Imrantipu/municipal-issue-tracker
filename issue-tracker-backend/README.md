# Issue Tracker Backend

Public Infrastructure Issue Reporting System - Spring Boot backend with Hexagonal Architecture.

## 📋 Overview

A comprehensive issue tracking system for public infrastructure complaints. Citizens can report issues (road damage, streetlights, water problems, etc.), staff can manage and resolve them, and admins can oversee the entire system.

**Built for**: Portfolio project demonstrating mid-level software engineering skills for German job market.

## 🛠️ Tech Stack

### Backend
- **Java**: 17
- **Spring Boot**: 4.0.1
- **Database**: PostgreSQL 15
- **ORM**: Spring Data JPA + Hibernate
- **Authentication**: JWT (JSON Web Tokens)
- **Security**: Spring Security 6
- **Build Tool**: Maven
- **Code Generation**: Lombok

### Architecture
- **Pattern**: Hexagonal Architecture (Ports & Adapters)
- **Principles**: SOLID, Clean Code
- **Testing**: JUnit 5, Mockito, TestContainers
- **Target Coverage**: 85%+

### DevOps
- **Containerization**: Docker + Docker Compose
- **Code Review**: CodeRabbit AI
- **Database Migrations**: Flyway (planned)

## 🏗️ Architecture

This project follows **Hexagonal Architecture** (also called Ports and Adapters or Clean Architecture).

### Key Principles
- **Domain Layer**: Pure business logic (no framework dependencies)
- **Application Layer**: Use case implementations (orchestration)
- **Infrastructure Layer**: Technical details (Spring Boot, JPA, REST)

### Benefits
- ✅ Business logic independent of frameworks
- ✅ Easy to test (domain tests run in milliseconds)
- ✅ Can swap database/framework without touching business logic
- ✅ 85%+ test coverage achievable

See [ADR 0001](docs/adr/0001-use-hexagonal-architecture.md) for detailed architectural decision.

## 📁 Project Structure

```
src/main/java/com/issuetracker/
├── domain/                   # Core business logic (framework-independent)
│   ├── model/               # Business entities (User, Issue, Timeline)
│   ├── port/                # Interfaces (contracts)
│   │   ├── in/              # Input ports (use cases)
│   │   └── out/             # Output ports (repositories)
│   └── service/             # Domain services
│
├── application/              # Application layer (orchestration)
│   ├── service/             # Use case implementations
│   ├── dto/                 # Data Transfer Objects
│   └── mapper/              # Domain ↔ DTO conversions
│
└── infrastructure/           # Infrastructure layer (technical details)
    ├── adapter/
    │   ├── in/web/          # REST controllers (Spring MVC)
    │   └── out/persistence/ # Database adapters (JPA)
    ├── config/              # Spring configuration
    └── security/            # JWT, CORS, authentication
```

## 🚀 Getting Started

### Prerequisites

Before running this project, ensure you have:

- **Java 17 or higher** ([Download](https://adoptium.net/))
- **Docker & Docker Compose** ([Download](https://www.docker.com/get-started))
- **Maven 3.8+** (included via Maven Wrapper)

### Quick Start

#### 1. Clone the Repository

```bash
git clone <repository-url>
cd issue-tracker-backend
```

#### 2. Start PostgreSQL Database

```bash
# Start PostgreSQL in Docker
docker-compose up -d

# Verify database is running
docker-compose ps
```

You should see `issuetracker-db` with status `Up (healthy)`.

#### 3. Run the Application

```bash
# Using Maven Wrapper (recommended)
./mvnw spring-boot:run

# OR using installed Maven
mvn spring-boot:run
```

The application will start on **http://localhost:8080**

#### 4. Verify Installation

Open your browser and visit:
- **Health Check**: http://localhost:8080/actuator/health
- **API Documentation**: http://localhost:8080/swagger-ui.html (after adding SpringDoc)

Expected response:
```json
{
  "status": "UP"
}
```

## 🔧 Configuration

### Environment Variables

The application can be configured using environment variables. See [.env.example](.env.example) for all available options.

**Required variables** (with defaults for development):
```bash
# Database
POSTGRES_DB=issuetracker
POSTGRES_USER=dev
POSTGRES_PASSWORD=devpassword

# JWT Secret (use strong random key in production!)
JWT_SECRET=your-256-bit-secret-key-change-this-in-production-please-make-it-very-long
```

### Generate Secure JWT Secret

For production, generate a secure JWT secret:
```bash
openssl rand -base64 32
```

Then set it as an environment variable:
```bash
export JWT_SECRET="<generated-secret>"
```

## 📚 Documentation

### Project Documentation
- [Requirements](docs/requirements.md) - Complete project requirements and features
- [Database Schema](docs/database-schema.md) - Database design with performance optimizations
- [Architecture Decision Records](docs/adr/) - Why we made each technical decision
- [SOLID Principles Guide](docs/solid-principles-guide.md) - How SOLID principles shape our code
- [Folder Structure Evolution](docs/folder-structure-evolution.md) - Understanding different architectures

### API Documentation
- **Swagger UI**: http://localhost:8080/swagger-ui.html (interactive API docs)
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## 🧪 Testing

### Run All Tests

```bash
./mvnw test
```

### Run Tests with Coverage Report

```bash
./mvnw test jacoco:report
```

Coverage report will be generated at: `target/site/jacoco/index.html`

### Test Strategy

- **Unit Tests**: Domain layer (pure Java, no Spring)
- **Integration Tests**: API endpoints + database
- **Test Coverage Target**: 85%+

See [Testing Guide](docs/testing-guide.md) for detailed testing practices.

## 🔒 Security

### Authentication
- JWT-based authentication
- Token expiry: 24 hours
- Password hashing: BCrypt (cost factor: 12)

### CORS Configuration
- Allowed origins: http://localhost:3000 (Next.js frontend)
- Configurable via `cors.allowed-origins` property

### Security Best Practices
- ✅ Never store passwords in plain text
- ✅ Validate all user input
- ✅ Use parameterized queries (prevent SQL injection)
- ✅ Environment variables for secrets
- ✅ HTTPS only in production

See [Security Guide](docs/security-guide.md) for details.

## 🐳 Docker

### Development with Docker

```bash
# Start PostgreSQL only
docker-compose up -d

# Stop PostgreSQL
docker-compose down

# Stop and remove data (⚠️ deletes all data!)
docker-compose down -v
```

### Production Docker Build

```bash
# Build application JAR
./mvnw clean package

# Build Docker image
docker build -t issue-tracker-backend .

# Run container
docker run -p 8080:8080 \
  -e JWT_SECRET="your-secret" \
  -e POSTGRES_URL="jdbc:postgresql://host.docker.internal:5432/issuetracker" \
  issue-tracker-backend
```

## 🎯 Features

### MVP Features (Current)
- [ ] User registration and authentication (JWT)
- [ ] Role-based access control (Admin, Staff, Citizen)
- [ ] Issue creation with categories (Roads, Streetlights, Water, etc.)
- [ ] Issue assignment to staff
- [ ] Issue status tracking (Pending → In Progress → Resolved → Closed)
- [ ] Issue timeline (audit trail of all changes)
- [ ] Dashboard views per role

### Planned Features
- [ ] Email notifications
- [ ] Image upload for issue photos
- [ ] Advanced search and filtering
- [ ] Reporting and analytics
- [ ] Payment integration
- [ ] Mobile app API support

## 📊 Code Quality

### Standards
- **Test Coverage**: 85%+ (target for German market)
- **Code Duplication**: < 2%
- **Cyclomatic Complexity**: < 5 per method
- **Method Length**: < 20 lines
- **JavaDoc**: Required for public APIs

### Tools
- **CodeRabbit AI**: Automated code reviews
- **SonarQube**: Code quality analysis (planned)
- **Checkstyle**: Code style enforcement (planned)

### Run Code Quality Checks

```bash
# Run tests with coverage
./mvnw clean test jacoco:report

# Run CodeRabbit review
coderabbit --prompt-only
```

## 🤝 Contributing

This is a portfolio project, but feedback and suggestions are welcome!

### Development Workflow
1. Create feature branch: `git checkout -b feature/your-feature`
2. Write code following SOLID principles
3. Write tests (aim for 85%+ coverage)
4. Run CodeRabbit review: `coderabbit --prompt-only`
5. Commit with conventional commits: `feat: add user login`
6. Create pull request

### Code Style
- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use Lombok to reduce boilerplate
- Write meaningful variable names (no abbreviations)
- Add JavaDoc for public APIs
- Comment WHY, not WHAT

See [CLAUDE.md](~/.claude/CLAUDE.md) for detailed coding standards.

## 🐛 Troubleshooting

### Common Issues

#### Port 8080 already in use
```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>
```

#### Database connection fails
```bash
# Check if PostgreSQL is running
docker-compose ps

# View PostgreSQL logs
docker-compose logs postgres

# Restart PostgreSQL
docker-compose restart postgres
```

#### Tests failing
```bash
# Clean build and retry
./mvnw clean test

# Run specific test
./mvnw test -Dtest=IssueServiceTest
```

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👤 Author

**Imran Howlader Tipu**
- Email: imrantipu90@yahoo.com
- GitHub: [@Imrantipu](https://github.com/Imrantipu)

## 🙏 Acknowledgments

- **Spring Boot Team** - Excellent framework and documentation
- **Robert C. Martin (Uncle Bob)** - Clean Architecture principles
- **Tom Hombergs** - "Get Your Hands Dirty on Clean Architecture" book
- **German Tech Community** - High engineering standards inspiration

## 📖 Learning Resources

Books and articles that influenced this project:
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html) by Robert C. Martin
- [Get Your Hands Dirty on Clean Architecture](https://leanpub.com/get-your-hands-dirty-on-clean-architecture) by Tom Hombergs
- [Domain-Driven Design Distilled](https://www.amazon.com/Domain-Driven-Design-Distilled-Vaughn-Vernon/dp/0134434420) by Vaughn Vernon
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/) by Alistair Cockburn

---

**Status**: 🚧 In Development (Sprint 1 - Foundation & Authentication)

**Last Updated**: December 26, 2025
