# Health Check Configuration

## 🏥 Обзор

Система health check обеспечивает, что Spring Boot приложение корректно запустилось перед тем, как деплоймент будет считаться успешным. Это предотвращает ситуации, когда контейнер запущен, но приложение упало с ошибками.

## 🔍 Компоненты системы

### 1. Spring Boot Actuator

**Зависимость в pom.xml:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

**Конфигурация в application.properties:**
```properties
# Spring Boot Actuator Configuration
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=when-authorized
management.health.defaults.enabled=true
management.health.db.enabled=true
management.health.diskspace.enabled=true
management.endpoint.health.probes.enabled=true
management.health.livenessState.enabled=true
management.health.readinessState.enabled=true
```

**Доступные endpoints:**
- `http://localhost:8080/actuator/health` - Общее состояние
- `http://localhost:8080/actuator/health/liveness` - Проверка жизнеспособности
- `http://localhost:8080/actuator/health/readiness` - Проверка готовности

### 2. Docker Healthcheck

**В docker-compose.yml:**
```yaml
healthcheck:
  test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", 
         "http://localhost:8080/actuator/health/readiness", "||", "exit", "1"]
  interval: 10s      # Проверять каждые 10 секунд
  timeout: 5s        # Таймаут на запрос
  retries: 12        # 12 попыток = 2 минуты
  start_period: 40s  # Первые 40 секунд не считать за failed
```

**Статусы healthcheck:**
- `starting` - Контейнер запускается
- `healthy` - Приложение здорово
- `unhealthy` - Приложение не отвечает или вернуло ошибку

**Проверка статуса:**
```bash
docker ps  # Смотреть колонку STATUS
docker inspect backend --format='{{.State.Health.Status}}'
```

### 3. Скрипт health-check.sh

**Расположение:** `scripts/health-check.sh`

**Использование:**
```bash
./scripts/health-check.sh [container_name] [max_wait_seconds]

# Примеры:
./scripts/health-check.sh backend 120    # Ждать до 120 секунд
./scripts/health-check.sh backend        # По умолчанию 120 секунд
```

**Что проверяет скрипт:**
1. ✅ Контейнер запущен и работает
2. ✅ Spring Boot успешно стартовал (ищет "Started...Application in")
3. ✅ Health endpoint отвечает статусом 200 OK
4. ❌ Отсутствие фатальных ошибок в логах
5. ❌ Отсутствие исключений при старте

**Фатальные ошибки, которые детектируются:**
- `Error starting ApplicationContext`
- `APPLICATION FAILED TO START`
- `BeanCreationException`
- `UnsatisfiedDependencyException`
- Generic `Caused by:.*Exception`

### 4. Интеграция в deploy.sh

**Workflow деплоя с health check:**

1. **Backup** - Создание резервной копии текущего образа
2. **Build/Start** - Сборка и запуск нового образа
3. **Health Check** - Проверка успешного запуска (120 секунд)
4. **Success** ✅ - Деплой успешен, очистка старых образов
5. **Failure** ❌ - Автоматический откат к backup образу

**Логика отката:**
```bash
if ! ./scripts/health-check.sh backend 120; then
    echo "❌ Health check failed - rolling back..."
    docker compose down
    docker tag "$BACKUP_IMAGE" jacademicsupport-app:latest
    docker compose up -d
    # Проверяем откат
    if ./scripts/health-check.sh backend 60; then
        echo "✅ Rollback successful"
    fi
fi
```

## 📊 Типичные сценарии

### Сценарий 1: Успешный деплой
```
=== Starting deployment ===
...
8. Performing health check...
⏳ Waiting for application to start... (5s elapsed)
⏳ Waiting for application to start... (10s elapsed)
✓ Spring Boot application started (15s elapsed)
Verifying health endpoint...
✅ SUCCESS: Application is healthy!

=== Health Check Summary ===
Total startup time: 18s
Container: backend
Status: HEALTHY

=== Spring Boot Startup Message ===
Started JAcademicSupprtApplication in 6.368 seconds (process running for 7.018)
```

### Сценарий 2: Ошибка при запуске (с откатом)
```
=== Starting deployment ===
...
8. Performing health check...
⏳ Waiting for application to start... (5s elapsed)
❌ ERROR: Fatal error detected in application logs

=== Error logs ===
Error creating bean with name 'userService': Unsatisfied dependency...

⚠️  Rolling back to previous version...
Verifying rollback...
✅ Rollback successful - previous version is running
⚠️  Please fix the issues and redeploy
```

### Сценарий 3: Timeout
```
8. Performing health check...
⏳ Waiting for application to start... (30s elapsed)
⏳ Waiting for application to start... (60s elapsed)
⏳ Waiting for application to start... (90s elapsed)
⏳ Waiting for application to start... (120s elapsed)

❌ ERROR: Application did not become healthy within 120s

=== Last 50 lines of logs ===
...database connection timeout...
```

## 🔧 Проверка здоровья приложения

### Локальная проверка
```bash
# 1. Проверка через curl
curl http://localhost:8080/actuator/health

# Ответ при успехе:
{
  "status": "UP",
  "groups": ["liveness", "readiness"]
}

# 2. Проверка readiness
curl http://localhost:8080/actuator/health/readiness

# Ответ:
{
  "status": "UP"
}

# 3. Проверка с деталями (требует авторизацию)
curl http://localhost:8080/actuator/health -H "Authorization: Bearer <token>"

# Ответ с деталями:
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 499963174912,
        "free": 123456789012,
        "threshold": 10485760,
        "exists": true
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

### Проверка в Docker
```bash
# Статус healthcheck
docker ps --format "table {{.Names}}\t{{.Status}}"

# Детали healthcheck
docker inspect backend | jq '.[0].State.Health'

# История проверок
docker inspect backend --format='{{range .State.Health.Log}}{{.ExitCode}} {{.Output}}{{end}}'

# Логи последней проверки
docker inspect backend --format='{{(index .State.Health.Log 0).Output}}'
```

## 🚨 Troubleshooting

### Проблема: Health check всегда fails

**Причины:**
1. Приложение стартует медленно
2. База данных недоступна
3. Неправильная конфигурация endpoint

**Решение:**
```bash
# 1. Увеличить start_period в docker-compose.yml
start_period: 60s  # Было 40s

# 2. Проверить логи
docker logs backend --tail=100

# 3. Проверить доступность БД
docker exec backend nc -zv postgres 5432

# 4. Попробовать health endpoint вручную
docker exec backend wget -O- http://localhost:8080/actuator/health
```

### Проблема: Деплой проходит, но приложение не работает

**Диагностика:**
```bash
# 1. Проверить статус контейнера
docker ps -a

# 2. Проверить health status
docker inspect backend --format='{{.State.Health.Status}}'

# 3. Посмотреть последние логи
docker logs backend --tail=50 --follow

# 4. Проверить переменные окружения
docker exec backend env | grep -E "(JWT_SECRET|GIGACHAT|POSTGRES)"
```

### Проблема: Откат не срабатывает

**Причины:**
- Нет backup образа
- Backup образ тоже сломан

**Ручной откат:**
```bash
# 1. Посмотреть доступные образы
docker images jacademicsupport-app

# 2. Выбрать рабочий backup
docker tag jacademicsupport-app:backup-20241031-120000 jacademicsupport-app:latest

# 3. Перезапустить
cd /home/JAcademicSupport
docker compose down
docker compose up -d

# 4. Проверить
./scripts/health-check.sh backend 60
```

## 🎯 Расширенная конфигурация

### Кастомный Health Indicator

Создайте класс для специфичных проверок:

```java
@Component
public class GigaChatHealthIndicator implements HealthIndicator {
    
    @Autowired
    private GigaChatService gigaChatService;
    
    @Override
    public Health health() {
        try {
            // Проверяем доступность GigaChat API
            gigaChatService.getAccessToken();
            return Health.up()
                .withDetail("gigachat", "Available")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("gigachat", "Unavailable")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

### Мониторинг метрик

Добавьте в `application.properties`:
```properties
# Expose metrics endpoint
management.endpoints.web.exposure.include=health,info,metrics

# Enable application metrics
management.metrics.enable.jvm=true
management.metrics.enable.process=true
management.metrics.enable.system=true
```

Доступ к метрикам:
```bash
curl http://localhost:8080/actuator/metrics
curl http://localhost:8080/actuator/metrics/jvm.memory.used
curl http://localhost:8080/actuator/metrics/http.server.requests
```

## 📈 Интеграция с мониторингом

### Prometheus + Grafana

1. Добавьте зависимость:
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

2. Expose Prometheus endpoint:
```properties
management.endpoints.web.exposure.include=health,info,prometheus
```

3. Настройте Prometheus scraping:
```yaml
scrape_configs:
  - job_name: 'spring-boot'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['backend:8080']
```

## 📝 Best Practices

1. **Всегда используйте readiness probe** для проверки готовности к обработке запросов
2. **Liveness probe** для автоматического перезапуска зависших приложений
3. **Увеличьте start_period** для приложений с долгим стартом
4. **Мониторьте метрики** для предотвращения проблем
5. **Логируйте результаты** health checks для анализа
6. **Тестируйте откат** на staging окружении

## 🔗 Полезные ссылки

- [Spring Boot Actuator Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Docker Healthcheck Reference](https://docs.docker.com/engine/reference/builder/#healthcheck)
- [Kubernetes Probes](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/)

## ✅ Резюме

Теперь ваш деплой:
- ✅ Проверяет успешный запуск Spring Boot
- ✅ Детектирует фатальные ошибки автоматически
- ✅ Откатывается к предыдущей версии при сбое
- ✅ Логирует подробную информацию о старте
- ✅ Интегрирован с Docker healthcheck
- ✅ Использует Actuator endpoints для мониторинга

**Ожидаемый результат:** GitHub Actions деплой завершится с ошибкой, если Spring Boot приложение не запустится корректно.
