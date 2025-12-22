# Stage 1: Build stage
FROM maven:3.9.11-eclipse-temurin-25-alpine AS build

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml first for better layer caching
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests for faster builds)
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Create a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership to non-root user
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring:spring

# Expose the application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/actuator/health || exit 1

# Set JVM options for containerized environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
