@echo off
echo Starting all microservices...

:: Start Microservices
echo Starting PlayerManagementService...
start cmd /k "cd playerManagementService && mvn spring-boot:run"

echo Starting AdminManagementService...
start cmd /k "cd adminManagementService && mvn spring-boot:run"

echo Starting TournamentService...
start cmd /k "cd tournamentService && mvn spring-boot:run"

echo Starting SagaOrchestrator...
start cmd /k "cd sagaOrchestrator && mvn spring-boot:run"

echo All services are starting. Each service has been opened in a new terminal window.
pause