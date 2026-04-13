# Stage 1: Build code với Maven và Java 21 chuẩn
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Chạy code với Java 21 (bản Alpine siêu nhẹ)
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080

# Thêm tham số -Xmx300m để giới hạn RAM, giúp Server Render Free không bị sập
ENTRYPOINT ["java", "-Xmx300m", "-jar", "app.jar"]