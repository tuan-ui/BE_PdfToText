FROM openjdk:17-jdk-slim

# Install LibreOffice and required libraries
RUN apt-get update && apt-get install -y \
    libreoffice \
    default-jre \
    fonts-dejavu \
    && apt-get clean
# Set the working directory inside the container
WORKDIR /app
COPY ./environment.env /app/environment.env
# Copy the JAR file from your local machine into the container
COPY target/*.jar NOFFICE-main-0.0.1-SNAPSHOT.jar

# Expose the port your Spring Boot app runs on (default is 8080)
EXPOSE 8081

# Run the JAR file
ENTRYPOINT ["java", "-jar", "NOFFICE-main-0.0.1-SNAPSHOT.jar"]