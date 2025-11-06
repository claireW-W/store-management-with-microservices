#!/bin/bash

# Docker Quick Start Script

set -e

echo "🐳 Docker Service Startup Script"
echo "=================================="
echo ""

# Color definitions
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${YELLOW}⚠️  Docker is not running. Please start Docker Desktop first.${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Docker is running${NC}"
echo ""

# Check if images are built
if ! docker images | grep -q "tutorial-11-group-08-store-backend"; then
    echo -e "${BLUE}📦 First time setup, building images (this may take 5-10 minutes)...${NC}"
    docker-compose build
    echo -e "${GREEN}✅ Build completed${NC}"
    echo ""
fi

# Start services
echo -e "${BLUE}🚀 Starting all services...${NC}"
docker-compose up -d

echo ""
echo -e "${GREEN}✅ Services are starting...${NC}"
echo ""
echo "📋 Service URLs:"
echo "  - Frontend:           http://localhost:3000"
echo "  - Store Backend:      http://localhost:8080/api"
echo "  - Email Service:      http://localhost:8085/api"
echo "  - Bank Service:       http://localhost:8082/api"
echo "  - Warehouse Service:  http://localhost:8083/api"
echo "  - Delivery Service:   http://localhost:8084/api"
echo "  - RabbitMQ Mgmt:      http://localhost:15672"
echo ""
echo "⏳ Wait 30-60 seconds for all services to fully start..."
echo ""
echo "📊 View service status:"
echo "   docker-compose ps"
echo ""
echo "📝 View logs:"
echo "   docker-compose logs -f"
echo ""
echo "🛑 Stop services:"
echo "   docker-compose down"
echo ""
