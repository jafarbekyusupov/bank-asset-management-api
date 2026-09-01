FROM docker.io/library/maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /usr/src/app
COPY . .

RUN mvn clean package -DskipTests

FROM docker.io/library/eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /usr/src/app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
