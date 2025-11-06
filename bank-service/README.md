# Bank Service - Store Online Shop

A microservice for handling payment processing, refunds, and financial transactions in the Store Online Shop system.

## 🏗️ Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Java Version**: 17
- **Database**: PostgreSQL 15+
- **ORM**: Spring Data JPA / Hibernate
- **Build Tool**: Gradle 8.5+
- **Validation**: Jakarta Validation
- **Logging**: SLF4J + Logback
- **Testing**: JUnit 5 + Testcontainers
- **Message Queue**: RabbitMQ (CloudAMQP)
- **Real-time Communication**: WebSocket (STOMP)

## 📋 Prerequisites

Before running the application, ensure you have the following installed:

- **Java 17** or higher
- **Gradle 8.5+**
- **PostgreSQL 15+**
- **Git**
- **CloudAMQP Account** (free tier) - Sign up at https://www.cloudamqp.com/

## 🚀 First Time Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd bank-service
```

### 2. Database Setup

#### Create Bank Service Database
```bash
# Connect to PostgreSQL as superuser
psql -U postgres

# Create bank database
CREATE DATABASE bank_db;

# Exit psql
\q
```

#### Run Database Schema
```bash
# Apply the bank service database schema
psql -U postgres -d bank_db -f ../database_schemas/bank_db.sql
```

### 3. CloudAMQP Setup (RabbitMQ)

#### Sign up for CloudAMQP
1. Visit https://www.cloudamqp.com/
2. Create a free account
3. Click "Create New Instance"
4. Select "Little Lemur" (Free plan)
5. Choose a region close to you
6. Click "Create Instance"

#### Get Connection Details
1. Click on your instance name
2. Copy the following information:
   - Host (e.g., `sparrow-01.cloudamqp.com`)
   - Port (usually `5672`)
   - Username
   - Password
   - Virtual Host (Vhost)

### 4. Environment Configuration

#### Configure Application Properties
Update `src/main/resources/application.yml` with your CloudAMQP credentials:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/bank_db
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:123}
  
  rabbitmq:
    host: ${RABBITMQ_HOST:your-instance.cloudamqp.com}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:your-username}
    password: ${RABBITMQ_PASSWORD:your-password}
    virtual-host: ${RABBITMQ_VHOST:your-vhost}
```

#### Environment Variables (Recommended)
Create environment variables for sensitive data:

```bash
# Database
export DB_USERNAME=postgres
export DB_PASSWORD=123

# RabbitMQ (CloudAMQP)
export RABBITMQ_HOST=your-instance.cloudamqp.com
export RABBITMQ_PORT=5672
export RABBITMQ_USERNAME=your-username
export RABBITMQ_PASSWORD=your-password
export RABBITMQ_VHOST=your-vhost
```

See `ENV_TEMPLATE.md` for detailed configuration instructions.

#### Configuration Files to Update:
- `src/main/resources/application.yml` (main configuration)
- `src/main/resources/application-dev.yml` (development)
- `src/main/resources/application-test.yml` (testing)

#### Common Issues:
- **Password Authentication Failed**: Update PostgreSQL password in config files
- **Connection Refused**: Ensure PostgreSQL is running
- **Database Not Found**: Run database setup steps first
- **RabbitMQ Connection Failed**: Verify CloudAMQP credentials and network connectivity

### 5. Build and Run

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

### 6. Verify Installation

```bash
# Check if the service is running
curl http://localhost:8082/api/bank/health

# Expected response: "Bank Service is running"
```

## 🔄 Subsequent Runs

### Quick Start (After Initial Setup)

```bash
# Navigate to project directory
cd bank-service

# Start the application
./gradlew bootRun
```

### Alternative Run Methods

#### Using JAR file
```bash
# Build the JAR
gradle clean build

# Run the JAR
java -jar build/libs/bank-service-1.0.0.jar
```

#### Using Docker (Optional)
```bash
# Build Docker image
docker build -t bank-service .

# Run container
docker run -p 8082:8082 \
  -e DB_USERNAME=bank_user \
  -e DB_PASSWORD=bank_password \
  bank-service
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
# Drop and recreate bank database
psql -U postgres -c "DROP DATABASE IF EXISTS bank_db;"
psql -U postgres -c "CREATE DATABASE bank_db;"
psql -U postgres -d bank_db -f ../database_schemas/bank_db.sql
```

#### View Database
```bash
# Connect to bank database
psql -U postgres -d bank_db

# List tables (should show 4 tables)
\dt

# View transactions
SELECT * FROM transactions LIMIT 10;

# View customer accounts
SELECT * FROM customer_accounts;

# View store accounts
SELECT * FROM store_accounts;
```

## 📊 API Documentation

### Base URL
```
http://localhost:8082/api/bank
```

### Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/payment` | Process payment from customer to store |
| POST | `/refund` | Process refund from store to customer |
| POST | `/lost-package-refund` | Process lost package refund |
| GET | `/health` | Service health check |

### Example API Calls

#### Process Payment
```bash
curl -X POST http://localhost:8082/api/bank/payment \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-001",
    "customerId": "CUST-001",
    "amount": 99.99,
    "currency": "AUD",
    "paymentMethod": "CREDIT_CARD",
    "description": "Order payment"
  }'
```

#### Process Refund
```bash
curl -X POST http://localhost:8082/api/bank/refund \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": "TXN-1234567890-ABC12345",
    "orderId": "ORD-001",
    "refundAmount": 99.99,
    "reason": "Customer cancellation",
    "description": "Full refund"
  }'
```

#### Process Lost Package Refund
```bash
curl -X POST http://localhost:8082/api/bank/lost-package-refund \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-001",
    "customerId": "CUST-001",
    "deliveryId": "DEL-001",
    "refundAmount": 99.99,
    "currency": "AUD",
    "lostReason": "Package lost during delivery",
    "description": "Lost package compensation"
  }'
```

## 🧪 Testing

### Run All Tests
```bash
./gradlew test
```

### Run Specific Test Class
```bash
./gradlew test -Dtest=BankServiceTest
```

### Run Integration Tests
```bash
./gradlew test -Dtest=*IntegrationTest
```

### Test with Coverage
```bash
./gradlew test jacoco:report
```

## 📈 Monitoring

### Health Check
```bash
curl http://localhost:8082/api/bank/health
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

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_USERNAME` | `postgres` | Database username |
| `DB_PASSWORD` | `your_password` | Database password |
| `SERVER_PORT` | `8082` | Application port |
| `LOG_LEVEL` | `INFO` | Logging level |

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
psql -U postgres -l | grep bank_db

# Verify bank_db has 4 tables
psql -U postgres -d bank_db -c "\dt"
```

#### Port Already in Use
```bash
# Find process using port 8082
lsof -i :8082

# Kill the process
kill -9 <PID>
```

#### Maven Build Fails
```bash
# Clean Maven cache
./gradlew clean

# Update dependencies
./gradlew dependencies
```

### Logs

#### View Application Logs
```bash
# Follow logs in real-time
tail -f logs/application.log

# View error logs only
grep ERROR logs/application.log
```

## 📁 Project Structure

```
bank-service/
├── src/
│   ├── main/
│   │   ├── java/com/store/bank/
│   │   │   ├── controller/          # REST API controllers
│   │   │   ├── service/            # Business logic
│   │   │   ├── repository/         # Data access layer
│   │   │   ├── model/              # Entity models
│   │   │   ├── dto/                # Data transfer objects
│   │   │   ├── errors/             # Exception handling
│   │   │   └── BankTransactionApplication.java
│   │   └── resources/
│   │       ├── application.yml     # Main configuration
│   │       ├── application-dev.yml # Development config
│   │       └── application-test.yml # Test config
│   └── test/                       # Test classes
├── build.gradle                    # Gradle configuration
├── settings.gradle                 # Gradle settings
├── gradlew                         # Gradle wrapper (Unix)
├── gradlew.bat                     # Gradle wrapper (Windows)
├── .gitignore                      # Git ignore rules
└── README.md                       # This file
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/new-feature`
3. Commit changes: `git commit -am 'Add new feature'`
4. Push to branch: `git push origin feature/new-feature`
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 📞 Support

For support and questions:
- Create an issue in the repository
- Contact the development team
- Check the documentation wiki

---

## 🏗️ Microservices Architecture

This Bank Service is part of a **microservices architecture** with **separate applications and separate databases on one server**:

| Service | Database | Status |
|---------|----------|--------|
| **Bank Service** | `bank_db` | ✅ **Implemented** |
| Store Backend | `store_backend_db` | 🔄 Team member responsibility |
| Warehouse Service | `warehouse_db` | 🔄 Team member responsibility |
| DeliveryCo Service | `delivery_db` | 🔄 Team member responsibility |
| Email Service | `email_db` | 🔄 Team member responsibility |

### Database Independence
- Each service owns its own PostgreSQL database
- Services cannot directly access other services' databases
- Data isolation and independent scaling

### 📡 Communication Architecture

This service implements **three communication methods** to meet project requirements:

#### 1️⃣ REST API (Synchronous)
- **Type**: Request-Response HTTP
- **Use**: Direct client-server communication
- **Examples**: Payment processing, refund requests, balance queries

#### 2️⃣ RabbitMQ (Asynchronous)
- **Type**: Message Queue (AMQP)
- **Provider**: CloudAMQP (Managed Service)
- **Use**: Asynchronous processing, service decoupling
- **Examples**: Lost package refunds, order cancellation notifications

#### 3️⃣ WebSocket (Real-time)
- **Type**: Bidirectional streaming
- **Protocol**: STOMP over WebSocket
- **Use**: Real-time notifications to clients
- **Examples**: Balance updates, transaction notifications

📖 **Detailed documentation**: See `COMMUNICATION_ARCHITECTURE.md` for comprehensive information about all three communication methods, including examples, configuration, and testing instructions.

**Note**: This service is part of the Store Online Shop microservices architecture. Ensure all dependent services are running for full functionality.
