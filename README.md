# Store Online Shop System

## 🧩 Project Overview

A microservices-based e-commerce platform demonstrating distributed system architecture with independent services for order management, inventory, payment processing, delivery tracking, and email notifications. Built as a full-stack application with Spring Boot backend services and a React frontend.

## ⚙️ Tech Stack

### Backend
- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Build Tool**: Gradle 8.5+
- **ORM**: JPA/Hibernate
- **Database**: PostgreSQL 15+ (5 independent databases)
- **Message Queue**: RabbitMQ (CloudAMQP)
- **Connection Pool**: HikariCP

### Frontend
- **Framework**: React with TypeScript
- **Real-time Communication**: WebSocket (STOMP over SockJS)
- **Build Tool**: npm

### Infrastructure
- **Containerization**: Docker & Docker Compose
- **API Style**: RESTful APIs
- **Architecture Pattern**: Microservices with Database per Service

## 🔍 Key Features

- **Distributed Order Processing**: Implemented end-to-end order lifecycle from creation to delivery with atomic inventory reservations and payment processing
- **Asynchronous Communication**: Integrated RabbitMQ for event-driven architecture enabling decoupled service communication and reliable message delivery
- **Real-time Updates**: WebSocket-based notifications for live order status updates and delivery tracking without page refresh
- **Fault Tolerance**: Designed compensation patterns for payment timeouts, inventory rollbacks, and graceful error handling with non-blocking email notifications
- **Multi-database Architecture**: Implemented database-per-service pattern with 5 independent PostgreSQL databases ensuring data isolation and service independence

## 🧠 What I Learned

- Learned to design distributed systems with proper service boundaries and database-per-service pattern for microservices scalability
- Gained experience implementing event-driven architecture with RabbitMQ for asynchronous communication and service decoupling
- Learned to implement compensation patterns for handling failures in distributed transactions (Saga pattern)
- Developed understanding of real-time communication patterns using WebSocket/STOMP for bidirectional client-server messaging
- Practiced designing for failure with graceful degradation, non-blocking operations, and comprehensive error handling strategies

## 🎥 Demo

<!-- Demo video or GIF link will be added here -->

## 🧪 How to Run Locally

### Prerequisites
- Docker and Docker Compose
- (Optional) Java 17+, Gradle 8.5+, PostgreSQL 15+ for manual setup

### Quick Start with Docker

```bash
# Start all services
docker-compose up -d --build

# Or use the startup script
./docker-start.sh

# Stop all services
./docker-stop.sh
```

The application will be available at:
- **Frontend**: http://localhost:3000
- **Store Backend API**: http://localhost:8080/api
- **Warehouse Service**: http://localhost:8083/api
- **Bank Service**: http://localhost:8082/api
- **Delivery Service**: http://localhost:8084/api
- **Email Service**: http://localhost:8085/api
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)

### Default Configuration
- **Database User**: `postgres`
- **Database Password**: `08310744` (configurable via `POSTGRES_PASSWORD` environment variable)
- **RabbitMQ**: Local instance (guest/guest) or CloudAMQP (configure in `application.yml`)

### Manual Setup (Alternative)

If not using Docker, follow these steps:

1. **Create Databases**:
```bash
psql -U postgres -c "CREATE DATABASE store_backend_db;"
psql -U postgres -c "CREATE DATABASE warehouse_db;"
psql -U postgres -c "CREATE DATABASE bank_db;"
psql -U postgres -c "CREATE DATABASE delivery_db;"
psql -U postgres -c "CREATE DATABASE email_db;"
```

2. **Apply Schemas**:
```bash
psql -U postgres -d store_backend_db -f database_schemas/store_backend_db.sql
psql -U postgres -d warehouse_db -f database_schemas/warehouse_db.sql
psql -U postgres -d bank_db -f database_schemas/bank_db.sql
psql -U postgres -d delivery_db -f database_schemas/delivery_db.sql
psql -U postgres -d email_db -f database_schemas/email_db.sql
```

3. **Start Services**:
```bash
# Terminal 1 - Store Backend
cd store-backend-service && ./gradlew bootRun

# Terminal 2 - Warehouse Service
cd warehouse-service && ./gradlew bootRun

# Terminal 3 - Bank Service
cd bank-service && ./gradlew bootRun

# Terminal 4 - Delivery Service
cd delivery-service && ./gradlew bootRun

# Terminal 5 - Email Service
cd email-service && ./gradlew bootRun

# Terminal 6 - Frontend
cd frontend && npm install && npm start
```

4. **Configure RabbitMQ**:
   - Set up CloudAMQP account (free tier available)
   - Update `application.yml` files with CloudAMQP connection string
   - Or run local RabbitMQ: `docker run -d -p 5672:5672 -p 15672:15672 rabbitmq:3-management`

## 📄 Documentation

<!-- Documentation links will be added here -->
- API Documentation: (to be added)
- Architecture Diagrams: (to be added)
- Testing Guide: (to be added)
- Deployment Guide: (to be added)

---

## 🏗️ Architecture

### Microservices

| Service | Port | Database | Description |
|---------|------|----------|-------------|
| **Store Backend** | 8080 | `store_backend_db` | Core business logic, order management |
| **Warehouse Service** | 8083 | `warehouse_db` | Inventory management and stock reservations |
| **Bank Service** | 8082 | `bank_db` | Payment processing and account management |
| **Delivery Service** | 8084 | `delivery_db` | Package delivery and tracking |
| **Email Service** | 8085 | `email_db` | Email notifications and templates |
| **Frontend** | 3000 | - | React application |

### Communication Patterns

- **REST API**: Synchronous HTTP calls for direct service-to-service communication
- **RabbitMQ**: Asynchronous message queue for event-driven communication
- **WebSocket**: Real-time bidirectional communication for live updates

### Database Architecture

The system uses **5 independent PostgreSQL databases** with **21 tables** total:
- Store Backend: 7 tables (users, products, orders, order_items, etc.)
- Warehouse: 4 tables (warehouses, inventory, reservations, transactions)
- Bank: 4 tables (accounts, transactions, payment_methods, etc.)
- Delivery: 3 tables (deliveries, tracking_events, etc.)
- Email: 3 tables (email_templates, email_queue, email_logs)

---

## 📋 Project Structure

```
.
├── store-backend-service/    # Main order processing service
├── warehouse-service/       # Inventory management service
├── bank-service/            # Payment processing service
├── delivery-service/        # Delivery tracking service
├── email-service/           # Email notification service
├── frontend/                # React frontend application
├── database_schemas/        # Database schema SQL files
├── docker-compose.yml       # Docker orchestration
└── README.md               # This file
```

---

## 📄 License

This project is licensed under the MIT License.
