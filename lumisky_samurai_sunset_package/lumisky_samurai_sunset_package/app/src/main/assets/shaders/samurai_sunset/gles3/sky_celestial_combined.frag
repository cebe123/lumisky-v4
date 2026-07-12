#version 300 es
precision highp float;
in vec2 v_Uv;
out vec4 fragColor;

uniform vec2 u_Resolution;
uniform float u_Time;
uniform float u_DayProgress;
uniform float u_NightAmount;
uniform vec2 u_Parallax;

vec3 skyGradient(vec2 uv, float nightAmount) {
    vec3 topSunset = vec3(0.69, 0.075, 0.12);
    vec3 midSunset = vec3(0.98, 0.22, 0.10);
    vec3 lowSunset = vec3(1.00, 0.62, 0.20);
    vec3 topNight = vec3(0.015, 0.025, 0.09);
    vec3 midNight = vec3(0.055, 0.055, 0.16);
    vec3 lowNight = vec3(0.16, 0.09, 0.22);

    float a = smoothstep(0.0, 0.56, uv.y);
    float b = smoothstep(0.52, 1.0, uv.y);
    vec3 day = mix(lowSunset, midSunset, a);
    day = mix(day, topSunset, b);
    vec3 night = mix(lowNight, midNight, a);
    night = mix(night, topNight, b);
    return mix(day, night, clamp(nightAmount, 0.0, 1.0));
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

vec4 renderSky(vec2 uv) {
    vec2 p = uv + u_Parallax * 0.012;
    float night = clamp(u_NightAmount, 0.0, 1.0);
    vec3 color = skyGradient(p, night);

    float drift = fract(u_Time * 0.0018);
    float cloud = 0.0;
    cloud = max(cloud, cloudCluster(p, vec2(fract(0.18 + drift) - 0.10, 0.79), 0.80));
    cloud = max(cloud, cloudCluster(p, vec2(fract(0.68 + drift * 0.72), 0.68), 0.58));
    cloud = max(cloud, cloudCluster(p, vec2(fract(0.02 + drift * 0.45), 0.55), 0.48));
    cloud = max(cloud, cloudCluster(p, vec2(fract(0.50 + drift * 0.60), 0.47), 0.62));

    vec3 cloudColor = mix(vec3(1.0, 0.49, 0.18), vec3(0.18, 0.15, 0.32), night);
    color = mix(color, cloudColor, cloud * 0.40);
    return vec4(color, 1.0);
}


uniform vec2 u_Resolution;
uniform vec2 u_SunPosition;
uniform vec3 u_SunColor;
uniform float u_SunAlpha;

float circleMask(vec2 uv, vec2 center, float radius, float feather) {
    float d = length(uv - center);
    return 1.0 - smoothstep(radius - feather, radius + feather, d);
}

vec4 renderSun(vec2 uv) {
    float core = circleMask(uv, u_SunPosition, 0.112, 0.004);
    float glow = exp(-18.0 * max(length(uv - u_SunPosition) - 0.08, 0.0)) * 0.22;
    float alpha = clamp((core + glow) * u_SunAlpha, 0.0, 1.0);
    return vec4(u_SunColor * (core + glow), alpha);
}


uniform vec2 u_Resolution;
uniform vec2 u_MoonPosition;
uniform float u_MoonAlpha;
uniform float u_MoonPhase;

float circleMask(vec2 uv, vec2 center, float radius, float feather) {
    float d = length(uv - center);
    return 1.0 - smoothstep(radius - feather, radius + feather, d);
}

vec4 renderMoon(vec2 uv) {
    float disk = circleMask(uv, u_MoonPosition, 0.064, 0.004);
    float phaseOffset = mix(-0.045, 0.045, clamp(u_MoonPhase, 0.0, 1.0));
    float cut = circleMask(uv, u_MoonPosition + vec2(phaseOffset, 0.0), 0.061, 0.004);
    float phaseMask = mix(1.0 - cut, cut, step(0.5, u_MoonPhase));
    float crescent = disk * mix(phaseMask, 1.0, 1.0 - abs(u_MoonPhase * 2.0 - 1.0));
    float glow = exp(-22.0 * max(length(uv - u_MoonPosition) - 0.04, 0.0)) * 0.16;
    float alpha = clamp((crescent + glow) * u_MoonAlpha, 0.0, 1.0);
    return vec4(vec3(0.82, 0.88, 1.0) * (crescent + glow), alpha);
}

vec4 renderCombined(vec2 uv) {
    vec4 sky = renderSky(uv);
    vec4 sun = renderSun(uv);
    vec4 moon = renderMoon(uv);
    vec3 color = mix(sky.rgb, sun.rgb, sun.a);
    color = mix(color, moon.rgb, moon.a);
    return vec4(color, 1.0);
}

void main() { fragColor = renderCombined(v_Uv); }
