FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy Maven wrapper and pom.xml for dependency caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies
RUN chmod +x mvnw && ./mvnw dependency:go-offline

# Copy source code
COPY src/ src/

# Build application
RUN ./mvnw clean package -DskipTests

# Copy the built jar
RUN cp target/*.jar app.jar

EXPOSE 3032

# Health check (optional)
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:3030/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
