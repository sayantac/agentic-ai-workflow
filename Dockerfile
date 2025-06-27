# Multi-module Dockerfile for Maven project

# Build stage
FROM maven:3.9-eclipse-temurin-21-jammy AS builder
WORKDIR /build
ARG MODULE

# Copy only the parent pom.xml and the specified module directory
COPY pom.xml pom.xml
COPY ${MODULE} ${MODULE}

WORKDIR /build/${MODULE}
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
ARG MODULE

COPY --from=builder /build/${MODULE}/target/*.jar app.jar

# Environment variables with defaults that can be overridden
ENV SPRING_DATASOURCE_URL=""
ENV SPRING_DATASOURCE_USERNAME=""
ENV SPRING_DATASOURCE_PASSWORD=""
ENV AWS_REGION=""
ENV AWS_ACCESS_KEY=""
ENV AWS_SECRET_KEY=""
ENV AWS_SESSION_TOKEN=""

# Expose the application port
EXPOSE 8080

# Health check using curl to check the application's actuator health endpoint
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"] 