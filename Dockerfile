# FROM public.ecr.aws/docker/library/node:buster-slim
# WORKDIR /usr/src/app
# COPY package*.json ./
# RUN npm install
# COPY . .
# EXPOSE 80
# CMD ["node", "server.js"]

FROM amazoncorretto:8
WORKDIR /usr/src/app
COPY . .
RUN ./mvnw package
EXPOSE 5050
CMD ["java", "-jar", "target/WeShare-1.0-SNAPSHOT.jar"]