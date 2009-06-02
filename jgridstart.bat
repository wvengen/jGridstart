@echo off
REM
REM Batch file for running jGridstart
REM

set BASE=%~dp0
set LIB=%BASE%\thirdparty
set CLASSPATH=%LIB%\bouncycastle\bcprov-jdk14-142.jar;%LIB%\commons-cli\commons-cli-1.2.jar;%LIB%\flyingsaucer\core-renderer-minimal.jar;%BASE%\bin
set INVOKED_PROGRAM=%0

REM undocumented behaviour: first parameter can be "test" to run unit tests,
REM "cli" to force the command-line version, or "gui" to force the gui version
REM to be run.
REM By default, the gui version will be run.

if not "%1"=="test" goto :notest
	echo.
	echo Running jGridstart tests
	echo.
	set CLASSPATH=%CLASSPATH%;%LIB%\junit\junit-4.5.jar;%LIB%\junit\abbot.jar
	java org.junit.runner.JUnitCore nl.nikhef.jgridstart.AllTests
	goto :end

:notest
if not "%1"=="cli" goto :nowantcli
	shift
	java nl.nikhef.jgridstart.cli.Main $@
	goto :end

:nowantcli
if not "%1"=="gui" goto :nowantgui
	shift
	java nl.nikhef.jgridstart.gui.Main $@
	goto :end

:nowantgui
java nl.nikhef.jgridstart.gui.Main $@
goto :end

:end