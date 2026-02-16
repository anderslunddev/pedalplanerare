FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy backend and frontend as siblings (pom.xml uses ${project.basedir}/../frontend)
COPY backend ./backend
COPY frontend ./frontend

# Install a modern Node.js (needed for Vite / ESM syntax)
RUN apt-get update \
    && apt-get install -y curl \
    && curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y nodejs \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app/backend

# Build the Spring Boot jar (this will also build the frontend via the Maven exec plugin)
RUN mvn -q -DskipTests clean package

FROM eclipse-temurin:17-jre AS runtime

WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/backend/target/pedalboard-backend-0.0.1-SNAPSHOT.jar app.jar

# Render sets PORT env var; Spring Boot is configured to use it via server.port=${PORT:8080}
EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]

