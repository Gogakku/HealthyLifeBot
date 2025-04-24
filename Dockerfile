FROM gradle:7-jdk17 AS build
WORKDIR /app
COPY . .
# Добавляем права на выполнение для gradlew
RUN chmod +x gradlew
RUN ./gradlew build --no-daemon  # Используем gradlew для сборки

FROM openjdk:17-slim
WORKDIR /app
COPY --from=build /app/build/libs/HealthyLifeBot.jar app.jar
ENV PORT=8080
EXPOSE $PORT
CMD ["java", "-jar", "app.jar"]
