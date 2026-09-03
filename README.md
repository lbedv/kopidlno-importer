# Kopidlno RUIAN Importer

A lightweight, robust Spring Boot application that downloads, parses, and imports RUIAN XML data for the municipality of Kopidlno into a PostgreSQL database.

## How to Run

### Option 1: Docker Compose
This will spin up a PostgreSQL database, automatically initialize the schema, build the Java application, and run the import process.

```bash
docker compose up --build
```

### Option 2: Local Run (Maven)
If you have a PostgreSQL database already running, you can configure the connection in `application.yml` and run the app directly using Maven:

```bash
./mvnw spring-boot:run
```

## Running Tests
Tests are configured to use an isolated in-memory H2 database.
```bash
./mvnw test
```
