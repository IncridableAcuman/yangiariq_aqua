# Multi-stage Dockerfile for YANGIARIQ AQUA AI Backend (Java 21 / Spring Boot 3)
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace/app

# Copy gradle wrapper & configs
COPY gradle/ gradle/
COPY gradlew build.gradle settings.gradle ./

# Download dependencies (layer caching)
RUN ./gradlew dependencies --no-daemon || true

# Copy source code and build production jar
COPY src/ src/
RUN ./gradlew bootJar -x test --no-daemon

# Stage 2: Hardened Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install curl and tzdata for container health probes and UTC consistency
RUN apk add --no-cache curl tzdata && \
    addgroup -S aqua && adduser -S aqua -G aqua

USER aqua:aqua

COPY --from=builder --chown=aqua:aqua /workspace/app/build/libs/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=40.0 -XX:+ExitOnOutOfMemoryError -Duser.timezone=UTC" \
    PORT=8080 \
    SERVER_PORT=8080

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health/liveness || exit 1

STOPSIGNAL SIGTERM

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar \"$@\"", "--"]
