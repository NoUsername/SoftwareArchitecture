@echo off
echo press enter to see the magic happen!
@echo on
pause
mvn install assembly:assembly
:: mvn -Dmaven.test.skip=true  install assembly:assembly
pause