FROM openjdk:17-oracle
MAINTAINER baeldung.com
RUN groupadd -r user && useradd -r -g user user
USER user:user
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
