# Stage 1: Build the shared-library and adminManagementService JAR files
FROM maven:3.9.8-eclipse-temurin-17 AS build

# Set the working directory in the container
WORKDIR /opt/app

# Copy the shared-library directory into the container
COPY shared-library /opt/app/shared-library

# Build the shared-library and install it in the local Maven repository
WORKDIR /opt/app/shared-library

RUN mvn clean install -DskipTests

# Copy the adminManagementService directory into the container
WORKDIR /opt/app/adminManagementService
COPY adminManagementService /opt/app/adminManagementService

# Build the adminManagementService, ensuring shared-library is available
RUN mvn clean package -DskipTests

# Stage 2: Create a runtime image
FROM openjdk:17

# Set the working directory in the container
WORKDIR /opt/app

# Copy the JAR file from the build stage
COPY --from=build /opt/app/adminManagementService/target/*.jar /opt/app/app.jar

# Copy the serviceAccountKey.json file into the image
COPY serviceAccountKey.json /opt/app/serviceAccountKey.json

# Define the command to run the JAR file when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]




