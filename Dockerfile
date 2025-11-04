FROM openjdk

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} app.jar

# Spring boot entrypoint
ENTRYPOINT ["java","-jar","/app.jar"]