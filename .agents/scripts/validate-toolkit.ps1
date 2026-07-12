$ErrorActionPreference = "Stop"
$errors = New-Object System.Collections.Generic.List[string]

$agentsFiles = Get-ChildItem -Recurse -Filter 'AGENTS.md'
foreach ($file in $agentsFiles) {
    if ($file.Length -gt 16000) { $errors.Add("AGENTS too large (>16KB): $($file.FullName)") }
}

$names = @{}
$skills = Get-ChildItem '.agents/skills' -Directory
foreach ($skill in $skills) {
    $file = Join-Path $skill.FullName 'SKILL.md'
    if (-not (Test-Path $file)) { $errors.Add("Missing SKILL.md: $($skill.FullName)"); continue }
    $text = Get-Content $file -Raw
    if ($text -notmatch '(?m)^name:\s*(.+)$') { $errors.Add("Missing name: $file"); continue }
    $name = $matches[1].Trim()
    if ($text -notmatch '(?m)^description:\s*.+$') { $errors.Add("Missing description: $file") }
    if ($names.ContainsKey($name)) { $errors.Add("Duplicate skill name: $name") } else { $names[$name] = $file }
}

if ($errors.Count -gt 0) {
    $errors | ForEach-Object { Write-Error $_ }
    exit 1
}
Write-Host "PASS: $($skills.Count) skills, $($agentsFiles.Count) AGENTS files."
