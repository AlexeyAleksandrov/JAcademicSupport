# Health Check Infinite Loop Fix

## 🐛 Проблема

**Симптом:** Health check зацикливается после успешного старта Spring:
```
✓ Spring Boot application started (252s elapsed)
Verifying health endpoint...
⚠️  Warning: Spring started but health endpoint not ready yet
Waiting additional time for Actuator endpoints...

✓ Spring Boot application started (252s elapsed)  # <-- повторяется снова!
Verifying health endpoint...
⚠️  Warning: Spring started but health endpoint not ready yet
```

## 🔍 Причины

**1. Отсутствие флага SPRING_STARTED**
```bash
# Проблема: этот блок выполнялся каждую итерацию цикла
if check_spring_started; then
    # После первого обнаружения "Started" в логах,
    # условие всегда true, код выполняется снова и снова
fi
```

**2. Неинициализированная переменная HEALTH_CHECK_ATTEMPTS**
```bash
# while цикл пытался использовать неинициализированную переменную
while ! check_health && [ $HEALTH_CHECK_ATTEMPTS -lt 10 ]; do
    # $HEALTH_CHECK_ATTEMPTS пустая -> условие всегда true -> бесконечный цикл
done
```

**3. Health endpoint возвращал 401/403 (Spring Security блокировка)**
- SecurityConfig изменения еще не задеплоены
- Actuator endpoints требуют JWT токен
- Нет fallback проверки

## ✅ Исправления

### 1. Добавлен флаг SPRING_STARTED

**Файл:** `scripts/health-check.sh`

```bash
SPRING_STARTED=false

while true; do
    # Проверяем только если Spring еще не был обнаружен
    if check_spring_started && [ "$SPRING_STARTED" = false ]; then
        SPRING_STARTED=true
        # Этот блок выполнится ТОЛЬКО ОДИН РАЗ
        # ...
        exit 0  # Выход после успеха
    fi
done
```

**Результат:** Блок проверки health endpoint выполняется только один раз.

### 2. Инициализированы переменные

```bash
LAST_LOG_CHECK=0
LAST_PROGRESS_CHECK=0
LAST_PROGRESS=""
LAST_CRASH_CHECK=0
HEALTH_CHECK_ATTEMPTS=0      # Добавлено!
SPRING_STARTED=false         # Добавлено!
```

**Результат:** Корректная работа while цикла с проверкой попыток.

### 3. Улучшена функция check_health с fallback

```bash
check_health() {
    # Пробуем readiness endpoint
    local readiness_response=$(docker exec "$CONTAINER_NAME" wget --spider http://localhost:8080/actuator/health/readiness 2>&1)
    
    if echo "$readiness_response" | grep -q "200 OK"; then
        return 0
    fi
    
    # Пробуем базовый health endpoint
    local health_response=$(docker exec "$CONTAINER_NAME" wget --spider http://localhost:8080/actuator/health 2>&1)
    
    if echo "$health_response" | grep -q "200 OK"; then
        return 0
    fi
    
    # Fallback: если endpoints блокируются (401/403), проверяем порт
    if echo "$readiness_response" | grep -qE "(401|403)"; then
        echo "⚠️  Actuator endpoint returns 401/403 (blocked by Spring Security)"
        echo "    Falling back to port availability check..."
        
        if docker exec "$CONTAINER_NAME" nc -z localhost 8080 2>/dev/null; then
            echo "✓ Port 8080 is responding (app is running)"
            return 0
        fi
    fi
    
    return 1
}
```

**Результат:** 
- Показывает детальную информацию о проблемах
- Fallback на проверку доступности порта если Security блокирует
- Работает даже если SecurityConfig изменения еще не задеплоены

### 4. Добавлен netcat в Docker образ

**Файл:** `Dockerfile`

```dockerfile
# Устанавливаем wget для healthcheck и netcat для fallback проверки
RUN apk add --no-cache wget netcat-openbsd
```

**Результат:** Возможность fallback проверки доступности порта.

### 5. Ограничение попыток проверки health endpoint

```bash
# Максимум 10 попыток = 50 секунд после старта Spring
HEALTH_CHECK_ATTEMPTS=0
while ! check_health && [ $HEALTH_CHECK_ATTEMPTS -lt 10 ]; do
    HEALTH_CHECK_ATTEMPTS=$((HEALTH_CHECK_ATTEMPTS + 1))
    echo "⏳ Waiting for health endpoint... (attempt ${HEALTH_CHECK_ATTEMPTS}/10)"
    sleep 5
done

if [ $HEALTH_CHECK_ATTEMPTS -ge 10 ]; then
    echo "❌ ERROR: Health endpoint did not become available within 50 seconds"
    echo ""
    echo "Possible causes:"
    echo "- Actuator endpoints blocked by Spring Security (check SecurityConfig)"
    exit 1
fi
```

**Результат:** Четкий timeout с понятным сообщением об ошибке.

## 🚀 Полный список изменений для коммита

### Измененные файлы:

1. **`SecurityConfig.java`** - Разрешен публичный доступ к `/actuator/health/**`
2. **`scripts/health-check.sh`** - Исправлен бесконечный цикл + fallback + отладка
3. **`Dockerfile`** - Добавлен netcat-openbsd

### Команды для коммита:

```bash
git add .
git commit -m "fix: health check infinite loop and add Security config for Actuator

BREAKING ISSUES FIXED:
1. Health check infinite loop after Spring startup
2. Actuator endpoints blocked by Spring Security (401/403)
3. Uninitialized HEALTH_CHECK_ATTEMPTS variable

CHANGES:
- Add SPRING_STARTED flag to prevent re-entering health check block
- Initialize HEALTH_CHECK_ATTEMPTS=0 to fix while loop condition
- Add /actuator/health/** to permitAll in SecurityConfig
- Add detailed HTTP response debugging in check_health()
- Add fallback port check with netcat when endpoints blocked
- Install netcat-openbsd in Dockerfile for fallback checks
- Limit health endpoint attempts to 10 (50 seconds timeout)
- Add clear error messages for troubleshooting

SECURITY:
- Health endpoint shows only status without auth (show-details=when-authorized)
- Other Actuator endpoints remain protected

Fixes deployment failures where app starts successfully but health check loops forever"

git push origin master
```

## 📊 Ожидаемый результат

### До исправления:
```
✓ Spring Boot application started (252s elapsed)
Verifying health endpoint...
⚠️  Warning: Spring started but health endpoint not ready yet
✓ Spring Boot application started (252s elapsed)
⚠️  Warning: Spring started but health endpoint not ready yet
[бесконечный цикл]
```

### После исправления (с новым SecurityConfig):
```
✓ Spring Boot application started (115s elapsed)

Verifying health endpoint...
✅ SUCCESS: Application is healthy!

=== Health Check Summary ===
Total startup time: 120s (2m 0s)
Status: HEALTHY

=== Spring Boot Startup Message ===
Started JAcademicSupprtApplication in 104.318 seconds

✅ Health check passed!
=== Deployment completed successfully! ===
```

### После исправления (без SecurityConfig изменений - fallback):
```
✓ Spring Boot application started (115s elapsed)

Verifying health endpoint...
⏳ Waiting for health endpoint... (attempt 1/10)
⚠️  Actuator endpoint returns 401/403 (blocked by Spring Security)
    Falling back to port availability check...
✓ Port 8080 is responding (app is running)

✅ SUCCESS: Application is healthy!

=== Health Check Summary ===
Total startup time: 125s (2m 5s)
Status: HEALTHY (via fallback port check)
```

## 🔍 Как это работает теперь

### Workflow проверки:

1. **Ожидание запуска контейнера** (max 120s)
2. **Ожидание "Started...Application" в логах** (max 600s)
3. **Однократная проверка health endpoint** (max 50s, 10 попыток):
   - Попытка 1: `/actuator/health/readiness`
   - Попытка 2: `/actuator/health`
   - Fallback: проверка порта 8080 с netcat (если 401/403)
4. **Успех или ошибка с подробным описанием**

### Проверка краша каждые 5 секунд:
- Контейнер все еще запущен?
- Exit code != 0? → немедленный fail

## 🎯 Преимущества

1. ✅ **Нет бесконечных циклов** - флаг SPRING_STARTED
2. ✅ **Работает даже если Security блокирует** - fallback на netcat
3. ✅ **Четкий timeout** - максимум 50 секунд после старта Spring
4. ✅ **Понятные ошибки** - детальная отладка и подсказки
5. ✅ **Обратная совместимость** - работает со старой версией SecurityConfig

## 📚 Связанная документация

- `ACTUATOR_SECURITY_FIX.md` - Исправление Spring Security для Actuator
- `TIMEOUT_FIX.md` - Увеличение таймаутов для медленного VPS
- `HEALTH_CHECK_SETUP.md` - Полное руководство по health checks

---

**Критическое исправление!** Без этих изменений деплой зависает в бесконечном цикле даже при успешном старте приложения.
