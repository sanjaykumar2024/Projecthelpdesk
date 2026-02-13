# Use Java 21 + Maven (important!)
FROM maven:3.9.6-eclipse-temurin-21

WORKDIR /app

# Copy everything
COPY . .

# Build the project
RUN mvn clean package -DskipTests

# Expose port
EXPOSE 8080

# Run the jar
CMD ["sh", "-c", "java -jar target/*.jar"]
