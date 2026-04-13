# Bước 1: Mượn một máy ảo có sẵn Maven và Java 17 để build code
FROM maven:3.8.5-openjdk-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Chạy lệnh build ra file .jar (bỏ qua test cho nhanh)
RUN mvn clean package -DskipTests

# Bước 2: Lấy file .jar vừa build xong đem đi chạy
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Mở cổng 8080 để Render biết đường gọi vào
EXPOSE 8080
# Lệnh khởi động Spring Boot
ENTRYPOINT ["java","-jar","app.jar"]