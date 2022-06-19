FROM openjdk:11
MAINTAINER thecoolersuptelov.github.com
COPY ../target/redis-like-app-0.9.jar redis-like-app-0.9.jar
ENTRYPOINT ["java","-jar","/redis-like-app-0.9.jar"]