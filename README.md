# Scalable Order Processing System

Interview-ready Java Spring Boot backend for authentication, users, products, inventory, orders, payment simulation, notifications, Redis caching, Kafka async processing, JWT security, rate limiting, Swagger, MySQL, and Docker.

## Project Structure

```text
scalable-order-processing-system
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── sql/schema.sql
└── src/main
    ├── java/com/interview/ordersystem
    │   ├── ScalableOrderProcessingSystemApplication.java
    │   ├── auth
    │   ├── common
    │   ├── config
    │   ├── exception
    │   ├── gateway
    │   ├── inventory
    │   ├── messaging
    │   ├── notification
    │   ├── order
    │   ├── payment
    │   ├── product
    │   ├── security
    │   └── user
    └── resources/application.properties
```

## How To Run

```bash
docker compose up -d mysql redis zookeeper kafka
mvn spring-boot:run
```

Open:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health check: `http://localhost:8080/actuator/health`
- Gateway routes: `http://localhost:8080/api/gateway/routes`

Docker app build:

```bash
docker build -t scalable-order-processing-system .
docker run --network host scalable-order-processing-system
```

## Main API Flow

1. Register admin.
2. Register customer.
3. Login and copy the JWT token.
4. Admin creates product.
5. Admin adds inventory.
6. Customer creates order.
7. Order event is published to Kafka.
8. Kafka consumer simulates payment.
9. Payment updates order status and creates notification.

## Postman / cURL Examples

Register admin:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Admin","email":"admin@example.com","password":"secret123","role":"ADMIN"}'
```

Register customer:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Customer","email":"customer@example.com","password":"secret123","role":"CUSTOMER"}'
```

Login:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"secret123"}'
```

Create product:

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"sku":"PHONE-001","name":"Smartphone","description":"Demo phone","price":20000}'
```

Add inventory:

```bash
curl -X PUT http://localhost:8080/api/inventory \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"productId":1,"quantity":50}'
```

Create order:

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer YOUR_CUSTOMER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"items":[{"productId":1,"quantity":2}]}'
```

## Module Notes And Interview Answers

### 1. Authentication Service

Files: `auth`, `security`, `user`.

What it does: registers users, stores BCrypt password hashes, logs users in, and returns JWT tokens.

Interview answer: "I used stateless JWT authentication so the backend does not need server-side sessions. This makes horizontal scaling easier because any application instance can validate the token using the same signing secret."

HR-friendly: "Users can securely sign up and log in. The system gives them a secure pass token for later requests."

Technical: `AuthController` receives DTOs, `AuthService` validates and saves users, `JwtService` signs tokens, and `JwtAuthenticationFilter` reads Bearer tokens before protected APIs.

### 2. User Management

Files: `user`.

What it does: admin can view all users; authenticated users can fetch user details.

Interview answer: "User management is separated from authentication so identity, profile, and role logic can evolve independently."

### 3. Product Service

Files: `product`.

What it does: admin creates, updates, and deactivates products. Anyone can read products.

Interview answer: "Product reads are cached in Redis because product catalog data is read frequently and changes less often than orders."

### 4. Inventory Service

Files: `inventory`.

What it does: admin updates stock; order creation reserves stock.

Interview answer: "I used pessimistic locking while reserving inventory to avoid two concurrent orders overselling the same product."

### 5. Order Service

Files: `order`.

What it does: customer creates orders; system calculates total, reserves inventory, saves order, and publishes a Kafka event.

Interview answer: "Order creation keeps the critical database changes transactional, then delegates payment to Kafka so the request does not wait for every downstream task."

### 6. Payment Simulation

Files: `payment`.

What it does: simulates success or failure and updates order status.

Interview answer: "In real life this would integrate with Razorpay, Stripe, PayU, or an internal payment gateway. Here it is simulated but isolated behind `PaymentService`."

### 7. Notification Service

Files: `notification`.

What it does: stores notification records after payment.

Interview answer: "Notification is separated because email, SMS, push, and WhatsApp can be added later without changing order logic."

### 8. API Gateway Basics

Files: `gateway`.

What it does: exposes route metadata. In a real microservice system this would become Spring Cloud Gateway, NGINX, Kong, or AWS API Gateway.

Interview answer: "An API gateway centralizes routing, authentication, rate limiting, and cross-cutting policies."

### 9. Redis Cache

Files: `ProductService`, `RedisConfig`.

What it does: caches product list and product details.

Interview answer: "Redis reduces repeated database reads for hot data and improves latency."

### 10. Kafka Async Processing

Files: `messaging`.

What it does: order creation publishes `orders.created`; consumer processes payment asynchronously.

Interview answer: "Kafka decouples order and payment modules. If payment is slow, orders can still be accepted and processed from the queue."

### 11. Logging And Monitoring

Files: `NotificationService`, `OrderCreatedConsumer`, `application.properties`.

What it does: logs important async events and exposes Actuator endpoints.

Interview answer: "Actuator gives health and metrics endpoints, which can be connected to Prometheus and Grafana."

### 12. Global Exception Handling

Files: `exception`.

What it does: returns consistent API errors for validation, not found, bad request, forbidden, and unexpected errors.

Interview answer: "Global exception handling keeps controllers clean and makes API responses predictable."

### 13. Rate Limiting Basics

Files: `RateLimitFilter`.

What it does: limits requests per IP per minute.

Interview answer: "Rate limiting protects the system from abuse and accidental traffic spikes. In production I would store counters in Redis for distributed rate limiting."

### 14. Docker Setup

Files: `Dockerfile`, `docker-compose.yml`.

What it does: runs MySQL, Redis, Kafka, and Zookeeper locally.

Interview answer: "Docker Compose makes the developer environment repeatable and close to production dependencies."

## System Design

HLD:

```text
Client/Postman
   |
Spring Security + Rate Limit Filter
   |
REST Controllers
   |
Services
   |
MySQL + Redis + Kafka
   |
Kafka Consumer -> Payment -> Notification
```

LLD:

```text
Controller -> DTO validation -> Service -> Repository -> Entity -> Database
OrderService -> InventoryService.reserve()
OrderService -> KafkaProducerService.publishOrderCreated()
OrderCreatedConsumer -> PaymentService.processPayment()
PaymentService -> NotificationService.create()
```

## Database Design

Tables:

- `users`: stores account and role data.
- `products`: stores product catalog.
- `inventory`: stores stock per product.
- `orders`: stores order header.
- `order_items`: stores products inside an order.
- `payments`: stores simulated payment result.
- `notifications`: stores user notifications.

ER diagram explanation:

```text
User 1 -> many Orders
Order 1 -> many OrderItems
Product 1 -> many OrderItems
Product 1 -> 1 Inventory
Order 1 -> 1 Payment
User 1 -> many Notifications
```

Indexes:

- `users.email`: fast login and unique account lookup.
- `products.sku`: fast product lookup and uniqueness.
- `orders.user_id`: fast customer order history.
- `orders.status`: fast admin filtering by status.
- `inventory.product_id`: fast stock lookup.

## Scalability Explanation

This architecture is scalable because:

- JWT is stateless, so multiple app instances can run behind a load balancer.
- Redis handles hot read traffic for product catalog data.
- Kafka decouples order creation from slower payment and notification work.
- MySQL indexes improve lookup performance.
- Layered architecture keeps modules isolated and easier to split into microservices later.

How Amazon or Flipkart process orders:

1. Validate cart and customer.
2. Reserve inventory.
3. Create order.
4. Send order event to a queue.
5. Process payment.
6. Confirm order.
7. Notify customer.
8. Trigger warehouse and delivery systems.

Synchronous vs asynchronous:

- Synchronous: caller waits for the work to finish.
- Asynchronous: caller submits work and another worker finishes it later.

Message queues improve performance because slow downstream systems do not block the main request path.

Load balancing means traffic is distributed across many application instances.

Horizontal scaling means adding more machines or containers instead of only making one machine bigger.

Microservices means splitting the system into independently deployable services such as auth, catalog, inventory, order, payment, and notification.

## Line-By-Line Learning Guide

Read the project in this order:

1. `ScalableOrderProcessingSystemApplication`: starts Spring Boot and enables caching.
2. `application.properties`: configures MySQL, Redis, Kafka, JWT, Swagger, and Actuator.
3. `SecurityConfig`: defines public and protected routes.
4. `JwtAuthenticationFilter`: extracts JWT from the `Authorization` header.
5. `AuthService`: registers and logs in users.
6. `ProductService`: shows CRUD plus Redis cache annotations.
7. `InventoryService`: shows stock update and locked reservation.
8. `OrderService`: shows transaction, total calculation, inventory reservation, and Kafka publishing.
9. `OrderCreatedConsumer`: listens to Kafka and starts payment.
10. `PaymentService`: updates payment and order status.
11. `GlobalExceptionHandler`: converts exceptions into clean JSON responses.

## Resume Description

Built a scalable order processing backend using Java, Spring Boot, Spring Security, JWT, Spring Data JPA, MySQL, Redis, Kafka, Docker, Swagger, and Actuator. Implemented authentication, role-based access control, product catalog, inventory reservation with locking, asynchronous order payment processing, notification records, global exception handling, rate limiting, caching, and production-style layered architecture.

## LinkedIn Description

I built an interview-ready Scalable Order Processing System with Spring Boot, JWT security, MySQL, Redis caching, Kafka-based async processing, Docker, Swagger, and Actuator. The project covers real backend concepts such as layered architecture, DTOs, inventory locking, event-driven processing, global exception handling, and horizontal scaling readiness.

## GitHub Push Commands

```bash
git init
git add .
git commit -m "Build scalable order processing system"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/scalable-order-processing-system.git
git push -u origin main
```

## Future Improvements

- Split modules into real microservices.
- Add Spring Cloud Gateway.
- Add distributed rate limiting with Redis.
- Add payment gateway integration.
- Add Testcontainers integration tests.
- Add Prometheus and Grafana dashboards.
- Add outbox pattern for guaranteed event publishing.
- Add order cancellation and inventory release.
