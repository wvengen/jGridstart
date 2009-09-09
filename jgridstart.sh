#!/bin/sh
JAVA=java
BASE=`dirname $0`
LIB="$BASE/thirdparty"
CLASSPATH="$LIB/bouncycastle/bcprov-jdk14-143.jar:$LIB/bouncycastle/bcmail-jdk14-143.jar:$LIB/bouncycastle/mail-1.4.1.jar:$LIB/commons/commons-lang-2.4.jar:$LIB/commons/commons-cli-1.2.jar:$LIB/flyingsaucer/core-renderer-minimal.jar:$LIB/flyingsaucer/iText-2.0.8.jar:$LIB/swingworker/swing-worker-1.2.jar:$BASE/bin"
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
	$JAVA org.junit.runner.JUnitCore nl.nikhef.jgridstart.AllTests
elif [ "$1" = "cli" ]; then
	shift
	$JAVA nl.nikhef.jgridstart.cli.Main $@
elif [ "$1" = "gui" ]; then
	shift
	$JAVA nl.nikhef.jgridstart.gui.Main $@
elif [ "$1" = "check" ]; then
	INVOKED_PROGRAM="$INVOKED_PROGRAM $1"
	export INVOKED_PROGRAM
	shift
	$JAVA nl.nikhef.jgridstart.CertificateCheck $@
elif [ "$DISPLAY" ]; then
	$JAVA nl.nikhef.jgridstart.gui.Main $@
else
	$JAVA nl.nikhef.jgridstart.cli.Main $@
fi
