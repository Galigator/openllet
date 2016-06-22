@echo off

set java=java

if exist "%JAVA_HOME%\bin\java.exe" set java="%JAVA_HOME%\bin\java"

if "%openllet_java_args%"=="" set openllet_java_args=-Xmx512m

%java% %openllet_java_args% -jar lib\openllet-cli.jar  %*