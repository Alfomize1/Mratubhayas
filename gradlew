#!/bin/sh
##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

APP_BASE_NAME=`basename "$0"`
APP_HOME="`cd "\`dirname \"$0\"\`" >/dev/null 2>&1 && pwd`"

# If gradle is installed on the system, use it to ensure wrapper exists
if [ ! -f "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" ]; then
    if command -v gradle >/dev/null 2>&1; then
        echo "Generating gradle-wrapper.jar using system gradle..."
        gradle wrapper
    fi
fi

exec "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" "$@" 2>/dev/null || gradle "$@"
