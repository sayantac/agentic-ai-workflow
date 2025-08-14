# Build stage
FROM maven:3.9.6-eclipse-temurin-21-jammy AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-jammy

# Add metadata labels
LABEL maintainer="sayantan.chakraborty@nagarro.com"
LABEL version="1.0"
LABEL description="Spring Boot MCP Server"

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"] 