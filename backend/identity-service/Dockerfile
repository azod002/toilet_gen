FROM gradle:8.11-jdk17 AS build
WORKDIR /app
COPY gradle/ gradle/
COPY gradlew build.gradle.kts settings.gradle.kts gradle.properties ./
COPY build-logic/ build-logic/
COPY backend/shared/ backend/shared/
COPY backend/identity-service/ backend/identity-service/
RUN sed -i '/org.gradle.java.home/d' gradle.properties && ./gradlew :backend:identity-service:installDist --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/backend/identity-service/build/install/identity-service/ ./
EXPOSE 8081
CMD ["./bin/identity-service"]
