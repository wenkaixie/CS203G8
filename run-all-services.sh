#!/bin/bash

# Function to kill processes running on specific ports
kill_ports() {
    local ports=(7070 8080 9090 9696 3000)
    echo "Killing any processes running on ports: ${ports[*]}..."

    for port in "${ports[@]}"; do
        # Find the process running on the port and kill it
        pid=$(lsof -t -i:$port)
        if [ -n "$pid" ]; then
            echo "Killing process $pid on port $port..."
            kill -9 "$pid" || echo "Failed to kill process on port $port"
        else
            echo "No process running on port $port."
        fi
    done
}

# Function to perform a Maven clean install for shared-library
build_shared_library() {
    local shared_library_dir=$1

    echo "Building shared-library from $shared_library_dir..."
    (
        cd "$shared_library_dir" || exit
        mvn clean install
    )
}

# Function to start a Spring Boot service in a new terminal
start_service() {
    local service_dir=$1
    local service_name=$2

    echo "Starting $service_name from $service_dir in a new terminal..."
    osascript -e "tell application \"Terminal\" to do script \"cd '$service_dir' && mvn spring-boot:run\""
}

# Function to start the React application in a new terminal
start_react_app() {
    local react_dir=$1

    echo "Starting React application from $react_dir in a new terminal..."
    osascript -e "tell application \"Terminal\" to do script \"cd '$react_dir' && npm start\""
}

# Kill any processes running on the specified ports
kill_ports

# Build the shared-library
build_shared_library "backend/shared-library"

# List of Spring Boot services and their directories
services=(
    "playerManagementService"
    "adminManagementService"
    "tournamentService"
    "sagaOrchestrator"
)

# Start all Spring Boot services in new terminals
for service in "${services[@]}"; do
    # Adjusted path for microservices inside the backend folder
    start_service "backend/$service" "$service"
done

# Start the React application in a new terminal
start_react_app "frontend"

echo "All services and the React application are starting in new terminals."