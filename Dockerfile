FROM eclipse-temurin:11-jre-alpine
VOLUME /tmp
COPY target/cicd-demo-*.jar app.jar
EXPOSE 80
ENTRYPOINT [ "java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar","--server.port=80" ]
