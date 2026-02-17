# syntax=docker/dockerfile:1

# === Stage 1: Build the Application ===
FROM maven:3.9.11-eclipse-temurin-21-noble AS build

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .

# This caches the .m2 directory locally on the host, so subsequent builds
# don't re-download dependencies even if you change pom.xml
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline

# Copy the rest of your source code and build
COPY src ./src

# -T 1C: Uses 1 thread per CPU core to speed up compilation
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -T 1C

# === Stage 2: Create the Final Runtime Image ===
FROM eclipse-temurin:21-jre-alpine

# Set a non-root user
RUN addgroup --system spring && adduser --system --ingroup spring springuser

WORKDIR /app

# Create logs directory and set ownership to springuser
RUN mkdir -p /app/logs && chown -R springuser:spring /app/logs

# Copy the built .jar file from the 'build' stage
COPY --from=build /app/target/*.jar app.jar

RUN chown -R springuser:spring /app

USER springuser

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]