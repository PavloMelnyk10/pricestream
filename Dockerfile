# Build stage
FROM maven:3.9.16-eclipse-temurin-25 AS build
WORKDIR /app

# Copy POM files for dependency resolution caching
COPY pom.xml .
COPY core/pom.xml core/
COPY collector/pom.xml collector/
COPY processor/pom.xml processor/
COPY api/pom.xml api/
COPY app/pom.xml app/

# Fetch dependencies
RUN mvn dependency:go-offline -B

# Copy all source directories
COPY core/src core/src
COPY collector/src collector/src
COPY processor/src processor/src
COPY api/src api/src
COPY app/src app/src

# Compile and package the application
RUN mvn clean package -DskipTests

# Run stage
FROM bellsoft/liberica-runtime-container:jre-25-slim-musl
WORKDIR /app

# Copy the Spring Boot runnable JAR from build stage
COPY --from=build /app/app/target/app-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
