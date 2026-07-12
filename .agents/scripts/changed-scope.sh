#!/usr/bin/env bash
set -euo pipefail
echo '== Status =='; git status --short
echo; echo '== Unstaged stat =='; git diff --stat
echo; echo '== Staged stat =='; git diff --cached --stat
