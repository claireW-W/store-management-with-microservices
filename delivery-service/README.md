# Delivery Service - Store Online Shop

A microservice for handling package delivery, tracking, and logistics management in the Store Online Shop system.

## 🚚 Features

- **Package Delivery Management**: Create and track delivery requests
- **Status Tracking**: Real-time delivery status updates with automatic progression
- **Lost Package Handling**: Configurable lost package probability (default 5%)
- **RabbitMQ Integration**: Asynchronous messaging for status updates
- **RESTful APIs**: 6 comprehensive API endpoints for delivery operations
- **Scheduled Tasks**: Automatic status updates every 5 seconds
- **Database Persistence**: PostgreSQL with JPA/Hibernate
- **JSONB Support**: Advanced address storage with PostgreSQL JSONB

## 🏗️ Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Java Version**: 17
- **Database**: PostgreSQL 15+
- **ORM**: Spring Data JPA / Hibernate
- **Message Queue**: RabbitMQ
- **Build Tool**: Gradle 8.5+
- **Validation**: Jakarta Validation
- **Logging**: SLF4J + Logback
- **Testing**: JUnit 5 + Testcontainers

## 📋 Prerequisites

Before running the application, ensure you have the following installed:

- **Java 17** or higher
- **Gradle 8.5+**
- **PostgreSQL 15+**
- **RabbitMQ** (optional, for message queue functionality)
- **Git**

## 🚀 First Time Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd delivery-service
```

### 2. Database Setup

#### Create Delivery Service Database

```bash
# Connect to PostgreSQL as superuser
psql -U postgres

# Create delivery database
CREATE DATABASE delivery_db;

# Exit psql
\q
```

#### Run Database Schema

```bash
# Apply the delivery service database schema
psql -U postgres -d delivery_db -f ../database_schemas/delivery_db.sql
```

### 3. RabbitMQ Setup (Optional)

RabbitMQ is optional but recommended for full functionality:

```bash
# Using Docker (recommended)
docker run -d --name rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  rabbitmq:3-management

# Or install locally
# brew install rabbitmq  # macOS
# sudo apt-get install rabbitmq-server  # Ubuntu
```

**Note**: The service will work without RabbitMQ, but status update messages won't be sent.

### 4. Environment Configuration

#### Create Environment Variables

```bash
# Create .env file (optional, for local development)
echo "DB_USERNAME=delivery_user" > .env
echo "DB_PASSWORD=delivery_password" >> .env
```

#### Configure Application Properties

The application uses the following default configuration:

```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/delivery_db
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:123}
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
```

### 5. Database Password Configuration

**Important**: Update the database password in configuration files if needed:

```bash
# Update application.yml, application-dev.yml, and application-test.yml
# Change password from 'your_password' to your actual PostgreSQL password
# Default password in config files is '123'
```

#### Configuration Files to Update:

- `src/main/resources/application.yml`
- `src/main/resources/application-dev.yml`
- `src/main/resources/application-test.yml`

#### Common Issues:

- **Password Authentication Failed**: Update password in all config files
- **Connection Refused**: Ensure PostgreSQL is running
- **Database Not Found**: Run database setup steps first

### 6. Build and Run

```bash
# Clean and compile
./gradlew compileJava

# Run tests
./gradlew test

# Package the application
./gradlew build

# Run the application
./gradlew bootRun
```

### 7. Verify Installation

```bash
# Check if the service is running
curl http://localhost:8082/api/delivery/config/lost-probability

# Expected response: JSON with current lost probability configuration
```

## 🔄 Subsequent Runs

### Quick Start (After Initial Setup)

```bash
# Navigate to project directory
cd delivery-service

# Start the application
./gradlew bootRun
```

### Alternative Run Methods

#### Using JAR file

```bash
# Build the JAR
gradle clean build

# Run the JAR
java -jar build/libs/delivery-service-1.0.0.jar
```

#### Using Docker (Optional)

```bash
# Build Docker image
docker build -t delivery-service .

# Run container
docker run -p 8082:8082 \
  -e DB_USERNAME=delivery_user \
  -e DB_PASSWORD=delivery_password \
  delivery-service
```

## 🛠️ Development

### Running in Development Mode

```bash
# Run with development profile
gradle bootRun --args='--spring.profiles.active=dev'
```

### Database Management

#### Reset Database

```bash
# Drop and recreate delivery database
psql -U postgres -c "DROP DATABASE IF EXISTS delivery_db;"
psql -U postgres -c "CREATE DATABASE delivery_db;"
psql -U postgres -d delivery_db -f ../database_schemas/delivery_db.sql
```

#### View Database

```bash
# Connect to delivery database
psql -U postgres -d delivery_db

# List tables (should show 3 tables)
\dt

# View deliveries
SELECT * FROM deliveries LIMIT 10;

# View delivery items
SELECT * FROM delivery_items;

# View delivery status history
SELECT * FROM delivery_status_history;
```

## 📊 API Documentation

### Base URL

```
http://localhost:8082/api/delivery
```

### Endpoints

| Method | Endpoint                     | Description             |
| ------ | ---------------------------- | ----------------------- |
| POST   | `/request`                 | Create delivery request |
| PUT    | `/{deliveryId}/status`     | Update delivery status  |
| POST   | `/{deliveryId}/lost`       | Handle lost package     |
| GET    | `/lost-packages`           | Query lost packages     |
| PUT    | `/config/lost-probability` | Set lost probability    |
| GET    | `/config/lost-probability` | Get lost probability    |

### Example API Calls

#### Create Delivery Request

```bash
curl -X POST http://localhost:8082/api/delivery/request \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-001",
    "customerId": "CUST-001",
    "shippingAddress": "123 Main St, Sydney NSW 2000, Australia",
    "warehouseId": 1,
    "carrier": "Australia Post",
    "notes": "Fragile items - handle with care"
  }'
```

#### Update Delivery Status

```bash
curl -X PUT http://localhost:8082/api/delivery/DEL-123/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "PICKED_UP",
    "location": "Sydney Warehouse",
    "notes": "Package picked up successfully"
  }'
```

#### Handle Lost Package

```bash
curl -X POST http://localhost:8082/api/delivery/DEL-123/lost \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "Package lost during transit",
    "notes": "Last seen at Sydney sorting facility"
  }'
```

#### Set Lost Probability

```bash
curl -X PUT http://localhost:8082/api/delivery/config/lost-probability \
  -H "Content-Type: application/json" \
  -d '{
    "probability": 0.1
  }'
```

## 🧪 Testing

### Run All Tests

```bash
./gradlew test
```

### Run Specific Test Class

```bash
./gradlew test -Dtest=DeliveryServiceTest
```

### Run Integration Tests

```bash
./gradlew test -Dtest=*IntegrationTest
```

### Run API Tests

```bash
# Run the simple test script
./simple_test.sh
```

### Test with Coverage

```bash
./gradlew test jacoco:report
```

## 📈 Monitoring

### Health Check

```bash
curl http://localhost:8082/api/delivery/config/lost-probability
```

### Actuator Endpoints

```bash
# Application info
curl http://localhost:8082/actuator/info

# Health details
curl http://localhost:8082/actuator/health

# Metrics
curl http://localhost:8082/actuator/metrics
```

## 🔧 Configuration

### Environment Variables

| Variable                  | Default       | Description                      |
| ------------------------- | ------------- | -------------------------------- |
| `DB_USERNAME`           | `postgres`  | Database username                |
| `DB_PASSWORD`           | `123`       | Database password                |
| `SERVER_PORT`           | `8082`      | Application port                 |
| `LOG_LEVEL`             | `INFO`      | Logging level                    |
| `DELIVERY_FAILURE_RATE` | `0.05`      | Default lost package probability |
| `RABBITMQ_HOST`         | `localhost` | RabbitMQ host                    |
| `RABBITMQ_PORT`         | `5672`      | RabbitMQ port                    |

### Application Profiles

- **default**: Production configuration
- **dev**: Development configuration (detailed logging)
- **test**: Test configuration (in-memory database)

## 🐛 Troubleshooting

### Common Issues

#### Database Connection Failed

```bash
# Check PostgreSQL is running
sudo systemctl status postgresql

# Check database exists
psql -U postgres -l | grep delivery_db

# Verify delivery_db has 3 tables
psql -U postgres -d delivery_db -c "\dt"
```

#### Port Already in Use

```bash
# Find process using port 8082
lsof -i :8082

# Kill the process
kill -9 <PID>
```

#### RabbitMQ Connection Issues

```bash
# Check if RabbitMQ is running
docker ps | grep rabbitmq

# Check RabbitMQ logs
docker logs rabbitmq

# Restart RabbitMQ
docker restart rabbitmq
```

#### Delivery Status Not Updating

```bash
# Check if scheduled tasks are running
# Look for "Starting automatic delivery status update" in logs

# Check database for active deliveries
psql -U postgres -d delivery_db -c "SELECT * FROM deliveries WHERE status IN ('PENDING_PICKUP', 'PICKED_UP', 'IN_TRANSIT');"
```

#### Build Fails

```bash
# Clean build cache
./gradlew clean

# Update dependencies tree
./gradlew dependencies
```

#### View Application Logs

```bash
# Follow logs in real-time
tail -f logs/application.log

# View error logs only
grep ERROR logs/application.log
## 📁 Project Structure

```delivery-service/
├── src/
│   ├── main/
│   │   ├── java/com/store/delivery/
│   │   │   ├── controller/          # REST API controllers
│   │   │   ├── service/            # Business logic
│   │   │   ├── repository/         # Data access layer
│   │   │   ├── entity/             # Entity models
│   │   │   ├── dto/                # Data transfer objects
│   │   │   ├── config/             # Configuration classes
│   │   │   ├── scheduler/          # Scheduled tasks
│   │   │   ├── errors/             # Exception handling
│   │   │   └── DeliveryApplication.java
│   │   └── resources/
│   │       ├── application.yml     # Main configuration
│   │       ├── application-dev.yml # Development config
│   │       └── application-test.yml # Test config
│   └── test/                       # Test classes
├── build.gradle                    # Gradle configuration
├── settings.gradle                 # Gradle settings
├── gradlew                         # Gradle wrapper (Unix)
├── gradlew.bat                     # Gradle wrapper (Windows)
├── simple_test.sh                  # API test script
├── .gitignore                      # Git ignore rules
└── README.md                       # This file
```

## 🏗️ Microservices Architecture

This Delivery Service is part of a **microservices architecture** with **separate applications and separate databases on one server**:

| Service                    | Database             | Status                        |
| -------------------------- | -------------------- | ----------------------------- |
| Bank Service               | `bank_db`          | 🔄 Team member responsibility |
| Store Backend              | `store_backend_db` | 🔄 Team member responsibility |
| Warehouse Service          | `warehouse_db`     | 🔄 Team member responsibility |
| **Delivery Service** | `delivery_db`      | ✅**Implemented**       |
| Email Service              | `email_db`         | 🔄 Team member responsibility |

### Database Independence

- Each service owns its own PostgreSQL database
- Services cannot directly access other services' databases
- Inter-service communication via REST APIs
- Data isolation and independent scaling

**Note**: This service is part of the Store Online Shop microservices architecture. The Delivery Service handles package delivery, tracking, and logistics management with configurable lost package probability.
