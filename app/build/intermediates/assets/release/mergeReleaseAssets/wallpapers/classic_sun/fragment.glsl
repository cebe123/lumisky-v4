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

uniform float u_Sunset;
uniform float u_Sunrise;
uniform float u_NightAmount;

varying vec2 v_TexCoord;

float circleMask(vec2 uv, vec2 center, float radius) {
    vec2 delta = uv - center;
    delta.y /= u_AspectRatio;
    return 1.0 - smoothstep(radius * 0.8, radius, length(delta));
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

    float fog = 1.0 - smoothstep(0.4, 1.0, v_TexCoord.y);
    vec3 fogColor = vec3(0.1, 0.2, 0.3);
    sky = mix(sky, fogColor, fog * nightAmount * 0.35);

    float morningFogAmount = 0.0;
    if (u_Minute >= u_Sunrise && u_Minute <= u_Sunrise + 20.0) {
        morningFogAmount = smoothstep(u_Sunrise, u_Sunrise + 20.0, u_Minute);
    }
    float morningFog = 1.0 - smoothstep(0.3, 1.0, v_TexCoord.y);
    vec3 morningFogColor = vec3(0.6, 0.65, 0.7);
    sky = mix(sky, morningFogColor, morningFogAmount * morningFog * 0.35);


    vec4 fg = texture2D(u_ForegroundTexture, v_TexCoord);

    if (u_IsNight > 0.5 && fg.a < 0.5) {

        vec3 C_MOON_CORE = vec3(0.95, 0.95, 0.98);
        vec3 C_MOON_HALO = vec3(0.6, 0.7, 0.8);
        float radius = 0.055;

        vec2 moonDelta = v_TexCoord - u_MoonPos;
        moonDelta.y /= u_AspectRatio;
        float moonDist = length(moonDelta);

        float disk = 1.0 - smoothstep(radius - 0.01, radius, moonDist);

        float haloStr = exp(-moonDist * 6.0) * 0.55;

        sky += C_MOON_HALO * haloStr;

        sky = mix(sky, C_MOON_CORE, disk);
    }

    
    vec4 sun = vec4(0.0);
    if (u_DrawSun > 0.5 && u_IsNight < 0.5) {
        float baseSize = 0.20;

        float sunSize = mix(baseSize, baseSize, smoothstep(0.1, 0.9, dayProgress));
        float glowRadius = mix(0.3, 0.2, smoothstep(0.0, 1.0, dayProgress));

        vec2 sunUV = (v_TexCoord - u_SunPos);
        sunUV.y /= -u_AspectRatio;
        sunUV = sunUV / sunSize + vec2(0.5);

        sun = texture2D(u_SunTexture, sunUV);
        float mask = circleMask(v_TexCoord, u_SunPos, sunSize * 0.6);
        sun *= mask;

        float glow = softGlow(v_TexCoord, u_SunPos, glowRadius) * 0.25;
        sky += u_SunColor * glow;

        sun.rgb *= u_SunColor;
    }

    vec3 result = mix(sky, sun.rgb, sun.a);

    result = mix(result, fg.rgb, fg.a);

    gl_FragColor = vec4(result, 1.0);
}
