#!/bin/bash
set -e  # Останавливать при любой ошибке

echo "=== Starting deployment ==="
date

# Добавляем проверки
echo "1. Checking Docker access..."
docker ps > /dev/null

echo "2. Stopping existing containers..."
docker compose down

echo "3. Pulling latest code from GitHub..."
git fetch origin
git reset --hard origin/master

echo "4. Building and starting new containers..."
# Pass environment variables to docker-compose
export GIGACHAT_API_TOKEN="${GIGACHAT_API_TOKEN}"
docker compose up -d --build

echo "5. Checking container status..."
if ! docker compose ps | grep -q "Up"; then
    echo "❌ ERROR: No containers are running!"
    exit 1
fi

echo "=== Deployment completed successfully! ==="
date