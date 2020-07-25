#!/bin/sh
 
# Execute process with gosu
if [ "$(id -u)" = "0" ]; then
  exec gosu java-app \
            java $JAVA_OPTS \
            -Djava.security.egd=file:/dev/./urandom \            
            -jar $JAR_PATH \
            --spring.config.location=$HOME_DIR/application.yml \
            --logging.config=$HOME_DIR/log4j2.yml \
            "$@"
fi