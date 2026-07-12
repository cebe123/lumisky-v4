param([switch]$Run)
$ErrorActionPreference = "Stop"

$paths = @()
$paths += git diff --name-only
$paths += git diff --cached --name-only
$paths += (git status --porcelain | ForEach-Object { if ($_.Length -gt 3) { $_.Substring(3) } })
$paths = $paths | Sort-Object -Unique

$tasks = New-Object System.Collections.Generic.List[string]
function Add-Task([string]$task) { if (-not $tasks.Contains($task)) { $tasks.Add($task) } }

foreach ($path in $paths) {
    switch -Regex ($path) {
        '^app/'       { Add-Task ':app:compileDebugKotlin'; Add-Task ':app:testDebugUnitTest' }
        '^core/'      { Add-Task ':core:compileDebugKotlin'; Add-Task ':core:testDebugUnitTest' }
        '^engine/'    { Add-Task ':engine:compileDebugKotlin'; Add-Task ':engine:testDebugUnitTest' }
        '^wallpaper/' { Add-Task ':wallpaper:compileDebugKotlin'; Add-Task ':wallpaper:testDebugUnitTest' }
        '^build-logic/' { Add-Task ':build-logic:check' }
        '^benchmark/' { Add-Task ':benchmark:compileDebugKotlin' }
        '\.(glsl|vert|frag)$' { Add-Task ':app:assembleDebug' }
        '(definition|manifest|catalog).*\.json$' { Add-Task ':app:assembleDebug' }
        '^(settings\.gradle\.kts|build\.gradle\.kts|gradle/)' { Add-Task 'assembleDebug' }
    }
}

if ($tasks.Count -eq 0) {
    Write-Host 'No changed module detected. Suggested: .\gradlew.bat help'
    exit 0
}

$gradle = if (Test-Path '.\gradlew.bat') { '.\gradlew.bat' } else { './gradlew' }
$command = "$gradle " + ($tasks -join ' ')
Write-Host "Suggested command:`n$command"

if ($Run) {
    & $gradle @tasks
    exit $LASTEXITCODE
}
