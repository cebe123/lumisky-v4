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

varying vec2 v_Uv;

const vec3 WORLD_FALLBACK = vec3(0.040, 0.040, 0.070);
const vec3 SKY_TOP = vec3(0.055, 0.080, 0.160);
const vec3 SKY_MID = vec3(0.350, 0.285, 0.455);
const vec3 SKY_LOW = vec3(0.735, 0.405, 0.505);
const vec3 HORIZON_GOLD = vec3(1.000, 0.760, 0.225);
const vec3 HORIZON_ORANGE = vec3(1.000, 0.355, 0.105);
const vec3 HORIZON_ROSE = vec3(0.875, 0.205, 0.245);
const vec3 NIGHT_TOP = vec3(0.030, 0.038, 0.075);
const vec3 NIGHT_HORIZON = vec3(0.205, 0.105, 0.155);

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

float resolvePeakAlignedProgress(float minute, float startMinute, float peakMinute, float endMinute) {
    if (endMinute <= startMinute) {
        return 0.5;
    }

    float safePeak = clamp(peakMinute, startMinute, endMinute);
    if (safePeak <= startMinute || safePeak >= endMinute) {
        return clamp((minute - startMinute) / (endMinute - startMinute), 0.0, 1.0);
    }

    if (minute <= safePeak) {
        float firstHalf = max(safePeak - startMinute, 1.0);
        return clamp(((minute - startMinute) / firstHalf) * 0.5, 0.0, 0.5);
    }

    float secondHalf = max(endMinute - safePeak, 1.0);
    return clamp(0.5 + ((minute - safePeak) / secondHalf) * 0.5, 0.5, 1.0);
}

vec3 agxSoftClamp(vec3 color) {
    color = max(color, vec3(0.0));
    return color / (color + vec3(0.78));
}

void main() {
    vec2 uv = v_Uv;
    float aspect = u_Resolution.x / max(u_Resolution.y, 1.0);
    float shaderAspect = max(u_AspectRatio, aspect);
    float horizonY = clamp(u_HorizonY, 0.20, 0.36);
    float skyY = clamp((uv.y - horizonY) / max(1.0 - horizonY, 0.001), 0.0, 1.0);

    float nightAmount = clamp(u_NightAmount, 0.0, 1.0);
    float dayAmount = 1.0 - (nightAmount * 0.35);
    float solarProgress = resolvePeakAlignedProgress(u_Minute, u_Sunrise, u_SolarNoon, u_Sunset);
    float sunsetBand = exp(-skyY * 4.8);
    float sunsetStrength = smoothstep(0.32, 1.0, solarProgress);
    float morningStrength = 1.0 - smoothstep(0.0, 0.42, solarProgress);

    vec3 daySky = mix(HORIZON_GOLD, HORIZON_ORANGE, smoothstep(0.0, 0.11, skyY));
    daySky = mix(daySky, SKY_LOW, smoothstep(0.08, 0.35, skyY));
    daySky = mix(daySky, SKY_MID, smoothstep(0.24, 0.68, skyY));
    daySky = mix(daySky, SKY_TOP, smoothstep(0.56, 1.0, skyY));
    daySky = mix(daySky, daySky * vec3(0.92, 0.78, 1.18), morningStrength * 0.22);
    daySky = mix(daySky, daySky * vec3(1.16, 0.88, 0.84), sunsetStrength * 0.34);

    vec3 nightSky = mix(NIGHT_HORIZON, NIGHT_TOP, pow(skyY, 0.85));
    vec3 color = mix(daySky, nightSky, nightAmount * 0.22);

    vec2 sun = vec2(clamp(u_SunPos.x, 0.0, 1.0), clamp(1.0 - u_SunPos.y, 0.0, 1.0));
    sun.y = max(sun.y, horizonY + 0.075);
    vec2 aspectUv = vec2(uv.x * aspect, uv.y);
    float glowY = mix(horizonY + 0.025, sun.y, 0.24);
    vec2 aspectSun = vec2(sun.x * aspect, glowY);

    float sunHorizonAffinity = exp(-abs(sun.y - horizonY) * 3.0);
    float sunAboveHorizon = smoothstep(horizonY - 0.035, horizonY + 0.045, sun.y);
    float visibleSun = max(u_DrawSun * (1.0 - u_IsNight), 0.72 * dayAmount * sunAboveHorizon);
    float visibleSunGlow = visibleSun * sunHorizonAffinity;
    float horizontalGlow = exp(-abs(aspectUv.x - aspectSun.x) * 1.85);
    float verticalGlow = exp(-abs(uv.y - horizonY) * 10.5);
    float hdriGlow = horizontalGlow * verticalGlow * visibleSunGlow;
    float coreGlow = exp(-(pow(aspectUv.x - aspectSun.x, 2.0) * 8.0 + pow(uv.y - glowY, 2.0) * 62.0)) * visibleSunGlow;

    color += HORIZON_ORANGE * sunsetBand * dayAmount * 0.34;
    color += HORIZON_GOLD * hdriGlow * 0.95;
    color += vec3(1.0, 0.48, 0.18) * coreGlow * 0.70;
    color += HORIZON_ROSE * exp(-skyY * 2.4) * dayAmount * 0.12;

    float ray = exp(-abs(aspectUv.x - aspectSun.x) * 3.9) *
        smoothstep(horizonY - 0.01, horizonY + 0.38, uv.y) *
        (1.0 - smoothstep(horizonY + 0.55, 1.0, uv.y));
    color += vec3(1.0, 0.455, 0.255) * ray * visibleSunGlow * 0.13;

    vec2 sunDelta = uv - sun;
    sunDelta.x *= shaderAspect;
    float sunDist = length(sunDelta);
    float sunDisk = smoothstep(0.044, 0.030, sunDist) * visibleSun;
    float sunHalo = exp(-sunDist * 8.6) * visibleSun;
    color += vec3(1.0, 0.55, 0.18) * sunHalo * 0.34;
    color = mix(color, vec3(1.0, 0.88, 0.48), sunDisk);

    float cloudAlpha = max(u_CloudAlpha, 0.0) * u_HasAtmosphere;
    vec2 wind = vec2(u_CloudOffset * 0.06 + u_Time * 0.0014, 0.0);
    vec2 highUv = vec2(uv.x * 2.05 + uv.y * 0.62, uv.y * 11.8) + wind;
    vec2 lowUv = vec2(uv.x * 2.85 - uv.y * 0.42, uv.y * 17.0) + wind * 1.35;
    float highNoise = fbm(highUv + vec2(0.0, 4.0));
    float lowNoise = fbm(lowUv + vec2(6.0, 1.5));
    float highBand = smoothstep(horizonY + 0.20, horizonY + 0.46, uv.y) *
        (1.0 - smoothstep(horizonY + 0.74, 1.0, uv.y));
    float lowBand = smoothstep(horizonY + 0.035, horizonY + 0.17, uv.y) *
        (1.0 - smoothstep(horizonY + 0.34, horizonY + 0.54, uv.y));
    float highStreak = smoothstep(0.54, 0.78, highNoise) * highBand;
    float lowStreak = smoothstep(0.47, 0.73, lowNoise) * lowBand;
    float cloudStreak = (highStreak * 0.56 + lowStreak * 1.12) * cloudAlpha;

    vec3 highCloud = mix(vec3(0.330, 0.255, 0.420), vec3(0.820, 0.260, 0.300), hdriGlow);
    vec3 lowCloud = mix(HORIZON_ROSE, vec3(1.0, 0.585, 0.315), hdriGlow + 0.25);
    color = mix(color, highCloud, highStreak * cloudAlpha * 0.32);
    color = mix(color, lowCloud, lowStreak * cloudAlpha * 0.45);

    float below = smoothstep(horizonY + 0.03, 0.0, uv.y);
    vec3 lowerMist = mix(WORLD_FALLBACK, vec3(0.355, 0.115, 0.125), dayAmount);
    color = mix(color, lowerMist, below * 0.46);
    color += HORIZON_ORANGE * exp(-abs(uv.y - (horizonY - 0.025)) * 18.0) * dayAmount * 0.16;

    float vignette = 1.0 - smoothstep(0.50, 1.08, length((uv - vec2(0.5, 0.53)) * vec2(0.88, 1.0)));
    color *= mix(0.74, 1.0, vignette);

    color = agxSoftClamp(color * 1.42);
    gl_FragColor = vec4(color, 1.0);
}
