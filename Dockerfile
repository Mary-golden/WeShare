FROM openjdk:17-jdk-slim
WORKDIR /usr/src/app


RUN apt-get update && apt-get install -y maven
RUN mvn clean install -DskipTests
COPY target/WeShareServer/usr/src/app/WeShareServer

EXPOSE 5050
CMD ["java", "-cp", "target/classes:target/dependency/*", "weshare.server.WeShareServer"]