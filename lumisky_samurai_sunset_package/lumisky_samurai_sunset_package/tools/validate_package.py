#!/usr/bin/env python3
from pathlib import Path
import json
from PIL import Image

PACKAGE_ID = "samurai_sunset"
LAYERS = [
    "mountains", "middle_ground", "architecture", "atmosphere_overlay",
    "main_subject", "left_foreground", "foreground_cliff"
]

root = Path(__file__).resolve().parents[1]
wp = root / "app/src/main/assets/wallpapers" / PACKAGE_ID
errors = []

for name in LAYERS:
    path = wp / f"assets/layers/{name}.png"
    if not path.exists():
        errors.append(f"missing {path}")
        continue
    image = Image.open(path)
    if image.size != (1440, 2560):
        errors.append(f"wrong size {name}: {image.size}")
    if image.mode != "RGBA":
        errors.append(f"not RGBA {name}: {image.mode}")
    else:
        amin, amax = image.getchannel("A").getextrema()
        if amin != 0 or amax != 255:
            errors.append(f"alpha range {name}: {(amin, amax)}")

manifest = json.loads((wp / "manifest.json").read_text(encoding="utf-8"))
if len(manifest["creator"]["layers"]) > 8:
    errors.append("creator layer limit exceeded")

if errors:
    print("VALIDATION FAILED")
    print("\n".join(errors))
    raise SystemExit(1)
print("VALIDATION OK")
