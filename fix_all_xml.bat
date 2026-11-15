@echo off
cd /d C:\Users\lilith\DfRootApp\app\src\main\res\menu
for %%f in (*.xml) do (
    powershell -c "(Get-Content '%%f' -Raw) -replace 'DfRoot', 'App' | Set-Content -Path '%%f' -Encoding UTF8"
)

cd /d C:\Users\lilith\DfRootApp\app\src\main\res\navigation
for %%f in (*.xml) do (
    powershell -c "(Get-Content '%%f' -Raw) -replace 'DfRoot', 'App' | Set-Content -Path '%%f' -Encoding UTF8"
)

cd /d C:\Users\lilith\DfRootApp\app\src\main\res\xml
for %%f in (*.xml) do (
    powershell -c "(Get-Content '%%f' -Raw) -replace 'DfRoot', 'App' | Set-Content -Path '%%f' -Encoding UTF8"
)

cd /d C:\Users\lilith\DfRootApp\app\src\main\res\values
for %%f in (*.xml) do (
    powershell -c "(Get-Content '%%f' -Raw) -replace 'DfRoot', 'App' | Set-Content -Path '%%f' -Encoding UTF8"
)

cd /d C:\Users\lilith\DfRootApp\app\src\main\res\values-night
for %%f in (*.xml) do (
    powershell -c "(Get-Content '%%f' -Raw) -replace 'DfRoot', 'App' | Set-Content -Path '%%f' -Encoding UTF8"
)

echo Done!