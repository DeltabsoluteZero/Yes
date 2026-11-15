@echo off
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.21.9-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%
gradlew.bat clean compileDebugKotlin --no-configuration-cache --info 2>&1 > build_clean.txt