FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/CommunityBot-1.0.0-SNAPSHOT.jar CommunityBot.jar

ENTRYPOINT ["java", "-jar", "CommunityBot.jar"]