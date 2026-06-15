@echo off
chcp 65001 >nul

REM Скрипт для компиляции и запуска DST-демо

echo ========================================
echo Компиляция DST-демо...
echo ========================================

REM Создаём директорию для скомпилированных файлов
if not exist out mkdir out

REM Компилируем все Java файлы
javac -encoding UTF-8 -d out src\main\java\model\*.java src\main\java\service\*.java src\main\java\DemoApplication.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ОШИБКА КОМПИЛЯЦИИ!
    pause
    exit /b 1
)

echo.
echo ========================================
echo Запуск программы...
echo ========================================
echo.

java -cp out DemoApplication

echo.
pause
