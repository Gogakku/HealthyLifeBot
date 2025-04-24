FROM gradle:7-jdk17 AS build
WORKDIR /app
COPY . .
RUN ./gradlew build --no-daemon  # Используем gradlew, если он присутствует

FROM openjdk:17-slim
WORKDIR /app
COPY --from=build /app/build/libs/HealthyLifeBot.jar app.jar
ENV PORT=8080
EXPOSE $PORT
CMD ["java", "-jar", "app.jar"]
