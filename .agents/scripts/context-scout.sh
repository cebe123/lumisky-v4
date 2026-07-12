#!/usr/bin/env bash
set -euo pipefail
query="${1:?usage: context-scout.sh <query>}"
echo '== Git status =='
git status --short
echo
echo "== Targeted matches: $query =="
roots=()
for d in app core engine wallpaper build-logic benchmark; do [[ -e "$d" ]] && roots+=("$d"); done
[[ ${#roots[@]} -eq 0 ]] && roots=(.)
rg -n --hidden --smart-case \
  --glob '!**/build/**' --glob '!**/.gradle/**' --glob '!**/.git/**' --glob '!**/.idea/**' \
  --glob '!**/*.{png,jpg,jpeg,webp,mp4,zip,jar,aar}' "$query" "${roots[@]}" | head -n 60
