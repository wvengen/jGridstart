#!/bin/sh
export BASE=`dirname $0`
export LIB="$BASE/thirdparty"
export CLASSPATH="$LIB/bouncycastle/bcprov-jdk14-142.jar:$LIB/commons-cli/commons-cli-1.2.jar:$LIB/flyingsaucer/core-renderer-minimal.jar:$BASE/bin"
export INVOKED_PROGRAM="$0"
if [ "$DISPLAY" ]; then
	java nl.nikhef.jgridstart.gui.Main $@
else
	java nl.nikhef.jgridstart.cli.Main $@
fi
