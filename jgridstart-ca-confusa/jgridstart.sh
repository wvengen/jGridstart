#!/bin/sh

# you need to request an OAuth key+secret from the Confusa administrator
OAUTH_KEY=key
OAUTH_SECRET=secret
# if you have your own Confusa instance, you may want to set
#EXTRAARGS=-Djgridstart.ca.base=https://confusa.my.domain/confusa/

if ! mvn -v >/dev/null 2>&1; then
	echo "You need to install maven first: http://maven.apache.org/" >/dev/stderr
	exit 1
fi

mvn -B -q exec:java -Dexec.mainClass="nl.nikhef.jgridstart.gui.Main" -Dexec.args="`echo $@`" -Djgridstart.requestwizard.provider=nl.nikhef.jgridstart.ca.confusa.RequestWizard -Djgridstart.ca.provider=nl.nikhef.jgridstart.ca.confusa.ConfusaCA -Djgridstart.ca.oauth.key="$OAUTH_KEY" -Djgridstart.ca.oauth.secret="$OAUTH_SECRET" $EXTRAARGS
