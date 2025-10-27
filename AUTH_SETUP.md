# Система авторизации и аутентификации с JWT

## Обзор

Проект использует **Spring Security** с **JWT (JSON Web Token)** для аутентификации и авторизации пользователей.

## Компоненты системы

### 1. User Entity (`models/User.java`)
Модель пользователя с полями:
- `id` - уникальный идентификатор
- `firstName` - имя
- `lastName` - фамилия
- `middleName` - отчество (необязательно)
- `email` - email (используется как username)
- `password` - хешированный пароль (BCrypt)
- `enabled` - статус активации

### 2. Security Configuration (`configuration/SecurityConfig.java`)
Конфигурация Spring Security:
- Отключен CSRF (используется JWT)
- Stateless сессии
- BCrypt для хеширования паролей
- JWT фильтр для каждого запроса
- Публичные эндпоинты: `/api/auth/**`
- Все остальные эндпоинты требуют аутентификации

### 3. JWT Components

#### JwtTokenProvider (`security/JwtTokenProvider.java`)
- Генерация JWT токенов
- Валидация токенов
- Извлечение данных из токенов
- Использует HS256 алгоритм

#### JwtAuthenticationFilter (`security/JwtAuthenticationFilter.java`)
- Перехватывает все HTTP запросы
- Извлекает JWT из заголовка `Authorization: Bearer <token>`
- Валидирует токен
- Устанавливает аутентификацию в Security Context

### 4. Services

#### CustomUserDetailsService (`services/CustomUserDetailsService.java`)
- Загружает пользователя из БД по email
- Реализует интерфейс Spring Security `UserDetailsService`

#### UserService (`services/UserService.java`)
- Регистрация новых пользователей
- Аутентификация пользователей
- Генерация JWT токенов

### 5. REST API Endpoints (`controllers/rest/AuthController.java`)

#### POST `/api/auth/register`
Регистрация нового пользователя

**Request Body:**
```json
{
  "firstName": "Иван",
  "lastName": "Иванов",
  "middleName": "Иванович",
  "email": "ivan@example.com",
  "password": "password123"
}
```

**Success Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": 1,
  "email": "ivan@example.com",
  "firstName": "Иван",
  "lastName": "Иванов",
  "middleName": "Иванович"
}
```

**Error Response (400):**
```json
{
  "status": 400,
  "message": "Ошибка регистрации",
  "details": "Email уже используется!",
  "timestamp": "2025-10-27T18:10:21"
}
```

#### POST `/api/auth/login`
Авторизация пользователя

**Request Body:**
```json
{
  "email": "ivan@example.com",
  "password": "password123"
}
```

**Success Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": 1,
  "email": "ivan@example.com",
  "firstName": "Иван",
  "lastName": "Иванов",
  "middleName": "Иванович"
}
```

**Error Response (401):**
```json
{
  "status": 401,
  "message": "Ошибка авторизации",
  "details": "Неверный email или пароль",
  "timestamp": "2025-10-27T18:10:21"
}
```

## Конфигурация

### application.properties
```properties
# JWT Configuration
jwt.secret=${JWT_SECRET:MySecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLongForHS256Algorithm}
jwt.expiration=${JWT_EXPIRATION:86400000}
```

### Переменные окружения

Для продакшн окружения установите следующие переменные:

```bash
JWT_SECRET=your_secret_key_at_least_256_bits_long
JWT_EXPIRATION=86400000  # 24 часа в миллисекундах
```

**ВАЖНО:** 
- JWT secret должен быть минимум 256 бит (32 символа) для алгоритма HS256
- Никогда не храните секретный ключ в коде или публичных репозиториях
- В продакшн используйте сильный случайный ключ

## Использование

### 1. Регистрация пользователя

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Иван",
    "lastName": "Иванов",
    "middleName": "Иванович",
    "email": "ivan@example.com",
    "password": "password123"
  }'
```

### 2. Вход в систему

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ivan@example.com",
    "password": "password123"
  }'
```

### 3. Использование JWT токена

После успешной регистрации или входа вы получите JWT токен. Используйте его для доступа к защищённым эндпоинтам:

```bash
curl -X GET http://localhost:8080/api/protected-endpoint \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

## Безопасность

1. **Хеширование паролей**: Используется BCrypt с автоматической солью
2. **JWT подпись**: Токены подписываются секретным ключом
3. **Истечение токенов**: Токены действительны 24 часа (настраивается)
4. **Stateless**: Сервер не хранит сессии, всё в токене
5. **HTTPS**: В продакшн обязательно используйте HTTPS

## Docker Deployment

В `docker-compose.yml` добавлены переменные окружения для JWT:

```yaml
environment:
  - JWT_SECRET=${JWT_SECRET:-MySecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLongForHS256Algorithm}
  - JWT_EXPIRATION=${JWT_EXPIRATION:-86400000}
```

Установите переменные окружения перед запуском:

```bash
export JWT_SECRET="your_strong_secret_key_here"
export JWT_EXPIRATION=86400000
docker-compose up -d
```

## Будущие улучшения

- [ ] Подтверждение email при регистрации
- [ ] Восстановление пароля
- [ ] Refresh токены
- [ ] Роли и права доступа (ROLE_USER, ROLE_ADMIN)
- [ ] OAuth2 интеграция (Google, VK и т.д.)
- [ ] Rate limiting для защиты от брутфорса

## Структура базы данных

Таблица `users`:
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);
```

## Troubleshooting

### Ошибка "JWT signature does not match"
- Проверьте, что используется правильный JWT_SECRET
- Убедитесь, что секрет минимум 256 бит (32 символа)

### Ошибка "Expired JWT token"
- Токен истёк, нужно войти заново
- Можно увеличить `jwt.expiration` в настройках

### Ошибка "Email уже используется"
- Пользователь с таким email уже зарегистрирован
- Используйте другой email или войдите с существующим
