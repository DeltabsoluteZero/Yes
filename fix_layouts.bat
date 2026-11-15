@echo off
cd /d C:\Users\lilith\DfRootApp\app\src\main\res\layout
for %%f in (*.xml) do (
    powershell -c "(Get-Content '%%f' -Raw) -replace 'DfRoot', 'App' | Set-Content -Path '%%f' -Encoding UTF8"
)
echo Done!