# Subscription Manager — REST API

A Spring Boot REST API for managing personal subscriptions with full authentication, email verification, JWT-based security, multi-currency support, and spending reports.

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Environment Variables](#environment-variables)
  - [Running Locally](#running-locally)
  - [Running with Docker](#running-with-docker)
- [API Reference](#api-reference)
  - [Auth](#auth)
  - [User](#user)
  - [Subscriptions](#subscriptions)
  - [Reports](#reports)
- [Security](#security)
- [Database Migrations](#database-migrations)
- [CI/CD](#cicd)

---

## Features

- User registration with **email confirmation**
- JWT authentication with **refresh token rotation**
- Refresh tokens stored as **SHA-256 hashes** (never in plain text)
- Automatic cleanup of expired tokens (scheduled, configurable interval)
- CRUD for subscriptions with **pagination**
- **Multi-currency support** (PLN, USD, EUR) with live exchange rates from NBP API
- Exchange rates **cached in Redis** (24h TTL)
- **Spending reports** — summary, breakdown by billing model, breakdown by currency
- Password change and password reset via email token
- **Rate limiting** on login and registration endpoints (Bucket4j + Redis)
- Global exception handling with consistent API error responses
- Database schema managed by **Flyway**
- OpenAPI 3.0 / Swagger UI documentation (disabled in production)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.3 |
| Security | Spring Security + JWT (jjwt 0.12.6) |
| Database | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Migrations | Flyway |
| Cache / Rate Limiting | Redis + Bucket4j 8.10.1 |
| Redis Client | Lettuce |
| Mapping | MapStruct 1.5.5 |
| Email | Spring Mail + Thymeleaf templates |
| Exchange Rates | NBP API (National Bank of Poland) |
| API Docs | SpringDoc OpenAPI 2.8.5 (Swagger UI) |
| Build | Maven |
| Containerization | Docker (multi-stage build) |
| CI/CD | Jenkins |

---

## Project Structure

```
src/main/java/com/pawlak/subscription/
├── auth/                        # Login, logout, token refresh
│   ├── controller/
│   ├── service/
│   └── dto/
├── config/                      # OpenAPI configuration
├── currency/                    # Multi-currency support, NBP integration
│   └── dto/
├── email/                       # Async email sending service
├── exception/                   # Global exception handling
│   ├── base/                    # BusinessException base class
│   ├── domain/                  # Domain-specific exceptions
│   └── handler/                 # GlobalExceptionHandler
├── ratelimit/                   # Rate limiting (Bucket4j + Redis)
├── response/                    # ApiResponse wrapper, PaginationMetadata
├── security/
│   ├── config/                  # SecurityConfig, AuthenticationManagerConfig
│   ├── jwt/                     # JwtService, JwtAuthenticationFilter
│   └── refresh/                 # RefreshToken entity, service, cleanup
├── subscription/                # Subscription domain
│   ├── controller/
│   ├── service/                 # SubscriptionService, SubscriptionReportService
│   ├── mapper/
│   ├── model/
│   ├── repository/
│   └── dto/
├── token/                       # Registration and password reset tokens
│   ├── registrationtoken/
│   ├── resetpasswordtoken/
│   └── emailbuilder/
└── user/                        # User registration and management
    ├── controller/
    ├── service/
    ├── model/
    ├── repository/
    └── dto/

src/main/resources/
├── application.yml              # Base configuration
├── application-dev.yml          # Development profile
├── application-prod.yml         # Production profile
├── db/migration/                # Flyway SQL migrations
│   └── V1__init_schema.sql
└── templates/                   # Thymeleaf email templates
```

---

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+
- PostgreSQL 16
- Redis
- SMTP server (e.g. Gmail, Mailtrap)

### Environment Variables

Create a `.env` file or set the following variables in your environment:

| Variable | Description |
|---|---|
| `DB_URL` | JDBC URL, e.g. `jdbc:postgresql://localhost:5432/subscription` |
| `DB_USER` | Database username |
| `DB_PASSWORD` | Database password |
| `JWT_SECRET` | Base64-encoded secret key (min. 256-bit) |
| `JWT_EXPIRATION_TIME` | Access token expiry in ms, e.g. `900000` (15 min) |
| `EMAIL_SMTP_URL` | SMTP host, e.g. `smtp.gmail.com` |
| `EMAIL_USER` | SMTP username |
| `MAIL_PASSWORD` | SMTP password |
| `BASE_URL` | Base URL for email links, e.g. `http://localhost:8080` |
| `CORS_ALLOWED_ORIGINS` | Allowed frontend origins, e.g. `http://localhost:3000` |
| `REDIS_HOST` | Redis host, e.g. `localhost` |
| `REDIS_PORT` | Redis port, e.g. `6379` |
| `REDIS_PASSWORD` | Redis password (optional) |

### Running Locally

```bash
# Clone the repository
git clone https://github.com/your-username/subscription.git
cd subscription

# Start PostgreSQL and Redis
docker-compose -f docker-compose.dev.yml up -d

# Run with dev profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The application starts on `http://localhost:8080`.
Swagger UI is available at `http://localhost:8080/swagger-ui/index.html`.

### Running with Docker

```bash
# Build the image
docker build -t subscription-backend .

# Run the container
docker run -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/subscription \
  -e DB_USER=postgres \
  -e DB_PASSWORD=secret \
  -e JWT_SECRET=your_base64_secret \
  -e JWT_EXPIRATION_TIME=900000 \
  -e EMAIL_SMTP_URL=smtp.gmail.com \
  -e EMAIL_USER=you@gmail.com \
  -e MAIL_PASSWORD=your_app_password \
  -e BASE_URL=http://localhost:8080 \
  -e CORS_ALLOWED_ORIGINS=http://localhost:3000 \
  -e REDIS_HOST=host.docker.internal \
  -e REDIS_PORT=6379 \
  subscription-backend
```

---

## API Reference

All responses follow a consistent envelope:

```json
{
  "message": "...",
  "data": {},
  "pagination": null,
  "timestamp": "2025-01-01T12:00:00Z"
}
```

Error responses:

```json
{
  "message": "Validation error description",
  "data": null,
  "timestamp": "2025-01-01T12:00:00Z"
}
```

---

### Auth

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/auth/login` | No | Authenticate user, returns access + refresh token |
| `POST` | `/api/auth/refresh` | No | Rotate refresh token, returns new token pair |
| `POST` | `/api/auth/logout` | No | Revoke refresh token |

> Login is rate limited to **10 requests per 60 seconds** per IP.

**Login request:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Login response:**
```json
{
  "message": "Login successful",
  "data": {
    "role": "USER",
    "accessToken": "eyJ...",
    "refreshToken": "550e8400-..."
  }
}
```

---

### User

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/user/register` | No | Register a new user |
| `GET` | `/api/user/confirm?token=` | No | Confirm account via email token |
| `PATCH` | `/api/user/currency` | Yes | Change preferred display currency |
| `POST` | `/api/user/change-password` | Yes | Change password (revokes all sessions) |
| `POST` | `/api/user/reset-password` | No | Send password reset link to email |
| `POST` | `/api/user/set-new-password` | Yes | Set new password using reset token |

> Registration is rate limited to **5 requests per hour** per IP.

**Register request:**
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "secret123"
}
```

**Change currency request:**
```json
{
  "currency": "USD"
}
```
Supported values: `PLN`, `USD`, `EUR`

---

### Subscriptions

All subscription endpoints require a valid `Authorization: Bearer <token>` header.

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/subscriptions` | List all subscriptions (paginated) |
| `GET` | `/api/subscriptions/{id}` | Get subscription by ID |
| `POST` | `/api/subscriptions` | Create a new subscription |
| `PUT` | `/api/subscriptions/{id}` | Update a subscription |
| `DELETE` | `/api/subscriptions/{id}` | Delete a subscription |

**Pagination query params:** `page` (default: 0), `size` (default: 20), `sort`

**Create/update request:**
```json
{
  "name": "Netflix",
  "description": "Streaming service",
  "price": 49.99,
  "currency": "PLN",
  "subscriptionModel": "MONTHLY"
}
```

Supported `currency` values: `PLN`, `USD`, `EUR`
Supported `subscriptionModel` values: `MONTHLY`, `YEARLY`

**Subscription response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Netflix",
  "description": "Streaming service",
  "price": 49.99,
  "currency": "PLN",
  "subscriptionModel": "MONTHLY",
  "convertedPrice": 12.34,
  "displayCurrency": "USD"
}
```

`convertedPrice` and `displayCurrency` reflect the user's preferred currency setting.

---

### Reports

All report endpoints require a valid `Authorization: Bearer <token>` header.
All costs are normalized to **monthly** and converted to the user's preferred currency.
`YEARLY` subscriptions are divided by 12 for monthly cost calculation.

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/subscriptions/report/summary` | Total count, monthly and yearly spend |
| `GET` | `/api/subscriptions/report/by-model` | Breakdown by billing model (MONTHLY/YEARLY) |
| `GET` | `/api/subscriptions/report/by-currency` | Breakdown by original subscription currency |

**Summary response:**
```json
{
  "message": "Summary retrieved",
  "data": {
    "totalCount": 8,
    "monthlyCost": 142.50,
    "yearlyCost": 1710.00,
    "currency": "PLN"
  }
}
```

**By model response:**
```json
{
  "message": "Breakdown by model retrieved",
  "data": [
    { "model": "MONTHLY", "count": 5, "monthlyCost": 89.99, "currency": "PLN" },
    { "model": "YEARLY",  "count": 3, "monthlyCost": 52.51, "currency": "PLN" }
  ]
}
```

**By currency response:**
```json
{
  "message": "Breakdown by currency retrieved",
  "data": [
    { "currency": "EUR", "count": 1, "monthlyCost": 17.51, "displayCurrency": "PLN" },
    { "currency": "PLN", "count": 4, "monthlyCost": 79.99, "displayCurrency": "PLN" },
    { "currency": "USD", "count": 3, "monthlyCost": 45.00, "displayCurrency": "PLN" }
  ]
}
```

---

## Security

- **Access token:** JWT, expires in 15 minutes (configurable via `JWT_EXPIRATION_TIME`)
- **Refresh token:** rotated on every use, stored as SHA-256 hash, never in plain text
- **Session expiry:** long-lived session invalidation independent of token rotation
- **Password hashing:** BCrypt
- **Rate limiting:** Bucket4j + Redis on `/api/auth/login` and `/api/user/register`
- **CORS:** restricted to origins defined in `CORS_ALLOWED_ORIGINS` (supports `PATCH`)
- **Session policy:** stateless (no server-side HTTP sessions)
- **Swagger UI:** disabled in production profile
- Expired and revoked tokens are purged automatically (configurable via `cleanup.refresh-token.interval-ms`)

---

## Database Migrations

Schema is managed by **Flyway**. Migrations are located in `src/main/resources/db/migration/`.

| Version | Description |
|---|---|
| `V1` | Initial schema — all tables and indexes |

On startup, Flyway automatically applies any pending migrations.
`ddl-auto` is set to `validate` — Hibernate will not modify the schema.

To add a new migration, create a file following the naming convention:
```
V{version}__{description}.sql
```
Example: `V2__add_subscription_start_date.sql`

> Never modify an already-applied migration — Flyway validates checksums on every startup.

---

## CI/CD

The project includes a `Jenkinsfile` with the following pipeline:

1. **Checkout** — pull source from SCM
2. **Build** — build Docker image tagged with `BUILD_NUMBER` and `latest`
3. **Deploy** — stop old container, start new one with production profile on port `8082`
4. **Health check** — verify `/actuator/health` endpoint (5 retries, 10s interval)
5. **Cleanup** — prune old Docker images

On failure, the container logs are printed and the container is removed automatically.
