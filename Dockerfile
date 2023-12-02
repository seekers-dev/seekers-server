# syntax=docker/dockerfile:1

FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:resolve

COPY src ./src
COPY server.ini ./
COPY include ./include
RUN ./mvnw compile

CMD ["./mvnw", "exec:java"]