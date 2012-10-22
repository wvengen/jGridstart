@echo off
REM - if you have your own TestCA instance, you may want to set
REM SET EXTRAARGS=-Djgridstart.ca.base=http://my.site/testca/
mvn -B -q exec:java -Dexec.mainClass="nl.nikhef.jgridstart.gui.Main" -Dexec.args="%$" -Djgridstart.requestwizard.provider=nl.nikhef.jgridstart.ca.dutchgrid.RequestWizard -Djgridstart.ca.provider=nl.nikhef.jgridstart.ca.dutchgrid.DutchGridCA %EXTRAARGS%
