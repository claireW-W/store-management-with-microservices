#!/bin/bash

# Docker Stop Script

echo "🛑 Stopping all Docker services..."
echo "=================================="

docker-compose down

echo ""
echo "✅ All services stopped"
echo ""
echo "💡 Tips:"
echo "   - Stop and remove data: docker-compose down -v"
echo "   - Stop and remove images: docker-compose down -v --rmi all"
echo ""
