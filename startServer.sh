#!/usr/bin/env bash

JVM_DEBUG_PORT="${JVM_DEBUG_PORT:-5005}"
JVM_JMX_PORT="${JVM_JMX_PORT:-7199}"
CLASSPATH="${CLASSPATH:-$(pwd)/zookeeper-rest-proxy.jar}"
JVM_PARAMS="${JVM_PARAMS:-}"

java \
-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=${JVM_DEBUG_PORT} \
-cp ${CLASSPATH} \
-Dcom.sun.management.jmxremote.port=${JVM_JMX_PORT} \
-Dcom.sun.management.jmxremote.rmi.port=${JVM_JMX_PORT} \
-Dcom.sun.management.jmxremote.ssl=false \
-Dcom.sun.management.jmxremote.authenticate=false \
${JVM_PARAMS} \
zookeeper.rest.Bootstrap $@