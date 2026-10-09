# CI runner image for Gitea Actions (runs on master host, mounts docker sock + kubeconfig + ssh)
FROM eclipse-temurin:25-jdk
USER root
RUN apt-get update \
 && apt-get install -y --no-install-recommends ca-certificates gnupg openssh-client git jq procps docker.io \
 && rm -rf /var/lib/apt/lists/*
# Maven 3.9.10 copied from master (matches project requirement)
COPY maven /opt/maven
# kubectl copied from master
COPY kubectl /usr/local/bin/kubectl
ENV MAVEN_HOME=/opt/maven
ENV PATH=/opt/maven/bin:$PATH
RUN chmod +x /usr/local/bin/kubectl
WORKDIR /workspace
