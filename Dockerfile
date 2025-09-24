# Используем multi-stage build для автоматической сборки jar внутри контейнера
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/JAcademicSupport-0.0.1-SNAPSHOT.jar app.jar
COPY scripts/cron-job.sh /app/scripts/cron-job.sh
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-jar", "/app/app.jar"]