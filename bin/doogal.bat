@echo off

title doogal

if not "%JAVA_HOME%" == "" goto haveJavaHome
set JAVA=java
goto haveJava
:haveJavaHome
set JAVA=%JAVA_HOME%\bin\java
:haveJava

if not "%DOOGAL_HOME%" == "" goto haveDoogalHome
echo The DOOGAL_HOME environment variable is not defined correctly.
set DOOGAL_HOME=.
:haveDoogalHome

%JAVA% -server -jar %DOOGAL_HOME%\lib\doogal-0.0.1-SNAPSHOT-jar-with-dependencies.jar
