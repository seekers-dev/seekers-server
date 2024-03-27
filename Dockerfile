# syntax=docker/dockerfile:1

FROM eclipse-temurin:11-jdk

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends openjfx xorg libgl1-mesa-glx \
    && rm -rf /var/lib/apt/lists/*

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ["chmod", "+x", "./mvnw"]
RUN ./mvnw dependency:resolve

COPY src ./src
COPY config.ini ./
RUN ./mvnw compile

ENTRYPOINT ["./mvnw"]