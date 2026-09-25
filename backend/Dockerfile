# syntax=docker/dockerfile:1
# Étape 1 : compilation avec Maven. Le dépôt Maven est mis en cache entre deux builds
# (--mount=type=cache) : seul le premier build télécharge les dépendances.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -Dmaven.wagon.http.retryHandler.count=5 package -DskipTests

# Étape 2 : image d'exécution légère
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/presencelab.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
