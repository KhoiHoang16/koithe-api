# KoiThe API

Backend REST API cho hệ thống POS và đặt hàng đa kênh MilkTea. 


## Tech stack

| Area | Technology |
|---|---|
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

    .
    ├── docs/                       # Partner guides, API collection, architecture notes
    ├── src/main/java/com/milktea/
    │   ├── auth/                   # Login, register, refresh, logout
    │   ├── cart/                   # Cart and cart items
    │   ├── catalog/                # Categories, products, variants, toppings
    │   ├── common/                 # Shared responses, exceptions, audit base
    │   ├── config/                 # Security and OpenAPI configuration
    │   ├── customer/               # Customer profile / loyalty scaffold
    │   ├── health/                 # Health endpoint
    │   ├── inventory/              # Suppliers and purchase orders
    │   ├── order/                  # POS/omnichannel orders and order lines
    │   ├── payment/                # Transactions and gateway adapters
    │   ├── promotion/              # Campaigns, discount rules, vouchers
    │   ├── report/                 # Summary report scaffold
    │   ├── security/               # JWT filter and user principal
    │   ├── shift/                  # Cashier shift scaffold
    │   ├── table/                  # Table and QR scaffold
    │   ├── user/                   # Users, roles, repositories
    │   └── MilkTeaApplication.java
    ├── src/main/resources/db/migration/ # Flyway schema/seed/index migrations (V1–V5)
    ├── src/test/java/              # Existing unit/controller tests (coverage is partial)
    ├── .env.example                # Local environment-variable template
    ├── docker-compose.yml          # PostgreSQL, Redis, backend services
    ├── Dockerfile                  # Backend container image
    └── pom.xml                     # Maven dependencies and build




