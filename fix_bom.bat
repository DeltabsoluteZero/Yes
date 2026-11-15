@echo off
cd /d C:\Users\lilith\DfRootApp\app\src\main\res
for /r %%f in (*.xml) do (
    powershell -c "$c = Get-Content '%%f' -Raw; $c = $c.TrimStart([char]0xFEFF, [char]0xFFFE, [char]0xEF, [char]0xBB, [char]0xBF); [System.IO.File]::WriteAllText('%%f', $c, [System.Text.Encoding]::UTF8)"
)
echo Done!