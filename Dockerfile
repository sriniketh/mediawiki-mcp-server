# Build stage
FROM gradle:8.14-jdk11 AS build
WORKDIR /app

COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle/ gradle/

RUN gradle dependencies --no-daemon

COPY src/ src/

RUN gradle build --no-daemon

# Runtime stage
FROM openjdk:11.0.16-jre-slim
WORKDIR /app

COPY --from=build /app/build/libs/mediawiki_mcp_server-all.jar app.jar

# Create logs directory for logback
RUN mkdir -p logs

CMD ["java", "-jar", "app.jar"]
