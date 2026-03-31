FROM gradle:8.11-jdk17 AS build
WORKDIR /app
COPY gradle/ gradle/
COPY gradlew build.gradle.kts settings.gradle.kts gradle.properties ./
COPY build-logic/ build-logic/
COPY backend/shared/ backend/shared/
COPY backend/sos-service/ backend/sos-service/
RUN sed -i '/org.gradle.java.home/d' gradle.properties && ./gradlew :backend:sos-service:installDist --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/backend/sos-service/build/install/sos-service/ ./
EXPOSE 8083
CMD ["./bin/sos-service"]
