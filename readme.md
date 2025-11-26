# WMS Employee Service

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17.2-blue.svg)](https://www.postgresql.org/)
[![Auth0](https://img.shields.io/badge/Auth0-2.25.0-red.svg)](https://auth0.com/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

A Spring Boot microservice for managing employee data and authentication, part of a larger warehouse management system (
WMS). Features stateless JWT authentication with Auth0, modular architecture, and comprehensive API endpoints for
employee management.

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [Available Commands](#available-commands)
- [API Documentation](#api-documentation)
- [Authentication](#authentication)
- [Project Structure](#project-structure)
- [Configuration](#configuration)
- [Testing](#testing)
- [Project Status](#project-status)
- [Contributing](#contributing)
- [License](#license)

## Overview

**wms-employee** is a production-ready Spring Boot microservice designed for enterprise warehouse management systems. It
provides:

- 🔐 **Dual Authentication**: OAuth2 web login + JWT API authentication via Auth0
- 👥 **Employee Management**: Complete CRUD operations for employee data
- 🏗️ **Modular Architecture**: Separate modules for auth and core application logic
- 📊 **Database Management**: PostgreSQL with Liquibase migrations
- 🔄 **Message Queuing**: RabbitMQ and NATS integration for event-driven architecture
- 🛡️ **Security First**: Stateless JWT validation, CORS protection, and user enumeration prevention

## Tech Stack

### Backend Framework

- **Java 17** - LTS version with modern language features
- **Spring Boot 3.1.4** - Enterprise application framework
- **Spring Security** - OAuth2 Resource Server with JWT validation
- **Spring Data JPA** - Data persistence with Hibernate
- **Maven** - Dependency management and build automation

### Database & Migrations

- **PostgreSQL 17.2** - Primary relational database
- **Liquibase** - Database schema version control and migrations

### Authentication & Authorization

- **Auth0 SDK 2.25.0** - Identity provider integration
- **OAuth2** - Authorization framework
- **JWT** - Stateless token-based authentication

### Messaging & Events

- **RabbitMQ 3** - Message broker for async communication
- **NATS 2.10** - Pub/sub messaging with JetStream

### Development Tools

- **Lombok** - Boilerplate code reduction
- **Spring Boot DevTools** - Hot reload and development utilities
- **Docker & Docker Compose** - Container orchestration

### Testing

- **JUnit 5** - Unit testing framework
- **Spring Boot Test** - Integration testing support
- **Spring Security Test** - Security testing utilities

## Architecture

### Modular Monolith Structure

```
wms-employee/
├── wms-app/              # Main application module
│   ├── controller/       # REST API endpoints
│   ├── service/          # Business logic layer
│   ├── repository/       # Data access layer
│   ├── model/            # Domain entities
│   └── config/           # Security & app configuration
└── auth-module/          # Authentication module
    ├── dto/              # Auth data transfer objects
    ├── exceptions/       # Auth-specific exceptions
    └── service/          # Auth0 integration services
```

### Authentication Flow

The application implements **stateless JWT authentication** with Auth0:

1. **Registration** (`EmployeeCommandService:38`):
    - Creates user in Auth0 via Management API
    - Saves user metadata to local PostgreSQL database
    - Implements transactional rollback: if database save fails, Auth0 user is deleted

2. **API Login - Stateless JWT** (`LoginService:114`):
    - Authenticates credentials with Auth0 using Resource Owner Password Grant
    - Verifies user exists in local database
    - Returns complete token response: `access_token`, `id_token`, `token_type`, `expires_in`
    - **Security**: Prevents user enumeration by authenticating first, then checking database

3. **OAuth2 Web Login** (Alternative flow):
    - Browser-based redirect authentication
    - Creates server-side session for web clients
    - Returns user profile after successful authentication

### Security Model

**Stateless JWT-based OAuth2 Resource Server** (`SecurityConfig:50`):

- **Architecture**: Fully stateless - no server-side sessions for API calls
- **Custom JWT Decoder** with audience validation (`AudienceValidator`)
- **Authority Mapping**: Extracts permissions from JWT `permissions` claim with `SCOPE_` prefix
- **Endpoint Protection**:
    - `/api/public/**` - Public access
    - `/api/auth/login`, `/api/auth/token` - Public (authentication endpoints)
    - `/api/employee/**` - Requires valid JWT token
    - `/api/private/**` - Requires valid JWT token
    - `/api/admin/**` - Requires JWT token + `SCOPE_admin` authority
    - All other requests - **DENIED** (API-only, no web pages served)
- **CORS**: Enabled for localhost:4200, localhost:3000, localhost:8080
- **CSRF**: Disabled for `/api/**` endpoints (not needed for stateless JWT authentication)

## Getting Started

### Prerequisites

- **Java 17** or higher
- **Maven 3.6+**
- **Docker** and **Docker Compose**
- **Auth0 Account** with configured application

### 1. Clone the Repository

```bash
git clone <repository-url>
cd wms-employee
```

### 2. Configure Auth0

Create an `auth0.env` file in the project root:

```properties
AUTH_DOMAIN=your-tenant.auth0.com
AUTH_CLIENT_ID=your-client-id
AUTH_CLIENT_SECRET=your-client-secret
AUTH_AUDIENCE=your-api-audience
AUTH_MGT_TOKEN_URL=https://your-tenant.auth0.com/oauth/token
```

**Auth0 Application Settings Requirements**:

- **Application Type**: Regular Web Application
- **Grant Types**: Enable "Password" grant type for API authentication
- **Allowed Callback URLs**: `http://localhost:8080/login/oauth2/code/auth0`
- **Allowed Logout URLs**: `http://localhost:8080`
- **Connection**: Enable "Username-Password-Authentication" database connection

### 3. Start Infrastructure

Start PostgreSQL, RabbitMQ, and NATS services:

```bash
# Initialize database permissions (first time only)
docker compose exec postgres chmod +x /docker-entrypoint-initdb.d/init-multiple-databases.sh

# Start all services
docker compose up -d
```

### 4. Initialize Database

Build and run Liquibase migrations:

```bash
# Navigate to database directory
cd wms-app/src/main/resources/db

# Build Liquibase Docker image
docker build -f Dockerfile -t employee-db .

# Apply database migrations
docker run --rm \
  --network=wms-employee_microservices-network \
  --name liquibase-employee-run \
  --entrypoint liquibase employee-db update \
  --defaultsFile=/liquibase/liquibase-employee.properties
```

### 5. Run the Application

```bash
# Return to project root
cd ../../../../..

# Build project
mvn clean install

# Run application
mvn spring-boot:run
```

The application will be available at `http://localhost:8080`

## Available Commands

### Build & Run

```bash
# Clean and build project
mvn clean install

# Run application (requires auth0.env file)
mvn spring-boot:run

# Build without running tests
mvn clean install -DskipTests

# Create executable JAR
mvn clean package
```

### Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=EmployeeCommandServiceTest

# Run specific test method
mvn test -Dtest=EmployeeCommandServiceTest#testMethodName

# Run tests with coverage
mvn test jacoco:report
```

### Database Management

```bash
# Apply database migrations
docker run --rm \
  --network=wms-employee_microservices-network \
  --name liquibase-employee-run \
  --entrypoint liquibase employee-db update \
  --defaultsFile=/liquibase/liquibase-employee.properties

# Drop all database objects
docker run --rm \
  --network=wms-employee_microservices-network \
  --name liquibase-employee-run \
  employee-db dropAll \
  --defaultsFile=/liquibase/liquibase-employee.properties

# Rollback last migration
docker run --rm \
  --network=wms-employee_microservices-network \
  --name liquibase-employee-run \
  --entrypoint liquibase employee-db rollbackCount 1 \
  --defaultsFile=/liquibase/liquibase-employee.properties
```

### Docker Services

```bash
# Start all services
docker compose up -d

# Stop all services
docker compose down

# View logs
docker compose logs -f

# View specific service logs
docker compose logs -f postgres
```

## API Documentation

### Authentication Endpoints

#### Get JWT Token (Recommended for API clients)

```bash
POST /api/auth/token
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "yourpassword"
}

# Response
{
  "access_token": "eyJhbGc...",
  "id_token": "eyJhbGc...",
  "token_type": "Bearer",
  "expires_in": "86400"
}
```

#### Get User Profile (Legacy)

```bash
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "yourpassword"
}

# Response: Auth0UserDto with user details
```

### Protected Endpoints

All protected endpoints require the `Authorization` header:

```bash
GET /api/employee/{id}
Authorization: Bearer <access_token>

GET /api/private/resource
Authorization: Bearer <access_token>

GET /api/admin/resource
Authorization: Bearer <access_token>
# Requires 'admin' permission in JWT
```

### Example Usage

#### cURL

```bash
# Get token
curl -X POST http://localhost:8080/api/auth/token \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"pass"}'

# Use token
curl -X GET http://localhost:8080/api/private/resource \
  -H "Authorization: Bearer eyJhbGc..."
```

#### JavaScript (Fetch API)

```javascript
// 1. Login and get token
const response = await fetch('http://localhost:8080/api/auth/token', {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({
        email: 'user@example.com',
        password: 'password'
    })
});

const {access_token} = await response.json();
localStorage.setItem('access_token', access_token);

// 2. Use token for API calls
const data = await fetch('http://localhost:8080/api/private/data', {
    headers: {
        'Authorization': `Bearer ${access_token}`
    }
});
```

For complete API documentation, see [AUTH0_INTEGRATION.md](AUTH0_INTEGRATION.md)

## Authentication

### Two Authentication Methods

1. **OAuth2 Login Flow** (Web Browsers):
    - Browser-based redirect authentication
    - Visit `http://localhost:8080/` and click "Log In with Auth0"
    - Creates server-side session
    - Best for web applications

2. **Password Grant Flow** (API/Mobile Clients):
    - Programmatic authentication with email/password
    - Returns JWT tokens
    - Stateless, no session
    - Best for mobile apps, SPAs, and API clients

### Security Features

- ✅ **User Enumeration Prevention**: Authenticate with Auth0 before database lookup
- ✅ **Audience Validation**: Custom JWT decoder ensures tokens are for this API
- ✅ **Permission-based Authorization**: Extract permissions from JWT claims
- ✅ **CORS Protection**: Configured allowed origins
- ✅ **Transactional Rollback**: Failed database operations trigger Auth0 user deletion
- ✅ **Stateless Sessions**: No server-side session storage for API requests

For detailed authentication documentation, see [AUTH0_INTEGRATION.md](AUTH0_INTEGRATION.md)

## Project Structure

```
wms-employee/
├── wms-app/                                # Main application module
│   ├── src/main/java/
│   │   └── org/localhost/wmsemployee/
│   │       ├── WmsEmployeeApplication.java # Application entry point
│   │       ├── config/                     # Configuration classes
│   │       │   ├── SecurityConfig.java     # Security & JWT configuration
│   │       │   ├── WebConfig.java          # CORS & web configuration
│   │       │   └── validator/              # Custom validators
│   │       ├── controller/                 # REST API controllers
│   │       │   ├── EmployeeCommandController.java
│   │       │   ├── EmployeeQueryController.java
│   │       │   └── LoginController.java
│   │       ├── service/                    # Business logic
│   │       │   ├── auth/                   # Auth0 integration services
│   │       │   ├── employee/               # Employee management services
│   │       │   └── login/                  # Login orchestration
│   │       ├── repository/                 # Data access layer
│   │       ├── model/                      # Domain entities
│   │       ├── dto/                        # Data transfer objects
│   │       └── exceptions/                 # Custom exceptions & handlers
│   ├── src/main/resources/
│   │   ├── application.properties          # Application configuration
│   │   ├── db/                            # Database migrations
│   │   │   ├── changelog/                 # Liquibase changelogs
│   │   │   ├── Dockerfile                 # Liquibase Docker image
│   │   │   └── liquibase-employee.properties
│   │   └── static/                        # Static resources
│   └── src/test/java/                     # Test files
├── auth-module/                           # Authentication module
│   └── src/main/java/auth/
│       ├── dto/                           # Auth DTOs
│       ├── exceptions/                    # Auth exceptions
│       └── service/                       # Auth services
├── docker-compose.yml                     # Infrastructure services
├── auth0.env                              # Auth0 credentials (not in git)
├── pom.xml                                # Parent POM
├── CLAUDE.md                              # Project documentation for AI
├── AUTH0_INTEGRATION.md                   # Authentication guide
└── README.md                              # This file
```

## Configuration

### Application Properties

Key configuration in `wms-app/src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5433/employee
spring.datasource.username=postgres
spring.datasource.password=postgres
# Auth0 Configuration
auth0.connection=Username-Password-Authentication
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://your-tenant.auth0.com/
# CORS (configured in WebConfig.java)
# Allowed origins: localhost:4200, localhost:3000, localhost:8080
```

### Infrastructure Services

Services defined in `docker-compose.yml`:

| Service    | Port             | Description                                                           |
|------------|------------------|-----------------------------------------------------------------------|
| PostgreSQL | 5433             | Primary database with `employee_login_facade` and `surveys` databases |
| RabbitMQ   | 5672, 15672      | Message broker (management UI on 15672)                               |
| NATS       | 4222, 6222, 8222 | Pub/sub messaging with JetStream                                      |

## Testing

The project includes comprehensive unit and integration tests:

```bash
# Run all tests
mvn test

# Run with coverage report
mvn test jacoco:report

# Run specific test suites
mvn test -Dtest=*ServiceTest           # All service tests
mvn test -Dtest=*ControllerTest        # All controller tests
mvn test -Dtest=*IntegrationTest       # All integration tests
```

### Test Structure

- **Unit Tests**: Service layer, model validation, exception handling
- **Integration Tests**: Controller endpoints, security configuration, database operations
- **Test Configuration**: Uses H2 in-memory database for fast execution

## Project Status

🚧 **Active Development**

### Completed Features

- ✅ Auth0 integration with dual authentication flows
- ✅ JWT token validation and authorization
- ✅ Employee registration and management
- ✅ Database migrations with Liquibase
- ✅ Modular architecture implementation
- ✅ Security configuration with CORS and CSRF protection
- ✅ Comprehensive exception handling

### In Progress

- 🔄 RabbitMQ message publishing for employee events
- 🔄 NATS integration for pub/sub patterns
- 🔄 Advanced employee query capabilities
- 🔄 API documentation with OpenAPI/Swagger

### Planned Features

- 📋 Role-based access control (RBAC) enhancements
- 📋 Employee status workflow management
- 📋 Audit logging for all operations
- 📋 Rate limiting and API throttling
- 📋 Metrics and monitoring with Actuator

## Contributing

We welcome contributions! Please follow these guidelines:

1. **Fork the repository**
2. **Create a feature branch**: `git checkout -b feature/your-feature-name`
3. **Follow code style**: Use project's Lombok and Spring Boot conventions (see [CLAUDE.md](CLAUDE.md))
4. **Write tests**: Maintain test coverage above 80%
5. **Commit your changes**: Use conventional commit messages
6. **Push to your fork**: `git push origin feature/your-feature-name`
7. **Submit a Pull Request**

### Code Style Guidelines

- Use constructor-based dependency injection with `@RequiredArgsConstructor`
- Prefer `record` types for immutable DTOs
- Use Bean Validation annotations (`@Valid`, `@Size`, `@Email`)
- Centralize exception handling with `@ControllerAdvice`
- Follow REST controller patterns: controllers handle routing, services contain business logic
- Use SLF4J for logging (`@Slf4j`)

For complete guidelines, see [CLAUDE.md](CLAUDE.md)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## Additional Resources

- [Auth0 Integration Guide](AUTH0_INTEGRATION.md) - Comprehensive authentication documentation
- [Project Documentation](CLAUDE.md) - Detailed project structure and development guidelines
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/3.1.4/reference/html/)
- [Auth0 Java SDK](https://github.com/auth0/auth0-java)
- [Liquibase Documentation](https://docs.liquibase.com/)

## Support

For issues, questions, or contributions:

- 🐛 [Report a Bug](../../issues/new?template=bug_report.md)
- 💡 [Request a Feature](../../issues/new?template=feature_request.md)
- 📧 Contact the team

---

**Built with ❤️ using Spring Boot and Auth0**