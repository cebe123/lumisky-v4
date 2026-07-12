#!/usr/bin/env python3
from __future__ import annotations
import argparse
import json
import shutil
from pathlib import Path

PACKAGE_ID = "samurai_sunset"

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("project_root", type=Path)
    parser.add_argument("--force", action="store_true")
    args = parser.parse_args()

    package_root = Path(__file__).resolve().parent
    src_assets = package_root / "app/src/main/assets"
    dst_assets = args.project_root / "app/src/main/assets"

    for kind in ("wallpapers", "shaders"):
        src = src_assets / kind / PACKAGE_ID
        dst = dst_assets / kind / PACKAGE_ID
        if dst.exists():
            if not args.force:
                raise SystemExit(f"{dst} already exists; use --force")
            shutil.rmtree(dst)
        dst.parent.mkdir(parents=True, exist_ok=True)
        shutil.copytree(src, dst)

    index_path = dst_assets / "wallpapers/index.json"
    data = json.loads(index_path.read_text(encoding="utf-8"))
    items = data.setdefault("wallpapers", [])
    entry = {"id": PACKAGE_ID, "manifest": f"wallpapers/{PACKAGE_ID}/manifest.json"}
    if not any(item.get("id") == PACKAGE_ID for item in items):
        items.append(entry)
        index_path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"Installed {PACKAGE_ID}")

if __name__ == "__main__":
    main()
