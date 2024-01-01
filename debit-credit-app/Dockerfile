FROM openjdk:21

MAINTAINER john@zarg.co.uk

EXPOSE 8080
VOLUME /tmp
ADD target/debit-credit-app-1.0.2-SNAPSHOT.jar app.jar
CMD ["java", "-jar", "-Drds.hostname=postgres:5432", "/app.jar"]
