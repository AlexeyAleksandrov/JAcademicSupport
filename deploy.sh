#!/bin/bash
set -e  # Останавливать при любой ошибке

echo "=== Starting deployment ==="
date

# Включаем Docker BuildKit для ускорения сборки
export DOCKER_BUILDKIT=1
export COMPOSE_DOCKER_CLI_BUILD=1

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

echo "3. Pulling latest code from GitHub..."
git fetch origin

# Проверяем, есть ли изменения в критичных файлах
NEED_REBUILD=false
if git diff --name-only HEAD origin/master | grep -qE "^(Dockerfile|pom.xml|src/|mvnw)"; then
    echo "✓ Code changes detected - rebuild required"
    NEED_REBUILD=true
else
    echo "✓ No code changes - using cached image"
fi

git reset --hard origin/master

echo "4. Stopping existing containers..."
docker compose down

echo "5. Starting containers..."
# Pass environment variables to docker-compose
export GIGACHAT_API_TOKEN="${GIGACHAT_API_TOKEN}"
export JWT_SECRET="${JWT_SECRET}"
export JWT_EXPIRATION="${JWT_EXPIRATION}"

# Пересобираем только если есть изменения в коде
if [ "$NEED_REBUILD" = true ]; then
    echo "Building new image..."
    docker compose up -d --build
else
    echo "Using existing image..."
    docker compose up -d
fi

echo "6. Checking container status..."
sleep 5  # Даём контейнерам время на старт
if ! docker compose ps | grep -q "Up"; then
    echo "❌ ERROR: No containers are running!"
    docker compose logs --tail=50
    exit 1
fi

echo "7. Cleaning up old images..."
docker image prune -f

echo "=== Deployment completed successfully! ==="
date