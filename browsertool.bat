@echo off

set BASE=%~dp0
set LIB=%BASE%\thirdparty
set CLASSPATH=%LIB%\commons\commons-cli-1.2.jar;%BASE%\thirdparty\winregistry\WinRegistry-3.4.jar;%BASE%\bin
set INVOKED_PROGRAM=%0

java nl.nikhef.jgridstart.install.BrowserTool %1 %2 %3 %4 %5 %6 %7 %8 %9
