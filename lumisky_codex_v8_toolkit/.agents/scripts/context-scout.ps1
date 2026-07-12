param(
    [Parameter(Mandatory = $true)][string]$Query,
    [string[]]$Roots = @("app", "core", "engine", "wallpaper", "build-logic", "benchmark"),
    [int]$MaxMatches = 60
)

$ErrorActionPreference = "Stop"
Write-Host "== Git status =="
git status --short

$existingRoots = $Roots | Where-Object { Test-Path $_ }
if ($existingRoots.Count -eq 0) { $existingRoots = @(".") }

Write-Host "`n== Targeted matches: $Query =="
$rg = Get-Command rg -ErrorAction SilentlyContinue
if ($rg) {
    $args = @(
        "-n", "--hidden", "--smart-case",
        "--glob", "!**/build/**",
        "--glob", "!**/.gradle/**",
        "--glob", "!**/.git/**",
        "--glob", "!**/.idea/**",
        "--glob", "!**/*.{png,jpg,jpeg,webp,mp4,zip,jar,aar}"
    ) + @($Query) + $existingRoots
    & rg @args | Select-Object -First $MaxMatches
} else {
    Get-ChildItem $existingRoots -Recurse -File |
        Where-Object { $_.FullName -notmatch '[\\/](build|\.gradle|\.git|\.idea)[\\/]' -and $_.Extension -notin @('.png','.jpg','.jpeg','.webp','.mp4','.zip','.jar','.aar') } |
        Select-String -Pattern $Query |
        Select-Object -First $MaxMatches
}
