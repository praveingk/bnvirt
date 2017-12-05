#!/bin/sh

BNVHOME=`dirname $0`/..
BNV_JAR="${BNVHOME}/target/BNV.jar"


JVM_OPTS="-Xms512m -Xmx2g"
## If you want JaCoCo Code Coverage reports... uncomment line below
#JVM_OPTS="$JVM_OPTS -javaagent:${BNVHOME}/lib/jacocoagent.jar=dumponexit=true,output=file,destfile=${BNVHOME}/target/jacoco.exec"
JVM_OPTS="$JVM_OPTS -XX:+TieredCompilation"
JVM_OPTS="$JVM_OPTS -XX:+UseCompressedOops"
JVM_OPTS="$JVM_OPTS -XX:+UseConcMarkSweepGC -XX:+AggressiveOpts -XX:+UseFastAccessorMethods"
JVM_OPTS="$JVM_OPTS -XX:MaxInlineSize=8192 -XX:FreqInlineSize=8192" 
JVM_OPTS="$JVM_OPTS -XX:CompileThreshold=1500 -XX:PreBlockSpin=8" 

if [ ! -e ${BNV_JAR} ]; then
  cd ${BNVHOME}
  echo "Packaging BNV for you..."
  mvn package 
  cd -
fi

echo "Starting BNV..."
java ${JVM_OPTS} -Dlog4j.configurationFile=${BNVHOME}/config/log4j2.xml -Djavax.net.ssl.keyStore=${BNVHOME}/config/sslStore -jar ${BNV_JAR} $@
