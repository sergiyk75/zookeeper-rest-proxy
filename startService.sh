#!/bin/bash

DEBUG_PORT="${DEBUG_PORT:-5005}"
JMX_PORT="${JMX_PORT:-7199}"
CLASSPATH="${CLASSPATH:-$(pwd)/zookeeper-rest-proxy.jar}"
JVM_FLAGS="${JVM_FLAGS:-}"

java \
-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=${DEBUG_PORT} \
-cp ${CLASSPATH} \
-Dcom.sun.management.jmxremote.port=${JMX_PORT} \
-Dcom.sun.management.jmxremote.rmi.port=${JMX_PORT} \
-Dcom.sun.management.jmxremote.ssl=false \
-Dcom.sun.management.jmxremote.authenticate=false \
${JVM_FLAGS} \
zookeeper.rest.Bootstrap $@