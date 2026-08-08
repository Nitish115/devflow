#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Apache Maven Wrapper startup batch script, version 3.3.2
# ----------------------------------------------------------------------------

# OS specific support.  $var _must_ be set to true or false.
cygwin=false
darwin=false
nonstop=false
os400=false
hpux=false
case "`uname`" in
CYGWIN*) cygwin=true ;;
Darwin*) darwin=true ;;
NonStop*) nonstop=true ;;
OS400*) os400=true ;;
HP-UX*) hpux=true ;;
esac

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin; then
  [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
fi

# Locate executable
if [ -n "$JAVA_HOME" ] ; then
  if [ -x "$JAVA_HOME/bin/java" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
  else
    echo "Warning: JAVA_HOME is set to an invalid directory: $JAVA_HOME" >&2
    JAVACMD="java"
  fi
else
  JAVACMD="java"
fi

if ! command -v "$JAVACMD" >/dev/null 2>&1 ; then
  echo "Error: JAVA_HOME is not set and no 'java' command could be found in your PATH." >&2
  exit 1
fi

# wrapper binary logic
WRAPPER_JAR="./.mvn/wrapper/maven-wrapper.jar"
exec "$JAVACMD" -jar "$WRAPPER_JAR" "$@"
