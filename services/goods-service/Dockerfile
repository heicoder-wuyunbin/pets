# ============================================================
# goods-service Dockerfile
# ============================================================
FROM eclipse-temurin:25-jre
LABEL maintainer="pets-project"

WORKDIR /app

COPY target/goods-service-*.jar app.jar

ENV JAVA_OPTS="-Xms128m -Xmx256m"
ENV TZ=Asia/Shanghai

EXPOSE 9005

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
