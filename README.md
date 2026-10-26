# URL Shortening Service

A RESTful API for creating and managing shortened URLs, redirecting visitors to their original destinations, and retrieving access statistics. The application is built with Java and Spring Boot and persists data in PostgreSQL.

This project is part of my backend development roadmap and is based on the [URL Shortening Service project on roadmap.sh](https://roadmap.sh/projects/url-shortening-service).

## Features

- Create a shortened URL from a valid original URL.
- Generate a unique, seven-character alphanumeric short code.
- Retrieve the details of a shortened URL by its code.
- Redirect a short URL to its original destination using HTTP `302 Found`.
- Update the destination associated with an existing short code.
- Delete a shortened URL.
- Retrieve URL metadata and its access count.
- Validate request payloads and return structured API errors.
- Manage database schema changes with Flyway.
- Explore the API through Swagger UI and OpenAPI.
- Run automated unit and integration tests.
- Build and run the application with Docker or Docker Compose.

## Technology stack

| Area | Technology |
| --- | --- |
| Language | Java 25 |
| Application framework | Spring Boot 4.1.1 |
| HTTP API | Spring Web MVC |
| Persistence | Spring Data JPA / Hibernate |
| Database | PostgreSQL 17 (Docker configuration) |
| Database migrations | Flyway |
| Request validation | Jakarta Bean Validation |
| API documentation | springdoc-openapi 3.1.1, OpenAPI 3.1 |
| Build | Maven Wrapper |
| Testing | Spring Boot test support, JUnit, Mockito |
| CI | GitHub Actions |
| Containerization | Docker and Docker Compose |

## Prerequisites

For local development:

- Git
- JDK 25
- PostgreSQL 17 or a compatible PostgreSQL version
- A terminal (PowerShell on Windows, or a shell on Linux/macOS)

Maven does not need to be installed separately because the repository includes the Maven Wrapper.

For the containerized setup, install Docker Engine/Desktop with Docker Compose support. A local Java and PostgreSQL installation is not required when using the Compose environment.

## Configuration and environment variables

The application reads its database settings from environment variables. Do not commit real passwords or other secrets.

### Local application

The `dev` Spring profile uses the following variables:

| Variable | Purpose | Example |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | Selects the application profile | `dev` |
| `DATABASE_URL` | JDBC URL for the development database | `jdbc:postgresql://localhost:5432/url_shortening` |
| `DATABASE_USERNAME` | Database username | `postgres` |
| `DATABASE_PASSWORD` | Database password | Set your local password |

The local development database defaults to `url_shortening` and port `5432`. Create the database before starting the application. Flyway applies the migrations at startup.

### Automated tests

The test configuration uses a separate database and these variables:

| Variable | Purpose | Example |
| --- | --- | --- |
| `TEST_DB_URL` | JDBC URL for the test database | `jdbc:postgresql://localhost:5432/url_shortening_test` |
| `TEST_DB_USERNAME` | Test database username | `postgres` |
| `TEST_DB_PASSWORD` | Test database password | Set your local test password |

Create `url_shortening_test` in your local PostgreSQL instance before running the test suite. Use a disposable test database: integration tests write data to it.

### Docker Compose

Compose reads these variables from the project-root `.env` file:

| Variable | Purpose | Default |
| --- | --- | --- |
| `POSTGRES_DB` | Database created by the PostgreSQL container | `url_shortening` |
| `POSTGRES_USER` | PostgreSQL user | `postgres` |
| `POSTGRES_PASSWORD` | PostgreSQL password; required | No default |
| `POSTGRES_PORT` | Host port mapped to PostgreSQL | `5432` |

Create your local `.env` from the example file:

```bash
cp .env.example .env
```

On PowerShell:

```powershell
Copy-Item .env.example .env
```

Edit `.env` and replace placeholder passwords with values suitable for your environment. The `.env` file is for local configuration and must not be committed.

## Local development

### 1. Clone the repository

```bash
git clone https://github.com/cleoaguiar/url-shortening-service.git
cd url-shortening-service
```

### 2. Create the databases

Create separate development and test databases in PostgreSQL:

```sql
CREATE DATABASE url_shortening;
CREATE DATABASE url_shortening_test;
```

### 3. Configure environment variables

Set the variables in the terminal session where you will run the application. Replace the example passwords with your own local credentials.

Linux/macOS:

```bash
export SPRING_PROFILES_ACTIVE=dev
export DATABASE_URL=jdbc:postgresql://localhost:5432/url_shortening
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD='your_local_password'
```

Windows PowerShell:

```powershell
$env:SPRING_PROFILES_ACTIVE = "dev"
$env:DATABASE_URL = "jdbc:postgresql://localhost:5432/url_shortening"
$env:DATABASE_USERNAME = "postgres"
$env:DATABASE_PASSWORD = "your_local_password"
```

### 4. Run the application

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

The API is available at `http://localhost:8080`. Flyway applies database migrations during startup.

### 5. Run automated tests

Tests use the separate test database. Set the test variables before running the command.

Linux/macOS:

```bash
export TEST_DB_URL=jdbc:postgresql://localhost:5432/url_shortening_test
export TEST_DB_USERNAME=postgres
export TEST_DB_PASSWORD='your_local_test_password'
./mvnw --batch-mode clean verify
```

Windows PowerShell:

```powershell
$env:TEST_DB_URL = "jdbc:postgresql://localhost:5432/url_shortening_test"
$env:TEST_DB_USERNAME = "postgres"
$env:TEST_DB_PASSWORD = "your_local_test_password"
.\mvnw.cmd --batch-mode clean verify
```

The `verify` lifecycle runs the test suite and fails the build if a test fails. The GitHub Actions workflow provisions a PostgreSQL service and configures the test environment variables automatically.

## Run with Docker

The Dockerfile builds the application JAR in a build stage and runs it in a Java 25 runtime image. The standalone container still needs access to a PostgreSQL database; Docker Compose is the simplest way to run the complete environment.

Build the image:

```bash
docker build -t url-shortening-service:local .
```

Run the container after ensuring PostgreSQL is reachable from the container. Supply the connection settings for a database accessible to Docker:

```bash
docker run --rm --name url-shortening-service \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e DATABASE_URL="jdbc:postgresql://HOST_REACHABLE_FROM_CONTAINER:5432/url_shortening" \
  -e DATABASE_USERNAME="postgres" \
  -e DATABASE_PASSWORD="your_database_password" \
  url-shortening-service:local
```

Replace `HOST_REACHABLE_FROM_CONTAINER` with a hostname or address reachable from the container. `localhost` inside the container refers to the container itself, not your host machine. Ensure the database exists and is reachable before starting the application.

## Run the complete environment with Docker Compose

Docker Compose starts PostgreSQL and the application, waits for the database health check, and persists database files in a named volume.

1. Create the environment file:

   ```bash
   cp .env.example .env
   ```

   On Windows PowerShell, use `Copy-Item .env.example .env`.

2. Edit `.env` and set `POSTGRES_PASSWORD` to a non-placeholder local password.

3. Build and start the services:

   ```bash
   docker compose up --build -d
   ```

4. Follow the application logs if needed:

   ```bash
   docker compose logs -f app
   ```

5. Open the API at `http://localhost:8080` and the interactive API documentation at `http://localhost:8080/swagger-ui/index.html`.

Stop the services while retaining database data:

```bash
docker compose down
```

To also delete the persisted database volume and its data, run:

```bash
docker compose down -v
```

Use the volume-removal command only when you intentionally want to discard the local database.

## API overview

The API uses JSON for request and response bodies. The main endpoints are:

| Method | Endpoint | Description | Success response |
| --- | --- | --- | --- |
| `POST` | `/api/urls` | Create a shortened URL | `201 Created` |
| `GET` | `/api/urls/{shortCode}` | Retrieve shortened URL details | `200 OK` |
| `PUT` | `/api/urls/{shortCode}` | Update the destination URL | `200 OK` |
| `DELETE` | `/api/urls/{shortCode}` | Delete a shortened URL | `204 No Content` |
| `GET` | `/api/urls/{shortCode}/statistics` | Retrieve URL statistics | `200 OK` |
| `GET` | `/{shortCode}` | Redirect to the original destination | `302 Found` |

Example request to create a short URL:

```bash
curl -X POST http://localhost:8080/api/urls \
  -H "Content-Type: application/json" \
  -d '{"originalUrl":"https://example.com/articles"}'
```

The response includes the generated `shortCode`. Open `http://localhost:8080/{shortCode}` to follow the redirect, replacing `{shortCode}` with the returned value.

### API documentation

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON generated by springdoc: `http://localhost:8080/v3/api-docs`
- OpenAPI YAML file in the repository: [`docs/openapi.yaml`](docs/openapi.yaml)
- OpenAPI YAML served by the application: `http://localhost:8080/docs/openapi.yaml`

### Error responses

API errors use a structured response containing `timestamp`, `status`, `error`, `message`, and `path`. Invalid request data is rejected with `400 Bad Request`; requests for unknown short codes return `404 Not Found`. Unexpected server errors are handled centrally and should not expose internal implementation details.

## Project structure

```text
.
├── .github/workflows/ci.yml
├── docs/
│   ├── openapi.yaml
│   └── technical-decisions.md
├── src/
│   ├── main/
│   │   ├── java/com/cleoaguiar/urlshorteningservice/
│   │   │   ├── controller/       # REST endpoints and redirects
│   │   │   ├── domain/entity/    # JPA entities
│   │   │   ├── dto/              # Request and response objects
│   │   │   ├── exception/        # API errors and exception handling
│   │   │   ├── repository/       # Persistence access
│   │   │   └── service/          # Business logic and code generation
│   │   └── resources/
│   │       ├── db/migration/     # Flyway SQL migrations
│   │       ├── application.properties
│   │       └── application-dev.properties
│   └── test/
│       ├── java/                 # Unit and integration tests
│       └── resources/
│           └── application-test.properties
├── .env.example
├── Dockerfile
├── compose.yaml
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

## Technical decisions

See [docs/technical-decisions.md](docs/technical-decisions.md) for the architecture, important implementation choices, and their trade-offs.

## Continuous integration

The GitHub Actions workflow runs on pushes and pull requests targeting `develop` or `main`. It provisions PostgreSQL, configures the test environment variables, and runs:

```bash
./mvnw --batch-mode clean verify
```

A failed test causes the workflow to fail.

## Project reference

This project follows the scope of the [URL Shortening Service project on roadmap.sh](https://roadmap.sh/projects/url-shortening-service).
