FROM gradle:8.11-jdk17 AS build
WORKDIR /app
COPY gradle/ gradle/
COPY gradlew build.gradle.kts settings.gradle.kts gradle.properties ./
COPY build-logic/ build-logic/
COPY backend/shared/ backend/shared/
COPY backend/gamification-service/ backend/gamification-service/
RUN sed -i '/org.gradle.java.home/d' gradle.properties && ./gradlew :backend:gamification-service:installDist --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/backend/gamification-service/build/install/gamification-service/ ./
EXPOSE 8084
CMD ["./bin/gamification-service"]
