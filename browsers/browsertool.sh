#!/bin/sh
if ! mvn -v >/dev/null 2>&1; then
	echo "You need to install maven first: http://maven.apache.org/" >/dev/stderr
	exit 1
fi
mvn -B -q exec:java -Dexec.mainClass="nl.nikhef.browsers.BrowserTool" -Dexec.args="$@"
