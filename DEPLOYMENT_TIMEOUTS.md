# Deployment Timeouts Configuration

## 🕐 Проблема которую решаем

**Симптом:** Деплой падал с ошибкой "Application did not become healthy within 60s", хотя приложение продолжало запускаться.

**Причина:** Spring Boot приложение с 15 JPA репозиториями + Hibernate инициализация занимает 60-90 секунд при первом запуске.

## ✅ Решение

### 1. Адаптивные таймауты в deploy.sh

**Логика:**
- **Первый деплой** (нет backup образа): 240 секунд (4 минуты)
- **Обычный деплой** (есть backup): 180 секунд (3 минуты)
- **Откат**: 120 секунд (2 минуты, т.к. используется закэшированный образ)

```bash
if docker images "$BACKUP_IMAGE" -q | grep -q .; then
    HEALTH_CHECK_TIMEOUT=180  # Обычный деплой
    HAS_BACKUP=true
else
    HEALTH_CHECK_TIMEOUT=240  # Первый деплой
    HAS_BACKUP=false
fi
```

**Почему такие значения:**
- Первый деплой медленнее из-за:
  - Первичной инициализации JPA (сканирование 15 репозиториев)
  - Создание схемы БД (DDL операции)
  - Холодный старт всех компонентов
- Последующие деплои быстрее из-за:
  - Прогретой БД (схема уже существует)
  - Кэшированных Docker слоев
  - Меньше DDL операций

### 2. Увеличенные параметры Docker healthcheck

**В docker-compose.yml:**
```yaml
healthcheck:
  test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", 
         "http://localhost:8080/actuator/health/readiness", "||", "exit", "1"]
  interval: 10s      # Проверять каждые 10 секунд
  timeout: 5s        # Таймаут на запрос
  retries: 18        # 18 × 10s = 180 секунд проверок
  start_period: 60s  # Первые 60 секунд игнорировать failures
```

**Расчет:**
- `start_period: 60s` - Не считать контейнер unhealthy первые 60 секунд
- `retries: 18` - После start_period еще 180 секунд на успешный старт
- **Итого:** до 240 секунд до признания контейнера unhealthy

### 3. Оптимизированный health-check.sh

**Улучшения:**
- ✅ Default timeout: 180 секунд (было 120)
- ✅ Проверка ошибок каждые 15 секунд (было 10) - меньше нагрузка
- ✅ Показ прогресса инициализации
- ✅ Получение 100 строк логов (было 50)

**Индикаторы прогресса:**
```
📋 Starting application...
📋 Initializing JPA repositories...
📋 Connecting to database...
📋 JPA initialization complete...
📋 Starting Tomcat server...
✓ Spring Boot application started
```

## 📊 Типичное время запуска

| Сценарий | Время запуска | Таймаут |
|----------|---------------|---------|
| Первый деплой (DDL + init) | 60-120s | 240s |
| Обычный деплой (только update) | 40-80s | 180s |
| Откат к backup | 30-60s | 120s |
| Локальный запуск (dev) | 20-40s | - |

## 🎯 Что показывает вывод при деплое

### Успешный первый деплой:
```bash
8. Performing health check...
⚠️  First deployment detected - using extended timeout (240s)

=== Health Check Started ===
Container: backend
Max wait time: 240s
⚠️  Note: First startup may take 60-120s due to JPA/Hibernate initialization

✓ Container is running
Waiting for Spring Boot application to start...

⏳ Waiting for application to start... (0s elapsed)
📋 Starting application...
⏳ Waiting for application to start... (10s elapsed)
📋 Initializing JPA repositories...
⏳ Waiting for application to start... (20s elapsed)
📋 Connecting to database...
⏳ Waiting for application to start... (30s elapsed)
⏳ Waiting for application to start... (40s elapsed)
📋 JPA initialization complete...
⏳ Waiting for application to start... (50s elapsed)
📋 Starting Tomcat server...
⏳ Waiting for application to start... (60s elapsed)
✓ Spring Boot application started (65s elapsed)

Verifying health endpoint...
✅ SUCCESS: Application is healthy!

=== Health Check Summary ===
Total startup time: 70s
Container: backend
Status: HEALTHY

=== Spring Boot Startup Message ===
Started JAcademicSupprtApplication in 6.368 seconds (process running for 68.523)
```

### Обычный деплой (с backup):
```bash
8. Performing health check...

=== Health Check Started ===
Container: backend
Max wait time: 180s

⏳ Waiting for application to start... (10s elapsed)
📋 Initializing JPA repositories...
⏳ Waiting for application to start... (20s elapsed)
⏳ Waiting for application to start... (30s elapsed)
✓ Spring Boot application started (35s elapsed)

✅ SUCCESS: Application is healthy!
Total startup time: 38s
```

## 🔧 Настройка таймаутов

### Если приложение запускается еще дольше

**Вариант 1: Увеличить в deploy.sh**
```bash
# Найти эти строки и изменить значения
HEALTH_CHECK_TIMEOUT=240  # Увеличить до 300
HEALTH_CHECK_TIMEOUT=360  # Для первого деплоя
```

**Вариант 2: Увеличить в docker-compose.yml**
```yaml
healthcheck:
  retries: 24        # 240 секунд
  start_period: 90s  # Дать больше времени
```

### Если хотите ускорить проверки

**⚠️ Не рекомендуется** для продакшена, но для dev:
```bash
HEALTH_CHECK_TIMEOUT=60  # Быстрые деплои
```

В docker-compose.yml:
```yaml
healthcheck:
  interval: 5s       # Проверять чаще
  retries: 12        # 60 секунд
  start_period: 30s  # Меньше grace period
```

## 🐛 Troubleshooting

### Проблема: Приложение все еще не успевает

**Диагностика:**
```bash
# Посмотреть реальное время запуска
docker logs backend | grep "Started.*Application"
# Вывод: Started JAcademicSupprtApplication in 6.368 seconds (process running for 95.018)
#        ^^^^ JVM время     ^^^^ реальное время от старта контейнера
```

**Решение:**
Если "process running for" > 180 секунд, увеличьте таймауты.

### Проблема: Health endpoint отвечает, но с задержкой

**Симптом:** 
```
✓ Spring Boot application started
⚠️  Warning: Spring started but health endpoint not ready yet
Waiting additional time for Actuator endpoints...
```

**Причины:**
- Actuator endpoints инициализируются после Spring context
- Health indicators (DB, disk) выполняют проверки

**Решение:** Увеличить sleep после обнаружения "Started":
```bash
# В health-check.sh
sleep 10  # Было 5
```

### Проблема: Откат также не успевает за 120s

**Решение в deploy.sh:**
```bash
if ./scripts/health-check.sh backend 180; then  # Увеличить с 120
```

## 📈 Оптимизация времени запуска

Если хотите ускорить сам Spring Boot:

### 1. Lazy initialization
**application.properties:**
```properties
spring.main.lazy-initialization=true
```
⚠️ Осторожно: ошибки конфигурации проявятся при первом использовании

### 2. Exclude auto-configurations
```properties
spring.autoconfigure.exclude=\
  org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration,\
  org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration
```

### 3. Disable JMX
```properties
spring.jmx.enabled=false
```

### 4. Optimize JPA
```properties
spring.jpa.properties.hibernate.temp.use_jdbc_metadata_defaults=false
spring.jpa.open-in-view=false
```

### 5. Use native compilation (advanced)
GraalVM Native Image для мгновенного старта (~0.1s), но:
- Сложная настройка
- Ограничения reflection
- Больше времени на сборку

## 🎓 Best Practices

1. **Не уменьшайте таймауты ниже реального времени запуска + 50%**
   - Реальное время: 60s → Таймаут: минимум 90s
   
2. **Мониторьте время запуска** для выявления деградации:
   ```bash
   docker logs backend | grep "Started.*Application" | tail -5
   ```

3. **Используйте start_period** вместо увеличения retries:
   - `start_period: 90s, retries: 12` лучше чем
   - `start_period: 30s, retries: 24`

4. **Проверяйте логи при timeout** для понимания что тормозит:
   ```bash
   docker logs backend --tail=200 | grep -E "(INFO|WARN)"
   ```

5. **Для production:** лучше больший таймаут, чем false positive rollback

## 📝 Summary

### Текущая конфигурация:
- ✅ **Первый деплой:** 240 секунд
- ✅ **Обычный деплой:** 180 секунд
- ✅ **Откат:** 120 секунд
- ✅ **Docker healthcheck:** 60s start + 180s retries
- ✅ **Адаптивный вывод прогресса** инициализации

### Ожидаемые результаты:
- ✅ Деплой завершается успешно даже при медленном старте
- ✅ Нет false positive rollbacks
- ✅ Видно прогресс инициализации (JPA, Hibernate, Tomcat)
- ✅ При реальных ошибках откат все еще работает

Эта конфигурация должна покрыть 99% случаев запуска вашего Spring Boot приложения.
