# syntax=docker/dockerfile:1.4
# Включаем BuildKit для оптимизации

# Используем multi-stage build для автоматической сборки jar внутри контейнера
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Копируем только pom.xml и mvn wrapper для кэширования зависимостей
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY mvnw.cmd .

# Скачиваем зависимости (этот слой будет кэшироваться, пока pom.xml не изменится)
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B

# Теперь копируем исходники (этот слой инвалидируется только при изменении кода)
COPY src ./src

# Собираем приложение с использованием кэша Maven
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests -B

# Финальный образ - только runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Устанавливаем wget для healthcheck и netcat для fallback проверки
RUN apk add --no-cache wget netcat-openbsd

# Копируем только собранный jar
COPY --from=build /app/target/JAcademicSupport-0.0.1-SNAPSHOT.jar app.jar
COPY scripts/cron-job.sh /app/scripts/cron-job.sh

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod

# Используем exec форму для корректной обработки сигналов
ENTRYPOINT ["java", "-jar", "/app/app.jar"]