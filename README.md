# Enterprise Shell API

A reference architecture Spring Boot backend for enterprise applications. This is a **shell** — simple enough to understand quickly, but structured exactly as a real production app should be. Every pattern here is meant to be copied and extended by developers building real features.

## Tech Stack

- **Java 21**, **Spring Boot 3.5.x**, **Gradle** (Groovy DSL)
- **Spring Security** with Okta JWT validation via userinfo endpoint
- **Spring JDBC** (JdbcTemplate / NamedParameterJdbcTemplate) — no JPA/ORM
- **Flyway** for database migrations
- **MySQL** (production/local) / **H2** (tests)
- **Springdoc OpenAPI 2.x** — Swagger UI
- **JaCoCo** for test coverage
- **Lombok** for boilerplate reduction

## Prerequisites

- Java 21+
- MySQL 8.x (for local development)
- Gradle 8.x (wrapper included)

## Running Locally

### 1. Set up MySQL

```sql
CREATE DATABASE enterprise_shell;
```

### 2. Set environment variables (or use defaults)

```bash
export DB_USERNAME=root
export DB_PASSWORD=root
export OKTA_ISSUER=https://your-domain.okta.com/oauth2/default
export OKTA_CLIENT_ID=your-client-id
```

### 3. Run the application

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

The API will be available at `http://localhost:8080`.

Swagger UI: `http://localhost:8080/swagger-ui.html`

## Running Tests

```bash
./gradlew test
```

Tests use the `test` profile automatically, which configures an H2 in-memory database. Flyway migrations run against H2 so the schema matches production.

Coverage reports are generated at `build/reports/jacoco/test/html/index.html`.

## Project Structure

```
com.enterprise.shellapi
├── controller/     — @RestController classes
├── service/        — @Service classes (business logic)
├── repository/     — JdbcTemplate repository classes (data access)
├── model/          — Domain objects (Record, PersonalInfo, WorkInfo, Preferences, etc.)
├── dto/            — Request/response objects for the API layer
├── config/         — SecurityConfig, CorsConfig, JwtConfig, OpenApiConfig
├── exception/      — GlobalExceptionHandler, custom exceptions
├── filter/         — OktaJwtAuthenticationFilter
└── util/           — SqlQueryLoader, shared utilities
```

## SQL Query Externalization

**All SQL queries live in `src/main/resources/sql/queries.yml`** — not as string literals in Java code.

### Why?

- **Readability**: SQL is readable as SQL, not buried in Java string concatenation
- **Reviewability**: DBAs and developers can review all queries in one file
- **Consistency**: One place to check for query patterns, naming conventions, and indexing needs
- **Searchability**: `grep` for any table or column name and find every query that touches it

### How it works

`SqlQueryLoader` reads `queries.yml` at startup and exposes queries by category and name:

```java
// In a repository class:
String sql = sqlQueryLoader.getQuery("records", "findById");
```

The YAML structure:

```yaml
records:
  findAll: "SELECT * FROM records ..."
  findById: "SELECT * FROM records WHERE id = :id"
```

All queries use **named parameters** (`:id`, `:name`) with `NamedParameterJdbcTemplate`.

## Authentication

Authentication uses **Okta JWT validation via the userinfo endpoint**. This matches the existing production pattern (not spring-security-oauth2-resource-server).

### How it works

1. Client sends `Authorization: Bearer <token>` header
2. `OktaJwtAuthenticationFilter` extracts the token
3. Filter calls `GET https://{okta.issuer}/v1/userinfo` with the bearer token
4. On success: user's email and groups are extracted, authentication is set in `SecurityContextHolder`
5. On failure: 401 JSON error response

### Public endpoints (no auth required)

- `GET /actuator/health`
- `GET /swagger-ui.html` (and related Swagger resources)
- `GET /v3/api-docs/**`

### Configuration

Set `OKTA_ISSUER` and `OKTA_CLIENT_ID` environment variables, or configure in the profile YAML:

```yaml
okta:
  issuer: https://your-domain.okta.com/oauth2/default
  client-id: your-client-id
```

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/lookups` | Get all lookup values as `{ value, label }` objects |
| GET | `/api/records?page=0&size=10&name=&email=&department=&status=&address=` | Search/list records (paginated) |
| GET | `/api/records/{id}` | Get a single record with all nested objects |
| POST | `/api/records` | Create a new record |
| PUT | `/api/records/{id}` | Update an existing record |
| DELETE | `/api/records/{id}` | Delete a record |

### Record structure

Records use a nested object model matching UI sections:

```json
{
  "id": 1,
  "personalInfo": {
    "name": "Alice Johnson",
    "email": "alice@company.com",
    "phone": "(555) 123-4567",
    "address": "123 Main St",
    "dateOfBirth": "1990-03-15",
    "ssn": "***-**-1234",
    "bio": "Senior engineer"
  },
  "workInfo": {
    "jobTitle": "Staff Engineer",
    "manager": "Bob Smith",
    "department": "Engineering",
    "status": "active",
    "startDate": "2022-01-10",
    "employmentType": "full-time"
  },
  "preferences": {
    "remoteEligible": true,
    "notificationsEnabled": true,
    "notificationChannels": ["email", "slack"],
    "accessLevel": "standard",
    "notes": null
  },
  "emergencyContacts": [
    {
      "id": 1,
      "name": "Jane Johnson",
      "relationship": "Spouse",
      "phone": "(555) 987-6543",
      "email": "jane@email.com",
      "isPrimary": true
    }
  ],
  "certifications": [
    {
      "id": 1,
      "name": "AWS Solutions Architect",
      "issuingBody": "Amazon",
      "issueDate": "2023-06-01",
      "expiryDate": "2026-06-01",
      "credentialId": "AWS-12345"
    }
  ],
  "createdAt": "2024-01-15T10:30:00"
}
```

The request body for POST/PUT uses the same nested structure via `personalInfo`, `workInfo`, `preferences`, `emergencyContacts`, and `certifications` sections.

### Lookups response

The `/api/lookups` endpoint returns `{ value, label }` objects for all fields:

```json
{
  "departments": [{ "value": "Engineering", "label": "Engineering" }, ...],
  "statuses": [{ "value": "active", "label": "Active" }, ...],
  "employmentTypes": [{ "value": "full-time", "label": "Full-time" }, ...],
  "notificationChannels": [{ "value": "email", "label": "Email" }, ...],
  "accessLevels": [{ "value": "standard", "label": "Standard" }, ...],
  "relationships": [{ "value": "Spouse", "label": "Spouse" }, ...]
}
```

### Error responses

All errors use a consistent shape:

```json
{
  "message": "Record not found with id: 999",
  "status": 404,
  "timestamp": "2024-01-15T10:30:00",
  "errors": [
    { "field": "personalInfo.name", "message": "Name is required" }
  ]
}
```

The `errors` array is only present for validation errors (400).

## How to Add a New Endpoint (End-to-End)

Follow this order. Each layer builds on the previous one.

### 1. Migration

Create `src/main/resources/db/migration/V6__create_your_table.sql` with your schema.

### 2. Model

Create `src/main/java/com/enterprise/shellapi/model/YourModel.java`:

```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class YourModel {
    private Long id;
    private String name;
    // ...
}
```

### 3. DTO

Create request/response DTOs in the `dto` package with validation annotations:

```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class YourModelRequest {
    @NotBlank(message = "Name is required")
    private String name;
}
```

### 4. SQL Queries

Add queries to `src/main/resources/sql/queries.yml`:

```yaml
yourModel:
  findAll: "SELECT * FROM your_table ORDER BY id"
  findById: "SELECT * FROM your_table WHERE id = :id"
  insert: "INSERT INTO your_table (name) VALUES (:name)"
```

### 5. Repository

Create a repository that uses `SqlQueryLoader` and `NamedParameterJdbcTemplate`:

```java
@Repository
@RequiredArgsConstructor
public class YourModelRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SqlQueryLoader sqlQueryLoader;

    public List<YourModel> findAll() {
        String sql = sqlQueryLoader.getQuery("yourModel", "findAll");
        return jdbcTemplate.query(sql, Collections.emptyMap(), ROW_MAPPER);
    }
}
```

### 6. Service

Create a service with business logic:

```java
@Service
@RequiredArgsConstructor
public class YourModelService {
    private final YourModelRepository repository;
    // business logic here
}
```

### 7. Controller

Create a REST controller:

```java
@RestController
@RequestMapping("/api/your-models")
@RequiredArgsConstructor
public class YourModelController {
    private final YourModelService service;

    @GetMapping
    public ResponseEntity<List<YourModel>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }
}
```

### 8. Tests

Write tests at all three layers:
- **Controller test** (`@WebMvcTest`) — mock the service, test HTTP behavior
- **Service test** (JUnit + Mockito) — mock the repository, test business logic
- **Repository test** (`@JdbcTest`) — H2 in-memory, test actual SQL queries

## Spring Profiles

| Profile | Database | Use case |
|---------|----------|----------|
| `local` | MySQL on localhost:3306 | Local development |
| `test` | H2 in-memory | Automated tests |
| `prod` | MySQL (configured via env vars) | Production deployment |
