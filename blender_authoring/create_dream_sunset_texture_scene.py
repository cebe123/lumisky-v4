import json
import os

import bpy


ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
AUTHORING_DIR = os.path.join(ROOT, "blender_authoring")
TEXTURE_PATH = os.path.join(ROOT, "app", "src", "main", "assets", "dreamsunset", "sunset_reference.webp")
OUT_BLEND = os.path.join(AUTHORING_DIR, "dream_sunset_texture_authoring.blend")
OUT_PREVIEW = os.path.join(AUTHORING_DIR, "dream_sunset_texture_preview.png")
OUT_DATA = os.path.join(AUTHORING_DIR, "dream_sunset_blender_data.json")


def clear_scene():
    bpy.ops.object.select_all(action="SELECT")
    bpy.ops.object.delete()


def build_scene():
    clear_scene()
    scene = bpy.context.scene
    scene.render.resolution_x = 2000
    scene.render.resolution_y = 1000
    scene.frame_start = 1
    scene.frame_end = 180
    scene.frame_set(135)
    try:
        scene.render.engine = "BLENDER_EEVEE_NEXT"
    except TypeError:
        scene.render.engine = "BLENDER_EEVEE"
    scene.view_settings.view_transform = "Standard"
    scene.view_settings.look = "Medium High Contrast"
    scene.world.color = (0.03, 0.0, 0.0)

    image = bpy.data.images.load(TEXTURE_PATH)
    material = bpy.data.materials.new("Lumisky_Dream_Sunset_Texture")
    material.use_nodes = True
    nodes = material.node_tree.nodes
    links = material.node_tree.links
    nodes.clear()
    output = nodes.new("ShaderNodeOutputMaterial")
    emission = nodes.new("ShaderNodeEmission")
    tex = nodes.new("ShaderNodeTexImage")
    tex.image = image
    links.new(tex.outputs["Color"], emission.inputs["Color"])
    emission.inputs["Strength"].default_value = 1.0
    links.new(emission.outputs["Emission"], output.inputs["Surface"])

    mesh = bpy.data.meshes.new("Lumisky_Dream_Sunset_Texture_Mesh")
    mesh.from_pydata(
        [(-10.0, 0.0, -5.0), (10.0, 0.0, -5.0), (10.0, 0.0, 5.0), (-10.0, 0.0, 5.0)],
        [],
        [(0, 1, 2, 3)],
    )
    mesh.update()
    uv_layer = mesh.uv_layers.new(name="UVMap")
    for loop, uv in zip(uv_layer.data, [(0.0, 0.0), (1.0, 0.0), (1.0, 1.0), (0.0, 1.0)]):
        loop.uv = uv
    plane = bpy.data.objects.new("Lumisky_Dream_Sunset_Texture_Plane", mesh)
    bpy.context.collection.objects.link(plane)
    plane.data.materials.append(material)

    camera_data = bpy.data.cameras.new("Lumisky_Texture_Camera")
    camera = bpy.data.objects.new("Lumisky_Texture_Camera", camera_data)
    bpy.context.collection.objects.link(camera)
    camera.location = (0.0, -10.0, 0.0)
    camera.rotation_euler = (1.57079632679, 0.0, 0.0)
    camera_data.type = "ORTHO"
    camera_data.ortho_scale = 10.0
    scene.camera = camera

    data = {
        "id": "dream_sunset_texture_authoring",
        "source_reference": "C:/Users/adnan/OneDrive/Masaustu/sunset.webp",
        "androidTexture": "dreamsunset/sunset_reference.webp",
        "blenderkitCandidate": {
            "url": "https://www.blenderkit.com/asset-gallery-detail/eeb9093a-8e88-45e1-b9d1-494c16065556/",
            "name": "HDRi: Redsky",
            "use": "authoring reference only; runtime uses compressed wallpaper texture"
        },
        "pathType": "VERTICAL",
        "curve": "LINEAR",
        "horizonScreenY": 0.50,
        "horizonOffset": 0.50,
        "frames": [
            {
                "frame": 135,
                "minute": 1050,
                "label": "reference_texture_match",
                "screen_y": 0.565,
                "radius": 0.072,
                "glow": 1.0,
                "top": [0.34, 0.04, 0.09],
                "mid": [0.70, 0.12, 0.10],
                "horizon": [1.0, 0.30, 0.10]
            }
        ],
        "android_notes": {
            "method": "photo texture base plus light shader shimmer",
            "reason": "closest visual match to the supplied reference",
            "animation": "low-frequency water shimmer, 10 fps service policy",
            "moon": "hidden because the supplied target image has no visible moon"
        }
    }

    with open(OUT_DATA, "w", encoding="utf-8") as handle:
        json.dump(data, handle, indent=2)
        handle.write("\n")

    bpy.ops.wm.save_as_mainfile(filepath=OUT_BLEND)
    scene.render.filepath = OUT_PREVIEW
    bpy.ops.render.render(write_still=True)
    print(json.dumps({"blend": OUT_BLEND, "preview": OUT_PREVIEW, "data": OUT_DATA}))


if __name__ == "__main__":
    build_scene()
