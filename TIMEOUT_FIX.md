# Health Check Timeout Fix

## 🐛 Проблема

Деплой падал с ошибкой:
```
❌ ERROR: Application did not become healthy within 60s
❌ Rollback also failed - manual intervention required
```

**Причина:** Spring Boot с 15 JPA репозиториями + Hibernate инициализация занимает ~60-90 секунд на медленном VPS, но timeout был всего 60 секунд.

## ✅ Исправление

### Что было изменено:

1. **deploy.sh** - Адаптивные таймауты для медленного VPS:
   - 🆕 Первый деплой (нет backup): **600 секунд (10 минут)**
   - 🆕 Обычный деплой (есть backup): **420 секунд (7 минут)**
   - 🆕 Откат: **300 секунд (5 минут)**
   - ✅ Проверка наличия backup перед попыткой отката
   - ✅ Больше логов при ошибке (100 строк вместо 50)

2. **docker-compose.yml** - Увеличенные параметры healthcheck для медленного VPS:
   ```yaml
   retries: 54         # 540 секунд проверок (9 минут)
   start_period: 120s  # 2 минуты grace period
   # Итого: до 11 минут на запуск
   ```

3. **scripts/health-check.sh** - Улучшенный мониторинг + детекция краша:
   - 🆕 **Немедленная детекция краша контейнера** (exit code != 0)
   - 🆕 Проверка краша каждые 5 секунд
   - 🆕 Показ прогресса инициализации (JPA, Hibernate, Tomcat)
   - 🆕 Default timeout: 600 секунд (10 минут)
   - 🆕 Получение 100 строк логов (было 50)
   - ✅ Меньше нагрузки на медленный VPS
   - ✅ Более информативный вывод с минутами

4. **Документация:**
   - `DEPLOYMENT_TIMEOUTS.md` - полное руководство по таймаутам
   - `TIMEOUT_FIX.md` - это резюме

## 🚨 Критическое улучшение: Детекция краша

**Проблема:** Раньше если Spring крашился с ошибкой, скрипт ждал весь timeout.

**Решение:** Теперь каждые 5 секунд проверяется:
- Контейнер все еще запущен?
- Exit code контейнера (если упал)
- Если exit code != 0 → **немедленный fail без ожидания**

**Пример:**
```bash
⏳ Waiting for application to start... (10s elapsed)
❌ CRITICAL: Container crashed with exit code 1

=== Crash Logs (last 100 lines) ===
Error: Unable to access jarfile /app/app.jar
```

## 🚀 Что делать сейчас

### Шаг 1: Закоммитить исправления
```bash
git add .
git commit -m "fix: increase timeouts for slow VPS and add crash detection

- Increase first deploy timeout to 600s (10 min)
- Increase normal deploy timeout to 420s (7 min)
- Increase rollback timeout to 300s (5 min)
- Add immediate container crash detection (exit code)
- Check for crashes every 5 seconds
- Show initialization progress with minutes
- Increase docker healthcheck to 54 retries (9 min + 2 min grace)

Optimized for slow VPS with average startup time 80-90s"
git push origin master
```

### Шаг 2: Наблюдать за деплоем

Теперь вы увидите:
```
8. Performing health check...
⚠️  First deployment detected - using extended timeout (600s / 10 minutes)
⏱️  Health check timeout: 600s (10 minutes)

=== Health Check Started ===
Max wait time: 600s (10 minutes)
⚠️  Note: Running on slow VPS - startup may take up to 5-7 minutes

⏳ Waiting for application to start... (10s elapsed)
📋 Initializing JPA repositories...
⏳ Waiting for application to start... (20s elapsed)
📋 Connecting to database...
⏳ Still waiting... (120s / 600s elapsed, ~2m)
📋 JPA initialization complete...
⏳ Still waiting... (140s / 600s elapsed, ~2m)
📋 Starting Tomcat server...
✓ Spring Boot application started (155s elapsed)

Verifying health endpoint...
✅ SUCCESS: Application is healthy!

=== Health Check Summary ===
Total startup time: 160s (2m 40s)
Status: HEALTHY
```

**Если контейнер крашнется:**
```
⏳ Waiting for application to start... (10s elapsed)
❌ CRITICAL: Container crashed with exit code 1

=== Container Status ===
Exited (1) 2 seconds ago

=== Crash Logs (last 100 lines) ===
[логи ошибки]

❌ ERROR: Health check failed!
```

## 📊 Ожидаемые результаты

| Сценарий | Среднее время | Таймаут | Результат |
|----------|---------------|---------|-----------|
| Первый деплой (медленный VPS) | 80-150s | 600s ✅ | Успех |
| Обычный деплой | 60-100s | 420s ✅ | Успех |
| Откат | 40-80s | 300s ✅ | Успех |
| Краш контейнера | - | 5-10s ✅ | Немедленный fail |

## 🎯 Почему это безопасно

1. **Не увеличивает время успешного деплоя** - если приложение стартует за 90s, деплой завершится за 90s
2. **Адаптивность** - первый деплой (самый медленный) получает максимальный timeout
3. **Немедленный fail при крашах** - не ждет timeout если контейнер упал
4. **Видимость** - показывает что именно происходит и сколько времени осталось
5. **Меньше false positives** - достаточно времени для медленного VPS

## 🔍 Как проверить что проблема решена

После деплоя:
```bash
# 1. Проверить что контейнер healthy
docker ps
# Должно быть: backend ... Up X minutes (healthy)

# 2. Посмотреть реальное время запуска
docker logs backend | grep "Started.*Application"
# Должно быть < 420 секунд для обычного деплоя

# 3. Проверить health endpoint
curl http://your-server:8080/actuator/health
# Должно вернуть: {"status":"UP"}
```

## 📚 Дополнительная информация

- **Подробности**: см. `DEPLOYMENT_TIMEOUTS.md`
- **Health check настройка**: см. `HEALTH_CHECK_SETUP.md`
- **Оптимизация деплоя**: см. `DEPLOYMENT_OPTIMIZATION.md`

## ⚡ Если все еще медленно

Если даже 10 минут недостаточно (очень маловероятно):

1. **Проверить ресурсы VPS**:
   ```bash
   docker stats backend
   free -h
   ```

2. **Увеличить таймауты** в `deploy.sh`:
   ```bash
   HEALTH_CHECK_TIMEOUT=900  # 15 минут для первого деплоя
   ```

3. **Рассмотреть увеличение ресурсов VPS** (RAM, CPU)

4. **Оптимизировать Spring Boot** (см. DEPLOYMENT_TIMEOUTS.md):
   - Lazy initialization
   - Disable JMX
   - Optimize JPA

---

**Готово!** Закоммитьте и запушьте - деплой должен пройти успешно даже на медленном VPS, с немедленной детекцией крашей! 🚀
