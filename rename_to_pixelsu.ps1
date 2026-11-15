# PowerShell script to rename DfRoot to PixelSU
# Run from project root: C:\Users\lilith\DfRootApp

$sourceDir = "C:\Users\lilith\DfRootApp"
$oldPackage = "com.dfroot"
$newPackage = "com.pixelsu"
$oldAppName = "DfRoot"
$newAppName = "PixelSU"
$oldClassPrefix = "DfRoot"
$newClassPrefix = "PixelSU"

Write-Host "Renaming $oldPackage to $newPackage..." -ForegroundColor Cyan

# 1. Rename package directories
$oldJavaPath = "$sourceDir\app\src\main\java\com\dfroot"
$newJavaPath = "$sourceDir\app\src\main\java\com\pixelsu"

if (Test-Path $oldJavaPath) {
    # Create new directory structure
    $dirs = @(
        "com\pixelsu",
        "com\pixelsu\exploit",
        "com\pixelsu\hide",
        "com\pixelsu\model",
        "com\pixelsu\root",
        "com\pixelsu\service",
        "com\pixelsu\ui\exploit",
        "com\pixelsu\ui\hide",
        "com\pixelsu\ui\logs",
        "com\pixelsu\ui\main",
        "com\pixelsu\ui\settings",
        "com\pixelsu\utils"
    )
    
    foreach ($dir in $dirs) {
        $fullPath = "$sourceDir\app\src\main\java\$dir"
        if (-not (Test-Path $fullPath)) {
            New-Item -ItemType Directory -Path $fullPath -Force | Out-Null
        }
    }
    
    # Copy and rename files
    Get-ChildItem -Path "$oldJavaPath" -Recurse -File | ForEach-Object {
        $relPath = $_.FullName.Substring($oldJavaPath.Length + 1)
        $newFileName = $relPath -replace "^DfRoot", "PixelSU" -replace "DfRoot", "PixelSU"
        $destPath = "$newJavaPath\$newFileName"
        $destDir = Split-Path $destPath -Parent
        if (-not (Test-Path $destDir)) { New-Item -ItemType Directory -Path $destDir -Force | Out-Null }
        Copy-Item $_.FullName -Destination $destPath -Force
    }
}

# 2. Update all Kotlin/Java files - package declarations and imports
$kotlinFiles = Get-ChildItem -Path "$sourceDir\app\src\main\java\com\pixelsu" -Recurse -Filter "*.kt"
$javaFiles = Get-ChildItem -Path "$sourceDir\app\src\main\java\com\pixelsu" -Recurse -Filter "*.java"
$allSourceFiles = $kotlinFiles + $javaFiles

foreach ($file in $allSourceFiles) {
    $content = Get-Content $file.FullName -Raw
    
    # Update package declaration
    $content = $content -replace "^package $oldPackage", "package $newPackage"
    
    # Update imports
    $content = $content -replace "import $oldPackage\.", "import $newPackage."
    
    # Update class references in code
    $content = $content -replace "\bDfRootApplication\b", "PixelSUApplication"
    $content = $content -replace "\bDfRootLogger\b", "PixelSULogger"
    $content = $content -replace "\bDfRoot\b", "PixelSU"
    
    Set-Content $file.FullName -Value $content -Encoding UTF8
}

# 3. Update build.gradle.kts
$buildGradle = "$sourceDir\app\build.gradle.kts"
if (Test-Path $buildGradle) {
    $content = Get-Content $buildGradle -Raw
    $content = $content -replace 'namespace = "com\.dfroot"', 'namespace = "com.pixelsu"'
    $content = $content -replace 'applicationId = "com\.dfroot"', 'applicationId = "com.pixelsu"'
    Set-Content $buildGradle -Value $content -Encoding UTF8
}

# 4. Update AndroidManifest.xml
$manifest = "$sourceDir\app\src\main\AndroidManifest.xml"
if (Test-Path $manifest) {
    $content = Get-Content $manifest -Raw
    $content = $content -replace 'package="com\.dfroot"', 'package="com.pixelsu"'
    $content = $content -replace 'android:name="\.DfRootApplication"', 'android:name=".PixelSUApplication"'
    $content = $content -replace 'android:authorities="\$\{applicationId\}\.fileprovider"', 'android:authorities="${applicationId}.fileprovider"'
    $content = $content -replace 'com\.dfroot\.', 'com.pixelsu.'
    $content = $content -replace 'DfRoot', 'PixelSU'
    Set-Content $manifest -Value $content -Encoding UTF8
}

# 5. Update strings.xml
$strings = "$sourceDir\app\src\main\res\values\strings.xml"
if (Test-Path $strings) {
    $content = Get-Content $strings -Raw
    $content = $content -replace '<string name="app_name">.*?</string>', '<string name="app_name">PixelSU</string>'
    $content = $content -replace 'DfRoot', 'PixelSU'
    Set-Content $strings -Value $content -Encoding UTF8
}

# 6. Update themes.xml (app theme name)
$themes = "$sourceDir\app\src\main\res\values\themes.xml"
if (Test-Path $themes) {
    $content = Get-Content $themes -Raw
    $content = $content -replace 'Theme\.DfRoot', 'Theme.PixelSU'
    $content = $content -replace 'Theme\.DfRoot\.NoActionBar', 'Theme.PixelSU.NoActionBar'
    Set-Content $themes -Value $content -Encoding UTF8
}

$themesNight = "$sourceDir\app\src\main\res\values-night\themes.xml"
if (Test-Path $themesNight) {
    $content = Get-Content $themesNight -Raw
    $content = $content -replace 'Theme\.DfRoot', 'Theme.PixelSU'
    $content = $content -replace 'Theme\.DfRoot\.NoActionBar', 'Theme.PixelSU.NoActionBar'
    Set-Content $themesNight -Value $content -Encoding UTF8
}

# 7. Update navigation graph
$navGraph = "$sourceDir\app\src\main\res\navigation\nav_graph.xml"
if (Test-Path $navGraph) {
    $content = Get-Content $navGraph -Raw
    $content = $content -replace 'com\.dfroot\.', 'com.pixelsu.'
    Set-Content $navGraph -Value $content -Encoding UTF8
}

# 8. Update layout files (class references)
$layoutFiles = Get-ChildItem -Path "$sourceDir\app\src\main\res\layout" -Filter "*.xml"
foreach ($file in $layoutFiles) {
    $content = Get-Content $file.FullName -Raw
    $content = $content -replace 'class="com\.dfroot\.', 'class="com.pixelsu.'
    $content = $content -replace 'com\.dfroot\.', 'com.pixelsu.'
    Set-Content $file.FullName -Value $content -Encoding UTF8
}

# 9. Update menu files
$menuFiles = Get-ChildItem -Path "$sourceDir\app\src\main\res\menu" -Filter "*.xml"
foreach ($file in $menuFiles) {
    $content = Get-Content $file.FullName -Raw
    $content = $content -replace 'com\.dfroot\.', 'com.pixelsu.'
    Set-Content $file.FullName -Value $content -Encoding UTF8
}

# 10. Update xml resources
$xmlFiles = Get-ChildItem -Path "$sourceDir\app\src\main\res\xml" -Filter "*.xml"
foreach ($file in $xmlFiles) {
    $content = Get-Content $file.FullName -Raw
    $content = $content -replace 'com\.dfroot\.', 'com.pixelsu.'
    Set-Content $file.FullName -Value $content -Encoding UTF8
}

# 11. Update gradle.properties
$gradleProps = "$sourceDir\gradle.properties"
if (Test-Path $gradleProps) {
    $content = Get-Content $gradleProps -Raw
    $content = $content -replace 'com\.dfroot', 'com.pixelsu'
    Set-Content $gradleProps -Value $content -Encoding UTF8
}

# 12. Update settings.gradle.kts
$settingsGradle = "$sourceDir\settings.gradle.kts"
if (Test-Path $settingsGradle) {
    $content = Get-Content $settingsGradle -Raw
    $content = $content -replace 'rootProject\.name = ".*"', 'rootProject.name = "PixelSU"'
    Set-Content $settingsGradle -Value $content -Encoding UTF8
}

# 13. Clean up old package directory
if (Test-Path $oldJavaPath) {
    Remove-Item -Path $oldJavaPath -Recurse -Force
    Write-Host "Removed old package directory" -ForegroundColor Green
}

# 14. Clean build directory
$buildDir = "$sourceDir\app\build"
if (Test-Path $buildDir) {
    Remove-Item -Path $buildDir -Recurse -Force
    Write-Host "Cleaned build directory" -ForegroundColor Green
}

Write-Host "`n✅ Rename complete! Run build:" -ForegroundColor Green
Write-Host "  cd $sourceDir" -ForegroundColor Yellow
Write-Host "  .\gradlew.bat clean assembleDebug" -ForegroundColor Yellow