FROM eclipse-temurin:25-jre
LABEL maintainer="pets-project"
WORKDIR /app
COPY target/report-service-*.jar app.jar
ENV JAVA_OPTS="-Xms128m -Xmx256m"
ENV TZ=Asia/Shanghai
EXPOSE 9009
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
