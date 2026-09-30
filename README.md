# KoiThe API

Backend REST API for a milk-tea ordering and management system.

## Tech stack

| Area | Technology |
| --- | --- |
| Language & runtime | Java 21 |
| Framework | Spring Boot 3.3.12 |
| Build tool | Maven |
| Web API | Spring Web, Spring Validation |
| Security | Spring Security, JWT (JJWT) |
| Data | Spring Data JPA, PostgreSQL 16, Redis 7 |
| Database migration | Flyway |
| API documentation | Springdoc OpenAPI / Swagger UI |
| Mapping & boilerplate reduction | MapStruct, Lombok |
| Testing | JUnit, Spring Boot Test, Testcontainers |
| Containerization | Docker, Docker Compose |

## Project structure

```text
.
├── .github/                    # GitHub workflows and configuration
├── src/
│   ├── main/
│   │   ├── java/com/milktea/
│   │   │   ├── auth/           # Authentication and JWT endpoints
│   │   │   ├── cart/           # Shopping-cart domain
│   │   │   ├── catalog/        # Products, categories, variants, toppings
│   │   │   ├── common/         # Shared responses, exceptions, auditing
│   │   │   ├── config/         # Security and OpenAPI configuration
│   │   │   ├── customer/       # Customer domain
│   │   │   ├── health/         # Health-check endpoint
│   │   │   ├── order/          # Orders, tables, and shifts
│   │   │   ├── security/       # JWT filter and user details service
│   │   │   ├── user/           # Users and roles
│   │   │   └── MilkTeaApplication.java
│   │   └── resources/
│   │       ├── db/migration/   # Flyway SQL migrations
│   │       └── application*.yml
│   └── test/java/              # Unit and integration tests
├── .env.example                # Local environment-variable template
├── docker-compose.yml          # PostgreSQL, Redis, and backend services
├── Dockerfile                  # Multi-stage backend image build
└── pom.xml                     # Maven dependencies and build configuration
```

## Local development

1. Copy `.env.example` to `.env` and update local values as needed.
2. Start the stack with `docker compose up --build`.
3. The API is exposed on `http://localhost:8081` by default.

## Git hygiene

Do not commit `docs/`, environment files, Maven build output, local Maven cache, IDE files, or any `*.log` file (including Docker build and compose logs).
