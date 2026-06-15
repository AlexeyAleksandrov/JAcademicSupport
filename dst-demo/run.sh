#!/bin/bash

# Скрипт для компиляции и запуска DST-демо (Linux/Mac)

echo "========================================"
echo "Компиляция DST-демо..."
echo "========================================"

# Создаём директорию для скомпилированных файлов
mkdir -p out

# Компилируем все Java файлы
javac -encoding UTF-8 -d out src/main/java/model/*.java src/main/java/service/*.java src/main/java/DemoApplication.java

if [ $? -ne 0 ]; then
    echo ""
    echo "ОШИБКА КОМПИЛЯЦИИ!"
    exit 1
fi

echo ""
echo "========================================"
echo "Запуск программы..."
echo "========================================"
echo ""

java -cp out DemoApplication

echo ""
read -p "Нажмите Enter для продолжения..."
