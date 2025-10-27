#!/bin/bash
set -e  # Останавливать при любой ошибке

echo "=== Starting deployment ==="
date

# Добавляем проверки
echo "1. Checking Docker access..."
docker ps > /dev/null

echo "2. Creating external network if not exists..."
# Проверяем, существует ли сеть
if ! docker network ls | grep -q app-network; then
    echo "Creating app-network..."
    docker network create app-network
else
    echo "Network app-network already exists"
fi

echo "3. Stopping existing containers..."
docker compose down

echo "4. Pulling latest code from GitHub..."
git fetch origin
git reset --hard origin/master

echo "5. Building and starting new containers..."
# Pass environment variables to docker-compose
export GIGACHAT_API_TOKEN="${GIGACHAT_API_TOKEN}"
export JWT_SECRET="${JWT_SECRET}"
export JWT_EXPIRATION="${JWT_EXPIRATION}"
docker compose up -d --build

echo "6. Checking container status..."
if ! docker compose ps | grep -q "Up"; then
    echo "❌ ERROR: No containers are running!"
    exit 1
fi

echo "=== Deployment completed successfully! ==="
date