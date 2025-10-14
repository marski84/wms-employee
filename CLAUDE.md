# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**wms-employee** is a Spring Boot microservice for managing employee data and authentication, part of a larger warehouse management system (WMS). It uses Auth0 for authentication/authorization with OAuth2 and JWT tokens.

## Development Commands

### Build & Run
```bash
# Build project
mvn clean install

# Run application (requires auth0.env file with Auth0 credentials)
mvn spring-boot:run

# Run specific test class
mvn test -Dtest=EmployeeCommandServiceTest

# Run specific test method
mvn test -Dtest=EmployeeCommandServiceTest#testMethodName
```

### Database Management

The project uses PostgreSQL with Liquibase for schema management. Database operations require Docker:

```bash
# Start infrastructure (PostgreSQL, RabbitMQ, NATS)
docker compose up -d

# Initialize database permissions (first time only)
docker compose exec postgres chmod +x /docker-entrypoint-initdb.d/init-multiple-databases.sh

# Build Liquibase image
cd src/main/resources/db
docker build -f Dockerfile -t employee-db .

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
```

## Architecture

### Authentication Flow

The application implements **stateless JWT authentication** with Auth0:

1. **Registration** (`EmployeeCommandService:38`):
   - Creates user in Auth0 via Management API
   - Saves user metadata to local PostgreSQL database
   - Implements transactional rollback: if database save fails, Auth0 user is deleted to prevent orphan records

2. **API Login - Stateless JWT** (`LoginService:114`, endpoint: `POST /api/auth/token`):
   - Authenticates credentials with Auth0 using Resource Owner Password Grant
   - Verifies user exists in local database (only after successful Auth0 authentication)
   - Returns complete token response: `access_token`, `id_token`, `token_type` ("Bearer"), `expires_in`
   - **Security**: Prevents user enumeration by authenticating first, then checking database
   - **Usage**: Primary authentication method for all API clients, mobile apps, and SPAs

3. **Alternative Login** (`LoginService:78`, endpoint: `POST /api/auth/login`):
   - Same authentication flow as `/api/auth/token`
   - Returns user profile (Auth0UserDto) instead of raw tokens
   - **Usage**: For clients that need user details immediately after login

### Security Configuration

**Stateless JWT-based OAuth2 Resource Server** with custom validation (`SecurityConfig:50`):
- **Architecture**: Fully stateless - no server-side sessions, all authentication via JWT tokens
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
- **Session Policy**: `STATELESS` - No sessions created or maintained

**Security Best Practices Implemented**:
- User enumeration prevention: Authenticate with Auth0 before database lookup
- Identical error messages for "user not found" vs "invalid password"
- Configurable Auth0 connection name (externalized to application.properties)

### Service Layer Architecture

**Command-Query Separation**:
- `EmployeeCommandService` - Handles employee registration, Auth0 user creation
- `EmployeeQueryService` - Retrieves employee details
- `EmployeeDataService` - Database persistence layer
- `LoginService` - Authentication orchestration
- `Auth0ManagementTokenService` - Manages Auth0 M2M tokens for API access

### Data Model

**EmployeeData Entity** (`EmployeeData:21`):
- Primary table: `employee`
- Stores Auth0 user metadata (userId, email, username, name, nickname)
- Automatic timestamp management via JPA lifecycle callbacks
- Unique constraint on Auth0 userId

### Exception Handling

Global exception handler (`GlobalExceptionHandler`) provides centralized error responses for:
- `AuthenticationFailedException` - Invalid credentials
- `UserNotFoundException` - User not found in database
- `NoValidDtoException` - Invalid DTO validation
- `EmployeeNotFoundException` - Employee record not found

### Infrastructure Dependencies

- **PostgreSQL** (port 5433): Primary database for employee records
- **RabbitMQ** (ports 5672/15672): Message queuing (configured but usage TBD)
- **NATS** (ports 4222/6222/8222): Pub/sub messaging (configured but usage TBD)
- **Auth0**: External identity provider for OAuth2/JWT authentication

### Configuration Requirements

Create `auth0.env` in project root with:
- `AUTH_DOMAIN` - Auth0 tenant domain
- `AUTH_CLIENT_ID` - Auth0 application client ID
- `AUTH_CLIENT_SECRET` - Auth0 application client secret
- `AUTH_AUDIENCE` - Auth0 API audience
- `AUTH_MGT_TOKEN_URL` - Auth0 Management API token URL

The application loads these via `Dotenv` on startup (`WmsEmployeeApplication:14`).

**Auth0 Application Settings**:
- **Grant Types**: Must enable "Password" grant type for Resource Owner Password Grant flow
- **Connection**: Default is "Username-Password-Authentication" (configurable via `auth0.connection` property)

**Application Properties** (`application.properties`):
- `auth0.connection` - Auth0 database connection name (defaults to Username-Password-Authentication)
- `spring.security.oauth2.resourceserver.jwt.issuer-uri` - Auth0 tenant URL for JWT validation
- CORS origins, OAuth2 client configuration, logging levels

## Technology Stack

- **Java 17** with Spring Boot 3.1.4
- **Spring Security** with OAuth2 Resource Server (JWT validation)
- **JPA/Hibernate** with bytecode enhancement
- **Liquibase** for database migrations
- **Lombok** for boilerplate reduction
- **PostgreSQL** 17.2
- **Auth0** SDK 2.25.0
- **Maven** for build management

## API Usage Examples

### Authenticate and Get JWT Token

```bash
# Get JWT tokens for API access
curl -X POST http://localhost:8080/api/auth/token \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "yourpassword"
  }'

# Response:
{
  "access_token": "eyJhbGc...",
  "id_token": "eyJhbGc...",
  "token_type": "Bearer",
  "expires_in": "86400"
}
```

### Access Protected Endpoints

```bash
# Use the access token to call protected endpoints
curl -X GET http://localhost:8080/api/private/resource \
  -H "Authorization: Bearer eyJhbGc..."

# Admin endpoints require 'admin' permission in JWT
curl -X GET http://localhost:8080/api/admin/resource \
  -H "Authorization: Bearer eyJhbGc..."
```

### Frontend Integration

```javascript
// 1. Login and store token
const response = await fetch('http://localhost:8080/api/auth/token', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email: 'user@example.com', password: 'pass' })
});
const { access_token } = await response.json();
localStorage.setItem('access_token', access_token);

// 2. Use token in subsequent requests
const data = await fetch('http://localhost:8080/api/private/data', {
  headers: { 'Authorization': `Bearer ${access_token}` }
});
```