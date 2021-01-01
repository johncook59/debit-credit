FROM openjdk:11-jre-slim-buster

MAINTAINER john@zarg.co.uk

EXPOSE 8080
VOLUME /tmp
ADD build/libs/debit-credit-app-1.0.0-SNAPSHOT.jar app.jar
CMD ["java","-jar","/app.jar"]
