# URL Shortener Service

A simple URL shortener I built with Spring Boot. It takes long URLs and gives you short codes that redirect to the original URL. Also tracks basic click stats.

## What it does

- Create short URLs from long ones
- Redirect short codes back to original URLs
- Support custom aliases if you want branded links
- Track how many times each link was clicked
- Validate URLs and prevent duplicate aliases

## Tech stack

- Spring Boot 4.1.0 with Java 21
- H2 Database (file-based so data persists)
- Spring Data JPA
- Lombok (less boilerplate)
- JUnit 5 + Mockito for tests

## Architecture

Pretty standard layered approach:
- Controllers handle HTTP requests
- Services contain the business logic
- Repositories handle database operations
- Models/DTOs for data structures
- Global exception handler for consistent error responses

## Prerequisites

- Java 21+
- Maven 3.6+
- Git

## Setup

```bash
git clone <repository-url>
cd url-shortener
mvn clean install
```

No database setup needed - it uses H2 with file storage.

## Configuration

Edit `application.properties` or use env vars:

- `server.port` - defaults to 8080
- `server.host` - defaults to localhost (used for short URL generation)
- `spring.datasource.url` - H2 database connection

## Running

```bash
mvn spring-boot:run
```

Or build and run the JAR:
```bash
mvn clean package
java -jar target/url-shortener-0.0.1-SNAPSHOT.jar
```

App runs on http://localhost:8080

### H2 Console

If you want to poke around the database during dev: http://localhost:8080/h2-console

- JDBC URL: `jdbc:h2:file:./data/urlshortener`
- Username: `sa`
- Password: (empty)

## Tests

```bash
mvn test
```

Run specific tests:
```bash
mvn test -Dtest=UrlServiceTest
mvn test -Dtest=UrlControllerTest
```

Test coverage includes unit tests, controller tests, and integration tests. Covers happy paths plus edge cases like invalid URLs, duplicates, and validation failures.

## API Documentation

### POST /shorten

Creates a short URL from a long URL.

**Request:**
```json
{
  "url": "https://example.com/some/very/long/path",
  "custom_alias": "my-link"
}
```

`custom_alias` is optional. If not provided, a random 7-character code is generated.

**Success Response (201 Created):**
```json
{
  "short_code": "abc123",
  "short_url": "http://localhost:8080/abc123"
}
```

**Error Responses:**

- `400 Bad Request` - Invalid URL format or validation error
  ```json
  {
    "error": "URL must use http or https scheme",
    "status": 400,
    "timestamp": "2024-01-15T10:30:00Z"
  }
  ```

- `409 Conflict` - Custom alias already exists
  ```json
  {
    "error": "Custom alias 'my-link' already exists",
    "status": 409,
    "timestamp": "2024-01-15T10:30:00Z"
  }
  ```

### GET /{code}

Redirects to the original URL.

**Request:**
```
GET /abc123
```

**Response:**
```
HTTP/1.1 301 Moved Permanently
Location: https://example.com/some/very/long/path
```

**Error Response (404 Not Found):**
```json
{
  "error": "Short code 'abc123' not found",
  "status": 404,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## Usage Examples

### Create a Short URL

```bash
curl -X POST http://localhost:8080/shorten \
  -H "Content-Type: application/json" \
  -d '{"url": "https://example.com/very/long/path"}'
```

Response:
```json
{
  "short_code": "xY9zA2",
  "short_url": "http://localhost:8080/xY9zA2"
}
```

### Create with Custom Alias

```bash
curl -X POST http://localhost:8080/shorten \
  -H "Content-Type: application/json" \
  -d '{"url": "https://google.com", "custom_alias": "google"}'
```

Response:
```json
{
  "short_code": "google",
  "short_url": "http://localhost:8080/google"
}
```

### Redirect

```bash
curl -i http://localhost:8080/xY9zA2
```

Response:
```
HTTP/1.1 301 Moved Permanently
Location: https://example.com/very/long/path
```

## Design notes

### Short codes

I went with random 7-character Base62 codes. Gives about 3.5 trillion combinations so collisions are super rare. The database has unique constraints as a backup. I considered encoding database IDs but that makes codes predictable which feels like a security risk.

### Duplicate URLs

Same URL gets a new short code each time. This is useful for marketing campaigns where you want different tracking links. It does use more storage but the flexibility is worth it. Might add a deduplication option later if needed.

### Custom aliases

Rules: 3-50 chars, alphanumeric plus hyphens, case-insensitive. Can't start or end with hyphen. I blocked some reserved words like 'api' and 'admin' to avoid conflicts with future routes.

### Database

Using H2 file-based for now. Super easy setup, no external dependencies. For production you'd want to migrate to PostgreSQL or MySQL, but H2 is fine for this scale.

### Error handling

Centralized with @ControllerAdvice. Gives consistent error responses and proper HTTP status codes (400 for bad input, 404 for not found, 409 for conflicts).

## Data Model

### UrlMapping Entity

| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | Primary key (auto-generated) |
| `originalUrl` | String | The full original URL (max 2048 chars) |
| `shortCode` | String | Unique short code (max 20 chars) |
| `customAlias` | String | Optional custom alias (max 100 chars) |
| `createdAt` | LocalDateTime | Creation timestamp |
| `updatedAt` | LocalDateTime | Last update timestamp |
| `clickCount` | Long | Number of times the link was accessed |
| `lastAccessedAt` | LocalDateTime | Last access timestamp |

**Constraints**:
- `shortCode` must be unique
- `customAlias` must be unique (if present)
- `originalUrl` cannot be null

## Things I'd add with more time

- Redis caching for faster redirects
- Rate limiting to prevent abuse
- Authentication (API keys)
- Better analytics (referrer, user agent, geo)
- URL expiration
- QR code generation
- Custom domain support
- Prometheus metrics
- Swagger/OpenAPI docs

## License

Educational/demonstration project.

## Contributing

Feel free to submit PRs. Just make sure tests pass.
