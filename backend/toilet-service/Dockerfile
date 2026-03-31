FROM gradle:8.11-jdk17 AS build
WORKDIR /app
COPY gradle/ gradle/
COPY gradlew build.gradle.kts settings.gradle.kts gradle.properties ./
COPY build-logic/ build-logic/
COPY backend/shared/ backend/shared/
COPY backend/toilet-service/ backend/toilet-service/
RUN sed -i '/org.gradle.java.home/d' gradle.properties && ./gradlew :backend:toilet-service:installDist --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/backend/toilet-service/build/install/toilet-service/ ./
EXPOSE 8082
CMD ["./bin/toilet-service"]
