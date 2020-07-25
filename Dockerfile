FROM openjdk:8-jdk-alpine

ENV GOSU_VERSION 1.12
RUN set -eux; \
	\
	apk add --no-cache --virtual .gosu-deps \
		ca-certificates \
		dpkg \
		gnupg \
	; \
	\
	dpkgArch="$(dpkg --print-architecture | awk -F- '{ print $NF }')"; \
	wget -O /usr/local/bin/gosu "https://github.com/tianon/gosu/releases/download/$GOSU_VERSION/gosu-$dpkgArch"; \
	wget -O /usr/local/bin/gosu.asc "https://github.com/tianon/gosu/releases/download/$GOSU_VERSION/gosu-$dpkgArch.asc"; \
	\
	# verify the signature
	export GNUPGHOME="$(mktemp -d)"; \
	gpg --batch --keyserver hkps://keys.openpgp.org --recv-keys B42F6819007F00F88E364FD4036A9C25BF357DD4; \
	gpg --batch --verify /usr/local/bin/gosu.asc /usr/local/bin/gosu; \
	command -v gpgconf && gpgconf --kill all || :; \
	rm -rf "$GNUPGHOME" /usr/local/bin/gosu.asc; \
	\
	# clean up fetch dependencies
	apk del --no-network .gosu-deps; \
	\
	chmod +x /usr/local/bin/gosu; \
	# verify that the binary works
	gosu --version; \
	gosu nobody true

ARG JAR_NAME=app.jar

ARG HOME_DIR=/home/java-app

# Setting timezone
ENV TZ=Asia/Taipei

ENV JAR_PATH=$HOME_DIR/$JAR_NAME

RUN set -eux && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone

RUN set -eux && \
    addgroup --gid 9999 java-app && \
    adduser -S -u 9999 -g java-app -h $HOME_DIR -s /bin/sh -D java-app && \
    chown -R java-app:java-app $HOME_DIR

COPY --chown=java-app:java-app ./target/*-SNAPSHOT.jar $JAR_PATH

COPY --chown=java-app:java-app ./target/classes/application.yml $HOME_DIR/application.yml

COPY --chown=java-app:java-app ./target/classes/log4j2.yml $HOME_DIR/log4j2.yml

COPY ./entrypoint.sh entrypoint.sh

RUN chmod +x entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
