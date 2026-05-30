import json
import math
import os
import random

import bpy


ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
AUTHORING_DIR = os.path.join(ROOT, "blender_authoring")
OUT_BLEND = os.path.join(AUTHORING_DIR, "dream_sunset_reference_authoring.blend")
OUT_PREVIEW = os.path.join(AUTHORING_DIR, "dream_sunset_reference_preview.png")
OUT_DATA = os.path.join(AUTHORING_DIR, "dream_sunset_blender_data.json")

WIDTH = 2000
HEIGHT = 1000
HORIZON_SCREEN_Y = 0.50


def clear_scene():
    bpy.ops.object.select_all(action="SELECT")
    bpy.ops.object.delete()
    for collection in (
        bpy.data.meshes,
        bpy.data.curves,
        bpy.data.materials,
        bpy.data.images,
        bpy.data.lights,
        bpy.data.cameras,
    ):
        for item in list(collection):
            if item.users == 0:
                collection.remove(item)


def emission_material(name, color, alpha=1.0):
    mat = bpy.data.materials.new(name)
    mat.diffuse_color = color
    mat.use_nodes = True
    nodes = mat.node_tree.nodes
    links = mat.node_tree.links
    nodes.clear()

    output = nodes.new("ShaderNodeOutputMaterial")
    emission = nodes.new("ShaderNodeEmission")
    emission.inputs["Color"].default_value = color
    emission.inputs["Strength"].default_value = 1.0

    if alpha < 0.999:
        transparent = nodes.new("ShaderNodeBsdfTransparent")
        mix = nodes.new("ShaderNodeMixShader")
        mix.inputs[0].default_value = alpha
        links.new(transparent.outputs[0], mix.inputs[1])
        links.new(emission.outputs[0], mix.inputs[2])
        links.new(mix.outputs[0], output.inputs["Surface"])
        mat.blend_method = "BLEND"
        mat.use_screen_refraction = False
        mat.show_transparent_back = True
    else:
        links.new(emission.outputs[0], output.inputs["Surface"])

    return mat


def rect(name, x0, x1, z0, z1, y, mat):
    mesh = bpy.data.meshes.new(name + "Mesh")
    mesh.from_pydata(
        [(x0, y, z0), (x1, y, z0), (x1, y, z1), (x0, y, z1)],
        [],
        [(0, 1, 2, 3)],
    )
    mesh.update()
    obj = bpy.data.objects.new(name, mesh)
    bpy.context.collection.objects.link(obj)
    obj.data.materials.append(mat)
    return obj


def ellipse(name, cx, zc, rx, rz, y, mat, segments=96):
    verts = [(cx, y, zc)]
    for i in range(segments):
        angle = (math.tau * i) / segments
        verts.append((cx + math.cos(angle) * rx, y, zc + math.sin(angle) * rz))
    faces = []
    for i in range(1, segments + 1):
        faces.append((0, i, 1 if i == segments else i + 1))
    mesh = bpy.data.meshes.new(name + "Mesh")
    mesh.from_pydata(verts, [], faces)
    mesh.update()
    obj = bpy.data.objects.new(name, mesh)
    bpy.context.collection.objects.link(obj)
    obj.data.materials.append(mat)
    return obj


def lerp_color(a, b, t):
    return tuple(a[i] + (b[i] - a[i]) * t for i in range(4))


def add_curve_line(name, points, y, mat, bevel_depth):
    curve = bpy.data.curves.new(name, "CURVE")
    curve.dimensions = "3D"
    curve.resolution_u = 2
    curve.bevel_depth = bevel_depth
    curve.bevel_resolution = 1
    spl = curve.splines.new("POLY")
    spl.points.add(len(points) - 1)
    for point, (x, z) in zip(spl.points, points):
        point.co = (x, y, z, 1.0)
    obj = bpy.data.objects.new(name, curve)
    bpy.context.collection.objects.link(obj)
    obj.data.materials.append(mat)
    return obj


def setup_camera():
    camera_data = bpy.data.cameras.new("Lumisky_Reference_Camera")
    camera = bpy.data.objects.new("Lumisky_Reference_Camera", camera_data)
    bpy.context.collection.objects.link(camera)
    camera.location = (0.0, -10.0, 0.0)
    camera.rotation_euler = (math.radians(90.0), 0.0, 0.0)
    camera_data.type = "ORTHO"
    camera_data.ortho_scale = 10.0
    bpy.context.scene.camera = camera


def build_scene():
    random.seed(42)
    clear_scene()
    setup_camera()

    scene = bpy.context.scene
    scene.frame_start = 1
    scene.frame_end = 180
    scene.frame_set(135)
    scene.render.resolution_x = WIDTH
    scene.render.resolution_y = HEIGHT
    scene.render.film_transparent = False
    try:
        scene.render.engine = "BLENDER_EEVEE_NEXT"
    except TypeError:
        scene.render.engine = "BLENDER_EEVEE"
    scene.world = bpy.data.worlds.new("Lumisky_Reference_World")
    scene.world.color = (0.12, 0.01, 0.02)
    scene.view_settings.view_transform = "Standard"
    scene.view_settings.look = "Medium High Contrast"
    scene.view_settings.exposure = 0.0
    scene.view_settings.gamma = 1.0

    sky_top = (0.34, 0.035, 0.085, 1.0)
    sky_mid = (0.66, 0.105, 0.105, 1.0)
    sky_horizon = (1.00, 0.285, 0.105, 1.0)
    water_top = (0.105, 0.012, 0.020, 1.0)
    water_bottom = (0.020, 0.004, 0.006, 1.0)

    for i in range(72):
        t0 = i / 72.0
        t1 = (i + 1) / 72.0
        mid_t = (t0 + t1) * 0.5
        if mid_t < 0.48:
            color = lerp_color(sky_horizon, sky_mid, mid_t / 0.48)
        else:
            color = lerp_color(sky_mid, sky_top, (mid_t - 0.48) / 0.52)
        mat = emission_material(f"Lumisky_Sky_{i:02d}", color)
        rect(f"Lumisky_Sky_Strip_{i:02d}", -10.0, 10.0, t0 * 5.0, t1 * 5.0, 2.0, mat)

    glow_far = emission_material("Lumisky_Sunset_Red_Glow", (0.95, 0.09, 0.06, 0.30), 0.30)
    glow_warm = emission_material("Lumisky_Sunset_Orange_Glow", (1.0, 0.43, 0.12, 0.42), 0.42)
    glow_core = emission_material("Lumisky_Sunset_Core_Glow", (1.0, 0.69, 0.24, 0.35), 0.35)
    ellipse("Lumisky_Sunset_Red_Glow", 0.0, 0.72, 7.2, 2.9, 1.65, glow_far, 128)
    ellipse("Lumisky_Sunset_Orange_Glow", 0.0, 0.52, 4.2, 1.75, 1.55, glow_warm, 128)
    ellipse("Lumisky_Sunset_Core_Glow", 0.0, 0.58, 2.0, 0.92, 1.45, glow_core, 128)

    sun_mat = emission_material("Lumisky_Half_Sun", (1.0, 0.83, 0.42, 1.0))
    ellipse("Lumisky_Half_Sun", 0.0, 0.66, 0.72, 0.72, 1.35, sun_mat, 160)

    for i in range(54):
        t0 = i / 54.0
        t1 = (i + 1) / 54.0
        mid_t = (t0 + t1) * 0.5
        color = lerp_color(water_top, water_bottom, min(1.0, mid_t * 1.25))
        mat = emission_material(f"Lumisky_Ocean_{i:02d}", color)
        rect(f"Lumisky_Ocean_Strip_{i:02d}", -10.0, 10.0, -t1 * 5.0, -t0 * 5.0, 0.70, mat)

    horizon_mat = emission_material("Lumisky_Dark_Horizon", (0.060, 0.006, 0.010, 1.0))
    rect("Lumisky_Dark_Horizon", -10.0, 10.0, -0.035, 0.025, 0.38, horizon_mat)

    reflection_mats = []
    for i in range(8):
        alpha = 0.12 + i * 0.035
        color = (1.0, 0.30 + i * 0.035, 0.055, alpha)
        reflection_mats.append(emission_material(f"Lumisky_Reflection_{i}", color, alpha))

    for i in range(64):
        z = -0.08 - i * 0.073
        depth = min(1.0, i / 63.0)
        width = 3.15 * math.exp(-depth * 1.85) + 0.28
        segments = 54
        points = []
        phase = random.random() * math.tau
        for s in range(segments):
            p = s / (segments - 1)
            x = (p - 0.5) * width * (1.0 + 0.18 * math.sin(i * 0.43))
            wobble = math.sin(p * math.tau * (1.4 + depth * 3.0) + phase) * (0.010 + 0.025 * depth)
            wobble += random.uniform(-0.012, 0.012) * (1.0 - depth * 0.4)
            points.append((x, z + wobble))
        mat = reflection_mats[min(7, int((1.0 - depth) * 7.0))]
        add_curve_line(f"Lumisky_Reflection_Wave_{i:02d}", points, 0.18, mat, 0.006 + (1.0 - depth) * 0.008)

    dark_wave_mat = emission_material("Lumisky_Dark_Wave", (0.028, 0.003, 0.006, 0.38), 0.38)
    for i in range(44):
        z = -0.18 - i * 0.105
        points = []
        for s in range(80):
            p = s / 79.0
            x = -9.6 + p * 19.2
            points.append((x, z + math.sin(p * math.tau * 3.0 + i * 0.7) * 0.018))
        add_curve_line(f"Lumisky_Dark_Wave_{i:02d}", points, 0.08, dark_wave_mat, 0.004)

    cloud_mat = emission_material("Lumisky_Cloud_Silhouette", (0.100, 0.018, 0.040, 0.78), 0.78)
    cloud_deep_mat = emission_material("Lumisky_Deep_Cloud_Silhouette", (0.055, 0.006, 0.018, 0.84), 0.84)
    cloud_specs = [
        (-8.9, 2.15, 1.65, 0.28, cloud_deep_mat),
        (-7.7, 1.88, 1.45, 0.24, cloud_mat),
        (-6.2, 1.95, 1.10, 0.20, cloud_mat),
        (-4.8, 1.45, 1.25, 0.17, cloud_mat),
        (-3.6, 1.40, 0.95, 0.15, cloud_mat),
        (-1.1, 1.38, 0.78, 0.12, cloud_deep_mat),
        (0.2, 1.62, 1.45, 0.18, cloud_deep_mat),
        (1.6, 1.52, 1.18, 0.14, cloud_mat),
        (3.1, 1.30, 0.96, 0.12, cloud_mat),
        (5.9, 1.04, 0.92, 0.13, cloud_mat),
        (7.0, 1.45, 1.25, 0.18, cloud_deep_mat),
        (8.8, 2.12, 1.35, 0.25, cloud_mat),
        (9.7, 2.45, 1.10, 0.30, cloud_deep_mat),
    ]
    for index, (cx, zc, rx, rz, mat) in enumerate(cloud_specs):
        ellipse(f"Lumisky_Cloud_{index:02d}", cx, zc, rx, rz, 0.28, mat, 72)
        for j in range(3):
            ellipse(
                f"Lumisky_Cloud_{index:02d}_Lobe_{j}",
                cx + random.uniform(-rx * 0.45, rx * 0.45),
                zc + random.uniform(-rz * 0.35, rz * 0.55),
                rx * random.uniform(0.28, 0.55),
                rz * random.uniform(0.55, 1.10),
                0.24,
                mat,
                48,
            )

    small_cloud_mat = emission_material("Lumisky_Small_Cloud_Silhouette", (0.080, 0.012, 0.030, 0.64), 0.64)
    for i in range(24):
        cx = random.uniform(-5.5, 5.8)
        if -0.75 < cx < 0.85 and random.random() < 0.65:
            cx += 1.2
        zc = random.uniform(0.62, 2.95)
        ellipse(
            f"Lumisky_Small_Cloud_{i:02d}",
            cx,
            zc,
            random.uniform(0.20, 0.58),
            random.uniform(0.035, 0.095),
            0.22,
            small_cloud_mat,
            40,
        )

    moon_mat = emission_material("Lumisky_Hidden_Moon_Reference", (0.72, 0.66, 0.58, 0.16), 0.16)
    moon = ellipse("Lumisky_Hidden_Moon_Reference", -7.8, 4.15, 0.20, 0.20, 0.10, moon_mat, 64)
    moon.hide_render = True
    moon.hide_viewport = True

    scene["lumisky_reference"] = {
        "target": "sunset.webp",
        "horizon_screen_y": HORIZON_SCREEN_Y,
        "sun_screen_x": 0.50,
        "sun_screen_y": 0.565,
        "sun_radius": 0.072,
        "motion": "linear vertical",
    }

    bpy.ops.wm.save_as_mainfile(filepath=OUT_BLEND)
    scene.render.filepath = OUT_PREVIEW
    bpy.ops.render.render(write_still=True)

    data = {
        "id": "dream_sunset_reference_authoring",
        "source_reference": "C:/Users/adnan/OneDrive/Masaustu/sunset.webp",
        "pathType": "VERTICAL",
        "curve": "LINEAR",
        "horizonScreenY": HORIZON_SCREEN_Y,
        "horizonOffset": HORIZON_SCREEN_Y,
        "frames": [
            {
                "frame": 1,
                "minute": 360,
                "label": "red_sunrise",
                "screen_y": 0.50,
                "radius": 0.068,
                "glow": 0.85,
                "top": [0.30, 0.040, 0.085],
                "mid": [0.70, 0.120, 0.110],
                "horizon": [1.00, 0.300, 0.100],
            },
            {
                "frame": 90,
                "minute": 720,
                "label": "high_warm_sun",
                "screen_y": 0.78,
                "radius": 0.055,
                "glow": 0.70,
                "top": [0.36, 0.050, 0.105],
                "mid": [0.78, 0.145, 0.115],
                "horizon": [1.00, 0.355, 0.125],
            },
            {
                "frame": 135,
                "minute": 1050,
                "label": "reference_half_sun",
                "screen_y": 0.565,
                "radius": 0.072,
                "glow": 1.25,
                "top": [0.34, 0.035, 0.085],
                "mid": [0.66, 0.105, 0.105],
                "horizon": [1.00, 0.285, 0.105],
            },
            {
                "frame": 180,
                "minute": 1110,
                "label": "last_red_light",
                "screen_y": 0.515,
                "radius": 0.074,
                "glow": 0.95,
                "top": [0.20, 0.018, 0.050],
                "mid": [0.46, 0.060, 0.070],
                "horizon": [0.82, 0.115, 0.070],
            },
        ],
        "android_notes": {
            "useProceduralShader": True,
            "ocean": "lower half is dark procedural water with orange reflection column",
            "sun": "half-visible at the horizon in the reference frame, still driven by vertical linear orbit",
            "clouds": "dark silhouettes, placed above the horizon and in edge groups",
            "moon": "hidden for this reference because the supplied target image has no visible moon",
            "qualityTarget": "reference-matching authoring scene with battery-friendly runtime shader",
        },
    }
    with open(OUT_DATA, "w", encoding="utf-8") as handle:
        json.dump(data, handle, indent=2)
        handle.write("\n")

    print(json.dumps({"blend": OUT_BLEND, "preview": OUT_PREVIEW, "data": OUT_DATA}))


if __name__ == "__main__":
    build_scene()
