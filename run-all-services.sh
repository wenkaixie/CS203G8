#!/bin/bash

# Function to kill processes running on specific ports
kill_ports() {
    local ports=(7070 8080 9090 9696)
    echo "Killing any processes running on ports: ${ports[*]}..."

    for port in "${ports[@]}"; do
        # Find the process running on the port
        pid=$(lsof -t -i:$port 2>/dev/null)
        if [ -n "$pid" ]; then
            echo "Killing process $pid on port $port..."
            kill -9 "$pid" 2>/dev/null || echo "Failed to kill process $pid on port $port. You might need sudo."
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

# Function to build a microservice
build_microservice() {
    local service_dir=$1
    local service_name=$2

    echo "Building $service_name from $service_dir..."
    (
        cd "$service_dir" || exit
        mvn clean package -DskipTests
    )
}

# Function to run a microservice JAR
run_microservice() {
    local service_dir=$1
    local service_name=$2
    local jar_file="$service_dir/target/$service_name-0.0.1-SNAPSHOT.jar"

    if [ -f "$jar_file" ]; then
        echo "Starting $service_name from $jar_file..."
        java -jar "$jar_file" &
    else
        echo "JAR file for $service_name not found. Ensure it is built properly."
    fi
}

# Function to start the React application
start_react_app() {
    local react_dir=$1
    echo "Starting React application from $react_dir..."
    (
        cd "$react_dir" || exit
        npm start
    ) &
}

# Kill any processes running on the specified ports
kill_ports

# Build the shared-library
build_shared_library "backend/shared-library"

# List of Spring Boot services and their directories
services=(
    # "playerManagementService"
    # "adminManagementService"
    # "tournamentService"
    "sagaOrchestrator"
)

# Build and start all Spring Boot services
for service in "${services[@]}"; do
    service_dir="backend/$service"
    service_name=$(echo "$service" | awk '{print tolower($0)}') # Convert service name to lowercase for JAR naming
    build_microservice "$service_dir" "$service_name"
    run_microservice "$service_dir" "$service_name"
done

# Start the React application
start_react_app "frontend"

echo "All services and the React application are starting."