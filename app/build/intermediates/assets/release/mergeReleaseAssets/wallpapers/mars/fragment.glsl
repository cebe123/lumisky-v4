#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif

uniform float u_Time;
uniform vec2 u_Resolution;
uniform float u_TimeOfDay;
uniform sampler2D u_Texture;
uniform float u_Minute;
uniform float u_Sunrise;
uniform float u_Sunset;
uniform float u_DayProgress;


const float PI = 3.14159265359;
const float HORIZON_Y = 0.5;

const vec3 SKY_TOP = vec3(0.12, 0.03, 0.06);
const vec3 SKY_BOT = vec3(0.7, 0.15, 0.2);
const vec3 SKY_SET_TOP = vec3(0.08, 0.03, 0.02);
const vec3 SKY_SET_BOT = vec3(0.6, 0.1, 0.15);

const vec3 C_MOON_WHITE = vec3(0.9, 0.85, 0.8);

const vec3 SUN_TOP = vec3(1.0, 0.9, 0.4);
const vec3 SUN_BOT = vec3(0.8, 0.35, 0.5);

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
    for (int i = 0; i < 3; i++) {
        v += noise(p) * amp;
        p *= 2.1;
        amp *= 0.5;
    }
    return v;
}


float getLegacyTimeOfDay() {
    float sunrise = u_Sunrise;
    float sunset = u_Sunset;
    if (sunset <= sunrise) return u_DayProgress;
    float minute = u_Minute;
    float zenithMinute = sunrise + (sunset - sunrise) * 0.5;
    float horizon = 0.40;
    float result = 0.0;
    if (minute >= sunrise && minute <= zenithMinute) {
        float progress = (minute - sunrise) / (zenithMinute - sunrise);
        result = horizon * (1.0 - progress);
    } else if (minute >= zenithMinute && minute <= sunset) {
        float progress = (minute - zenithMinute) / (sunset - zenithMinute);
        result = horizon * progress;
    } else {
        float minutesSinceSunset = minute - sunset;
        if (minutesSinceSunset < 0.0) minutesSinceSunset += 1440.0;
        float nightDuration = (1440.0 - sunset) + sunrise;
        if (nightDuration < 1.0) nightDuration = 1.0;
        float progress = minutesSinceSunset / nightDuration;
        result = horizon + (progress * (1.0 - horizon));
    }
    return clamp(result, 0.0, 1.0);
}

float sdSun(vec2 p, vec2 center, float r) {
    return length(p - center) - r;
}

vec3 getSkyColor(vec2 uv) {
    float t = u_TimeOfDay;
    vec3 daySky = mix(SKY_BOT, SKY_TOP, uv.y);
    vec3 setSky = mix(SKY_SET_BOT, SKY_SET_TOP, uv.y);

    if (t < 0.45) {
        float p = t / 0.45;
        return mix(daySky, setSky, p * p);
    }
    return setSky;
}

void main() {
    float u_TimeOfDay = getLegacyTimeOfDay();
    float aspect = u_Resolution.x / u_Resolution.y;
    vec2 uv = gl_FragCoord.xy / u_Resolution.xy;
    vec2 p = uv * 2.0 - 1.0;
    p.x *= aspect;

    vec3 color = vec3(0.0);

    float tPhase = u_TimeOfDay;
    vec2 sunPos = vec2(0.0, -10.0);
    vec2 moonPos = vec2(0.0, -10.0);
    float sunSize = 0.11;
    float moonSize = 0.08;

    if (tPhase < 0.5) {
        float t = tPhase / 0.5;
        sunPos.y = 0.7 - (t * 1.5);
    } else {
        float t = (tPhase - 0.5) / 0.5;
        moonPos.x = 0.0;
        moonPos.y = -0.6 + sin(t * PI) * 1.4;
    }

    color = getSkyColor(uv);

    vec3 FOG_COLOR = vec3(0.55, 0.42, 0.38);
    float fogStrength = exp(-3.0 * abs(uv.y - 0.5)) * 0.95;
    color = mix(color, FOG_COLOR, fogStrength);

    float celestialMask = smoothstep(0.45, 0.52, uv.y);

    if (tPhase < 0.5) {
        float d = sdSun(p, sunPos, sunSize);

        if (d < 0.0) {
            float tGradient = smoothstep(0.2, 0.45, tPhase);
            float vSun = smoothstep(sunPos.y + sunSize, sunPos.y - sunSize, p.y);
            vec3 sunColor = mix(SUN_TOP, SUN_BOT, vSun * tGradient);

            float alpha = smoothstep(-0.95, -0.6, p.y);
            float setupFade = 1.0 - smoothstep(0.3, 0.5, tPhase);
            alpha = mix(1.0, alpha, setupFade);

            color = mix(color, sunColor, smoothstep(0.0, -0.01, d) * alpha * celestialMask);
        }

        float glow = exp(-6.0 * d) * 0.2;
        color += SUN_TOP * glow * (1.0 - tPhase * 2.0) * celestialMask;
    }

    if (tPhase >= 0.5) {
        float dMoon = length(p - moonPos) - moonSize;
        if (dMoon < 0.0) {
            float crater = fbm((p - moonPos) * 20.0);
            vec3 lunarColor = mix(C_MOON_WHITE, C_MOON_WHITE * 0.7, crater);
            color = mix(color, lunarColor, smoothstep(0.0, -0.01, dMoon) * celestialMask);
        }
        color += C_MOON_WHITE * exp(-8.0 * dMoon) * 0.15 * celestialMask;
    }

    float terrainMaxHeight = 0.77;
    float v = 1.0 - (uv.y / terrainMaxHeight);

    if (v >= 0.0 && v <= 1.0) {
        vec2 texUV = vec2(uv.x, v);
        vec4 marsSample = texture2D(u_Texture, texUV);
        vec3 marsColor = marsSample.rgb;

        float dayLight = smoothstep(0.5, 0.0, tPhase);
        float moonLight = 0.0;

        if (tPhase >= 0.5) {
            float moonElev = max(0.0, moonPos.y + 0.5);
            moonLight = moonElev * 0.25;
            moonLight += 0.15 * exp(-3.0 * length(p - moonPos));
        }

        marsColor *= (dayLight * 0.5 + 0.1 + moonLight);

        float edgeFade = smoothstep(0.0, 0.05, v);

        marsColor = mix(FOG_COLOR, marsColor, smoothstep(0.0, 0.1, v));

        float mask = marsSample.a * edgeFade * 1.3;
        color = mix(color, marsColor, mask);
    }

    gl_FragColor = vec4(color, 1.0);
}
