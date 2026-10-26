# Technical Decisions

This document records important implementation choices in the URL Shortening Service and the trade-offs they introduce.

## 1. Layered application structure

**Decision:** Separate HTTP controllers, DTOs, services, repositories, entities, and exception handling into packages.

**Rationale:** This keeps transport concerns, business rules, persistence, and error formatting easier to reason about and test independently.

**Trade-off:** The project has more classes and package boundaries than a minimal single-class application. For this service, the separation supports testing and future maintenance.

## 2. Spring Boot, Java 25, and Maven Wrapper

**Decision:** Use Java 25, Spring Boot, and the Maven Wrapper.

**Rationale:** Spring provides the HTTP, dependency-injection, validation, and persistence integration used by the service. The wrapper makes the Maven build reproducible without requiring each developer to install Maven separately.

**Trade-off:** Contributors need a compatible JDK, and using a recent Java version may require compatible tooling in local and CI environments.

## 3. PostgreSQL, Spring Data JPA, and Flyway

**Decision:** Persist shortened URLs in PostgreSQL through Spring Data JPA. Use Flyway versioned SQL migrations to manage the schema, with Hibernate configured to validate rather than create or update it automatically.

**Rationale:** Relational persistence provides durable records and database-enforced uniqueness for short codes. Versioned migrations make schema changes explicit and repeatable.

**Trade-off:** Local development and integration tests require a reachable PostgreSQL instance. Schema changes must be represented by migrations instead of relying on automatic Hibernate schema updates.

## 4. Random short codes with collision checks

**Decision:** Generate seven-character alphanumeric codes using `SecureRandom`, check whether a code already exists, and retry generation up to five times.

**Rationale:** Random codes avoid exposing sequential database identifiers as public short codes. The database also enforces uniqueness.

**Trade-off:** Random generation has a non-zero collision probability, which grows as the number of generated codes grows. The retry limit bounds the work for one request, but the database uniqueness constraint remains essential for correctness under concurrent requests.

## 5. Access-count behavior

**Decision:** Keep an access counter with each shortened URL and expose it through the statistics endpoint.

**Current behavior:** The service increments the counter when `findByShortCode` resolves an existing code; both the details lookup and the redirect flow use that method. Retrieving statistics uses a separate lookup and does not increment the counter.

**Trade-off:** The counter reflects successful code-resolution operations, not necessarily only human browser visits. Automated clients and direct API lookups can affect it.

## 6. Centralized request validation and error handling

**Decision:** Validate incoming request DTOs and centralize exception-to-HTTP-response mapping.

**Rationale:** Clients receive consistent HTTP statuses and a structured error body rather than duplicated handling in each controller.

**Trade-off:** The error response must balance useful client feedback with avoiding leakage of internal implementation details.

## 7. OpenAPI and interactive documentation

**Decision:** Maintain an OpenAPI YAML document and use springdoc-openapi to provide interactive API documentation.

**Rationale:** The static contract can be reviewed and versioned with the source code, while Swagger UI makes it easier to explore and invoke endpoints during development.

**Trade-off:** A static specification can drift from the controllers if it is not updated alongside API changes. Endpoint changes should therefore update `docs/openapi.yaml` and be reviewed against the implementation.

## 8. Docker Compose for the complete local environment

**Decision:** Provide a Docker image for the application and a Compose configuration for the application plus PostgreSQL.

**Rationale:** Compose documents and automates service wiring, database readiness checks, port mapping, and persistent database storage.

**Trade-off:** Containerized development requires Docker. The named PostgreSQL volume persists data across container restarts, so removing containers alone does not reset the database.

## 9. Automated tests in CI

**Decision:** Run `./mvnw --batch-mode clean verify` in GitHub Actions with a dedicated PostgreSQL service and explicit test environment variables.

**Rationale:** Unit and integration tests can run in a clean runner without relying on developer-specific database configuration. A test failure blocks a successful CI result.

**Trade-off:** CI takes longer than a compile-only build and depends on the database service starting successfully. This cost is intentional because the pipeline validates application behavior as well as compilation.
