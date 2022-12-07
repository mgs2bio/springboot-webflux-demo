FROM openjdk:17-oracle
MAINTAINER baeldung.com
COPY target/springboot-webflux-demo-0.0.1-SNAPSHOT.jar springboot-webflux-demo-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/springboot-webflux-demo-0.0.1-SNAPSHOT.jar"]