# 🔨 Build Stage (Maven)
FROM maven:3.9.9-eclipse-temurin-23 AS compiler

# Define working directory
WORKDIR /code_folder

# Copy Maven Wrapper & Project Files
COPY pom.xml ./
COPY mvnw ./
COPY .mvn .mvn/
COPY src ./src/

# Set execute permission & build
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# 🏃 Runtime Stage (Smaller Image)
FROM maven:3.9.9-eclipse-temurin-23

WORKDIR /app

# Copy the built JAR from previous stage
COPY --from=compiler /code_folder/target/assessment-0.0.1-SNAPSHOT.jar app.jar

# Set environment & expose port
ENV PORT=8080
EXPOSE ${PORT}

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]