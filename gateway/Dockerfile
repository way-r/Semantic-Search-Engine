FROM eclipse-temurin:21-jre-jammy

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} gateway.jar

ENTRYPOINT ["java", "-jar", "/gateway.jar"]