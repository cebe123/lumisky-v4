#!/usr/bin/env bash
set -euo pipefail
run=false
[[ "${1:-}" == '--run' ]] && run=true
mapfile -t paths < <({ git diff --name-only; git diff --cached --name-only; git status --porcelain | cut -c4-; } | sort -u)
tasks=()
add(){ [[ " ${tasks[*]} " == *" $1 "* ]] || tasks+=("$1"); }
for p in "${paths[@]}"; do
  case "$p" in
    app/*) add ':app:compileDebugKotlin'; add ':app:testDebugUnitTest' ;;
    core/*) add ':core:compileDebugKotlin'; add ':core:testDebugUnitTest' ;;
    engine/*) add ':engine:compileDebugKotlin'; add ':engine:testDebugUnitTest' ;;
    wallpaper/*) add ':wallpaper:compileDebugKotlin'; add ':wallpaper:testDebugUnitTest' ;;
    build-logic/*) add ':build-logic:check' ;;
    benchmark/*) add ':benchmark:compileDebugKotlin' ;;
  esac
  [[ "$p" =~ \.(glsl|vert|frag)$ ]] && add ':app:assembleDebug'
  [[ "$p" =~ (definition|manifest|catalog).*\.json$ ]] && add ':app:assembleDebug'
done
[[ ${#tasks[@]} -eq 0 ]] && { echo 'No changed module detected.'; exit 0; }
echo "Suggested: ./gradlew ${tasks[*]}"
$run && ./gradlew "${tasks[@]}"
