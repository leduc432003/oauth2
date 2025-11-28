@echo off
REM Docker Management Script for OAuth2 JWT Application (Windows)

setlocal enabledelayedexpansion

if "%1"=="" goto help
if "%1"=="help" goto help
if "%1"=="--help" goto help
if "%1"=="-h" goto help

if "%1"=="start" goto start
if "%1"=="stop" goto stop
if "%1"=="restart" goto restart
if "%1"=="build" goto build
if "%1"=="logs" goto logs
if "%1"=="status" goto status
if "%1"=="health" goto health
if "%1"=="clean" goto clean
if "%1"=="backup" goto backup
if "%1"=="restore" goto restore

echo [ERROR] Unknown command: %1
echo.
goto help

:start
echo [INFO] Starting all services...
if not exist .env (
    echo [WARN] .env file not found. Creating from .env.example...
    copy .env.example .env
    echo [INFO] .env file created. Please update it with your configuration.
    exit /b 1
)
docker-compose up -d
echo [INFO] Services started. Use 'docker-compose logs -f' to view logs.
goto end

:stop
echo [INFO] Stopping all services...
docker-compose stop
echo [INFO] Services stopped.
goto end

:restart
echo [INFO] Restarting all services...
docker-compose restart
echo [INFO] Services restarted.
goto end

:build
echo [INFO] Building and starting services...
if not exist .env (
    echo [WARN] .env file not found. Creating from .env.example...
    copy .env.example .env
    echo [INFO] .env file created. Please update it with your configuration.
    exit /b 1
)
docker-compose up -d --build
echo [INFO] Services built and started.
goto end

:logs
if "%2"=="" (
    docker-compose logs -f
) else (
    docker-compose logs -f %2
)
goto end

:status
echo [INFO] Checking services status...
docker-compose ps
goto end

:health
echo [INFO] Checking health status...
echo.
echo MySQL:
docker-compose exec db mysqladmin ping -h localhost -u root -p123456 2>nul && echo OK - Healthy || echo ERROR - Unhealthy
echo.
echo Redis:
docker-compose exec redis redis-cli ping 2>nul && echo OK - Healthy || echo ERROR - Unhealthy
echo.
echo Application:
curl -s http://localhost:8080/api/actuator/health >nul 2>&1 && echo OK - Healthy || echo ERROR - Unhealthy
goto end

:clean
echo [WARN] This will remove all containers and volumes. Are you sure? (Y/N)
set /p response=
if /i "%response%"=="Y" (
    echo [INFO] Cleaning up...
    docker-compose down -v
    echo [INFO] Cleanup complete.
) else (
    echo [INFO] Cleanup cancelled.
)
goto end

:backup
echo [INFO] Creating database backup...
for /f "tokens=2-4 delims=/ " %%a in ('date /t') do (set mydate=%%c%%a%%b)
for /f "tokens=1-2 delims=/:" %%a in ('time /t') do (set mytime=%%a%%b)
set timestamp=%mydate%_%mytime%
set backup_file=backup_%timestamp%.sql
docker-compose exec -T db mysqldump -u root -p123456 oauth2 > %backup_file%
echo [INFO] Backup created: %backup_file%
goto end

:restore
if "%2"=="" (
    echo [ERROR] Please provide backup file path
    exit /b 1
)
echo [WARN] This will restore the database from %2. Are you sure? (Y/N)
set /p response=
if /i "%response%"=="Y" (
    echo [INFO] Restoring database...
    docker-compose exec -T db mysql -u root -p123456 oauth2 < %2
    echo [INFO] Database restored.
) else (
    echo [INFO] Restore cancelled.
)
goto end

:help
echo Docker Management Script for OAuth2 JWT Application
echo.
echo Usage: docker.bat [command] [options]
echo.
echo Commands:
echo     start       Start all services
echo     stop        Stop all services
echo     restart     Restart all services
echo     build       Build and start services
echo     logs        View logs (optional: specify service name)
echo     status      Check services status
echo     health      Check health of all services
echo     clean       Remove all containers and volumes
echo     backup      Backup MySQL database
echo     restore     Restore MySQL database (requires backup file path)
echo     help        Show this help message
echo.
echo Examples:
echo     docker.bat start
echo     docker.bat logs app
echo     docker.bat backup
echo     docker.bat restore backup_20231127_120000.sql
echo.
goto end

:end
endlocal
