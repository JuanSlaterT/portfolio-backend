# Etapa de compilación OwO
FROM maven:3.9.16-eclipse-temurin-21 AS build

WORKDIR /app

COPY .mvn .mvn
COPY mvnw pom.xml ./

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

COPY src ./src

RUN ./mvnw clean package -DskipTests -B

# Luego de compilar, levantamos el servidor

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]