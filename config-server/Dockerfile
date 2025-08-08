# === Build Stage ===
FROM eclipse-temurin:24-jdk AS builder

WORKDIR /app

# Copy Maven build files first for dependency caching
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy the full source
COPY src ./src

# Package the app
RUN ./mvnw clean package -DskipTests

# === Runtime Stage ===
FROM eclipse-temurin:24-jre AS runtime

WORKDIR /app

# Copy built JAR from builder
COPY --from=builder /app/target/config-server-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8888

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
