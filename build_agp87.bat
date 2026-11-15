@echo off
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.21.9-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%
cd C:\Users\lilith\DfRootApp
gradlew.bat clean assembleDebug --no-configuration-cache