FROM openjdk:8-jdk-alpine

VOLUME /tmp

RUN apk --update add tzdata && \
    cp /usr/share/zoneinfo/Asia/Taipei /etc/localtime && \
    apk del tzdata && \
    rm -rf /var/cache/apk/*
    
RUN apk add --update ttf-dejavu fontconfig && rm -rf /var/cache/apk/*

COPY ./target/schedule-service-1.0.0-SNAPSHOT.jar schedule-service.jar

COPY ./target/classes/application.yml /usr/local/share/application.yml

COPY ./target/classes/log4j2.yml /usr/local/share/log4j2.yml

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /schedule-service.jar --spring.config.location=/usr/local/share/application.yml --logging.config=/usr/local/share/log4j2.yml"]
