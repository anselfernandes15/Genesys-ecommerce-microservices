# E-Commerce Microservices Application

A microservices-based e-commerce backend built with **Spring Boot 4.0.6**, **Java 21**, **PostgreSQL 16**, and **Docker**. Demonstrates inter-service communication, circuit breaker resilience, and containerized deployment.

## Architecture

```
┌──────────────────┐         REST          ┌──────────────────┐         REST         ┌─────────────────────┐
│  Product Service  │ ◄──────────────────── │  Order Service    │ ──────────────────► │ Notification Service │
│  Port 8083        │  Validate product     │  Port 8084        │  Send confirmation  │  Port 8085           │
│  PostgreSQL       │  \& check stock        │  PostgreSQL       │  (best-effort)      │  In-memory (logs)    │
│  (productdb)      │                       │  (orderdb)        │                     │                      │
│  CRUD APIs        │                       │  + Resilience4j   │                     │  POST /notifications │
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

1. Client sends `POST /orders` with product ID, quantity, and customer details
2. **Order Service** calls **Product Service** (`GET /products/{id}`) to validate the product exists and check stock availability
3. If Product Service is down, Resilience4j circuit breaker triggers a fallback response instead of crashing
4. Order Service calculates total price (`unit\_price × quantity`) and saves the order to PostgreSQL
5. Order Service calls **Notification Service** (`POST /notifications`) to log the order confirmation
6. If Notification Service is down, the order still succeeds (best-effort notification with try-catch)

## Tech Stack

|Technology|Purpose|
|-|-|
|Java 21|Language|
|Spring Boot 4.0.6|Application framework|
|Spring Data JPA|Database access with Hibernate ORM|
|PostgreSQL 16|Relational database (Docker)|
|Resilience4j|Circuit breaker for inter-service fault tolerance|
|Swagger / springdoc-openapi|Interactive API documentation|
|JUnit 5 + Mockito|Unit testing|
|Docker + Docker Compose|Containerization and orchestration|
|Lombok|Reduce boilerplate code|

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
└── config/            App configuration, JPA auditing, RestTemplate
```

## API Endpoints

### Product Service (port 8083)

|Method|URL|Description|
|-|-|-|
|POST|`/products`|Create a new product|
|GET|`/products`|List all active products|
|GET|`/products/{id}`|Get product by ID|
|PUT|`/products/{id}`|Update a product|
|DELETE|`/products/{id}`|Soft delete (sets active=false)|
|GET|`/products/category/{category}`|Filter products by category|

### Order Service (port 8084)

|Method|URL|Description|
|-|-|-|
|POST|`/orders`|Place a new order (validates with Product Service)|
|GET|`/orders`|List all orders|
|GET|`/orders/{id}`|Get order by ID|

### Notification Service (port 8085)

|Method|URL|Description|
|-|-|-|
|POST|`/notifications`|Receive and log order notification|

## Quick Start

### Prerequisites

* Docker Desktop
* Git

### Run with Docker Compose (recommended)

```bash
git clone https://github.com/anselfernandes15/Genesys-ecommerce-microservices.git
cd Genesys-ecommerce-microservices
docker-compose up --build
```

This starts all four containers (PostgreSQL + 3 services). Both databases (`productdb` and `orderdb`) are created automatically via `init.sql`.

### Test the Application

1. **Create a product:**

```bash
curl -X POST http://localhost:8083/products \\
  -H "Content-Type: application/json" \\
  -d '{"name":"Wireless Headphones","description":"Noise cancelling","price":79.99,"stock\_quantity":150,"sku":"WH-1000","category":"Electronics"}'
```

2. **Place an order:**

```bash
curl -X POST http://localhost:8084/orders \\
  -H "Content-Type: application/json" \\
  -d '{"product\_id":1,"quantity":2,"customer\_email":"test@example.com","customer\_name":"John Doe"}'
```

3. **View Swagger UI:**

   * Product Service: http://localhost:8083/swagger-ui.html
   * Order Service: http://localhost:8084/swagger-ui.html
   * Notification Service: http://localhost:8085/swagger-ui.html

### Run Locally (without Docker)

Requires Java 21, Maven, and a PostgreSQL instance on port 5555.

```bash
# Start each service in separate terminals
cd product-service \&\& ./mvnw spring-boot:run
cd order-service \&\& ./mvnw spring-boot:run
cd notification-service \&\& ./mvnw spring-boot:run
```

### Run Tests

```bash
cd product-service \&\& ./mvnw test      # 4 tests
cd order-service \&\& ./mvnw test        # 5 tests
cd notification-service \&\& ./mvnw test  # 2 tests
```

**Total: 11 unit tests across all services**

|Service|Test Class|Tests|What's Covered|
|-|-|-|-|
|Product|ProductServiceImplTest|4|Create product, get by ID, not found, soft delete|
|Order|OrderServiceImplTest|5|Place order, product not found, insufficient stock, get by ID, not found|
|Notification|NotificationServiceTest|2|Send confirmation, handle null fields|

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

Common audit fields (`created\_at`, `created\_by`, `updated\_at`, `updated\_by`) are defined once in `BaseEntity` and inherited by all entities. Spring Data JPA Auditing automatically populates these fields. With OAuth2 security, the `AuditorAware` bean would return the authenticated user from the JWT token.

### Why Docker multi-stage builds?

Stage 1 uses a full Maven+JDK image (\~800MB) to compile the code. Stage 2 copies only the JAR into a slim JRE image (\~300MB). The final production image is 60% smaller, reducing deployment time and attack surface.

## What I Would Add in Production

* **API Gateway** (Spring Cloud Gateway) for routing, rate limiting, and edge authentication
* **Service Discovery** (Eureka or Consul) for dynamic service registration
* **Event-Driven Architecture** (Kafka/SQS) for async inter-service communication
* **Centralized Logging** (ELK Stack or CloudWatch) for log aggregation
* **Distributed Tracing** (OpenTelemetry + Jaeger) for end-to-end request tracking
* **Flyway** for versioned database migrations instead of `ddl-auto=update`
* **Spring Security + OAuth2/JWT** for authentication and authorization
* **Integration Tests** with Testcontainers for real database testing
* **CI/CD Pipeline** (GitHub Actions) for automated build, test, and deployment
* **Common Module** for shared code (BaseEntity, exceptions) across services

