# Scalable Order Processing System

Backend project built using Java Spring Boot and MySQL.

## Features
- User login and registration using JWT
- Product and inventory management
- Order creation system
- Payment simulation
- Notification service
- Swagger API testing
- Redis caching
- Kafka-based async processing

## Technologies Used
- Java
- Spring Boot
- MySQL
- Spring Data JPA
- JWT
- Maven
- Swagger
- Redis
- Kafka

## Architecture
The project follows:
- Controller Layer
- Service Layer
- Repository Layer

for clean backend structure.

## Database Tables
- users
- products
- inventory
- orders
- order_items
- payments
- notifications

## How To Run

```bash
mvn spring-boot:run
```
Swagger UI:
http://localhost:8080/swagger-ui.html

## API Testing

Open Swagger UI in browser:

http://localhost:8080/swagger-ui.html

You can test all APIs from Swagger.

## What I Learned
- Spring Boot backend development
- REST API creation
- MySQL database connection
- JWT authentication
- Maven project management
- Backend debugging
- Layered architecture
