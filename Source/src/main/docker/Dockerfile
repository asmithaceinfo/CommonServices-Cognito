FROM frolvlad/alpine-oraclejdk8:slim
RUN sh -c 'apk update'
RUN sh -c 'apk upgrade'
RUN apk add --no-cache nss
VOLUME /tmp
ADD bdso-cognito-1.0.0.jar app.jar
RUN sh -c 'apk update'
RUN sh -c 'apk upgrade'
RUN sh -c 'touch /app.jar'
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]
