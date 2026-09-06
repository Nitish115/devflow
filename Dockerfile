# Stage 1: Build stage
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Copy pom.xml and pre-fetch dependencies for efficient Docker caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build the executable JAR
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user and group for security
RUN addgroup -S devflow && adduser -S devflow -G devflow

# Copy built application JAR from builder stage
COPY --from=builder /build/target/*.jar app.jar

# Ensure uploads directory exists with correct permissions
RUN mkdir -p /app/uploads && chown -R devflow:devflow /app

USER devflow

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
