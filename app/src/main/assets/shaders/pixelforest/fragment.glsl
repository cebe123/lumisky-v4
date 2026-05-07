#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif

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
    float radialFade = smoothstep(0.0, 1.5, distFromSun);
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

    vec3 sky = mix(baseSky, coldSky, radialFade * verticalFade * 0.5);

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

    if (u_IsNight > 0.5 && fg.a < 0.5) {
        vec2 moonDelta = v_TexCoord - u_MoonPos;
        moonDelta.y /= u_AspectRatio;
        float moonDist = length(moonDelta);

        vec2 moonUV = moonDelta / 0.24 + vec2(0.5);

        vec4 moonTex = texture2D(u_MoonTexture, moonUV);
        float moonMask = circleMask(v_TexCoord, u_MoonPos, 0.14 * 1.2);
        moonTex *= moonMask;

        float alphaFade = smoothstep(0.085, 0.02, moonDist);
        moonTex.a *= alphaFade;

        float moonGlow = softGlow(v_TexCoord, u_MoonPos, 0.35) * 0.35;
        sky += vec3(0.9, 0.9, 1.0) * moonGlow;

        float moonEdge = smoothstep(0.05, 0.07, moonDist);
        moonTex.rgb *= 1.2;
        moonTex.rgb += moonEdge * 0.9;

        vec3 moonColor = moonTex.rgb * moonTex.a;
        sky = mix(sky, moonColor, moonTex.a);
    }

    vec4 cloud = texture2D(u_CloudTexture, v_TexCoord + vec2(u_CloudOffset, 0.0));
    if (cloud.a > 0.0) {
        vec3 cloudColor = mix(vec3(1.0), vec3(0.2, 0.2, 0.3), nightAmount);
        sky = mix(sky, cloudColor, cloud.a * u_CloudAlpha);
    }

    vec4 sun = vec4(0.0);
    if (u_DrawSun > 0.5 && u_IsNight < 0.5) {
        float baseSize = 0.315;

        float sunSize = mix(baseSize, baseSize, smoothstep(0.1, 0.9, dayProgress));
        float glowRadius = mix(0.3, 0.2, smoothstep(0.0, 1.0, dayProgress));

        vec2 sunUV = (v_TexCoord - u_SunPos);
        sunUV.y /= -u_AspectRatio;

        sunUV = -sunUV;

        sunUV = sunUV / sunSize + vec2(0.5);

        sun = texture2D(u_SunTexture, sunUV);
        float mask = circleMask(v_TexCoord, u_SunPos, sunSize * 0.6);
        sun *= mask;

        float glow = softGlow(v_TexCoord, u_SunPos, glowRadius) * 0.12;
        sky += u_SunColor * glow;

        sun.rgb *= u_SunColor;
    }

    vec3 result = mix(sky, sun.rgb, sun.a);
    
    fg = texture2D(u_ForegroundTexture, v_TexCoord);

    fg.rgb *= fg.a;
    result = mix(result, fg.rgb, fg.a);

    gl_FragColor = vec4(result, 1.0);
}
