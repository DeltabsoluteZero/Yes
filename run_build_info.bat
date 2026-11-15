@echo off
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.21.9-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%
gradlew.bat compileDebugKotlin --no-configuration-cache --info > build_info2.txt 2>&1