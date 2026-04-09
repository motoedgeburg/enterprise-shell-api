# Enterprise Shell API

A reference architecture Spring Boot backend for enterprise applications. This is a **shell** — simple enough to understand quickly, but structured exactly as a real production app should be. Every pattern here is meant to be copied and extended by developers building real features.

## Tech Stack

- **Java 21**, **Spring Boot 3.5.x**, **Gradle** (Groovy DSL)
- **Spring Security** with Okta JWT validation via local JWKS signature verification
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
String sql = sqlQueryLoader.getQuery("records", "findByUuid");
```

The YAML structure:

```yaml
records:
  search: "SELECT uuid, name, address, department, status FROM records WHERE ..."
  findByUuid: "SELECT * FROM records WHERE uuid = :uuid"
  insert: "INSERT INTO records (uuid, name, email, ...) VALUES (:uuid, :name, :email, ...)"
```

All queries use **named parameters** (`:uuid`, `:name`) with `NamedParameterJdbcTemplate`.

## Database Migrations

Schema changes are managed by **Flyway**. Migration files live in `src/main/resources/db/migration/` and follow the naming convention `V{number}__{description}.sql`.

Current migrations:

| Version | Description |
|---------|-------------|
| V1 | Create records table |
| V2 | Create emergency contacts table |
| V3 | Create certifications table |
| V4 | Create lookup tables (departments, statuses, etc.) |
| V5 | Seed lookup and sample record data |
| V6 | Add `updated_at` column and indexes |
| V7 | Add UUID surrogate keys to records |

Flyway runs automatically on startup. Migrations are applied in version order and are never modified after being applied — new changes require a new migration file.

## Authentication

Authentication uses **Okta JWT validation via local JWKS signature verification**. The filter fetches the signing keys from Okta's `/v1/keys` endpoint, caches them for 1 hour, and validates JWT signatures locally — no per-request network calls.

### How it works

1. Client sends `Authorization: Bearer <token>` header
2. `OktaJwtAuthenticationFilter` extracts the token
3. Filter validates the JWT signature locally using cached JWKS public keys from `{okta.issuer}/v1/keys`
4. On success: user's email and groups are extracted from the JWT claims, authentication is set in `SecurityContextHolder`
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
| GET | `/api/records?name=&email=&department=&status=&address=` | Search/list records (flat summaries) |
| GET | `/api/records/{uuid}` | Get a single record with all nested objects |
| POST | `/api/records` | Create a new record |
| PUT | `/api/records/{uuid}` | Update an existing record |
| DELETE | `/api/records/{uuid}` | Delete a record |

### Search response (`GET /api/records`)

Returns a flat JSON array of summary objects. All query parameters are optional filters:

```json
[
  { "uuid": "b3a1c5d0-...", "name": "Alice Johnson", "address": "123 Main St", "department": "Engineering", "status": "active" },
  { "uuid": "e7d4f2a1-...", "name": "Bob Martinez", "address": "456 Liberty Ave", "department": "Product", "status": "active" }
]
```

### Detail response (`GET /api/records/{uuid}`)

Returns the full nested record. The internal numeric `id` is never exposed — `uuid` is the only public identifier:

```json
{
  "uuid": "b3a1c5d0-1234-5678-9abc-def012345678",
  "personalInfo": {
    "name": "Alice Johnson",
    "email": "alice@company.com",
    "phone": "(555) 123-4567",
    "address": "123 Main St",
    "dateOfBirth": "1990-03-15",
    "ssn": "123-45-6789",
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
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-16T14:22:00"
}
```

### Create/Update request body (`POST` / `PUT /api/records/{uuid}`)

The request body uses the same nested structure: `personalInfo`, `workInfo`, `preferences`, `emergencyContacts`, and `certifications`. For updates, pass the `id` on existing emergency contacts and certifications to preserve them — items without an `id` are created as new, and items removed from the array are deleted.

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
  "message": "Record not found: 550e8400-e29b-41d4-a716-446655440000",
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

Create `src/main/resources/db/migration/V8__create_your_table.sql` with your schema (next available version number).

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
