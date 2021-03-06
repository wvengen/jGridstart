#!/bin/sh

if ! mvn -v >/dev/null 2>&1; then
	echo "You need to install maven first: http://maven.apache.org/" >/dev/stderr
	exit 1
fi

if [ "$1" = "-screenshots" ]; then
	shift
	mvn -B -q exec:java -Dexec.mainClass="nl.nikhef.jgridstart.gui.util.ScreenshotsGuitest" -Dexec.classpathScope="test" -Dexec.args="`echo $@`"
	exit $?
fi

mvn -B -q exec:java -Dexec.mainClass="nl.nikhef.jgridstart.gui.Main" -Dexec.args="`echo $@`"
exit $?
