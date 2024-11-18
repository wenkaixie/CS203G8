# Stage 1: Build the JAR file using Maven
# FROM maven:3.9.8-eclipse-temurin-17 AS build

# # Set the working directory in the container
# WORKDIR /opt/app

# # Copy pom.xml and download dependencies
# COPY pom.xml .
# RUN mvn dependency:go-offline

# # Copy the rest of the source code
# COPY src ./src

# # Build the application and create a JAR file, skipping tests to speed up the build
# RUN mvn clean package -DskipTests

# Stage 2: Create a minimal runtime image with Amazon Corretto JDK
FROM public.ecr.aws/amazoncorretto/amazoncorretto:17.0.8-al2023-headless

# Set the working directory in the container
WORKDIR /opt/app

# Use a non-root user for security
# Copy the serviceAccountKey.json file into the image
COPY serviceAccountKey.json /opt/app/serviceAccountKey.json

# Copy the JAR file from the build stage
# COPY --from=build /opt/app/target/*.jar /opt/app/app.jar
COPY target/*.jar /opt/app/app.jar

# Define the command to run the JAR file when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]


# You copy the JAR file from the first stage (which was created in Stage 1) into this minimal image,
# and you define the command to run your application (with ENTRYPOINT).
# Stage 2 does not rebuild anything—it just reuses the build artifact (the JAR file) from Stage 1.
