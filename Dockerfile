FROM openjdk:17-jdk-slim
WORKDIR /usr/src/app


RUN apt-get update && apt-get install -y maven
RUN mvn clean install -DskipTests
COPY target/WeShareServer.jar /usr/src/app/WeShareServer.jar

EXPOSE 5050
CMD ["java", "-jar", "WeShareServer.jar"]
