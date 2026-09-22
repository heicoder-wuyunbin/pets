# ============================================================
# frontend Dockerfile (Vite 构建产物 + nginx 静态服务)
# 适配 arm64 (Apple Silicon / k3s)
# ============================================================
FROM docker.1ms.run/library/nginx:1.27-alpine
LABEL maintainer="pets-project"

COPY dist/ /usr/share/nginx/html/
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
