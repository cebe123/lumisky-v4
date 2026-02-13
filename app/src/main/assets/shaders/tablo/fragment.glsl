precision mediump float;

uniform vec2 u_SunPos;
uniform vec3 u_SunColor;
uniform float u_AspectRatio;
uniform float u_DrawSun;

uniform sampler2D u_SunTexture;
uniform sampler2D u_MoonTexture;
uniform sampler2D u_ForegroundTexture;

uniform vec2 u_MoonPos;
uniform float u_IsNight;
uniform float u_Minute;
uniform sampler2D u_CloudTexture;
uniform float u_CloudOffset;
uniform float u_CloudAlpha;

uniform float u_Sunset;
uniform float u_Sunrise;
uniform float u_NightAmount;

varying vec2 v_TexCoord;

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

void main() {
    float distFromSun = distance(v_TexCoord, u_SunPos);
    float verticalFade = clamp(v_TexCoord.y * 1.1, 0.0, 1.0);
    float radialFade = smoothstep(0.0, 0.5, distFromSun);
    float dayProgress = u_SunPos.x;

    vec3 morningSky = vec3(1.0, 0.45, 0.2);
    vec3 noonSky = vec3(0.5, 0.75, 0.95);
    vec3 preEveningSky = vec3(0.5, 0.7, 0.9);
    vec3 eveningSky = vec3(0.9, 0.6, 0.4);
    vec3 coldSky = vec3(0.1, 0.2, 0.35);
    vec3 nightSky = vec3(0.005, 0.001, 0.010);

    vec3 baseSky;
    if (dayProgress < 0.5) {
        baseSky = mix(morningSky, noonSky, smoothstep(0.0, 0.5, dayProgress));
    } else if (dayProgress < 0.85) {
        baseSky = mix(noonSky, preEveningSky, smoothstep(0.5, 0.85, dayProgress));
    } else {
        baseSky = mix(preEveningSky, eveningSky, smoothstep(0.85, 1.0, dayProgress));
    }

    vec3 sky = mix(baseSky, coldSky, radialFade * verticalFade);

    float nightAmount = u_NightAmount;

    sky = mix(sky, nightSky, nightAmount);

    float fog = smoothstep(1.0, 0.4, v_TexCoord.y);
    vec3 fogColor = vec3(0.1, 0.2, 0.3);
    sky = mix(sky, fogColor, fog * nightAmount * 0.35);

    float morningFogAmount = 0.0;
    if (u_Minute >= u_Sunrise && u_Minute <= u_Sunrise + 20.0) {
        morningFogAmount = smoothstep(u_Sunrise, u_Sunrise + 20.0, u_Minute);
    }
    float morningFog = smoothstep(1.0, 0.3, v_TexCoord.y);
    vec3 morningFogColor = vec3(0.6, 0.65, 0.7);
    sky = mix(sky, morningFogColor, morningFogAmount * morningFog * 0.35);

    vec4 fg = texture2D(u_ForegroundTexture, v_TexCoord);

    float horizonY = 0.61;

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

    vec3 finalComp = fg.rgb;

    float horizonClip = smoothstep(horizonY + 0.02, horizonY, v_TexCoord.y);

    vec4 sunOnly = vec4(0.0);
    if (u_DrawSun > 0.5 && u_IsNight < 0.5) {
        float sunBaseSize = 0.324;

        vec2 sunUV = (v_TexCoord - effectiveSunPos);
        sunUV.y /= -u_AspectRatio;
        sunUV = sunUV / sunBaseSize + vec2(0.5);

        sunOnly = texture2D(u_SunTexture, sunUV);
        float mask = circleMask(v_TexCoord, effectiveSunPos, sunBaseSize * 0.6);
        sunOnly *= mask;
        sunOnly.rgb *= u_SunColor;

        float glow = softGlow(v_TexCoord, effectiveSunPos, 0.5) * 0.4;
        sunOnly.rgb += u_SunColor * glow;
        sunOnly.a = max(sunOnly.a, glow);

        sunOnly.a *= horizonClip;
    }

    vec3 moonOnly = vec3(0.0);
    float moonAlpha = 0.0;
    if (u_IsNight > 0.5) {
        vec2 moonDelta = v_TexCoord - effectiveMoonPos;
        moonDelta.y /= u_AspectRatio;
        vec2 moonUV = moonDelta / (0.2 * 1.15) + vec2(0.5);
        vec4 moonTex = texture2D(u_MoonTexture, moonUV);
        float moonMask = circleMask(v_TexCoord, effectiveMoonPos, 0.16 * 1.15);
        moonTex *= moonMask;

        float glow = softGlow(v_TexCoord, effectiveMoonPos, 0.35) * 0.3;
        vec3 glowColor = vec3(0.9, 0.9, 1.0);
        moonTex.rgb += glowColor * glow;

        moonOnly = moonTex.rgb;
        moonAlpha = clamp(moonTex.a + glow, 0.0, 1.0);

        moonAlpha *= horizonClip;
    }

    finalComp = mix(finalComp, sunOnly.rgb, sunOnly.a);
    finalComp = mix(finalComp, moonOnly, moonAlpha);

    gl_FragColor = vec4(finalComp, 1.0);
}
