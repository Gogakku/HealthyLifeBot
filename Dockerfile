FROM openjdk:17-slim
WORKDIR /app
COPY build/libs/HealthyLifeBot.jar app.jar
CMD ["java", "-jar", "app.jar"]
