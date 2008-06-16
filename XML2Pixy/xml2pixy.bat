@echo off
set XML2PIXY_HOME=%~dp0
set LIBDIR=%~dp0lib
set BUILDDIR=%~dp0build

java -Dpixy.home=%XML2PIXY_HOME% -jar %BUILDDIR%\xml2pixy.jar %* 
