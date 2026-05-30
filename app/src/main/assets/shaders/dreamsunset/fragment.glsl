#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif

uniform vec2 u_Resolution;
uniform float u_Time;
uniform vec2 u_SunPos;
uniform float u_AspectRatio;
uniform float u_DrawSun;
uniform float u_IsNight;
uniform float u_NightAmount;
uniform float u_Minute;
uniform float u_Sunrise;
uniform float u_Sunset;
uniform float u_SolarNoon;
uniform float u_CloudOffset;
uniform float u_CloudAlpha;
uniform float u_HorizonY;
uniform float u_HasAtmosphere;
uniform sampler2D u_Texture;

varying vec2 v_Uv;

const float SOURCE_ASPECT = 2.0;
const vec3 FALLBACK_SKY_TOP = vec3(0.38, 0.04, 0.10);
const vec3 FALLBACK_SKY_HORIZON = vec3(0.95, 0.22, 0.08);
const vec3 FALLBACK_WATER = vec3(0.045, 0.006, 0.010);

float hash21(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);

    float a = hash21(i);
    float b = hash21(i + vec2(1.0, 0.0));
    float c = hash21(i + vec2(0.0, 1.0));
    float d = hash21(i + vec2(1.0, 1.0));

    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

float fbm(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 4; i++) {
        v += noise(p) * a;
        p = p * 2.03 + vec2(17.3, 9.7);
        a *= 0.5;
    }
    return v;
}

vec2 coverUv(vec2 uv, float screenAspect) {
    vec2 scale = vec2(1.0);
    if (screenAspect < SOURCE_ASPECT) {
        scale.x = screenAspect / SOURCE_ASPECT;
    } else {
        scale.y = SOURCE_ASPECT / screenAspect;
    }
    return (uv - vec2(0.5)) * scale + vec2(0.5);
}

vec3 fallbackColor(vec2 uv, float horizonY) {
    float skyY = clamp((uv.y - horizonY) / max(1.0 - horizonY, 0.001), 0.0, 1.0);
    float waterY = clamp((horizonY - uv.y) / max(horizonY, 0.001), 0.0, 1.0);
    vec3 sky = mix(FALLBACK_SKY_HORIZON, FALLBACK_SKY_TOP, smoothstep(0.0, 1.0, skyY));
    vec3 water = mix(vec3(0.11, 0.012, 0.018), FALLBACK_WATER, smoothstep(0.0, 1.0, waterY));
    return mix(water, sky, step(horizonY, uv.y));
}

float triangleWave(float value) {
    float f = fract(value);
    return 1.0 - abs((f * 2.0) - 1.0);
}

float circleMask(vec2 uv, vec2 center, float radius, float screenAspect) {
    vec2 d = uv - center;
    d.x *= screenAspect;
    return 1.0 - smoothstep(radius * 0.86, radius, length(d));
}

vec3 dynamicSkyColor(float skyY, float sunLift, float nightAmount) {
    vec3 lowTop = vec3(0.38, 0.035, 0.090);
    vec3 highTop = vec3(0.165, 0.150, 0.310);
    vec3 lowMid = vec3(0.78, 0.110, 0.085);
    vec3 highMid = vec3(0.82, 0.250, 0.160);
    vec3 lowHorizon = vec3(1.00, 0.285, 0.085);
    vec3 highHorizon = vec3(1.00, 0.520, 0.220);

    vec3 top = mix(lowTop, highTop, sunLift);
    vec3 mid = mix(lowMid, highMid, sunLift);
    vec3 horizon = mix(lowHorizon, highHorizon, sunLift);

    vec3 lower = mix(horizon, mid, smoothstep(0.0, 0.55, skyY));
    vec3 upper = mix(mid, top, smoothstep(0.35, 1.0, skyY));
    vec3 sky = mix(lower, upper, smoothstep(0.18, 0.82, skyY));
    vec3 night = mix(vec3(0.12, 0.015, 0.045), vec3(0.030, 0.006, 0.030), skyY);
    return mix(sky, night, nightAmount * 0.45);
}

vec3 agxSoftClamp(vec3 color) {
    color = max(color, vec3(0.0));
    return color / (color + vec3(0.78));
}

void main() {
    vec2 uv = v_Uv;
    float screenAspect = u_Resolution.x / max(u_Resolution.y, 1.0);
    float horizonY = clamp(1.0 - u_HorizonY, 0.46, 0.54);
    float waterMask = 1.0 - smoothstep(horizonY - 0.020, horizonY + 0.010, uv.y);
    float skyMask = 1.0 - waterMask;
    float skyY = clamp((uv.y - horizonY) / max(1.0 - horizonY, 0.001), 0.0, 1.0);
    float waterDepth = clamp((horizonY - uv.y) / max(horizonY, 0.001), 0.0, 1.0);
    float nightAmount = clamp(u_NightAmount, 0.0, 1.0);

    float sunLift = triangleWave(u_Time / 18.0);
    float animatedSunRadius = mix(0.076, 0.052, sunLift);
    vec2 engineSun = vec2(clamp(u_SunPos.x, 0.0, 1.0), clamp(1.0 - u_SunPos.y, 0.0, 1.0));
    vec2 animatedSun = vec2(engineSun.x, mix(horizonY + animatedSunRadius * 0.62, 0.84, sunLift));

    vec2 texUv = coverUv(uv, screenAspect);
    float wave = fbm(vec2(uv.x * 20.0, waterDepth * 30.0 + u_Time * 0.018));
    float ripple = sin(waterDepth * 90.0 + wave * 5.2 + u_Time * 0.030) * 0.5 + 0.5;
    float shimmer = smoothstep(0.50, 0.95, ripple) * smoothstep(0.08, 0.92, waterDepth);
    float horizontalFocus = exp(-pow(abs((uv.x - animatedSun.x) * max(screenAspect, 0.45)) / mix(0.030, 0.180, 1.0 - waterDepth), 2.0));

    vec2 waterTexUv = texUv;
    waterTexUv.x += (wave - 0.5) * 0.010 * waterMask;
    waterTexUv.y += (ripple - 0.5) * 0.006 * waterMask;
    vec2 sampleUv = mix(texUv, waterTexUv, waterMask * 0.42);
    sampleUv = clamp(sampleUv, vec2(0.001), vec2(0.999));

    vec4 photoSample = texture2D(u_Texture, vec2(sampleUv.x, 1.0 - sampleUv.y));
    vec3 color = mix(fallbackColor(uv, horizonY), photoSample.rgb, step(0.05, photoSample.a));

    vec3 dynamicSky = dynamicSkyColor(skyY, sunLift, nightAmount);
    float luma = dot(color, vec3(0.299, 0.587, 0.114));
    vec3 detailedSky = dynamicSky * (0.48 + luma * 1.18);

    vec2 bakedSunCenter = vec2(0.5, horizonY + 0.065);
    float bakedCore = circleMask(uv, bakedSunCenter, 0.125, screenAspect);
    float bakedGlowDist = length(vec2((uv.x - bakedSunCenter.x) * screenAspect, uv.y - bakedSunCenter.y));
    float bakedGlow = 1.0 - smoothstep(0.085, 0.260, bakedGlowDist);
    color = mix(color, detailedSky + vec3(1.0, 0.34, 0.08) * bakedGlow * 0.18, skyMask * (0.38 + bakedCore * 0.48));

    vec3 reflectionBoost = vec3(1.0, 0.32, 0.045) * horizontalFocus * shimmer * exp(-waterDepth * 2.2);
    color += reflectionBoost * waterMask * mix(0.10, 0.035, sunLift);

    float horizonLine = exp(-abs(uv.y - horizonY) * 220.0);
    color = mix(color, vec3(0.055, 0.006, 0.010), horizonLine * 0.30);

    vec2 sunDelta = uv - animatedSun;
    sunDelta.x *= screenAspect;
    float sunDist = length(sunDelta);
    float aboveHorizon = smoothstep(horizonY - 0.006, horizonY + 0.020, uv.y);
    float visibleSun = mix(1.0, 0.35, u_IsNight) * max(0.55, u_DrawSun);
    float sunHalo = exp(-sunDist * 7.5) * skyMask * visibleSun;
    float sunDisk = (1.0 - smoothstep(animatedSunRadius * 0.90, animatedSunRadius, sunDist)) * aboveHorizon * visibleSun;
    vec3 sunColor = mix(vec3(1.0, 0.82, 0.38), vec3(1.0, 0.94, 0.70), sunLift);
    color += vec3(1.0, 0.36, 0.08) * sunHalo * mix(0.34, 0.16, sunLift);
    color = mix(color, sunColor, sunDisk);

    vec3 nightTint = mix(color, color * vec3(0.45, 0.22, 0.32), nightAmount * 0.18);
    color = mix(color, nightTint, nightAmount * 0.35);

    float vignette = 1.0 - smoothstep(0.54, 1.08, length((uv - vec2(0.5, 0.50)) * vec2(0.86, 1.0)));
    color *= mix(0.78, 1.0, vignette);

    color = clamp(color * 1.03, 0.0, 1.0);
    gl_FragColor = vec4(color, 1.0);
}
