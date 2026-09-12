# Stage 1: Build the application
FROM eclipse-temurin:23-jdk AS build
WORKDIR /app
# Copy the maven wrapper and pom.xml first
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
# Make the wrapper executable
RUN chmod +x mvnw
# Download dependencies
RUN ./mvnw dependency:go-offline
# Copy the source code and build
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:23-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
