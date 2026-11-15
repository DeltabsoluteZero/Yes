@echo off
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.21.9-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%
gradlew.bat compileDebugKotlin --no-configuration-cache --no-daemon -Dkotlin.daemon.jvm.options="" 2>&1 > build_nodaemon.txt