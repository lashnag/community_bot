# Этап сборки
FROM maven:3.9.5-eclipse-temurin-17 as build

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests

# Этап выполнения
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY --from=build /app/target/CommunityBot-1.0.0-SNAPSHOT.jar CommunityBot.jar

ENTRYPOINT ["java", "-jar", "CommunityBot.jar"]