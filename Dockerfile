
# ============================================
# MINECORE 360 - ELECTRO DATA S.A.C.
# Dockerfile para Render
# Spring Boot + Java 21 + Maven
# ============================================

# ETAPA 1 - COMPILACION
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests -B

# ETAPA 2 - EJECUCION
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 10000

ENTRYPOINT ["java", "-jar", "app.jar"]
