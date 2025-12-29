FROM amazoncorretto:8-alpine3.17-jre

WORKDIR /usr/app

COPY target/java-maven-app-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

