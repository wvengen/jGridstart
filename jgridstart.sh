#!/bin/sh
BASE=`dirname $0`
LIB="$BASE/thirdparty"
CLASSPATH="$LIB/bouncycastle/bcprov-jdk14-142.jar:$LIB/commons-cli/commons-cli-1.2.jar:$LIB/flyingsaucer/core-renderer-minimal.jar:$BASE/bin"
INVOKED_PROGRAM="$0"
export BASE CLASSPATH INVOKED_PROGRAM

# undocumented behaviour: first parameter can be "test" to run unit tests,
# "cli" to force the command-line version, or "gui" to force the gui version
# to be run.
# By default, the gui version will be run when DISPLAY is set, or else the
# command-line version.
if [ "$1" = "test" ]; then
	echo
	echo "Running jGridstart tests"
	echo
	CLASSPATH="$CLASSPATH:$LIB/junit/junit-4.5.jar:$LIB/junit/abbot.jar"
	export CLASSPATH
	java org.junit.runner.JUnitCore nl.nikhef.jgridstart.AllTests
elif [ "$1" = "cli" ]; then
	shift
	java nl.nikhef.jgridstart.cli.Main $@
elif [ "$1" = "gui" ]; then
	shift
	java nl.nikhef.jgridstart.gui.Main $@
elif [ "$DISPLAY" ]; then
	java nl.nikhef.jgridstart.gui.Main $@
else
	java nl.nikhef.jgridstart.cli.Main $@
fi
