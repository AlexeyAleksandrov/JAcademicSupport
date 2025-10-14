#!/bin/bash
set -e  # Остановить выполнение при любой ошибке

echo "=== Starting deployment ==="
date

echo "1. Stopping existing containers..."
docker compose down

echo "2. Pulling latest code from GitHub..."
git fetch origin
git reset --hard origin/master
git pull origin master

echo "3. Building and starting new containers..."
docker compose up -d --build

echo "4. Checking container status..."
docker compose ps

echo "=== Deployment completed successfully! ==="
date