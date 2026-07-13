FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

RUN echo "ARCHIVOS GENERADOS:" && ls -lah /app/target

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar /app/app.jar

RUN echo "CONTENIDO FINAL:" && ls -lah /app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]