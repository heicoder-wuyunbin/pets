# ============================================================
# trade-service Dockerfile (本地 mvn package 后构建)
# 基于 JRE 25 运行, 适配 arm64 (Apple Silicon / k3s)
# ============================================================
FROM eclipse-temurin:25-jre
LABEL maintainer="pets-project"

WORKDIR /app

COPY target/trade-service-*.jar app.jar

ENV JAVA_OPTS="-Xms128m -Xmx256m"
ENV TZ=Asia/Shanghai

EXPOSE 9004

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
