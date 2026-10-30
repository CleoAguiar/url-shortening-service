
# Build stage
FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .

RUN chmod +x mvnw

RUN ./mvnw -B dependency:go-offline

COPY src/ src/
COPY docs/ docs/

RUN ./mvnw -B clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
