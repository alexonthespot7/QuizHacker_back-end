FROM maven:3.8.7-openjdk-18-slim AS build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:21-jdk-slim
ENV SPRING_CONFIG_NAME=application,production
COPY --from=build /target/quiztaker-0.0.1-SNAPSHOT.jar quiztaker.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","quiztaker.jar"]