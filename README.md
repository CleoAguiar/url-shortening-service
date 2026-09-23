# URL Shortening Service

A REST API for creating and managing shortened URLs, developed with Java and Spring Boot.

This project is being developed as part of my backend development roadmap and is based on the URL Shortening Service project proposed by roadmap.sh.

> 🚧 This project is currently under development.

## Project Overview

The URL Shortening Service is a backend application designed to provide the core functionality required for a URL shortening system.

The service will allow URLs to be associated with short, unique codes that can later be used to identify and access the original URLs.

The project focuses on backend development practices such as REST API design, persistence, database migrations, validation, exception handling, automated testing, and continuous integration.

## Technology Stack

* Java 25
* Spring Boot
* Spring Web
* Spring Data JPA
* PostgreSQL
* Flyway
* Jakarta Bean Validation
* Maven
* JUnit 5
* Mockito
* GitHub Actions

## Prerequisites

Before running the application locally, make sure you have installed:

* Java 25
* PostgreSQL
* Git

The project includes the Maven Wrapper, so a separate Maven installation is not required.

## Local Development

### 1. Clone the repository

```bash
git clone https://github.com/cleoaguiar/url-shortening-service
cd url-shortening-service
```

### 2. Create the PostgreSQL database

Create a local PostgreSQL database:

```sql
CREATE DATABASE url_shortening;
```

### 3. Configure environment variables

Configure the environment variables required by the application.

Use the `.env.example` file as a reference for the required variables.

Do not commit database credentials or other sensitive configuration to the repository.

### 4. Run the application

On Linux/macOS:

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Flyway migrations are executed automatically when the application starts.

### 5. Run the tests

On Linux/macOS:

```bash
./mvnw clean verify
```

On Windows:

```powershell
.\mvnw.cmd clean verify
```

## Project Status

🚧 **Work in Progress**

The project is currently under active development.

### Implemented

* PostgreSQL database configuration
* Database migrations with Flyway
* Short URL persistence model
* Short code generation strategy
* Application package structure
* Global exception handling
* URL shortening request validation
* Automated tests for implemented components
* Continuous integration with GitHub Actions

### In Progress / Planned

* URL shortening endpoint
* URL redirection
* URL statistics
* Additional API documentation
* Final integration and end-to-end tests

## Project Reference

This project is based on the [URL Shortening Service project](https://roadmap.sh/projects/url-shortening-service) from roadmap.sh.
