# Multi-stage Dockerfile for Spring Boot 3 Backend with Java 21

# Stage 1: Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace/app

# Copy gradle wrapper & configs
COPY gradle/ gradle/
COPY gradlew gradlew.bat build.gradle settings.gradle ./

# Download dependencies (layer caching)
RUN ./gradlew dependencies --no-daemon || true

# Copy source code and build bootable jar
COPY src/ src/
RUN ./gradlew bootJar -x test --no-daemon

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S aqua && adduser -S aqua -G aqua
USER aqua:aqua

COPY --from=builder /workspace/app/build/libs/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=40.0 -XX:+ExitOnOutOfMemoryError" \
    PORT=8080

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar \"$@\"", "--"]
