#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif

uniform vec2 u_Resolution;
uniform float u_Time;
uniform float u_TimeOfDay;
uniform vec2 u_SunPos;
uniform vec3 u_SunColor;
uniform float u_DrawSun;
uniform vec2 u_MoonPos;
uniform float u_IsNight;
uniform float u_NightAmount;
uniform float u_CloudOffset;
uniform float u_CloudAlpha;
uniform vec2 u_Parallax;

float circleMask(vec2 uv, vec2 center, float radius, float feather) {
    float d = length(uv - center);
    return 1.0 - smoothstep(radius - feather, radius + feather, d);
}

float ellipseMask(vec2 uv, vec2 center, vec2 radius, float feather) {
    float d = length((uv - center) / radius);
    return 1.0 - smoothstep(1.0 - feather, 1.0 + feather, d);
}

float cloudCluster(vec2 uv, vec2 base, float scale) {
    float c = 0.0;
    c = max(c, ellipseMask(uv, base + vec2(-0.12, 0.00) * scale, vec2(0.18, 0.030) * scale, 0.10));
    c = max(c, ellipseMask(uv, base + vec2(-0.02, 0.01) * scale, vec2(0.22, 0.035) * scale, 0.10));
    c = max(c, ellipseMask(uv, base + vec2( 0.10, 0.00) * scale, vec2(0.19, 0.027) * scale, 0.10));
    c = max(c, ellipseMask(uv, base + vec2( 0.20,-0.01) * scale, vec2(0.13, 0.020) * scale, 0.10));
    return c;
}

vec3 skyGradient(vec2 uv, float nightAmount) {
    vec3 topSunset = vec3(0.69, 0.075, 0.12);
    vec3 midSunset = vec3(0.98, 0.22, 0.10);
    vec3 lowSunset = vec3(1.00, 0.62, 0.20);

    vec3 topNight = vec3(0.015, 0.025, 0.09);
    vec3 midNight = vec3(0.055, 0.055, 0.16);
    vec3 lowNight = vec3(0.16, 0.09, 0.22);

    float lowerBlend = smoothstep(0.0, 0.56, uv.y);
    float upperBlend = smoothstep(0.52, 1.0, uv.y);

    vec3 sunset = mix(lowSunset, midSunset, lowerBlend);
    sunset = mix(sunset, topSunset, upperBlend);

    vec3 night = mix(lowNight, midNight, lowerBlend);
    night = mix(night, topNight, upperBlend);

    return mix(sunset, night, clamp(nightAmount, 0.0, 1.0));
}

void main() {
    vec2 uv = gl_FragCoord.xy / max(u_Resolution, vec2(1.0));
    float aspect = u_Resolution.x / max(u_Resolution.y, 1.0);
    vec2 sceneUv = uv + vec2(u_Parallax.x / max(aspect, 0.01), u_Parallax.y) * 0.015;

    float night = clamp(max(u_NightAmount, u_IsNight * 0.72), 0.0, 1.0);
    vec3 color = skyGradient(sceneUv, night);

    vec2 sunCenter = vec2(
        mix(0.12, 0.90, clamp(u_SunPos.x, 0.0, 1.0)),
        mix(-0.12, 0.78, clamp(u_SunPos.y, 0.0, 1.0))
    );
    vec2 moonCenter = vec2(
        mix(0.90, 0.10, clamp(u_MoonPos.x, 0.0, 1.0)),
        mix(-0.10, 0.82, clamp(u_MoonPos.y, 0.0, 1.0))
    );

    float sun = circleMask(sceneUv, sunCenter, 0.112, 0.004) * u_DrawSun;
    float sunGlow = exp(-18.0 * max(length(sceneUv - sunCenter) - 0.08, 0.0)) * 0.22 * u_DrawSun;
    vec3 sunColor = vec3(1.0, 0.90, 0.58);
    color = mix(color, sunColor, sun);
    color += sunColor * sunGlow * (1.0 - night);

    float moon = circleMask(sceneUv, moonCenter, 0.064, 0.004) * u_IsNight;
    float moonGlow = exp(-22.0 * max(length(sceneUv - moonCenter) - 0.04, 0.0)) * 0.16 * u_IsNight;
    vec3 moonColor = vec3(0.82, 0.88, 1.0);
    color = mix(color, moonColor, moon);
    color += moonColor * moonGlow;

    float drift = fract(u_Time * 0.0018 + u_CloudOffset);
    float cloud = 0.0;
    cloud = max(cloud, cloudCluster(sceneUv, vec2(fract(0.18 + drift) - 0.10, 0.79), 0.80));
    cloud = max(cloud, cloudCluster(sceneUv, vec2(fract(0.68 + drift * 0.72), 0.68), 0.58));
    cloud = max(cloud, cloudCluster(sceneUv, vec2(fract(0.02 + drift * 0.45), 0.55), 0.48));
    cloud = max(cloud, cloudCluster(sceneUv, vec2(fract(0.50 + drift * 0.60), 0.47), 0.62));
    cloud = max(cloud, cloudCluster(sceneUv, vec2(fract(0.82 + drift * 0.38), 0.39), 0.45));

    vec3 cloudColor = mix(vec3(1.0, 0.49, 0.18), vec3(0.18, 0.15, 0.32), night);
    float cloudAlpha = clamp(0.34 + u_CloudAlpha * 0.32, 0.18, 0.62);
    color = mix(color, cloudColor, cloud * cloudAlpha);

    float horizonGlow = (1.0 - smoothstep(0.28, 0.72, sceneUv.y)) * (1.0 - night);
    color += vec3(0.10, 0.025, 0.0) * horizonGlow;

    gl_FragColor = vec4(color, 1.0);
}
