# Expense Tracker

REST API for tracking personal expenses with monthly budgets and reports.

## Tech stack

Java 21, Spring Boot, Spring Security + JWT, PostgreSQL, Flyway (database migrations), Docker, GitHub Actions, JUnit + Testcontainers.

## Features

- Registration and login with JWT authentication
- Expense categories (each user sees only their own data)
- Expenses with filtering by period and category, and sorting
- Monthly budget limit per category; creating an expense returns a `budgetExceeded` warning
- Monthly report by category with comparison to the previous month
- Swagger UI for API documentation
- Integration tests running against a real PostgreSQL in a container (Testcontainers)

## Running with Docker

1. Create a `.env` file from the template:
   ```
   cp .env.example .env
   ```
2. Fill in the values. For `JWT_SECRET` use a long random value, for example the output of `openssl rand -base64 32`. Never commit `.env`.
3. Start the application together with the database:
   ```
   docker compose up --build
   ```

The API is available at `http://localhost:8080`.

## Running locally

Start only the database with `docker compose up postgres`, then run the application from your IDE. The default `local` profile reads settings from the same `.env` file.

## Tests

```
mvn test
```

Docker must be running: integration tests start a PostgreSQL container themselves. The same tests run in GitHub Actions on every pull request.

## API documentation

Swagger UI: `http://localhost:8080/swagger-ui/index.html`