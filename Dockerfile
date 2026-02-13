# Use Java 21
FROM eclipse-temurin:21-jdk

# Set working directory
WORKDIR /app

# Copy all files
COPY . .

# Build project using Maven Wrapper
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Expose port
EXPOSE 8080

# Run application
CMD ["sh", "-c", "java -jar target/*.jar"]
