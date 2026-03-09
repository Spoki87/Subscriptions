# Subscription Manager — REST API

A Spring Boot REST API for managing personal subscriptions with full authentication, email verification, and JWT-based security.

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
- [Security](#security)
- [CI/CD](#cicd)

---

## Features

- User registration with **email confirmation**
- JWT authentication with **refresh token rotation**
- Refresh tokens stored as **SHA-256 hashes** (never in plain text)
- Automatic cleanup of expired tokens (scheduled hourly)
- CRUD for subscriptions with **pagination**
- Password change and password reset via email token
- Global exception handling with consistent API error responses
- OpenAPI 3.0 / Swagger UI documentation

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.0.3 |
| Security | Spring Security + JWT (jjwt 0.12.6) |
| Database | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Mapping | MapStruct 1.5.5 |
| Email | Spring Mail + Thymeleaf templates |
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
├── email/                       # Async email sending service
├── exception/                   # Global exception handling
│   ├── base/                    # BusinessException base class
│   ├── domain/                  # Domain-specific exceptions
│   └── handler/                 # GlobalExceptionHandler
├── response/                    # ApiResponse wrapper, PaginationMetadata
├── security/
│   ├── config/                  # SecurityConfig, AuthenticationManagerConfig
│   ├── jwt/                     # JwtService, JwtAuthenticationFilter
│   └── refresh/                 # RefreshToken entity, service, cleanup
├── subscription/                # Subscription CRUD domain
│   ├── controller/
│   ├── service/
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
```

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.9+
- PostgreSQL
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

### Running Locally

```bash
# Clone the repository
git clone https://github.com/your-username/subscription.git
cd subscription

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
  subscription-backend
```

---

## API Reference

All responses follow a consistent envelope:

```json
{
  "message": "...",
  "data": { },
  "pagination": null,
  "timestamp": "2025-01-01T12:00:00Z"
}
```

### Auth

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/auth/login` | No | Authenticate user, returns access + refresh token |
| `POST` | `/api/auth/refresh` | No | Rotate refresh token, returns new token pair |
| `POST` | `/api/auth/logout` | No | Revoke refresh token |

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
    "refreshToken": "eyJ..."
  }
}
```

### User

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/user/register` | No | Register a new user |
| `GET` | `/api/user/confirm?token=` | No | Confirm account via email token |
| `POST` | `/api/user/change-password` | Yes | Change password using current password |
| `POST` | `/api/user/reset-password` | Yes | Send password reset token to email |
| `POST` | `/api/user/set-new-password` | Yes | Set new password using reset token |

**Register request:**
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "secret123"
}
```

### Subscriptions

All subscription endpoints require a valid `Authorization: Bearer <token>` header.

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/subscriptions` | List all subscriptions (paginated) |
| `GET` | `/api/subscriptions/{id}` | Get subscription by ID |
| `POST` | `/api/subscriptions` | Create a new subscription |
| `PUT` | `/api/subscriptions/{id}` | Update a subscription |
| `DELETE` | `/api/subscriptions/{id}` | Delete a subscription |

**Pagination query params:** `page`, `size`, `sort`

**Create/update request:**
```json
{
  "name": "Netflix",
  "description": "Streaming service",
  "price": 49.99
}
```

**Response:**
```json
{
  "message": "Subscription created",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Netflix",
    "description": "Streaming service",
    "price": 49.99
  },
  "pagination": {
    "totalElements": 5,
    "totalPages": 1,
    "currentPage": 0,
    "pageSize": 10
  }
}
```

---

## Security

- **Access token:** JWT, expires in 15 minutes (configurable)
- **Refresh token:** rotated on every use, stored as SHA-256 hash
- **Session expiry:** long-lived session invalidation supported
- **Password hashing:** BCrypt
- **CORS:** restricted to origins defined in `CORS_ALLOWED_ORIGINS`
- Expired and revoked tokens are purged automatically every hour

---

## CI/CD

The project includes a `Jenkinsfile` with the following pipeline:

1. **Checkout** — pull source from SCM
2. **Build** — build Docker image tagged with `BUILD_NUMBER` and `latest`
3. **Deploy** — stop old container, start new one with production profile on port `8082`
4. **Health check** — verify `/actuator/health` endpoint (5 retries, 10s interval)
5. **Cleanup** — prune old Docker images

On failure, the container logs are printed and the container is removed automatically.
