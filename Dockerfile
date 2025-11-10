# === Stage 1: Build the Application ===
# Use an official Maven image with Java 21
FROM maven:3.9.11-eclipse-temurin-21-noble AS build

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of your source code and build
COPY src ./src
RUN mvn package -DskipTests

# === Stage 2: Create the Final Runtime Image ===
# Use a minimal Java 21 JRE image
FROM eclipse-temurin:21-jre-noble

# Set a non-root user
RUN addgroup --system spring && adduser --system --ingroup spring springuser
USER springuser

WORKDIR /app

# Copy the built .jar file from the 'build' stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]