#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif

uniform vec2 u_SunPos;
uniform vec3 u_SunColor;
uniform float u_AspectRatio;
uniform float u_DrawSun;
uniform float u_Time;

uniform sampler2D u_SunTexture;
uniform sampler2D u_MoonTexture;
uniform sampler2D u_ForegroundTexture;

uniform vec2 u_MoonPos;
uniform float u_IsNight;
uniform float u_Minute;
uniform float u_Sunset;
uniform float u_Sunrise;
uniform float u_SolarNoon;
uniform float u_HorizonY;

varying vec2 v_TexCoord;
varying vec2 v_Uv;

const float MINUTES_PER_DAY = 1440.0;
const float SKY_HORIZON_CYCLE = 0.4;

const vec3 SKY_DAY_TOP = vec3(0.2, 0.5, 0.9);
const vec3 SKY_DAY_BOT = vec3(0.6, 0.8, 0.95);
const vec3 SKY_SET_TOP = vec3(0.2, 0.1, 0.4);
const vec3 SKY_SET_MID = vec3(0.8, 0.4, 0.2);
const vec3 SKY_SET_BOT = vec3(1.0, 0.3, 0.1);
const vec3 SKY_NIGHT_TOP = vec3(0.0, 0.0, 0.028);
const vec3 SKY_NIGHT_BOT = vec3(0.014, 0.026, 0.072);

float hash(vec2 p) {
    return fract(1e4 * sin(17.0 * p.x + p.y * 0.1) * (0.1 + abs(sin(p.y * 13.0 + p.x))));
}

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

float circleMask(vec2 uv, vec2 center, float radius) {
    vec2 delta = uv - center;
    delta.y /= u_AspectRatio;
    return smoothstep(radius, radius * 0.8, length(delta));
}

float softGlow(vec2 uv, vec2 center, float radius) {
    vec2 delta = uv - center;
    delta.y /= u_AspectRatio;
    float dist = length(delta);
    return pow(max(0.0, 1.0 - dist / radius), 2.5);
}

float outerHalo(vec2 uv, vec2 center, float innerRadius, float outerRadius) {
    return max(softGlow(uv, center, outerRadius) - softGlow(uv, center, innerRadius), 0.0);
}

float resolveDayBlend() {
    float transitionDuration = 60.0;
    return smoothstep(u_Sunrise - transitionDuration, u_Sunrise + transitionDuration, u_Minute) *
        (1.0 - smoothstep(u_Sunset - transitionDuration, u_Sunset + transitionDuration, u_Minute));
}

float resolveWarriorSkyCycle() {
    if (u_Sunset <= u_Sunrise) {
        return 0.0;
    }

    float minute = mod(u_Minute + MINUTES_PER_DAY, MINUTES_PER_DAY);
    float solarNoon = clamp(u_SolarNoon, u_Sunrise, u_Sunset);

    if (minute >= u_Sunrise && minute <= solarNoon && solarNoon > u_Sunrise) {
        float progress = (minute - u_Sunrise) / (solarNoon - u_Sunrise);
        return SKY_HORIZON_CYCLE * (1.0 - progress);
    }

    if (minute >= solarNoon && minute <= u_Sunset && u_Sunset > solarNoon) {
        float progress = (minute - solarNoon) / (u_Sunset - solarNoon);
        return SKY_HORIZON_CYCLE * progress;
    }

    float minutesSinceSunset = minute - u_Sunset;
    if (minutesSinceSunset < 0.0) {
        minutesSinceSunset += MINUTES_PER_DAY;
    }
    float nightDuration = max((MINUTES_PER_DAY - u_Sunset) + u_Sunrise, 1.0);
    float progress = clamp(minutesSinceSunset / nightDuration, 0.0, 1.0);
    return SKY_HORIZON_CYCLE + (progress * (1.0 - SKY_HORIZON_CYCLE));
}

vec3 warriorSkyColor(vec2 uv, float cycle) {
    if (cycle < 0.4) {
        float t = cycle / 0.4;
        vec3 gradDay = mix(SKY_DAY_BOT, SKY_DAY_TOP, uv.y);
        vec3 gradSet = mix(SKY_SET_BOT, SKY_SET_MID, smoothstep(0.0, 0.4, uv.y));
        gradSet = mix(gradSet, SKY_SET_TOP, smoothstep(0.4, 1.0, uv.y));
        return mix(gradDay, gradSet, t * t);
    }

    if (cycle < 0.6) {
        float t = (cycle - 0.4) / 0.2;
        vec3 gradSet = mix(SKY_SET_BOT, SKY_SET_MID, smoothstep(0.0, 0.4, uv.y));
        gradSet = mix(gradSet, SKY_SET_TOP, smoothstep(0.4, 1.0, uv.y));
        vec3 gradNight = mix(SKY_NIGHT_BOT, SKY_NIGHT_TOP, uv.y);
        return mix(gradSet, gradNight, t);
    }

    return mix(SKY_NIGHT_BOT, SKY_NIGHT_TOP, uv.y);
}

float getStars(vec2 uv) {
    float theta = 0.785;
    float cs = cos(theta);
    float sn = sin(theta);
    vec2 rotatedUV = vec2(uv.x * cs - uv.y * sn, uv.x * sn + uv.y * cs);

    vec2 starGrid = rotatedUV * 300.0 + vec2(43.0, 19.0);
    float starNoise = noise(starGrid);
    float stars = step(0.90, starNoise);
    vec2 starCell = floor(starGrid);

    float starPhase = hash(starCell + vec2(5.0, 17.0)) * 6.28;
    float speed = 0.45 + hash(starCell + vec2(7.0, 13.0)) * 0.7;
    float wave = sin(u_Time * speed + starPhase) * 0.5 + 0.5;
    float shimmer = sin(u_Time * (speed * 1.9) + starPhase * 1.7) * 0.5 + 0.5;
    float pulse = pow(wave, 4.2);

    float twinkleSelector = step(0.80, hash(starCell + vec2(11.0, 29.0)));
    float steadyBrightness = 0.62 + hash(starCell + vec2(2.0, 23.0)) * 0.34;
    float twinkleBrightness = 0.34 + pulse * 1.55 + shimmer * pulse * 0.18;
    float brightness = mix(steadyBrightness, twinkleBrightness, twinkleSelector);
    return stars * brightness;
}

vec4 sampleForegroundSoftened(vec2 uv) {
    vec2 fgUv = vec2((uv.x - 0.5) * 0.92 + 0.5, uv.y);
    vec4 fg = texture2D(u_ForegroundTexture, fgUv);
    vec4 fgNear = texture2D(u_ForegroundTexture, vec2(fgUv.x, min(fgUv.y + 0.010, 1.0)));
    vec4 fgDeep = texture2D(u_ForegroundTexture, vec2(fgUv.x, min(fgUv.y + 0.026, 1.0)));
    float detailMix = smoothstep(0.10, 0.52, fg.a);
    float softAlpha = fg.a * smoothstep(0.12, 0.46, fg.a);
    vec3 edgeColor = mix(fgDeep.rgb, fgNear.rgb, 0.55);
    vec3 softenedColor = mix(edgeColor, fg.rgb, detailMix);
    softenedColor = mix(softenedColor, edgeColor, (1.0 - detailMix) * 0.14);
    return vec4(softenedColor, softAlpha);
}

void main() {
    vec2 uv = v_TexCoord;
    vec2 skyUv = v_Uv;
    vec4 fg = sampleForegroundSoftened(uv);
    float horizonY = clamp(u_HorizonY, 0.0, 1.0);
    float skyHorizonY = 1.0 - horizonY;

    vec2 effectiveSunPos = u_SunPos;
    effectiveSunPos.x -= 0.18;
    effectiveSunPos.y -= 0.07;
    if (effectiveSunPos.y < horizonY) {
        effectiveSunPos.y += (horizonY - effectiveSunPos.y) * 0.20;
    }

    vec2 effectiveMoonPos = u_MoonPos;
    effectiveMoonPos.x -= 0.18;
    effectiveMoonPos.y -= 0.07;
    if (effectiveMoonPos.y < horizonY) {
        effectiveMoonPos.y += (horizonY - effectiveMoonPos.y) * 0.20;
    }

    float dayBlend = resolveDayBlend();
    float nightBlend = 1.0 - dayBlend;
    vec3 skyWithCelestial = warriorSkyColor(skyUv, resolveWarriorSkyCycle());
    skyWithCelestial *= (1.0 - nightBlend * 0.22);

    float starVis = smoothstep(skyHorizonY - 0.01, skyHorizonY + 0.30, skyUv.y);
    float starField = getStars(skyUv);
    skyWithCelestial += vec3(0.96, 0.98, 1.0) * starField * nightBlend * starVis * 0.74;

    float horizonClip = 1.0 - smoothstep(horizonY - 0.005, horizonY + 0.035, uv.y);
    float foregroundFreeMask = 1.0 - smoothstep(0.02, 0.76, fg.a);
    float boundaryAtmosphere = 1.0 - smoothstep(0.06, 0.22, fg.a);

    vec4 sunOnly = vec4(0.0);
    if (u_DrawSun > 0.5 && u_IsNight < 0.5) {
        float sunBaseSize = 0.324;
        vec2 sunUV = (uv - effectiveSunPos);
        sunUV.y /= -u_AspectRatio;
        sunUV = sunUV / sunBaseSize + vec2(0.5);

        sunOnly = texture2D(u_SunTexture, sunUV);
        float mask = circleMask(uv, effectiveSunPos, sunBaseSize * 0.6);
        sunOnly *= mask;
        sunOnly.rgb *= u_SunColor;
        float sunZenithSoftening = 1.0 - smoothstep(0.18, 0.52, effectiveSunPos.y);
        sunOnly.rgb *= mix(1.0, 0.95, sunZenithSoftening);
        sunOnly.rgb += vec3(1.0, 0.84, 0.48) * smoothstep(0.18, 0.72, sunOnly.a) * 0.08;
        sunOnly.a = clamp(sunOnly.a, 0.0, 1.0);
        sunOnly.a *= horizonClip;

        float sunOuterHalo = outerHalo(uv, effectiveSunPos, 0.34, 1.18);
        float sunFarHalo = outerHalo(uv, effectiveSunPos, 0.58, 1.46);
        skyWithCelestial += mix(vec3(1.0, 0.72, 0.28), u_SunColor, 0.52) *
            (sunOuterHalo * 0.34 * horizonClip);
        skyWithCelestial += mix(vec3(1.0, 0.78, 0.34), u_SunColor, 0.36) *
            (sunFarHalo * 0.18 * horizonClip);
    }

    vec3 moonOnly = vec3(0.0);
    float moonAlpha = 0.0;
    if (u_IsNight > 0.5) {
        vec2 moonDelta = uv - effectiveMoonPos;
        moonDelta.y /= u_AspectRatio;
        vec2 moonUV = moonDelta / (0.2 * 1.15) + vec2(0.5);
        vec4 moonTex = texture2D(u_MoonTexture, moonUV);
        float moonMask = circleMask(uv, effectiveMoonPos, 0.16 * 1.15);
        moonTex *= moonMask;

        moonOnly = moonTex.rgb;
        moonAlpha = clamp(moonTex.a, 0.0, 1.0);
        moonAlpha *= horizonClip;

        float moonOuterHalo = outerHalo(uv, effectiveMoonPos, 0.24, 0.84);
        float moonFarHalo = outerHalo(uv, effectiveMoonPos, 0.48, 1.10);
        skyWithCelestial += vec3(0.50, 0.66, 1.0) *
            (moonOuterHalo * 0.14 * horizonClip);
        skyWithCelestial += vec3(0.42, 0.56, 0.94) *
            (moonFarHalo * 0.05 * horizonClip);
    }

    skyWithCelestial = mix(skyWithCelestial, sunOnly.rgb, sunOnly.a);
    skyWithCelestial = mix(skyWithCelestial, moonOnly, moonAlpha);

    vec3 fgColor = fg.rgb;
    float nightTextureFade = smoothstep(0.12, 1.0, nightBlend) * smoothstep(0.10, 0.86, fg.a);
    fgColor = mix(fgColor, fgColor * vec3(0.74, 0.78, 0.84), nightTextureFade * 0.34);
    float topNightShade = nightBlend * pow(1.0 - uv.y, 1.15) * smoothstep(0.18, 0.72, fg.a);
    fgColor = mix(fgColor, fgColor * vec3(0.26, 0.29, 0.38), topNightShade * 0.72);
    fgColor = mix(fgColor, skyWithCelestial, boundaryAtmosphere * 0.88);

    float fgOpacity = fg.a;
    vec3 finalComp = mix(skyWithCelestial, fgColor, fgOpacity);
    finalComp += mix(vec3(1.0, 0.72, 0.30), u_SunColor, 0.52) *
        (outerHalo(uv, effectiveSunPos, 0.42, 1.58) * 0.28 * foregroundFreeMask * horizonClip * (1.0 - u_IsNight));
    finalComp += vec3(0.50, 0.64, 1.0) *
        (outerHalo(uv, effectiveMoonPos, 0.32, 1.16) * 0.10 * foregroundFreeMask * horizonClip * u_IsNight);
    gl_FragColor = vec4(finalComp, 1.0);
}
