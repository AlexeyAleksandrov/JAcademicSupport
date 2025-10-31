#!/bin/bash

# Health check script for Spring Boot application
# This script waits for the application to be fully started and healthy

set -e

CONTAINER_NAME="${1:-backend}"
MAX_WAIT_TIME="${2:-120}"  # Maximum wait time in seconds
CHECK_INTERVAL=5  # Check every 5 seconds

echo "=== Health Check Started ==="
echo "Container: $CONTAINER_NAME"
echo "Max wait time: ${MAX_WAIT_TIME}s"
echo "Check interval: ${CHECK_INTERVAL}s"

START_TIME=$(date +%s)

# Function to check if container is running
is_container_running() {
    docker ps --filter "name=$CONTAINER_NAME" --filter "status=running" --format "{{.Names}}" | grep -q "$CONTAINER_NAME"
}

# Function to check application health
check_health() {
    # Try readiness endpoint first (includes all health checks)
    if docker exec "$CONTAINER_NAME" wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health/readiness 2>&1 | grep -q "200 OK"; then
        return 0
    fi
    
    # Fallback to general health endpoint
    if docker exec "$CONTAINER_NAME" wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health 2>&1 | grep -q "200 OK"; then
        return 0
    fi
    
    return 1
}

# Function to get application logs
get_app_logs() {
    docker logs "$CONTAINER_NAME" --tail=50 2>&1
}

# Function to check for Spring Boot startup message
check_spring_started() {
    get_app_logs | grep -q "Started.*Application in"
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
while ! is_container_running; do
    ELAPSED=$(($(date +%s) - START_TIME))
    if [ $ELAPSED -ge $MAX_WAIT_TIME ]; then
        echo "❌ ERROR: Container did not start within ${MAX_WAIT_TIME}s"
        exit 1
    fi
    echo "⏳ Container not running yet... (${ELAPSED}s elapsed)"
    sleep $CHECK_INTERVAL
done

echo "✓ Container is running"
echo ""
echo "Waiting for Spring Boot application to start..."

LAST_LOG_CHECK=0
while true; do
    ELAPSED=$(($(date +%s) - START_TIME))
    
    # Check if max wait time exceeded
    if [ $ELAPSED -ge $MAX_WAIT_TIME ]; then
        echo ""
        echo "❌ ERROR: Application did not become healthy within ${MAX_WAIT_TIME}s"
        echo ""
        echo "=== Last 50 lines of logs ==="
        get_app_logs
        exit 1
    fi
    
    # Check for fatal errors every 10 seconds
    if [ $((ELAPSED - LAST_LOG_CHECK)) -ge 10 ]; then
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
    
    # Check if Spring Boot has started
    if check_spring_started; then
        echo "✓ Spring Boot application started (${ELAPSED}s elapsed)"
        echo ""
        echo "Verifying health endpoint..."
        
        # Wait a bit for all health checks to complete
        sleep 3
        
        if check_health; then
            END_TIME=$(date +%s)
            TOTAL_TIME=$((END_TIME - START_TIME))
            
            echo "✅ SUCCESS: Application is healthy!"
            echo ""
            echo "=== Health Check Summary ==="
            echo "Total startup time: ${TOTAL_TIME}s"
            echo "Container: $CONTAINER_NAME"
            echo "Status: HEALTHY"
            echo ""
            
            # Show startup message from logs
            echo "=== Spring Boot Startup Message ==="
            get_app_logs | grep "Started.*Application in" || echo "Could not find startup message"
            
            exit 0
        else
            echo "⚠️  Warning: Spring started but health endpoint not ready yet"
        fi
    fi
    
    echo "⏳ Waiting for application to start... (${ELAPSED}s elapsed)"
    sleep $CHECK_INTERVAL
done
