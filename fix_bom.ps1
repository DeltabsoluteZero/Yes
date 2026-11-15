$files = Get-ChildItem -Path "C:\Users\lilith\DfRootApp\app\src\main\res" -Recurse -Filter "*.xml"
foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw
    $content = $content.TrimStart([char]0xFEFF, [char]0xFFFE, [char]0xEF, [char]0xBB, [char]0xBF)
    [System.IO.File]::WriteAllText($file.FullName, $content, [System.Text.Encoding]::UTF8)
    Write-Host "Fixed: $($file.FullName)"
}
Write-Host "Done!"