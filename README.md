# Enterprise Shell API

A reference architecture Spring Boot backend for enterprise applications. This is a **shell** — simple enough to understand quickly, but structured exactly as a real production app should be. Every pattern here is meant to be copied and extended by developers building real features.

## Tech Stack

- **Java 21**, **Spring Boot 3.5.0**, **Gradle** (Groovy DSL)
- **Spring Security** with Okta JWT validation via local JWKS signature verification
- **Spring JDBC** (NamedParameterJdbcTemplate) — no JPA/ORM
- **Flyway** for database migrations
- **MySQL 8+** (local / production) / **H2** (tests, MySQL-compatible mode)
- **Springdoc OpenAPI 2.x** — Swagger UI at `/swagger-ui.html`
- **JaCoCo** for test coverage reporting
- **Lombok** for boilerplate reduction

## Prerequisites

- Java 21+
- MySQL 8+ (for local development)
- Gradle 8.x (wrapper included — use `./gradlew`)

## Running Locally

### 1. Set up MySQL

```sql
CREATE DATABASE enterprise_shell;
```

### 2. Run the application

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

The `local` profile:
- Connects to MySQL on `localhost:3306` using `root` with no password by default
- **Bypasses JWT authentication** (`auth.bypass=true`) so you can call the API without tokens
- Allows CORS from `http://localhost:5173` (Vite dev server)

Override database credentials with environment variables if needed:

```bash
export DB_USERNAME=root
export DB_PASSWORD=yourpassword
```

The API will be available at `http://localhost:8080`.

Swagger UI: `http://localhost:8080/swagger-ui.html`

## Running Tests

```bash
./gradlew test
```

Tests use the `test` profile automatically, which configures an H2 in-memory database in MySQL-compatible mode. Flyway migrations run against H2 so the schema matches production.

Coverage report:

```bash
./gradlew test jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

## Project Structure

```
com.enterprise.shellapi
├── controller/     — REST controllers (RecordController, LookupController)
├── service/        — Business logic and validation (RecordService, RecordRequestValidator)
├── repository/     — JDBC repositories (RecordRepository, EmergencyContactRepository, etc.)
├── model/          — Domain objects (Record, PersonalInfo, WorkInfo, Preferences, History)
├── dto/            — Request/response DTOs with Bean Validation annotations
├── config/         — SecurityConfig, CorsConfig, JwtConfig, OpenApiConfig
├── exception/      — GlobalExceptionHandler, RecordNotFoundException, ValidationException
├── filter/         — OktaJwtAuthenticationFilter
└── util/           — SqlQueryLoader
```

## API Endpoints

### Records

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/records` | Search/list records (returns flat summaries) |
| GET | `/api/records/{uuid}` | Get full record with all nested objects |
| POST | `/api/records` | Create a new record |
| PUT | `/api/records/{uuid}` | Update an existing record |
| DELETE | `/api/records/{uuid}` | Delete a record |

### Lookups

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/lookups` | Get all lookup values (departments, statuses, etc.) |

### Public (no auth)

| Path | Description |
|------|-------------|
| `/actuator/health` | Health check |
| `/swagger-ui.html` | Swagger UI |
| `/v3/api-docs/**` | OpenAPI spec |

## Record JSON Structure

The internal numeric `id` is never exposed — `uuid` is the only public identifier. Null fields are omitted from responses.

### Search response (`GET /api/records`)

All query parameters are optional: `name`, `email`, `department`, `status`, `address`.

```json
[
  {
    "uuid": "b3a1c5d0-...",
    "name": "Alice Johnson",
    "address": "123 Market St, Philadelphia, PA 19103",
    "department": "Engineering",
    "status": "active"
  }
]
```

### Detail response (`GET /api/records/{uuid}`)

```json
{
  "uuid": "b3a1c5d0-...",
  "personalInfo": {
    "name": "Alice Johnson",
    "email": "alice.johnson@company.com",
    "phone": "(215) 555-0101",
    "address": "123 Market St, Philadelphia, PA 19103",
    "dateOfBirth": "1990-03-15",
    "ssn": "123-45-6789",
    "bio": "Senior software engineer with 10 years of experience."
  },
  "workInfo": {
    "jobTitle": "Senior Software Engineer",
    "manager": "Jane Smith",
    "department": "Engineering",
    "status": "active",
    "startDate": "2019-06-01",
    "employmentType": "full-time"
  },
  "preferences": {
    "remoteEligible": true,
    "notificationsEnabled": true,
    "notificationChannels": ["email", "slack"],
    "accessLevel": "standard",
    "notes": "Team lead for the infrastructure squad."
  },
  "history": {
    "emergencyContacts": [
      {
        "id": 1,
        "recordId": 1,
        "name": "Michael Johnson",
        "relationship": "Spouse",
        "phone": "(215) 555-0199",
        "email": "michael.j@personal.com",
        "isPrimary": true
      }
    ],
    "certifications": [
      {
        "id": 1,
        "recordId": 1,
        "name": "AWS Solutions Architect",
        "issuingBody": "Amazon Web Services",
        "issueDate": "2022-03-15",
        "expiryDate": "2025-03-15",
        "credentialId": "AWS-SAP-001234"
      }
    ]
  },
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-16T14:22:00"
}
```

### Create/Update request (`POST` / `PUT /api/records/{uuid}`)

Uses the same nested structure: `personalInfo`, `workInfo`, `preferences`, and `history`.

- `personalInfo` and `workInfo` are **required**
- `preferences` and `history` are optional
- For updates, pass the `id` on existing emergency contacts and certifications to preserve them — items without an `id` are created as new, and items removed from the array are deleted

```json
{
  "personalInfo": {
    "name": "Alice Johnson",
    "email": "alice.johnson@company.com",
    "phone": "(215) 555-0101",
    "address": "123 Market St, Philadelphia, PA 19103",
    "dateOfBirth": "1990-03-15",
    "ssn": "123-45-6789",
    "bio": "Senior software engineer."
  },
  "workInfo": {
    "jobTitle": "Senior Software Engineer",
    "manager": "Jane Smith",
    "department": "Engineering",
    "status": "active",
    "startDate": "2019-06-01",
    "employmentType": "full-time"
  },
  "preferences": {
    "remoteEligible": true,
    "notificationsEnabled": true,
    "notificationChannels": ["email", "slack"],
    "accessLevel": "standard",
    "notes": "Team lead for the infrastructure squad."
  },
  "history": {
    "emergencyContacts": [
      {
        "name": "Michael Johnson",
        "relationship": "Spouse",
        "phone": "(215) 555-0199",
        "email": "michael.j@personal.com",
        "isPrimary": true
      }
    ],
    "certifications": [
      {
        "name": "AWS Solutions Architect",
        "issuingBody": "Amazon Web Services",
        "issueDate": "2022-03-15",
        "expiryDate": "2025-03-15",
        "credentialId": "AWS-SAP-001234"
      }
    ]
  }
}
```

### Validation Rules

| Field | Rules |
|-------|-------|
| `personalInfo.name` | Required |
| `personalInfo.email` | Required, valid email format |
| `personalInfo.phone` | Optional, must match `(NNN) NNN-NNNN` |
| `personalInfo.address` | Optional, min 5 characters |
| `personalInfo.dateOfBirth` | Optional, must be a past date |
| `personalInfo.ssn` | Optional, must match `NNN-NN-NNNN` |
| `personalInfo.bio` | Optional, max 500 characters |
| `workInfo.jobTitle` | Required |
| `workInfo.department` | Required, must match a lookup value |
| `workInfo.status` | Required, must match a lookup value |
| `workInfo.employmentType` | Required, must match a lookup value |
| `preferences.accessLevel` | Must match a lookup value |
| `preferences.notes` | Optional, max 1000 characters |
| `history.emergencyContacts[].name` | Required |
| `history.emergencyContacts[].relationship` | Required, must match a lookup value |
| `history.certifications[].name` | Required |
| `history.certifications[].issuingBody` | Required |
| `history.certifications[].issueDate` | Required |

### Lookups response (`GET /api/lookups`)

Returns `{ value, label }` objects for all dropdown fields:

```json
{
  "departments": [{ "value": "Engineering", "label": "Engineering" }],
  "statuses": [{ "value": "active", "label": "Active" }],
  "employmentTypes": [{ "value": "full-time", "label": "Full-time" }],
  "notificationChannels": [{ "value": "email", "label": "Email" }],
  "accessLevels": [{ "value": "standard", "label": "Standard" }],
  "relationships": [{ "value": "Spouse", "label": "Spouse" }]
}
```

### Error Responses

All errors use a consistent shape:

```json
{
  "message": "Record not found: 550e8400-e29b-41d4-a716-446655440000",
  "status": 404,
  "timestamp": "2024-01-15T10:30:00"
}
```

Validation errors (400) include a field-level `errors` array:

```json
{
  "message": "Validation failed",
  "status": 400,
  "timestamp": "2024-01-15T10:30:00",
  "errors": [
    { "field": "personalInfo.name", "message": "Name is required" },
    { "field": "workInfo.department", "message": "Must be one of: Engineering, Product, Design, ..." }
  ]
}
```

## Database

### Schema

The database has three main tables plus lookup tables for validation:

- **records** — core employee data (personal, work, preferences fields stored flat)
- **emergency_contacts** — one-to-many, cascade deletes with records
- **certifications** — one-to-many, cascade deletes with records
- **Lookup tables** — departments, statuses, employment_types, notification_channels, access_levels, relationships

UUIDs are generated by the database (`DEFAULT UUID()`), not by application code.

### Migrations

Schema changes are managed by **Flyway**. Migration files live in `src/main/resources/db/migration/` and follow the naming convention `V{number}__{description}.sql`.

| Version | Description |
|---------|-------------|
| V1 | Create records table with email unique constraint |
| V2 | Create emergency contacts table (FK to records) |
| V3 | Create certifications table (FK to records) |
| V4 | Create lookup tables (departments, statuses, employment types, etc.) |
| V5 | Seed lookup data and 8 sample records with contacts/certs |
| V6 | Add `updated_at` column and indexes (department, status, name) |
| V7 | Add UUID surrogate keys to records |
| V8 | Seed 52 additional records (60 total) |
| V9 | Set database-level UUID default |

Flyway runs automatically on startup. Migrations are applied in version order and are **never modified** after being applied — new changes always go in a new migration file.

## SQL Query Externalization

All SQL lives in `src/main/resources/sql/queries.yml` — not as string literals in Java code.

**Why?**
- **Readability**: SQL is readable as SQL, not buried in Java string concatenation
- **Reviewability**: DBAs and developers can review all queries in one file
- **Searchability**: `grep` for any table or column and find every query that touches it

`SqlQueryLoader` reads `queries.yml` at startup and exposes queries by category and name:

```java
String sql = sqlQueryLoader.getQuery("records", "findByUuid");
```

```yaml
records:
  findByUuid: >
    SELECT * FROM records WHERE uuid = :uuid
  insert: >
    INSERT INTO records (name, email, phone, ...)
    VALUES (:name, :email, :phone, ...)
```

All queries use **named parameters** (`:uuid`, `:name`) with `NamedParameterJdbcTemplate`.

## Authentication

Authentication uses **Okta JWT validation via local JWKS signature verification**. The filter fetches the signing keys from Okta's `/v1/keys` endpoint, caches them for 1 hour, and validates JWT signatures locally — no per-request network calls to Okta.

### Flow

1. Client sends `Authorization: Bearer <token>` header
2. `OktaJwtAuthenticationFilter` extracts the token
3. JWT signature is validated using cached JWKS public keys from `{okta.issuer}/v1/keys`
4. On success: email and groups are extracted from claims, authentication is set in `SecurityContextHolder`
5. On failure: 401 JSON response

### Auth Bypass (Local Development)

The `local` profile sets `auth.bypass=true`, which skips JWT validation and authenticates all requests as `local-dev@company.com` with `ROLE_USER`. This lets you develop and test without configuring Okta.

### Configuration

```yaml
okta:
  issuer: https://your-domain.okta.com/oauth2/default
  client-id: your-client-id
auth:
  bypass: false  # true in local profile
```

Or via environment variables: `OKTA_ISSUER`, `OKTA_CLIENT_ID`.

## Spring Profiles

| Profile | Database | Auth | CORS | Use case |
|---------|----------|------|------|----------|
| `local` | MySQL on localhost:3306 | Bypassed | localhost:5173 | Local development |
| `test` | H2 in-memory (MySQL mode) | N/A | localhost:5173 | Automated tests |
| `prod` | MySQL via `DB_URL` env var | Okta JWT | Configurable via `CORS_ALLOWED_ORIGINS` | Production |

## How to Add a New Endpoint

Follow this order. Each layer builds on the previous one.

### 1. Migration

Create `src/main/resources/db/migration/V{next}__{description}.sql` with your schema.

### 2. Model

Create a domain class in `model/`:

```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class YourModel {
    private Long id;
    private String name;
}
```

### 3. DTO

Create request DTOs in `dto/` with validation annotations:

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
  findAll: >
    SELECT * FROM your_table ORDER BY id
  findById: >
    SELECT * FROM your_table WHERE id = :id
  insert: >
    INSERT INTO your_table (name) VALUES (:name)
```

### 5. Repository

Create a repository using `SqlQueryLoader` and `NamedParameterJdbcTemplate`:

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

- **Repository test** (`@JdbcTest`) — runs real SQL against H2, verifies queries work
- **Service test** (JUnit + Mockito) — mocks the repository, tests business logic and edge cases
- **Controller test** (`@WebMvcTest`) — mocks the service, tests HTTP behavior and validation
