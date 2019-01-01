FROM java
WORKDIR /app
COPY /target/demo-docker-0.0.1-SNAPSHOT.jar .
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "demo-docker-0.0.1-SNAPSHOT.jar"]
