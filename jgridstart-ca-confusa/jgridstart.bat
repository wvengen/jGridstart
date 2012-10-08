@echo off
REM - you need to request an OAuth key+secret from the Confusa administrator
SET OAUTH_KEY=key
SET OAUTH_SECRET=secret
REM - if you have your own Confusa instance, you may want to set
REM SET EXTRAARGS=-Djgridstart.ca.base=https://confusa.my.domain/confusa/

mvn -B -q exec:java -Dexec.mainClass="nl.nikhef.jgridstart.gui.Main" -Dexec.args="%$" -Djgridstart.requestwizard.provider=nl.nikhef.jgridstart.ca.confusa.RequestWizard -Djgridstart.ca.provider=nl.nikhef.jgridstart.ca.confusa.ConfusaCA -Djgridstart.ca.oauth.key="%OAUTH_KEY%" -Djgridstart.ca.oauth.secret="%OAUTH_SECRET%" %EXTRAARGS%
