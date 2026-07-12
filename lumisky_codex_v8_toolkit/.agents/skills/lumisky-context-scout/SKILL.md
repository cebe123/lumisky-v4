---
name: lumisky-context-scout
description: Use first to locate the minimum Lumisky files and symbols for a task without scanning the whole repository; targeted context discovery and scope only.
---


1. Read root and nearest module `AGENTS.md`.
2. Run `git status --short`; note changed paths without opening every diff.
3. Classify one domain using `.agents/references/path-routing.md`.
4. Run at most three targeted searches (`rg -n`) using exact symbol, class, JSON key or error text.
5. Initial read limit: six files, relevant symbol ranges only. Exclude build/generated/binary assets.
6. Produce:
   - `Scope:` exact files/symbols.
   - `Why:` one sentence per file.
   - `Unknown:` only blocking uncertainty.
7. Stop when enough evidence exists. Do not edit code in this skill.

Optional helper: `.agents/scripts/context-scout.ps1 -Query "<symbol>"`.

