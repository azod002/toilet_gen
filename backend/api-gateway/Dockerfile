FROM gradle:8.11-jdk17 AS build
WORKDIR /app
COPY gradle/ gradle/
COPY gradlew build.gradle.kts settings.gradle.kts gradle.properties ./
COPY build-logic/ build-logic/
COPY backend/shared/ backend/shared/
COPY backend/api-gateway/ backend/api-gateway/
RUN sed -i '/org.gradle.java.home/d' gradle.properties && ./gradlew :backend:api-gateway:installDist --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/backend/api-gateway/build/install/api-gateway/ ./
EXPOSE 8080
CMD ["./bin/api-gateway"]
