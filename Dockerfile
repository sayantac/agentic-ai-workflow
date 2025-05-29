# Build stage
FROM maven:3.9-eclipse-temurin-21-jammy AS builder
WORKDIR /build
COPY . .

# Add build argument
ARG SPRING_PROFILES_ACTIVE=ollama
RUN mvn clean package -DskipTests -P${SPRING_PROFILES_ACTIVE}

# Run stage
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /build/target/*.jar app.jar

# Environment variables with defaults that can be overridden
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/adoptions
ENV SPRING_DATASOURCE_USERNAME=postgres
ENV SPRING_DATASOURCE_PASSWORD=postgres
ENV SERVER_PORT=8080
ENV AWS_REGION=us-east-1
ENV AWS_ACCESS_KEY=
ENV AWS_SECRET_KEY=
ENV AWS_SESSION_TOKEN=
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE}

# Expose the application port
EXPOSE 8080

# Health check using curl to check the application's actuator health endpoint
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"] 