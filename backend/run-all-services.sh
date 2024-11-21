#!/bin/bash

# Function to start a Spring Boot service
start_service() {
    local service_dir=$1
    local service_name=$2

    echo "Starting $service_name from $service_dir..."
    (
        cd "$service_dir" || exit
        mvn spring-boot:run
    ) &
}

# List of services and their directories
services=(
    "playerManagementService"
    "adminManagementService"
    "tournamentService"
    "sagaOrchestrator"
)

# Start all services
for service in "${services[@]}"; do
    start_service "$service/src/main/java/csd/$service" "$service"
done

echo "All services are starting. Logs are displayed above."