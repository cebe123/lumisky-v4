precision mediump float;

uniform float u_Time;
uniform vec2 u_Resolution;
uniform float u_TimeOfDay;
uniform sampler2D u_Texture;

const float PI = 3.14159265359;
const float HORIZON_Y = 0.5;

const vec3 C_SUN_HIGH = vec3(1.0, 1.0, 0.9);
const vec3 C_SUN_SET  = vec3(1.0, 0.3, 0.05);
const vec3 C_SUN_GONE = vec3(0.5, 0.0, 0.0);

const vec3 SKY_DAY_TOP = vec3(0.2, 0.5, 0.9);
const vec3 SKY_DAY_BOT = vec3(0.6, 0.8, 0.95);
const vec3 SKY_SET_TOP = vec3(0.1, 0.15, 0.35);
const vec3 SKY_SET_MID = vec3(0.8, 0.5, 0.3);
const vec3 SKY_SET_BOT = vec3(0.95, 0.2, 0.05);
const vec3 SKY_NIGHT_TOP = vec3(0.0, 0.0, 0.08);
const vec3 SKY_NIGHT_BOT = vec3(0.01, 0.02, 0.15);

const vec3 C_MOON_RED = vec3(0.8, 0.3, 0.1);
const vec3 C_MOON_WHITE = vec3(0.9, 0.9, 0.95);

float hash(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

float fbm(vec2 p) {
    float v = 0.0;
    float amp = 0.5;
    for (int i = 0; i < 4; i++) {
        v += noise(p) * amp;
        p *= 2.1;
        amp *= 0.5;
    }
    return v;
}

float sdSun(vec2 p, vec2 center, float r, float flattening) {
    vec2 d = p - center;
    d.y /= flattening;
    return length(d) - r;
}

vec3 getSkyColor(vec2 uv, float tCycle) {
    vec3 col = vec3(0.0);

    if (tCycle < 0.4) {
        float t = tCycle / 0.4;
        vec3 gradDay = mix(SKY_DAY_BOT, SKY_DAY_TOP, uv.y);

        float h = smoothstep(0.0, 1.0, uv.y + 0.2);
        vec3 gradSet = mix(SKY_SET_BOT, SKY_SET_MID, smoothstep(0.0, 0.4, uv.y));
        gradSet = mix(gradSet, SKY_SET_TOP, smoothstep(0.4, 1.0, uv.y));

        col = mix(gradDay, gradSet, t * t);

    } else if (tCycle < 0.6) {
        float t = (tCycle - 0.4) / 0.2;

        vec3 gradSet = mix(SKY_SET_BOT, SKY_SET_MID, smoothstep(0.0, 0.4, uv.y));
        gradSet = mix(gradSet, SKY_SET_TOP, smoothstep(0.4, 1.0, uv.y));

        vec3 gradNight = mix(SKY_NIGHT_BOT, SKY_NIGHT_TOP, uv.y);

        col = mix(gradSet, gradNight, t);

        float zodiac = exp(-4.0 * uv.y) * exp(-2.0 * abs(uv.x)) * (1.0 - t);
        col += vec3(0.3, 0.2, 0.4) * zodiac * 0.5;

    } else {
        float t = (tCycle - 0.6) / 0.4;
        vec3 gradNight = mix(SKY_NIGHT_BOT, SKY_NIGHT_TOP, uv.y);
        col = gradNight;
    }
    return col;
}

void main() {
    float aspect = u_Resolution.x / u_Resolution.y;
    vec2 uv = gl_FragCoord.xy / u_Resolution.xy;
    vec2 p = uv * 2.0 - 1.0;
    p.x *= aspect;

    vec3 color = vec3(0.0);

    vec2 sunPos = vec2(0.0, -10.0);
    vec2 moonPos = vec2(0.0, -10.0);
    vec3 sunColor = vec3(0.0);
    vec3 moonColor = vec3(0.0);
    float sunSize = 0.15;
    float moonSize = 0.025;

    if (u_TimeOfDay < 0.45) {
        float t = u_TimeOfDay / 0.4;
        sunPos.x = 0.0;

        sunPos.y = 0.8 - (t * 1.3);

        sunColor = mix(C_SUN_HIGH, C_SUN_SET, t * t);
        if (t > 0.8) sunColor = mix(sunColor, C_SUN_GONE, (t - 0.8) * 5.0);
    }

    if (u_TimeOfDay > 0.55) {
        float t = (u_TimeOfDay - 0.55) / 0.45;
        float horizonP = (HORIZON_Y * 2.0) - 1.0;
        moonPos.x = 0.0;
        moonPos.y = (horizonP - 0.2) + (t * 1.0);

        moonColor = mix(C_MOON_RED, C_MOON_WHITE, smoothstep(0.0, 0.6, t));
        moonSize = 0.05 * (1.0 + 0.3 * exp(-2.0 * max(0.0, moonPos.y - horizonP)));
    }

    color = getSkyColor(uv, u_TimeOfDay);

    if (u_TimeOfDay < 0.5) {
        float distCenter = length(p.x);
        float flattening = 1.0;
        float d = sdSun(p, sunPos, sunSize, flattening);

        float sunGlow = exp(-20.0 * d) * 2.0;
        color += sunColor * sunGlow * 0.1;

        float s = smoothstep(0.005, 0.0, d);
        color = mix(color, sunColor, s);
    }

    if (u_TimeOfDay > 0.5) {
        float d = length(p - moonPos) - moonSize;

        vec2 moonUV = (p - moonPos) / moonSize;
        float craters = fbm(moonUV * 4.0);
        vec3 lunarSurf = mix(moonColor * 0.8, moonColor * 1.2, craters);

        float halo = exp(-8.0 * d) * 0.5;
        color += moonColor * halo * 0.2;

        float m = smoothstep(0.005, 0.0, d);
        color = mix(color, lunarSurf, m);

        if (u_TimeOfDay > 0.6) {
            float starThresh = 0.98;
            float n = hash(uv * 10.0 + vec2(0.0, 1.0));
            float twinkle = noise(uv * 50.0 + u_Time);
            if (n > starThresh) {
                float brightness = (n - starThresh) / (1.0 - starThresh);
                color += vec3(brightness) * twinkle * (1.0 - halo);
            }
        }
    }

    float terrainMaxHeight = 1.02;
    float v = 1.0 - (uv.y / terrainMaxHeight);

    if (v >= 0.0 && v <= 1.0) {
        vec2 texUV = vec2(uv.x, v);
        vec4 desertSample = texture2D(u_Texture, texUV);
        vec3 desertColor = desertSample.rgb;

        float brightness = 1.0;
        if (u_TimeOfDay < 0.4) {
            brightness = 1.0 - (u_TimeOfDay / 0.4) * 0.6;
        } else if (u_TimeOfDay < 0.6) {
            float t = (u_TimeOfDay - 0.4) / 0.2;
            brightness = 0.4 - t * 0.15;
        } else {
            float t = (u_TimeOfDay - 0.6) / 0.4;
            brightness = 0.25 + t * 0.1;
        }

        vec3 nightTint = vec3(0.05, 0.05, 0.15);
        if (u_TimeOfDay >= 0.4) {
            float nightFactor = smoothstep(0.4, 0.6, u_TimeOfDay);
            desertColor = mix(desertColor, desertColor * nightTint * 4.0, nightFactor);
        }
        desertColor *= brightness;

        if (u_TimeOfDay < 0.5) {
            float sunDir = max(0.0, dot(normalize(vec3(p.x - sunPos.x, p.y - sunPos.y, 1.0)), vec3(0.0, 1.0, 0.0)));
            desertColor += sunColor * sunDir * 0.1 * brightness;
        }

        float edgeFade = smoothstep(0.0, 0.015, v);

        float finalMask = desertSample.a * edgeFade;

        color = mix(color, desertColor, finalMask);
    }

    gl_FragColor = vec4(color, 1.0);
}
