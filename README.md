# WorkOrder Hub

![CI](https://github.com/elyesf/workorder-hub/actions/workflows/ci.yml/badge.svg)

A full-stack web app for small field service teams (plumbing, heating, inspection) to create, assign and track work orders.

I built it as a portfolio project to show how I work with **Angular and Spring Boot** end to end. It's the kind of internal tool many companies still run on spreadsheets and email.

**Stack:** Java 21 · Spring Boot 3.5 · Spring Security (JWT) · Spring Data JPA · PostgreSQL · Flyway · Angular 20 · TypeScript · Docker · GitHub Actions

## Features

- Log in and register. The API issues a signed JWT, and roles control access (only admins can delete).
- Work order list with status cards, overdue count, text search, status filter and paging.
- Create and edit work orders with validation on both client and server.
- Status workflow with enforced rules: `OPEN -> IN_PROGRESS -> DONE`, cancel anytime before done. The API tells the UI which next steps are allowed.
- Optimistic locking: if two people save the same order, the second gets a clear 409 instead of overwriting.
- API docs with Swagger UI at `/swagger-ui.html`.

## Run it

**With Docker (everything in one command):**

```bash
docker compose up --build
```

Open http://localhost:4200 and log in with `demo@workorderhub.dev` / `demo12345` (demo data is created on first start).

**Without Docker:**

```bash
# API on :8080, in-memory H2 database with demo data
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=h2

# Angular on :4200, /api is proxied to :8080
cd frontend
npm install
npm start
```

**Tests:**

```bash
cd backend && mvn verify           # unit + MockMvc integration tests on H2
cd frontend && npm run test:ci     # Karma + Jasmine, headless Chrome
```

## Project structure

```
backend/
  auth/        registration, login, token issuing
  workorder/   entity, status rules, service, REST controller
  config/      security, JWT keys, CORS, OpenAPI, clock
  common/      error handling, paging response
  resources/db/migration/   Flyway SQL
frontend/src/app/
  core/        API services, auth service, interceptor, guard, models
  features/    login, work order list, work order form
  shared/      status label pipe, error message helper
```

## Design decisions

| Decision | Why |
|---|---|
| Flyway owns the schema, `ddl-auto: none` | Schema changes are versioned SQL files, never generated silently by Hibernate. |
| JWT signed with HS256 using Spring's resource server | No session state on the server, and token validation is standard Spring Security, not a hand-written filter. |
| Status rules live in the `WorkOrderStatus` enum | One place for the rules. The entity and the UI both use them, so the frontend never offers a step the API would reject. |
| Separate `PATCH /status` endpoint | Changing status is a business action with its own rules, not a field edit. |
| JPA Specifications for filters | Each optional filter is only added when it's used, instead of one large query full of `is null` checks. |
| `open-in-view: false`, DTO mapping inside transactions | No lazy-loading queries hidden in the web layer. |
| Injected `Clock` | "Today" is used for overdue checks, and a fixed clock makes that testable. |
| Standalone components, signals, functional interceptor | Current Angular style, with no NgModules. |

## API overview

| Method | Path | Notes |
|---|---|---|
| POST | `/api/auth/register` | returns a token |
| POST | `/api/auth/login` | returns a token |
| GET | `/api/work-orders?status=&q=&page=&size=` | paged list |
| GET | `/api/work-orders/stats` | counts per status, overdue |
| GET | `/api/work-orders/{id}` | |
| POST | `/api/work-orders` | create |
| PUT | `/api/work-orders/{id}` | edit fields |
| PATCH | `/api/work-orders/{id}/status` | change status, 409 if not allowed |
| DELETE | `/api/work-orders/{id}` | admin only |

## Next steps

- Attachments (photos from the field)
- Email notification when an order is assigned
- Testcontainers instead of H2 for integration tests

---

Elyes Zarrad · [LinkedIn](https://www.linkedin.com/in/Elyes-Zarrad)
