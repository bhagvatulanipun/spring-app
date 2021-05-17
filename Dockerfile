#MAINTAINER Nipun
FROM maven:3.5.2-jdk-8-alpine AS MAVEN_BUILD
COPY pom.xml /tmp/
COPY src /tmp/src/
WORKDIR /tmp/
RUN mvn package

FROM openjdk:8-jdk-alpine
WORKDIR /
COPY --from=MAVEN_BUILD /tmp/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 9090
EXPOSE 8080
