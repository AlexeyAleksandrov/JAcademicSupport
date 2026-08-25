# Запуск проекта локально

Инструкция для развёртывания приложения на Windows.

---

## Требования

- **Java**: 17 или выше
- **PostgreSQL**: 17.x
- **Maven**: встроен (mvnw)

---

## 1. Установка PostgreSQL

### 1.1. Скачать PostgreSQL 17
https://www.postgresql.org/download/windows/

### 1.2. Установить с параметрами:
- Пользователь: `postgres`
- Пароль: `1111`
- Порт: `5432`

### 1.3. Проверка установки:
```powershell
psql --version
```
Ожидается: `psql (PostgreSQL) 17.x`

---

## 2. Создание базы данных

### 2.1. Создать БД:
```powershell
$env:PGPASSWORD='1111'
& "C:\Program Files\PostgreSQL\17\bin\createdb.exe" -U postgres AcademicSupport
```

### 2.2. Проверка:
```powershell
$env:PGPASSWORD='1111'
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -l
```
В списке должна быть БД `AcademicSupport`.

---

## 3. Импорт данных из бэкапа

### 3.1. Восстановление БД:
```powershell
$env:PGPASSWORD='1111'
$env:PAGER=''
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -d AcademicSupport -f db\backup_2026.08.23.sql
```

**Важно**: Выполнять из корня проекта (`JAcademicSupport/`).

### 3.2. Проверка импорта:
```powershell
$env:PGPASSWORD='1111'
$env:PAGER=''
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -d AcademicSupport -P pager=off -c "\dt"
```
Должны появиться таблицы: `competency`, `work_skill`, `vacancy`, и др.

---

## 4. Запуск приложения

### 4.1. Первый запуск (сборка):
```powershell
.\mvnw clean install
```

### 4.2. Запуск Spring Boot:
```powershell
.\mvnw spring-boot:run
```

### 4.3. Ожидаемый вывод:
```
The following 1 profile is active: "local"
...
Tomcat started on port 8080 (http)
Started JAcademicSupprtApplication in X.XXX seconds
```

**Профиль `local`** — авторизация отключена, все эндпоинты доступны без JWT токена.

---

## 5. Проверка работы

Открыть HTML страницы в браузере:
- http://localhost:8080/curriculum.html
- http://localhost:8080/dst.html
- http://localhost:8080/foresight.html

---

## 6. Остановка приложения

**Ctrl+C** в терминале или:
```powershell
# Найти процесс Java
Get-Process -Name java | Where-Object {$_.Path -like "*JAcademicSupport*"} | Stop-Process -Force
```

---

## Troubleshooting

### Ошибка: "Could not connect to database"
- Проверить, запущен ли PostgreSQL: `Get-Service -Name postgresql*`
- Проверить пароль: должен быть `1111`
- Проверить порт: `5432`

### Ошибка: "Port 8080 already in use"
```powershell
# Освободить порт 8080
Get-NetTCPConnection -LocalPort 8080 | Select-Object OwningProcess | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }
```

### Ошибка компиляции Maven
```powershell
# Очистить кэш Maven
.\mvnw clean
Remove-Item -Recurse -Force target
.\mvnw install
```

---

## Дополнительно

### Переключение на production профиль (с авторизацией):
```powershell
.\mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

### Просмотр логов БД:
```powershell
$env:PGPASSWORD='1111'
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -d AcademicSupport -c "SELECT tablename FROM pg_tables WHERE schemaname='public';"
```

### Пересоздание БД (если нужно):
```powershell
$env:PGPASSWORD='1111'
& "C:\Program Files\PostgreSQL\17\bin\dropdb.exe" -U postgres AcademicSupport
& "C:\Program Files\PostgreSQL\17\bin\createdb.exe" -U postgres AcademicSupport
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -d AcademicSupport -f db\backup_2026.08.23.sql
```
