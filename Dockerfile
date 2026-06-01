# Build stage
FROM gradle:8.10.2-jdk21 AS builder
WORKDIR /workspace

# Copy source and build executable jar
COPY . .
RUN chmod +x gradlew && ./gradlew bootJar --no-daemon -x test

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy jar from build stage
COPY --from=builder /workspace/build/libs/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

