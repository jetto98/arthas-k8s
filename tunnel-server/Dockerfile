FROM openjdk:8-jdk-alpine

ARG JAR_FILE
COPY ${JAR_FILE} app.jar
COPY start.sh start.sh
ENTRYPOINT ["sh", "/start.sh"]