FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-Xmx300m","-jar","app.jar"]