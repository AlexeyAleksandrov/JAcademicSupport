#!/bin/bash

# Health check script for Spring Boot application
# This script waits for the application to be fully started and healthy

set -e

CONTAINER_NAME="${1:-backend}"
MAX_WAIT_TIME="${2:-600}"  # Увеличен default до 600 секунд (10 минут) для медленного VPS
CHECK_INTERVAL=5  # Check every 5 seconds

echo "=== Health Check Started ==="
echo "Container: $CONTAINER_NAME"
echo "Max wait time: ${MAX_WAIT_TIME}s ($(($MAX_WAIT_TIME / 60)) minutes)"
echo "Check interval: ${CHECK_INTERVAL}s"

START_TIME=$(date +%s)

# Function to check if container is running
is_container_running() {
    docker ps --filter "name=$CONTAINER_NAME" --filter "status=running" --format "{{.Names}}" | grep -q "$CONTAINER_NAME"
}

# Function to check if container crashed (exited with non-zero code)
check_container_crashed() {
    # Проверяем есть ли контейнер в состоянии exited
    local exited_status=$(docker ps -a --filter "name=$CONTAINER_NAME" --filter "status=exited" --format "{{.Status}}" 2>/dev/null)
    
    if [ -n "$exited_status" ]; then
        # Получаем exit code
        local exit_code=$(docker inspect "$CONTAINER_NAME" --format='{{.State.ExitCode}}' 2>/dev/null || echo "")
        
        if [ -n "$exit_code" ] && [ "$exit_code" != "0" ]; then
            echo "❌ CRITICAL: Container crashed with exit code $exit_code"
            echo ""
            echo "=== Container Status ==="
            echo "$exited_status"
            return 0  # Crashed
        fi
    fi
    
    return 1  # Not crashed
}

# Function to get application logs
get_app_logs() {
    docker logs "$CONTAINER_NAME" --tail=100 2>&1
}

# Function to check for Spring Boot startup message
check_spring_started() {
    get_app_logs | grep -q "Started.*Application in"
}

# Function to get startup progress info
get_startup_progress() {
    local logs=$(get_app_logs)
    
    # Возвращаем ТОЛЬКО самое актуальное состояние (последнее достигнутое)
    # Проверяем в обратном порядке - от последних шагов к первым
    
    if echo "$logs" | grep -q "Tomcat started on port"; then
        echo "Tomcat started, finalizing..."
        return
    fi
    
    if echo "$logs" | grep -q "Starting service.*Tomcat"; then
        echo "Starting Tomcat server..."
        return
    fi
    
    if echo "$logs" | grep -q "Initialized JPA EntityManagerFactory"; then
        echo "JPA initialization complete..."
        return
    fi
    
    if echo "$logs" | grep -q "HikariPool.*Starting\|HikariPool.*Start completed"; then
        echo "Connecting to database..."
        return
    fi
    
    if echo "$logs" | grep -q "Bootstrapping Spring Data JPA repositories"; then
        echo "Initializing JPA repositories..."
        return
    fi
    
    if echo "$logs" | grep -q "Starting JAcademicSupprtApplication"; then
        echo "Starting application..."
        return
    fi
    
    # Если ничего не найдено, возвращаем пустую строку
    echo ""
}

# Function to check for fatal errors
check_for_errors() {
    local logs=$(get_app_logs)
    
    # Check for common Spring Boot startup errors
    if echo "$logs" | grep -qE "(Error starting ApplicationContext|APPLICATION FAILED TO START|Caused by:.*Exception|BeanCreationException|UnsatisfiedDependencyException)"; then
        return 0  # Error found
    fi
    
    return 1  # No errors
}

echo ""
echo "Waiting for container to be running..."
echo "⚠️  Note: Running on slow VPS - startup may take up to 5-7 minutes"
echo ""

INITIAL_WAIT_TIMEOUT=120  # Ждём макс 2 минуты пока контейнер запустится
WAIT_START=$(date +%s)

while ! is_container_running; do
    ELAPSED=$(($(date +%s) - START_TIME))
    WAIT_ELAPSED=$(($(date +%s) - WAIT_START))
    
    # Проверяем не крашнулся ли контейнер
    if check_container_crashed; then
        echo ""
        echo "=== Crash Logs (last 100 lines) ==="
        get_app_logs
        exit 1
    fi
    
    if [ $WAIT_ELAPSED -ge $INITIAL_WAIT_TIMEOUT ]; then
        echo "❌ ERROR: Container did not start within ${INITIAL_WAIT_TIMEOUT}s"
        echo ""
        echo "=== Docker Status ==="
        docker ps -a --filter "name=$CONTAINER_NAME"
        exit 1
    fi
    
    sleep $CHECK_INTERVAL
done

echo "✓ Container is running"
echo ""
echo "⏳ Waiting for Spring Boot to start (this may take several minutes)..."
echo ""

LAST_LOG_CHECK=0
LAST_PROGRESS_CHECK=0
LAST_PROGRESS=""
LAST_CRASH_CHECK=0
LAST_STATUS_UPDATE=0

while true; do
    ELAPSED=$(($(date +%s) - START_TIME))
    
    # КРИТИЧНО: Проверяем не упал ли контейнер каждые 5 секунд
    if [ $((ELAPSED - LAST_CRASH_CHECK)) -ge 5 ]; then
        if check_container_crashed; then
            echo ""
            echo "=== Crash Logs (last 100 lines) ==="
            get_app_logs
            exit 1
        fi
        LAST_CRASH_CHECK=$ELAPSED
    fi
    
    # Проверяем не запущен ли контейнер, но уже не работает
    if ! is_container_running; then
        echo ""
        echo "❌ ERROR: Container stopped unexpectedly"
        if check_container_crashed; then
            echo "=== Crash Logs ==="
            get_app_logs
        fi
        exit 1
    fi
    
    # Check if max wait time exceeded
    if [ $ELAPSED -ge $MAX_WAIT_TIME ]; then
        echo ""
        echo "❌ ERROR: Application did not start within ${MAX_WAIT_TIME}s ($(($MAX_WAIT_TIME / 60)) minutes)"
        echo ""
        echo "=== Last 100 lines of logs ==="
        get_app_logs
        exit 1
    fi
    
    # Show progress every 30 seconds (instead of every 10)
    if [ $((ELAPSED - LAST_PROGRESS_CHECK)) -ge 30 ]; then
        PROGRESS=$(get_startup_progress)
        if [ -n "$PROGRESS" ] && [ "$PROGRESS" != "$LAST_PROGRESS" ]; then
            echo "📋 $PROGRESS"
            LAST_PROGRESS="$PROGRESS"
        fi
        LAST_PROGRESS_CHECK=$ELAPSED
    fi
    
    # Show status update every 30 seconds
    if [ $((ELAPSED - LAST_STATUS_UPDATE)) -ge 30 ]; then
        echo "   ... still waiting (${ELAPSED}s elapsed, ~$(($ELAPSED / 60))m)"
        LAST_STATUS_UPDATE=$ELAPSED
    fi
    
    # Check for fatal errors every 15 seconds
    if [ $((ELAPSED - LAST_LOG_CHECK)) -ge 15 ]; then
        if check_for_errors; then
            echo ""
            echo "❌ ERROR: Fatal error detected in application logs"
            echo ""
            echo "=== Error logs ==="
            get_app_logs
            exit 1
        fi
        LAST_LOG_CHECK=$ELAPSED
    fi
    
    # Check if Spring Boot has started - THIS IS THE ONLY SUCCESS CONDITION
    if check_spring_started; then
        END_TIME=$(date +%s)
        TOTAL_TIME=$((END_TIME - START_TIME))
        
        echo ""
        echo "✅ SUCCESS: Spring Boot application started!"
        echo ""
        echo "=== Health Check Summary ==="
        echo "Total startup time: ${TOTAL_TIME}s ($(($TOTAL_TIME / 60))m $(($TOTAL_TIME % 60))s)"
        echo "Container: $CONTAINER_NAME"
        echo "Status: HEALTHY"
        echo ""
        
        # Show startup message from logs
        echo "=== Spring Boot Startup Message ==="
        get_app_logs | grep "Started.*Application in" || echo "Application started successfully"
        
        exit 0
    fi
    
    sleep $CHECK_INTERVAL
done
