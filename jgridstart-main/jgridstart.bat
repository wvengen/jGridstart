@echo off

if "%1"=="-screenshots" goto screenshots

mvn -B -q exec:java -Dexec.mainClass="nl.nikhef.jgridstart.gui.Main" -Dexec.args="%$"
goto end

:screenshots
shift
mvn -B -q exec:java -Dexec.mainClass="nl.nikhef.jgridstart.gui.util.ScreenshotsGuitest" -Dexec.classpathScope="test" -Dexec.args="%$"
goto end

:end
