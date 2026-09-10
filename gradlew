#!/usr/bin/env sh
##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

set -e

# Resolve symbolic links
PRG="$0"
while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

SAVED="`pwd`"
cd "`dirname \"$PRG\"`"/..
GRADLE_HOME="`pwd -P`"
cd "$SAVED"

# JVM options: use GRADLE_OPTS or JAVA_OPTS to override
DEFAULT_JVM_OPTS=""

exec java $DEFAULT_JVM_OPTS -classpath "$GRADLE_HOME/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
