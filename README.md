# E-Commerce Microservices Application

A microservices-based e-commerce backend built with **Spring Boot 4.0.6**, **Java 21**, **PostgreSQL 16**, and **Docker**. Demonstrates inter-service communication, circuit breaker resilience, JWT authentication, and containerized deployment.

## Architecture

```
┌──────────────────┐         REST          ┌──────────────────┐         REST         ┌─────────────────────┐
│  Product Service  │ ◄──────────────────── │  Order Service    │ ──────────────────► │ Notification Service │
│  Port 8083        │  Validate product     │  Port 8084        │  Send confirmation  │  Port 8085           │
│  PostgreSQL       │  (service-to-service  │  PostgreSQL       │  (best-effort)      │  In-memory (logs)    │
│  (productdb)      │   JWT auth)           │  (orderdb)        │                     │                      │
│  + Spring Security│                       │  + Resilience4j   │                     │  POST /notifications │
│  + JWT Auth       │                       │  + Spring Security│                     │                      │
└──────────────────┘                       └──────────────────┘                     └─────────────────────┘
                                                    │
                                          ┌─────────▼─────────┐
                                          │   PostgreSQL 16    │
                                          │   Docker           │
                                          │   productdb        │
                                          │   orderdb          │
                                          └───────────────────┘
```

## Order Flow

1. Client authenticates via `POST /auth/token` and receives a JWT token
2. Client sends `POST /orders` with JWT token in Authorization header
3. **Order Service** validates the JWT token via Spring Security filter
4. **Order Service** calls **Product Service** (`GET /products/{id}`) with a service-to-service JWT token to validate the product and check stock
5. If Product Service is down, Resilience4j circuit breaker triggers a fallback response instead of crashing
6. Order Service calculates total price (`unit_price × quantity`) and saves the order to PostgreSQL
7. Order Service calls **Notification Service** (`POST /notifications`) to log the order confirmation
8. If Notification Service is down, the order still succeeds (best-effort notification with try-catch)
9. The authenticated user is recorded in `created_by` audit field via JPA Auditing + SecurityContext integration

## Tech Stack

| Technology | Purpose |
|---|---|
| Java 21 | Language |
| Spring Boot 4.0.6 | Application framework |
| Spring Data JPA | Database access with Hibernate ORM |
| Spring Security + JWT | Authentication and authorization |
| PostgreSQL 16 | Relational database (Docker) |
| Resilience4j | Circuit breaker for inter-service fault tolerance |
| Swagger / springdoc-openapi | Interactive API documentation |
| JUnit 5 + Mockito + MockMvc | Unit and integration testing |
| Docker + Docker Compose | Containerization and orchestration |
| Lombok | Reduce boilerplate code |

## Project Structure

Each service follows a layered architecture:

```
service/
├── controller/        REST API endpoints
├── service/           Business logic (interface + implementation)
├── repository/        Data access layer (Spring Data JPA)
├── model/
│   ├── entity/        Database entities (JPA)
│   ├── dto/           Request/Response data transfer objects
│   └── enums/         Enum types (OrderStatus)
├── exception/         Custom exceptions + global error handler
├── mapper/            Entity ↔ DTO conversion
├── client/            Inter-service REST clients (Order Service only)
└── config/            Security, JWT, JPA auditing, RestTemplate
```

## API Endpoints

### Product Service (port 8083)

| Method | URL | Auth Required | Description |
|---|---|---|---|
| POST | `/auth/token` | No | Get JWT token |
| POST | `/products` | Yes | Create a new product |
| GET | `/products` | Yes | List all active products |
| GET | `/products/{id}` | Yes | Get product by ID |
| PUT | `/products/{id}` | Yes | Update a product |
| DELETE | `/products/{id}` | Yes | Soft delete (sets active=false) |
| GET | `/products/category/{category}` | Yes | Filter products by category |

### Order Service (port 8084)

| Method | URL | Auth Required | Description |
|---|---|---|---|
| POST | `/auth/token` | No | Get JWT token |
| POST | `/orders` | Yes | Place a new order |
| GET | `/orders` | Yes | List all orders |
| GET | `/orders/{id}` | Yes | Get order by ID |

### Notification Service (port 8085)

| Method | URL | Auth Required | Description |
|---|---|---|---|
| POST | `/notifications` | No | Receive and log order notification |

## Quick Start

### Prerequisites

- Docker Desktop
- Git

### Run with Docker Compose (recommended)

```bash
git clone https://github.com/anselfernandes15/Genesys-ecommerce-microservices.git
cd Genesys-ecommerce-microservices
docker-compose up --build
```

This starts all four containers (PostgreSQL + 3 services). Both databases (`productdb` and `orderdb`) are created automatically via `init.sql`.

### Test the Application

1. **Get a JWT token:**
```bash
curl -X POST http://localhost:8083/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

2. **Create a product (with token):**
```bash
curl -X POST http://localhost:8083/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_TOKEN>" \
  -d '{"name":"Wireless Headphones","description":"Noise cancelling","price":79.99,"stock_quantity":150,"sku":"WH-1000","category":"Electronics"}'
```

3. **Place an order (with token):**
```bash
curl -X POST http://localhost:8084/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_TOKEN>" \
  -d '{"product_id":1,"quantity":2,"customer_email":"test@example.com","customer_name":"John Doe"}'
```

4. **View Swagger UI:**
   - Product Service: http://localhost:8083/swagger-ui.html
   - Order Service: http://localhost:8084/swagger-ui.html
   - Notification Service: http://localhost:8085/swagger-ui.html

### Run Locally (without Docker)

Requires Java 21, Maven, and a PostgreSQL instance on port 5555.

```bash
# Start each service in separate terminals
cd product-service && ./mvnw spring-boot:run
cd order-service && ./mvnw spring-boot:run
cd notification-service && ./mvnw spring-boot:run
```

### Run Tests

```bash
cd product-service && ./mvnw test      # 9 tests (4 unit + 5 integration)
cd order-service && ./mvnw test        # 5 tests
cd notification-service && ./mvnw test  # 2 tests
```

**Total: 16 tests across all services**

| Service | Test Class | Type | Tests | What's Covered |
|---|---|---|---|---|
| Product | ProductServiceImplTest | Unit | 4 | Create, get by ID, not found, soft delete |
| Product | ProductControllerTest | Integration | 5 | POST with auth, GET with auth, GET by ID, 403 without token, public token endpoint |
| Order | OrderServiceImplTest | Unit | 5 | Place order, product not found, insufficient stock, get by ID, not found |
| Notification | NotificationServiceTest | Unit | 2 | Send confirmation, handle null fields |

## Security Architecture

### JWT Authentication Flow

```
1. Client → POST /auth/token (username + password)
2. Server validates credentials → returns signed JWT token
3. Client → GET /products (Authorization: Bearer <token>)
4. JwtAuthenticationFilter extracts and validates the token
5. Sets SecurityContext with authenticated user
6. Request proceeds to controller
7. AuditorAware reads SecurityContext → populates created_by field
```

### Service-to-Service Authentication

When Order Service calls Product Service, it generates its own JWT token signed with the same secret key and passes it in the Authorization header. Both services share the same JWT secret, enabling mutual trust without an external auth server.

In production, this would be replaced with an OAuth2 Authorization Server (Keycloak, Okta, or AWS Cognito) issuing tokens that all services validate independently.

## Technical Design Decisions

### Why separate databases per service?

Each microservice owns its data. Product Service uses `productdb`, Order Service uses `orderdb`. There are no cross-database foreign keys. This ensures services can be developed, deployed, and scaled independently. The Order entity stores `productId` as a plain column (not a JPA relationship) because the product lives in a different database.

### Why synchronous REST calls instead of async messaging?

For a three-service application, synchronous REST (via RestTemplate) is simpler to implement and debug. In production at scale, event-driven architecture with Kafka or SQS/SNS would provide better decoupling and reliability for inter-service communication.

### Why Resilience4j circuit breaker?

If Product Service goes down during order placement, without a circuit breaker the Order Service would hang or throw an unhandled exception. Resilience4j wraps the Product Service call with a circuit breaker that fails fast and returns a fallback response. Configuration: 5-call sliding window, 50% failure threshold, 10-second wait in open state.

### Why best-effort notifications?

The Notification Service call is wrapped in a try-catch. A failed notification should never cancel a confirmed order. In production, a message queue (SQS) with a Dead Letter Queue would ensure reliable delivery with retry.

### Why soft delete for products?

`DELETE /products/{id}` sets `active=false` instead of removing the row. This preserves referential integrity for existing orders and retains historical data for reporting.

### Why Interface + Implementation for services?

`ProductService` (interface) defines the contract. `ProductServiceImpl` provides the implementation. This enables loose coupling, easier unit testing with mocks, and supports the Open/Closed Principle.

### Why BaseEntity with JPA Auditing?

Common audit fields (`created_at`, `created_by`, `updated_at`, `updated_by`) are defined once in `BaseEntity` and inherited by all entities. Spring Data JPA Auditing automatically populates these fields. The `AuditorAware` bean reads the authenticated user from `SecurityContextHolder`, so `created_by` reflects the actual JWT user (e.g., "admin") rather than a hardcoded value.

### Why Docker multi-stage builds?

Stage 1 uses a full Maven+JDK image (~800MB) to compile the code. Stage 2 copies only the JAR into a slim JRE image (~300MB). The final production image is 60% smaller, reducing deployment time and attack surface.

## What I Would Add in Production

- **OAuth2 Authorization Server** (Keycloak/Cognito) replacing shared JWT secret with proper token issuing
- **API Gateway** (Spring Cloud Gateway) for routing, rate limiting, and edge authentication
- **Service Discovery** (Eureka or Consul) for dynamic service registration
- **Event-Driven Architecture** (Kafka/SQS) for async inter-service communication
- **Centralized Logging** (ELK Stack or CloudWatch) for log aggregation
- **Distributed Tracing** (OpenTelemetry + Jaeger) for end-to-end request tracking
- **Flyway** for versioned database migrations instead of `ddl-auto=update`
- **Integration Tests** with Testcontainers for real database testing
- **CI/CD Pipeline** (GitHub Actions) for automated build, test, and deployment
- **Common Module** for shared code (BaseEntity, exceptions) across services
