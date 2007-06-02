@echo off

REM
REM This script will run the bananas sample.

REM To use the script:
REM
REM 1. Java must be in your PATH, or you must modify this script to use
REM    your installed java.
REM
REM 2. You must set HME_HOME to point at your HME SDK directory.
REM

REM set HME_HOME=
if "%HME_HOME%"=="" goto nohme

java -cp "%HME_HOME%\hme.jar";"%HME_HOME%\hme-host-sample.jar";..\bananas.jar;samples.jar com.tivo.hme.host.sample.Main com.tivo.hme.samples.bananas.BananasSample
goto end

:nohme
echo You must set HME_HOME.

:end
