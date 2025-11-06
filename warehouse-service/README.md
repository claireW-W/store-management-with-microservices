# Warehouse Service

Warehouse Management Service for Store Online Shop System.

## Overview

This service manages warehouse operations, inventory tracking, and stock reservations.

## Features

- **Inventory Management**: Track product stock levels across multiple warehouses
- **Stock Reservations**: Reserve inventory for orders with automatic expiration
- **Inventory Transactions**: Complete audit trail of all inventory movements
- **Multi-Warehouse Support**: Manage inventory across multiple warehouse locations

## API Endpoints

### Inventory Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/warehouse/inventory/{productId}` | Query product inventory across all warehouses |
| POST | `/api/warehouse/reserve` | Reserve inventory for an order |

## Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: PostgreSQL 15+
- **Build Tool**: Gradle 8.5+
- **ORM**: JPA/Hibernate

## Database

The service uses the `warehouse_db` PostgreSQL database with 4 tables:
- `warehouses`: Warehouse locations
- `inventory`: Product stock levels
- `inventory_reservations`: Temporary stock reservations
- `inventory_transactions`: Inventory change audit log

## Running the Service

```bash
# Build and run
./gradlew bootRun

# Service will start on http://localhost:8083/api
```

## Configuration

Configure the following environment variables if needed:
- `DB_USERNAME`: Database username (default: postgres)
- `DB_PASSWORD`: Database password (default: 123)

## Health Check

```bash
curl http://localhost:8083/api/actuator/health
```

