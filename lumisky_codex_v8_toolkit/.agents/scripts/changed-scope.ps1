$ErrorActionPreference = "Stop"
Write-Host "== Status =="
git status --short
Write-Host "`n== Unstaged stat =="
git diff --stat
Write-Host "`n== Staged stat =="
git diff --cached --stat

$paths = @()
$paths += git diff --name-only
$paths += git diff --cached --name-only
$paths += (git status --porcelain | ForEach-Object { if ($_.Length -gt 3) { $_.Substring(3) } })
$paths = $paths | Sort-Object -Unique

Write-Host "`n== Touched areas =="
$areas = $paths | ForEach-Object {
    if ($_ -match '^([^/\\]+)') { $matches[1] } else { '<root>' }
} | Sort-Object -Unique
$areas | ForEach-Object { Write-Host "- $_" }
