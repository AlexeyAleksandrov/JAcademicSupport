# Используем официальный образ OpenJDK с Alpine Linux (легковесный)
FROM openjdk:17-jdk-alpine

# Устанавливаем рабочую директорию внутри контейнера
WORKDIR /app

# Копируем скомпилированный JAR-файл из целевой директории в контейнер
# Предполагается, что ваш Maven-проект собирается в jar с именем 'app.jar'
# (Замените 'your-app-name-0.0.1-SNAPSHOT.jar' на актуальное имя вашего jar-файла)
COPY target/JAcademicSupport-0.0.1-SNAPSHOT.jar app.jar

# Копируем скрипт в контейнер
COPY scripts/cron-job.sh /app/scripts/cron-job.sh

# Открываем порт, на котором работает ваше Spring Boot приложение (часто 8080)
EXPOSE 8080

# Добавляем переменную окружения для активации профиля prod
ENV SPRING_PROFILES_ACTIVE=prod

# Команда для запуска приложения при старте контейнера
ENTRYPOINT ["java", "-jar", "/app/app.jar"]