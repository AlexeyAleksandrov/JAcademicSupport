# CORS Configuration for Backend

## Проблема

Frontend на `http://localhost:3000` не может получить данные с backend `http://194.135.20.4:8080` из-за CORS (Cross-Origin Resource Sharing) политики безопасности браузера.

## Решение: Настройка CORS на Backend

Backend сервер должен отправлять следующие HTTP заголовки в ответах:

```
Access-Control-Allow-Origin: http://localhost:3000
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
Access-Control-Allow-Headers: Content-Type, Authorization
Access-Control-Allow-Credentials: true
```

---

## Примеры настройки CORS

### Для Spring Boot (Java)

Добавьте конфигурацию CORS:

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {
    
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Разрешить запросы с localhost:3000
        config.addAllowedOrigin("http://localhost:3000");
        
        // Также можно разрешить все origins (НЕ рекомендуется для production)
        // config.addAllowedOrigin("*");
        
        // Разрешенные методы
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");
        
        // Разрешенные заголовки
        config.addAllowedHeader("*");
        
        // Разрешить credentials (cookies, authorization headers)
        config.setAllowCredentials(true);
        
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
```

**Или добавьте аннотацию к контроллеру:**

```java
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class RpdController {
    // ваш код контроллера
}
```

---

### Для Express.js (Node.js)

Установите пакет `cors`:

```bash
npm install cors
```

Настройте в `app.js` или `server.js`:

```javascript
const express = require('express');
const cors = require('cors');

const app = express();

// Разрешить запросы с localhost:3000
app.use(cors({
  origin: 'http://localhost:3000',
  credentials: true
}));

// Или разрешить все origins (НЕ рекомендуется для production)
// app.use(cors());

// ваши роуты...
```

---

### Для Django (Python)

Установите `django-cors-headers`:

```bash
pip install django-cors-headers
```

В `settings.py`:

```python
INSTALLED_APPS = [
    # ...
    'corsheaders',
    # ...
]

MIDDLEWARE = [
    'corsheaders.middleware.CorsMiddleware',
    'django.middleware.common.CommonMiddleware',
    # ...
]

# Разрешить запросы с localhost:3000
CORS_ALLOWED_ORIGINS = [
    "http://localhost:3000",
]

# Или разрешить все origins (НЕ рекомендуется для production)
# CORS_ALLOW_ALL_ORIGINS = True

CORS_ALLOW_METHODS = [
    'DELETE',
    'GET',
    'OPTIONS',
    'PATCH',
    'POST',
    'PUT',
]

CORS_ALLOW_HEADERS = [
    'accept',
    'accept-encoding',
    'authorization',
    'content-type',
    'dnt',
    'origin',
    'user-agent',
    'x-csrftoken',
    'x-requested-with',
]
```

---

### Для ASP.NET Core (C#)

В `Program.cs` или `Startup.cs`:

```csharp
var builder = WebApplication.CreateBuilder(args);

// Добавить CORS политику
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowReactApp",
        policy =>
        {
            policy.WithOrigins("http://localhost:3000")
                  .AllowAnyHeader()
                  .AllowAnyMethod()
                  .AllowCredentials();
        });
});

var app = builder.Build();

// Использовать CORS
app.UseCors("AllowReactApp");

app.Run();
```

---

## Временное решение для разработки

Если вы не можете настроить CORS на backend прямо сейчас, используйте одно из решений:

### Вариант 1: Chrome с отключенной CORS проверкой (НЕ БЕЗОПАСНО!)

**Только для разработки!**

Закройте все окна Chrome и запустите с флагом:

```bash
chrome.exe --disable-web-security --user-data-dir="C:/ChromeDevSession"
```

### Вариант 2: Браузерное расширение

Установите расширение для Chrome/Firefox:
- **"CORS Unblock"** или **"Allow CORS"**
- Включите его только на время разработки
- **Не забудьте отключить после!**

---

## Production настройки

Для production **НЕ используйте** `Access-Control-Allow-Origin: *`!

Вместо этого явно укажите домен вашего frontend приложения:

```
Access-Control-Allow-Origin: https://your-production-domain.com
```

---

## Проверка CORS

После настройки CORS на backend, проверьте заголовки ответа в DevTools браузера:

1. Откройте DevTools (F12)
2. Перейдите на вкладку **Network**
3. Обновите страницу
4. Найдите запрос к `/rpd/show`
5. Проверьте **Response Headers** - должны присутствовать заголовки `Access-Control-Allow-*`

---

## Контакты

Если нужна помощь с настройкой CORS на вашем конкретном backend сервере, обратитесь к backend команде с этим документом.
