#!/bin/sh

# if you have your own Confusa instance, you may want to set
#EXTRAARGS=-Djgridstart.ca.base=https://my.site/testca/

if ! mvn -v >/dev/null 2>&1; then
	echo "You need to install maven first: http://maven.apache.org/" >/dev/stderr
	exit 1
fi

mvn -B -q exec:java -Dexec.mainClass="nl.nikhef.jgridstart.gui.Main" -Dexec.args="`echo $@`" -Djgridstart.requestwizard.provider=nl.nikhef.jgridstart.ca.dutchgrid.RequestWizard -Djgridstart.ca.provider=nl.nikhef.jgridstart.ca.dutchgrid.DutchGridCA $EXTRAARGS
