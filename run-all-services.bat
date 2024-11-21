@echo off

:: Function to kill processes running on specific ports
echo Killing any processes running on ports: 7070, 8080, 9090, 9696, 3000...
set ports=7070 8080 9090 9696 3000

for %%p in (%ports%) do (
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :%%p') do (
        echo Killing process %%a on port %%p...
        taskkill /PID %%a /F >nul 2>&1 || echo No process found on port %%p.
    )
)

:: Start Spring Boot services
set services=(
    playerManagementService
    adminManagementService
    tournamentService
    sagaOrchestrator
)

for %%s in %services% do (
    echo Starting %%s...
    start cmd /k "cd backend\%%s\src\main\java\csd\%%s && mvn spring-boot:run"
)

:: Start React application
echo Starting React application...
start cmd /k "cd frontend && npm start"

echo All services and the React application are starting. Each has been opened in a new terminal window.
pause