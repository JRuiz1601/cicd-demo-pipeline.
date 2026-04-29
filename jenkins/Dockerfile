FROM jenkins/jenkins:lts

USER root

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        ca-certificates \
        curl \
        tar \
        gzip \
    && curl -fsSL https://download.docker.com/linux/static/stable/x86_64/docker-29.0.4.tgz -o /tmp/docker.tgz \
    && tar -xzf /tmp/docker.tgz -C /tmp \
    && mv /tmp/docker/docker /usr/local/bin/docker \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* /tmp/docker /tmp/docker.tgz

USER jenkins