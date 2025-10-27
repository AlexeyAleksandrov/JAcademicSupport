# Интеграция Frontend и Backend через Docker Network

## Проблема

Когда фронтенд и бэкенд развёрнуты в **разных Docker Compose проектах**, они находятся в **разных Docker сетях** и не могут общаться друг с другом по именам контейнеров.

### Симптомы:
- ✅ Запросы на `http://VPS_IP:8080/api/...` работают (прямое подключение)
- ❌ Запросы на `http://VPS_IP/api/...` (через Nginx) падают с timeout
- ❌ Nginx не может достучаться до `backend:8080`

## Решение: Общая Docker сеть

Создайте **внешнюю Docker сеть**, которую будут использовать оба проекта.

## Пошаговая инструкция

### Шаг 1: Создайте внешнюю сеть на VPS

Подключитесь к VPS и выполните:

```bash
docker network create app-network
```

Проверьте, что сеть создана:
```bash
docker network ls | grep app-network
```

### Шаг 2: Обновите конфигурации (уже сделано)

#### Backend (`JAcademicSupport/docker-compose.yml`)

```yaml
services:
  app:
    container_name: backend  # ✅ Имя для обращения из других контейнеров
    networks:
      - myapp-network  # Внутренняя сеть для postgres
      - app-network    # ✅ Общая сеть для фронтенда
    # ...

networks:
  myapp-network:
    driver: bridge
  app-network:
    external: true  # ✅ Сеть создана вне docker-compose
```

#### Frontend (`react_test/docker-compose.yml`)

```yaml
services:
  frontend:
    container_name: academic-support-frontend
    networks:
      - app-network  # ✅ Та же общая сеть
    # ❌ Убрали extra_hosts - больше не нужны
    # ...

networks:
  app-network:
    external: true  # ✅ Сеть создана вне docker-compose
```

#### Frontend Nginx (`react_test/nginx.conf`)

```nginx
location /api/ {
    proxy_pass http://backend:8080/api/;  # ✅ backend резолвится через Docker DNS
    # ...
}
```

### Шаг 3: Деплоймент на VPS

#### 3.1. Создайте сеть (если ещё не создали)

```bash
ssh your_user@194.135.20.4

# Создайте внешнюю сеть
docker network create app-network
```

#### 3.2. Деплой бэкенда

```bash
cd /home/JAcademicSupport/

# Остановите старые контейнеры
docker compose down

# Пуллим изменения из Git
git pull origin master

# Запускаем с новой конфигурацией
export JWT_SECRET="your_secret_here"
export JWT_EXPIRATION=86400000
export GIGACHAT_API_TOKEN="your_token_here"
docker compose up -d --build
```

#### 3.3. Деплой фронтенда

```bash
cd /home/react_test/  # или путь к вашему фронтенду

# Остановите старый контейнер
docker compose down

# Пуллим изменения
git pull origin master

# Запускаем с новой конфигурацией
docker compose up -d --build
```

#### 3.4. Проверка

```bash
# Проверьте, что оба контейнера в сети app-network
docker network inspect app-network

# Вы должны увидеть:
# - backend (myapp-backend)
# - academic-support-frontend
```

**Ожидаемый вывод:**
```json
{
  "Containers": {
    "xxx": {
      "Name": "backend",
      "IPv4Address": "172.x.x.x/16"
    },
    "yyy": {
      "Name": "academic-support-frontend",
      "IPv4Address": "172.x.x.y/16"
    }
  }
}
```

### Шаг 4: Тестирование

#### Тест 1: Прямое подключение к бэкенду (должно работать как раньше)

```bash
curl -X POST http://194.135.20.4:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Test",
    "lastName": "User",
    "email": "direct@example.com",
    "password": "password123"
  }'
```

#### Тест 2: Через Nginx (теперь тоже должно работать!)

```bash
curl -X POST http://194.135.20.4/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Test",
    "lastName": "User",
    "email": "nginx@example.com",
    "password": "password123"
  }'
```

#### Тест 3: Проверка из контейнера фронтенда

```bash
# Зайдите в контейнер фронтенда
docker exec -it academic-support-frontend sh

# Попробуйте достучаться до бэкенда
wget -O- http://backend:8080/api/health 2>/dev/null || echo "Backend недоступен"

# Если backend доступен, вы увидите ответ от API
# Если нет - проверьте сеть
```

## Схема работы

### До исправления (НЕ РАБОТАЛО):
```
Запрос → http://194.135.20.4/api/auth/register
    ↓
[Frontend Nginx на порту 80]
    ↓
Пытается proxy_pass на backend:8080
    ↓
❌ backend не найден (разные Docker сети)
    ↓
TIMEOUT ERROR
```

### После исправления (РАБОТАЕТ):
```
Запрос → http://194.135.20.4/api/auth/register
    ↓
[Frontend Nginx на порту 80]
    ↓
proxy_pass на backend:8080
    ↓
✅ backend найден через app-network
    ↓
[Backend Spring Boot :8080]
    ↓
SUCCESS ✅
```

## Архитектура сетей

```
┌─────────────────────────────────────────┐
│          VPS (194.135.20.4)             │
│                                         │
│  ┌───────────────────────────────────┐ │
│  │     app-network (external)        │ │
│  │                                   │ │
│  │  ┌─────────────────────────────┐ │ │
│  │  │  frontend                   │ │ │
│  │  │  :80                        │ │ │
│  │  │  (Nginx)                    │ │ │
│  │  └──────────┬──────────────────┘ │ │
│  │             │                     │ │
│  │             │ proxy_pass          │ │
│  │             ↓                     │ │
│  │  ┌─────────────────────────────┐ │ │
│  │  │  backend                    │ │ │
│  │  │  :8080                      │ │ │
│  │  │  (Spring Boot)              │ │ │
│  │  └──────────┬──────────────────┘ │ │
│  └─────────────┼───────────────────┘ │
│                │                       │
│  ┌─────────────┼───────────────────┐ │
│  │ myapp-network (internal)        │ │
│  │             │                     │ │
│  │  ┌──────────▼──────────────────┐ │ │
│  │  │  postgres                   │ │ │
│  │  │  :5432                      │ │ │
│  │  └─────────────────────────────┘ │ │
│  └─────────────────────────────────┘ │
└─────────────────────────────────────────┘

Внешний мир:
- :80  → frontend → backend
- :8080 → backend (прямой доступ)
```

## Troubleshooting

### ❌ Ошибка: "network app-network not found"

**Причина:** Внешняя сеть не создана

**Решение:**
```bash
docker network create app-network
```

### ❌ Nginx не может достучаться до backend

**Проверка 1:** Убедитесь, что backend в сети app-network
```bash
docker network inspect app-network | grep backend
```

**Проверка 2:** Проверьте имя контейнера
```bash
docker ps | grep backend
# Должно быть: backend (не myapp-backend)
```

**Проверка 3:** Проверьте из frontend контейнера
```bash
docker exec academic-support-frontend ping -c 3 backend
```

### ❌ "Connection refused" или "Connection timeout"

**Причина:** Контейнеры в разных сетях или backend не запущен

**Решение:**
```bash
# Проверьте статус контейнеров
docker ps

# Перезапустите оба проекта
cd /home/JAcademicSupport && docker compose down && docker compose up -d
cd /home/react_test && docker compose down && docker compose up -d
```

### ❌ После обновления всё равно не работает

**Полная пересборка:**
```bash
# 1. Остановите всё
cd /home/JAcademicSupport && docker compose down
cd /home/react_test && docker compose down

# 2. Убедитесь, что сеть существует
docker network create app-network 2>/dev/null || echo "Сеть уже существует"

# 3. Запустите бэкенд
cd /home/JAcademicSupport
export JWT_SECRET="..."
export GIGACHAT_API_TOKEN="..."
docker compose up -d --build

# 4. Дождитесь запуска бэкенда (30 сек)
docker logs backend -f
# Ctrl+C когда увидите "Started JAcademicSupprtApplication"

# 5. Запустите фронтенд
cd /home/react_test
docker compose up -d --build

# 6. Проверьте сеть
docker network inspect app-network
```

## Преимущества этого подхода

✅ **Изоляция:** Фронтенд не имеет доступа к БД напрямую  
✅ **Масштабируемость:** Легко добавить ещё сервисы в app-network  
✅ **Безопасность:** Postgres в отдельной внутренней сети  
✅ **Гибкость:** Можно обновлять фронт и бэк независимо  
✅ **Production-ready:** Стандартный подход для микросервисов  

## Использование в клиентском коде

### В React приложении используйте относительные пути:

```javascript
// ✅ ПРАВИЛЬНО - через Nginx на порту 80
axios.post('/api/auth/register', data)

// ❌ НЕПРАВИЛЬНО - прямое подключение к бэкенду
axios.post('http://194.135.20.4:8080/api/auth/register', data)
```

### Преимущества относительных путей:
- Работает локально и на продакшн
- Автоматически использует HTTPS если настроен SSL
- Nginx обрабатывает CORS
- Единая точка входа

## Дальнейшие улучшения

1. **SSL/HTTPS** - настройте Let's Encrypt для Nginx
2. **Rate Limiting** - защита от DDoS в Nginx
3. **Healthcheck** - добавьте проверки здоровья контейнеров
4. **Monitoring** - логирование и мониторинг через Prometheus/Grafana
5. **CI/CD** - автоматический деплой через GitHub Actions

## Для локальной разработки

Создайте сеть локально:
```bash
docker network create app-network
```

И используйте те же docker-compose.yml файлы!
