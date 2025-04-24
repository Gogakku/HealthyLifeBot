FROM gradle:7-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle build --no-daemon

FROM openjdk:17-slim
WORKDIR /app
COPY --from=build /app/build/libs/HealthyLifeBot.jar app.jar
ENV PORT=8080
EXPOSE $PORT
CMD ["java", "-jar", "app.jar"]
