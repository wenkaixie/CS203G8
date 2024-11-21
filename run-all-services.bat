@echo off

:: Function to kill processes running on specific ports
:kill_ports
setlocal enabledelayedexpansion
set ports=7070 8080 9090 9696 3000
echo Killing any processes running on ports: %ports%

for %%p in (%ports%) do (
    for /f "tokens=5" %%P in ('netstat -aon ^| findstr :%%p') do (
        echo Found process on port %%p with PID %%P. Attempting to kill...
        taskkill /F /PID %%P >nul 2>&1
        if !ERRORLEVEL! == 0 (
            echo Successfully killed process %%P on port %%p.
        ) else (
            echo Failed to kill process %%P on port %%p. You might need administrative privileges.
        )
    )
)
endlocal
goto :EOF

:: Function to build the shared-library
:build_shared_library
echo Building shared-library from %1...
pushd %1
mvn clean install
popd
goto :EOF

:: Function to build a microservice
:build_microservice
echo Building %2 from %1...
pushd %1
mvn clean package -DskipTests
popd
goto :EOF

:: Function to run a microservice JAR
:run_microservice
set service_dir=%1
set service_name=%2
set jar_file=%service_dir%\target\%service_name%-0.0.1-SNAPSHOT.jar

if exist "%jar_file%" (
    echo Starting %service_name% from %jar_file%...
    start cmd /k "java -jar \"%jar_file%\""
) else (
    echo JAR file for %service_name% not found. Ensure it is built properly.
)
goto :EOF

:: Function to start the React application
:start_react_app
set react_dir=%1
echo Starting React application from %react_dir%...
start cmd /k "cd /d \"%react_dir%\" && npm start"
goto :EOF

:: Main script execution

:: Kill any processes running on the specified ports
call :kill_ports

:: Build the shared-library
call :build_shared_library "backend\shared-library"

:: List of Spring Boot services and their directories
set services=playerManagementService adminManagementService tournamentService sagaOrchestrator

:: Build and start all Spring Boot services
for %%s in (%services%) do (
    set service_dir=backend\%%s
    set service_name=%%s
    call :build_microservice "!service_dir!" "!service_name!"
    call :run_microservice "!service_dir!" "!service_name!"
)

:: Start the React application
call :start_react_app "frontend"

echo All services and the React application are starting.