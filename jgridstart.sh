#!/bin/sh
if [ "$JAVA_HOME" ]; then
	JAVA="$JAVA_HOME/bin/java"
else
	JAVA=java
fi
BASE=`dirname $0`
LIB="$BASE/thirdparty"
CLASSPATH="$LIB/bouncycastle/bcprov-jdk15-144.jar:$LIB/bouncycastle/bcmail-jdk15-144.jar:$LIB/bouncycastle/mail-1.4.3.jar:$LIB/commons/commons-lang-2.4.jar:$LIB/commons/commons-cli-1.2.jar:$LIB/flyingsaucer/core-renderer-minimal.jar:$LIB/flyingsaucer/iText-2.0.8.jar:$LIB/swingworker/swing-worker-1.2.jar:$BASE/bin"
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
	CLASSPATH="$CLASSPATH:$LIB/junit/junit-4.7.jar:$LIB/junit/abbot.jar"
	export CLASSPATH
	#$JAVA org.junit.runner.JUnitCore nl.nikhef.jgridstart.AllTests
	$JAVA nl.nikhef.jgridstart.logging.UserTestRunner
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
elif [ "$1" = "screenshots" ]; then
	shift
	CLASSPATH="$CLASSPATH:$LIB/junit/junit-4.7.jar:$LIB/junit/abbot.jar"
	export CLASSPATH
	$JAVA nl.nikhef.jgridstart.gui.util.GUIScreenshotsTest $@
elif [ "$DISPLAY" ]; then
	$JAVA nl.nikhef.jgridstart.gui.Main $@
else
	$JAVA nl.nikhef.jgridstart.cli.Main $@
fi
