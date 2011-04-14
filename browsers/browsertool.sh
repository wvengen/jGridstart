#!/bin/sh

if [ ! `which mvn` ]; then
	echo "You need to install maven first." >/dev/stderr
	echo "  http://maven.apache.org/" >/dev/stderr
	exit 1
fi

mvn -B -q exec:java -Dexec.mainClass="nl.nikhef.browsers.BrowserTool" -Dexec.args="$@"

