precision mediump float;

uniform float u_Time;
uniform vec2 u_Resolution;
uniform float u_Minute;
uniform float u_Sunrise;
uniform float u_Sunset;

uniform vec2 u_TouchPosition;
uniform float u_TouchTime;
uniform float u_WindSpeed;

varying vec2 v_Uv;

const vec3 C_NIGHT_SKY_TOP = vec3(0.01, 0.03, 0.06);
const vec3 C_NIGHT_SKY_HORIZON = vec3(0.05, 0.12, 0.20);
const vec3 C_NIGHT_OCEAN_BASE = vec3(0.02, 0.05, 0.10);
const vec3 C_MOON_CORE = vec3(0.95, 0.95, 0.98);
const vec3 C_MOON_HALO = vec3(0.6, 0.7, 0.8);

const vec3 C_DAY_SKY_TOP = vec3(0.1, 0.4, 0.8);
const vec3 C_DAY_SKY_HORIZON = vec3(0.6, 0.8, 0.95);
const vec3 C_DAY_OCEAN_BASE = vec3(0.05, 0.2, 0.5);

const vec3 C_SUN_CORE = vec3(1.0, 0.98, 0.92);
const vec3 C_SUN_HALO = vec3(1.0, 0.55, 0.15);

const float HORIZON_Y = 0.5;
const float CELESTIAL_HORIZON = 0.35;
const float CELESTIAL_PEAK = 0.89;
const float SUN_RADIUS = 0.070;
const float MOON_RADIUS = 0.043;

float hash(vec2 p) { return fract(1e4 * sin(17.0 * p.x + p.y * 0.1) * (0.1 + abs(sin(p.y * 13.0 + p.x)))); }
float noise(vec2 x) {
    vec2 i = floor(x);
    vec2 f = fract(x);
    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
}
float fbm(vec2 x) {
    float v = 0.0;
    float a = 0.5;
    vec2 shift = vec2(100);
    mat2 rot = mat2(cos(0.5), sin(0.5), -sin(0.5), cos(0.50));
    for (int i = 0; i < 4; ++i) {
        v += a * noise(x);
        x = rot * x * 2.0 + shift;
        a *= 0.5;
    }
    return v;
}

void main() {
    float aspectRatio = u_Resolution.x / u_Resolution.y;
    vec2 uv = v_Uv;

    float transitionDur = 60.0;
    float isDay = smoothstep(u_Sunrise - transitionDur, u_Sunrise + transitionDur, u_Minute) *
    (1.0 - smoothstep(u_Sunset - transitionDur, u_Sunset + transitionDur, u_Minute));

    float celestialY = -0.3;
    float celestialX = 0.5;
    // --- Gündüz/Gece Mantığı ---
    // 'isDay' gökyüzü arka plan renk geçişini kontrol eder (yumuşak).
    // 'isActuallyDay' Güneş/Ay çizimi için ikili (binary - keskin) anahtar görevi görür.
    // Bu, Güneş batarken Ay'ın silik bir şekilde belirmesini engeller.
    bool isActuallyDay = (u_Minute >= u_Sunrise && u_Minute <= u_Sunset);

    if (isActuallyDay) {
        float dayDuration = u_Sunset - u_Sunrise;
        float progress = (u_Minute - u_Sunrise) / dayDuration;
        float amplitude = CELESTIAL_PEAK - CELESTIAL_HORIZON;

        celestialY = CELESTIAL_HORIZON + sin(progress * 3.14159) * amplitude;
        celestialX = 0.5;
    } else {
        float nightStart = u_Sunset;
        float nightEnd = u_Sunrise + 1440.0;
        float current = u_Minute;
        if (current < u_Sunrise) current += 1440.0;
        float nightDuration = nightEnd - nightStart;
        float progress = (current - nightStart) / nightDuration;
        float amplitude = CELESTIAL_PEAK - CELESTIAL_HORIZON;

        celestialY = CELESTIAL_HORIZON + sin(progress * 3.14159) * amplitude;
        celestialX = 0.5;
    }

    vec2 bodyPos = vec2(celestialX, celestialY);

    vec2 aspectUV = uv;
    aspectUV.x *= aspectRatio;
    vec2 aspectBodyPos = bodyPos;
    aspectBodyPos.x *= aspectRatio;

    vec3 skyTop = mix(C_NIGHT_SKY_TOP, C_DAY_SKY_TOP, isDay);

    vec3 sunsetOrange = vec3(1.0, 0.5, 0.2);

    float sunLowIntensity = 1.0 - smoothstep(HORIZON_Y, HORIZON_Y + 0.35, celestialY);

    float distToSunX = abs(aspectUV.x - aspectBodyPos.x);
    float horizontalGlow = exp(-distToSunX * 2.0);

    // --- Ufuk Parıltısı Mantığı ---
    // Turuncu güneş parıltısını SADECE gündüzse etkinleştir.
    float horizonGlowMask = isActuallyDay ? (sunLowIntensity * horizontalGlow) : 0.0;

    vec3 dayHorizonBase = C_DAY_SKY_HORIZON;
    dayHorizonBase = mix(dayHorizonBase, sunsetOrange, horizonGlowMask * 0.9);

    vec3 moonGlowColor = vec3(0.4, 0.6, 0.9);
    float moonLowIntensity = 1.0 - smoothstep(HORIZON_Y, HORIZON_Y + 0.6, celestialY);

    vec3 nightHorizonBase = C_NIGHT_SKY_HORIZON;
    // Mavi ay parıltısını SADECE geceyse etkinleştir.
    float nightGlowMask = (!isActuallyDay) ? (moonLowIntensity * horizontalGlow) : 0.0;
    nightHorizonBase = mix(nightHorizonBase, moonGlowColor, nightGlowMask * 0.5);


    vec3 skyHorizon = mix(nightHorizonBase, dayHorizonBase, isDay);

    vec3 bodyCore = isActuallyDay ? C_SUN_CORE : C_MOON_CORE;
    vec3 bodyHalo = isActuallyDay ? C_SUN_HALO : C_MOON_HALO;
    vec3 oceanBase = mix(C_NIGHT_OCEAN_BASE, C_DAY_OCEAN_BASE, isDay);

    vec3 finalColor = vec3(0.0);
    float radius = isActuallyDay ? SUN_RADIUS : MOON_RADIUS;

    if (uv.y >= HORIZON_Y) {
        float skyT = (uv.y - HORIZON_Y) / (1.0 - HORIZON_Y);
        skyT = pow(skyT, 0.8);

        finalColor = mix(skyHorizon, skyTop, skyT);

        float verticalGlow = exp(-(uv.y - HORIZON_Y) * 5.0) * (isActuallyDay ? horizonGlowMask : nightGlowMask) * 0.35;
        finalColor += (isActuallyDay ? sunsetOrange : moonGlowColor) * verticalGlow;

        vec2 cloudUV = uv;
        cloudUV.x += u_Time * 0.005 * u_WindSpeed;
        float cloudNoise = fbm(cloudUV * 3.0);
        float cloudMask = smoothstep(0.4, 0.7, cloudNoise);
        cloudMask *= smoothstep(HORIZON_Y, HORIZON_Y + 0.2, uv.y);
        vec3 cloudColor = mix(vec3(0.35), vec3(1.0), isDay * 0.8 + 0.2);
        finalColor = mix(finalColor, cloudColor, cloudMask * 0.4);

        float dist = distance(aspectUV, aspectBodyPos);
        float disk = smoothstep(radius, radius - 0.01, dist);

        float haloStr = exp(-dist * 6.0) * 0.55;
        finalColor += bodyHalo * haloStr;
        finalColor = mix(finalColor, bodyCore, disk);

    } else {
        float oceanT = (HORIZON_Y - uv.y) / HORIZON_Y;

        vec3 oceanTop = skyHorizon;

        vec3 base = mix(oceanTop, oceanBase, pow(oceanT, 0.6));

        float horizonFog = smoothstep(0.1, 0.0, oceanT);
        finalColor = mix(base, skyHorizon, horizonFog * 0.5);

        float stretchFactor = 1.4;
        float reflectedY = HORIZON_Y + (HORIZON_Y - uv.y) / stretchFactor;

        float ripple = sin((uv.y * 150.0) - (u_Time * 0.8)) * 0.003;
        ripple += cos((uv.y * 50.0) + (u_Time * 0.5)) * 0.002;
        ripple *= smoothstep(0.0, 0.2, oceanT) * (1.0 + oceanT * 2.0);

        vec2 reflectUV = vec2(uv.x + ripple, reflectedY);
        reflectUV.x *= aspectRatio;

        float distR = distance(reflectUV, aspectBodyPos);

        float reflectionShape = smoothstep(radius + 0.02, radius - 0.04, distR);

        float bodyVis = smoothstep(HORIZON_Y - 0.1, HORIZON_Y + 0.1, celestialY);
        float bottomMask = smoothstep(1.0, 0.6, oceanT);
        float depthFade = exp(-oceanT * 0.6);

        vec3 reflectionCol = mix(bodyHalo, bodyCore, 0.6);

        vec3 glowReflection = (isActuallyDay ? sunsetOrange : moonGlowColor) * (isActuallyDay ? horizonGlowMask : nightGlowMask) * 0.6 * exp(-oceanT * 2.5);

        finalColor += glowReflection;

        finalColor = mix(finalColor, reflectionCol, reflectionShape * 0.5 * bodyVis * depthFade * bottomMask);
    }

    float vignette = 1.0 - length(v_Uv - 0.5) * 0.35;
    finalColor *= vignette;

    gl_FragColor = vec4(finalColor, 1.0);
}
