#!/bin/sh

BASE=`dirname $0`
CLASSPATH=$BASE/thirdparty/commons/commons-cli-1.2.jar:$BASE/bin
INVOKED_PROGRAM="$0"
export CLASSPATH INVOKED_PROGRAM COLUMNS

java nl.nikhef.jgridstart.install.BrowserTool $@
