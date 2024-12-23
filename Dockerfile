# Use a specific version of the OpenJDK image
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the JAR file into the container
COPY target/WalletApp-1.0-SNAPSHOT.jar app.jar

# Set the entry point for the container
ENTRYPOINT ["java", "-jar", "app.jar"]