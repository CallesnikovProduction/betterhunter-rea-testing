param(
    [string[]]$Tasks = @(':salary:test', ':vacancy:test', ':search:test', ':mvi:test', ':android-app:testDebugUnitTest', ':android-app:assembleDebug')
)

$ErrorActionPreference = 'Stop'
# A temporary ASCII path avoids Windows test-worker classpath encoding errors.
# No files are moved and no device or emulator commands are invoked.
$projectPath = $PSScriptRoot
$drive = @('B', 'Z', 'Y', 'X', 'W') | Where-Object { -not (Test-Path "${_}:\") } | Select-Object -First 1
if (-not $drive) { throw 'No free drive letter for the temporary build path.' }

subst "${drive}:" $projectPath
if ($LASTEXITCODE -ne 0) { throw 'Could not create the temporary build path.' }
$result = 1
try {
    Push-Location "${drive}:\"
    try {
        & .\gradlew.bat @Tasks --console=plain
        $result = $LASTEXITCODE
        if ($result -eq 0 -and $Tasks -contains ':android-app:assembleDebug') {
            Copy-Item -LiteralPath (Join-Path $projectPath 'android-app\build\outputs\apk\debug\android-app-debug.apk') -Destination (Join-Path $projectPath 'app-debug.apk')
        }
    } finally { Pop-Location }
} finally {
    subst "${drive}:" /D
    if ($LASTEXITCODE -ne 0) { Write-Warning "Could not remove temporary drive ${drive}:" }
}
exit $result
