# Étape 1 : compilation avec Maven (les tests tournent via ./mvnw test, hors image)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

# Étape 2 : image d'exécution légère
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/presencelab.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
