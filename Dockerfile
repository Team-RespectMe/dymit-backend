FROM openjdk:21-slim
COPY build/libs/net.noti-me.dymit-backend-latest.jar /app.jar
CMD ["java", "-jar", "/app.jar"]
