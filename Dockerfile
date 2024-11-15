FROM openjdk:17-jdk

ARG JAR_FILE=build/libs/*-SNAPSHOT.jar

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","-Dspring.profiles.active=prod", "-Duser.timezone=Asia/Seoul" ,"/app.jar"]

EXPOSE 8080
