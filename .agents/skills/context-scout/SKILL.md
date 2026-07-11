---
name: context-scout
description: Locates the minimum files and symbols needed for a code task. Use when finding implementations, dependencies, call sites, architecture entry points, or relevant project files.
---
# Context scout

## Goal
Build a minimal working context without reading the repository broadly.

## Procedure
1. List the nearest directory or module; avoid recursive file dumps.
2. Search exact symbols, filenames, routes, error text, imports, or resource IDs with `rg`/IDE search.
3. Rank matches: definition, direct caller, configuration, test.
4. Open definitions first, then only direct dependencies needed to understand the requested change.
5. Read targeted ranges around matches rather than entire large files.
6. Return a scope map containing at most:
   - primary file(s)
   - direct dependency/caller
   - relevant test/config
   - next action

## Stop conditions
Stop discovery when the edit location, contract, and validation path are known.

## Avoid
- Whole-repository summaries.
- Reading generated, build, cache, vendor, binary, or lock files unless directly relevant.
- Opening every search result.
