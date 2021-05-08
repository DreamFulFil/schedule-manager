#!/bin/sh
 
# Execute process with gosu
if [ "$(id -u)" = "0" ]; then
  exec gosu java-app \
            java $JAVA_OPTS \
            -Djava.security.egd=file:/dev/./urandom \
            -Dspring.config.location=$CONFIG_PATH/application.yml \
            -Dlogging.config=$CONFIG_PATH/log4j2.yml \
            -jar $JAR_PATH \
            "$@"
fi