# Email Service Dockerfile
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# Copy Gradle files
COPY gradle/ ./gradle/
COPY gradlew build.gradle settings.gradle ./

# Copy source code
COPY src/ ./src/

# Build the application
RUN ./gradlew bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]

