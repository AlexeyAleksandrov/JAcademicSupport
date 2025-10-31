# Actuator Security Fix

## 🐛 Проблема

**Симптом:** Health check падает с ошибкой, хотя приложение успешно запустилось:
```
Started JAcademicSupprtApplication in 104.318 seconds (process running for 113.692)
❌ ERROR: Health check failed!
```

## 🔍 Причина

**Spring Security блокирует Actuator endpoints!**

В `SecurityConfig.java` были разрешены только:
- `/api/auth/**` - публичные
- `/error` - публичные  
- Остальные - **требуют JWT аутентификацию**

Health check скрипт пытается обратиться к:
- `http://localhost:8080/actuator/health/readiness`
- `http://localhost:8080/actuator/health`

Но получает **401 Unauthorized** или **403 Forbidden**, потому что нет JWT токена!

## ✅ Решение

### Добавлены Actuator endpoints в permitAll

**Файл:** `SecurityConfig.java`

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**").permitAll()
    .requestMatchers("/error").permitAll()
    // Actuator health endpoints для мониторинга
    .requestMatchers("/actuator/health/**").permitAll()
    .requestMatchers("/actuator/health").permitAll()
    .requestMatchers("/actuator/info").permitAll()
    .anyRequest().authenticated()
)
```

## 🔒 Безопасность

**Вопрос:** Не опасно ли открывать `/actuator/health` публично?

**Ответ:** Нет, это стандартная практика:

1. **Health endpoint не раскрывает чувствительную информацию** без авторизации
   - Без токена: `{"status":"UP"}` - только общий статус
   - С токеном: детальная информация (БД, диск, память)

2. **Необходимо для мониторинга:**
   - Docker healthcheck
   - Kubernetes liveness/readiness probes
   - Load balancers
   - Deployment scripts

3. **application.properties уже настроен правильно:**
   ```properties
   # Показывать детали только авторизованным
   management.endpoint.health.show-details=when-authorized
   ```

4. **Другие Actuator endpoints все еще защищены:**
   - `/actuator/metrics` - требует аутентификацию
   - `/actuator/env` - требует аутентификацию
   - `/actuator/beans` - требует аутентификацию

## 🚀 Что делать

**Закоммитить исправление:**

```bash
git add .
git commit -m "fix: allow public access to Actuator health endpoints

- Add /actuator/health/** to permitAll in SecurityConfig
- Required for Docker healthcheck and deployment verification
- Health details still require authentication (show-details=when-authorized)
- Other Actuator endpoints remain protected

Fixes health check failures during deployment"

git push origin master
```

## 🧪 Проверка исправления

После деплоя health check должен пройти успешно:

```bash
=== Health Check Started ===
⏳ Waiting for application to start... (10s elapsed)
📋 Starting application...
📋 Initializing JPA repositories...
📋 JPA initialization complete...
✓ Spring Boot application started (115s elapsed)

Verifying health endpoint...
✅ SUCCESS: Application is healthy!

=== Health Check Summary ===
Total startup time: 118s (1m 58s)
Status: HEALTHY

=== Spring Boot Startup Message ===
Started JAcademicSupprtApplication in 104.318 seconds
```

## 🔧 Локальная проверка

Теперь можно проверить health endpoint без токена:

```bash
# Публичный доступ к статусу
curl http://localhost:8080/actuator/health
# Ответ: {"status":"UP"}

# Readiness probe
curl http://localhost:8080/actuator/health/readiness
# Ответ: {"status":"UP"}

# Liveness probe
curl http://localhost:8080/actuator/health/liveness
# Ответ: {"status":"UP"}

# Детали требуют токен
curl http://localhost:8080/actuator/health \
  -H "Authorization: Bearer <jwt-token>"
# Ответ с деталями: {"status":"UP","components":{"db":{"status":"UP"},...}}
```

## 📊 Что изменилось

| Endpoint | До исправления | После исправления |
|----------|----------------|-------------------|
| `/actuator/health` | 401 Unauthorized ❌ | 200 OK ({"status":"UP"}) ✅ |
| `/actuator/health/readiness` | 401 Unauthorized ❌ | 200 OK ✅ |
| `/actuator/health/liveness` | 401 Unauthorized ❌ | 200 OK ✅ |
| `/actuator/health` (with details) | 401 Unauthorized ❌ | 401 Unauthorized (требует токен) ✅ |
| `/actuator/metrics` | 401 Unauthorized ✅ | 401 Unauthorized (защищен) ✅ |
| `/actuator/env` | 401 Unauthorized ✅ | 401 Unauthorized (защищен) ✅ |

## 🎯 Best Practices

1. **Health endpoints всегда должны быть публичными** для мониторинга
2. **Используйте `show-details=when-authorized`** для защиты деталей
3. **Другие Actuator endpoints защищайте** аутентификацией
4. **Для production** можно ограничить доступ по IP (firewall/nginx)

## 📚 Дополнительная информация

- [Spring Boot Actuator Security](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.endpoints.security)
- [Health Endpoint Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.endpoints.health)

---

**Исправление критично для работы деплоя!** Без него health check всегда будет падать, даже если приложение работает.
