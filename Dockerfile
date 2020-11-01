FROM lion/jdk:1.0.0
MAINTAINER Mr.Liu 158442534@qq.com
ARG JAR_FILE
COPY target/${JAR_FILE} /opt/app.jar
ENV JAVA_OPTS='-Xmx256m -Xmx256m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=128m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=~/heapdump.hprof -Xloggc:~/gc.log' \
 SPRING_CLOUD_NACOS_CONFIG_SERVER_ADDR='127.0.0.1:8848' \
 SPRING_CLOUD_NACOS_CONFIG_NAMESPACE='' \
 LANG='C.UTF-8'
WORKDIR /opt
ENTRYPOINT ["/bin/sh","-c","java -jar app.jar ${JAVA_OPTS} --spring.cloud.nacos.config.server-addr=${SPRING_CLOUD_NACOS_CONFIG_SERVER_ADDR} --spring.cloud.nacos.config.namespace=${SPRING_CLOUD_NACOS_CONFIG_NAMESPACE} "]