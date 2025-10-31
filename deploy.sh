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

# Сохраняем текущий образ для возможного отката
echo "3. Backing up current image..."
BACKUP_IMAGE="jacademicsupport-app:backup-$(date +%Y%m%d-%H%M%S)"
if docker images jacademicsupport-app:latest -q | grep -q .; then
    docker tag jacademicsupport-app:latest "$BACKUP_IMAGE" || true
    echo "✓ Backup created: $BACKUP_IMAGE"
else
    echo "✓ No existing image to backup"
fi

echo "4. Pulling latest code from GitHub..."
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

echo "5. Stopping existing containers..."
docker compose down

echo "6. Starting containers..."
# Pass environment variables to docker-compose
export GIGACHAT_API_TOKEN="${GIGACHAT_API_TOKEN}"
export JWT_SECRET="${JWT_SECRET}"
export JWT_EXPIRATION="${JWT_EXPIRATION}"

# Пересобираем только если есть изменения в коде
if [ "$NEED_REBUILD" = true ]; then
    echo "Building new image..."
    if ! docker compose up -d --build; then
        echo "❌ ERROR: Failed to build and start containers"
        exit 1
    fi
else
    echo "Using existing image..."
    if ! docker compose up -d; then
        echo "❌ ERROR: Failed to start containers"
        exit 1
    fi
fi

echo "7. Checking container status..."
sleep 5  # Даём контейнерам время на старт
if ! docker compose ps | grep -q "Up"; then
    echo "❌ ERROR: No containers are running!"
    docker compose logs --tail=50
    exit 1
fi

echo "8. Performing health check..."
chmod +x scripts/health-check.sh

# Определяем таймаут в зависимости от наличия backup
# Таймауты увеличены для медленного VPS (средний запуск 80-90s)
if docker images "$BACKUP_IMAGE" -q | grep -q .; then
    HEALTH_CHECK_TIMEOUT=420  # 7 минут для обычного деплоя
    HAS_BACKUP=true
else
    HEALTH_CHECK_TIMEOUT=600  # 10 минут для первого деплоя (Hibernate инициализация + медленный VPS)
    HAS_BACKUP=false
    echo "⚠️  First deployment detected - using extended timeout (${HEALTH_CHECK_TIMEOUT}s / $((HEALTH_CHECK_TIMEOUT / 60)) minutes)"
fi

echo "⏱️  Health check timeout: ${HEALTH_CHECK_TIMEOUT}s ($((HEALTH_CHECK_TIMEOUT / 60)) minutes)"

# Запускаем health check с адаптивным timeout
if ./scripts/health-check.sh backend $HEALTH_CHECK_TIMEOUT; then
    echo "✅ Health check passed!"
else
    echo "❌ ERROR: Health check failed!"
    echo ""
    echo "=== Application failed to start properly ==="
    echo "This could be due to:"
    echo "- Missing environment variables"
    echo "- Database connection issues"
    echo "- Circular dependency injection"
    echo "- Configuration errors"
    echo "- Out of memory on VPS"
    echo ""
    
    # Пытаемся откатиться только если есть backup
    if [ "$HAS_BACKUP" = true ]; then
        echo "⚠️  Rolling back to previous version..."
        docker compose down
        docker tag "$BACKUP_IMAGE" jacademicsupport-app:latest
        docker compose up -d
        
        echo "Verifying rollback..."
        sleep 10  # Даём больше времени на старт после отката
        # Откат обычно быстрее, используем 5 минут
        if ./scripts/health-check.sh backend 300; then
            echo "✅ Rollback successful - previous version is running"
            echo "⚠️  Please fix the issues and redeploy"
            exit 1
        else
            echo "❌ Rollback also failed - manual intervention required"
            docker compose logs --tail=100
            exit 1
        fi
    else
        echo "⚠️  No backup available - this is the first deployment"
        echo "❌ Manual intervention required"
        echo ""
        echo "=== Recent logs ==="
        docker compose logs --tail=100
        exit 1
    fi
fi

echo "9. Cleaning up old images..."
docker image prune -f

# Удаляем старые backup образы (оставляем только последние 3)
echo "10. Cleaning up old backups..."
docker images jacademicsupport-app --format "{{.Tag}}" | grep "backup-" | sort -r | tail -n +4 | xargs -r -I {} docker rmi jacademicsupport-app:{} || true

echo ""
echo "=== Deployment completed successfully! ==="
echo "Application is healthy and ready to serve requests"
date