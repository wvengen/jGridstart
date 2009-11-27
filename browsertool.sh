#!/bin/sh

BASE=`dirname $0`
LIB="$BASE/thirdparty"
CLASSPATH=$LIB/commons/commons-lang-2.4.jar:$LIB/commons/commons-cli-1.2.jar:$BASE/bin
INVOKED_PROGRAM="$0"
export CLASSPATH INVOKED_PROGRAM COLUMNS

java nl.nikhef.jgridstart.install.BrowserTool $@
