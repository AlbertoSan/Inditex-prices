# ---------- Stage 1: build ----------
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build

# Cache dependencies: copy the POM first, then the source code
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:25-jre
WORKDIR /app

# Run as a non-root user
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring

# finalName=ms-price-service => target/ms-price-service.jar
COPY --from=build /build/target/ms-price-service.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
