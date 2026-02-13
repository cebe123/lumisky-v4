precision mediump float;

uniform float u_Time;
uniform vec2 u_Resolution;
uniform float u_TimeOfDay;
uniform sampler2D u_Texture1;
uniform sampler2D u_Texture2;

const float PI = 3.14159265359;
const float HORIZON_Y = 0.28;

const vec3 C_SUN_HIGH = vec3(1.0, 1.0, 0.8);
const vec3 C_SUN_SET  = vec3(1.0, 0.8, 0.1);
const vec3 C_SUN_ROMANTIC = vec3(1.0, 0.5, 0.2);

const vec3 C_MOON_WHITE = vec3(0.95, 0.95, 1.0);
const vec3 C_MOON_MYSTIC = vec3(0.2, 0.8, 1.0);
const vec3 C_MOON_RED = vec3(0.7, 0.2, 0.1);

const vec3 SKY_DAY_TOP = vec3(0.2, 0.5, 0.9);
const vec3 SKY_DAY_BOT = vec3(0.6, 0.8, 0.95);
const vec3 SKY_SET_TOP = vec3(0.2, 0.1, 0.4);
const vec3 SKY_SET_MID = vec3(0.8, 0.4, 0.2);
const vec3 SKY_SET_BOT = vec3(1.0, 0.3, 0.1);
const vec3 SKY_NIGHT_TOP = vec3(0.0, 0.0, 0.1);
const vec3 SKY_NIGHT_BOT = vec3(0.05, 0.1, 0.2);

vec3 adjustSaturation(vec3 color, float adjustment) {
    vec3 intensity = vec3(dot(color, vec3(0.2125, 0.7154, 0.0721)));
    return mix(intensity, color, 1.0 + adjustment);
}

vec3 getSkyColor(vec2 uv, float tCycle) {
    if (tCycle < 0.4) {
        float t = tCycle / 0.4;
        vec3 gradDay = mix(SKY_DAY_BOT, SKY_DAY_TOP, uv.y);
        vec3 gradSet = mix(SKY_SET_BOT, SKY_SET_MID, smoothstep(0.0, 0.4, uv.y));
        gradSet = mix(gradSet, SKY_SET_TOP, smoothstep(0.4, 1.0, uv.y));
        return mix(gradDay, gradSet, t * t);
    } else if (tCycle < 0.6) {
        float t = (tCycle - 0.4) / 0.2;
        vec3 gradSet = mix(SKY_SET_BOT, SKY_SET_MID, smoothstep(0.0, 0.4, uv.y));
        gradSet = mix(gradSet, SKY_SET_TOP, smoothstep(0.4, 1.0, uv.y));
        vec3 gradNight = mix(SKY_NIGHT_BOT, SKY_NIGHT_TOP, uv.y);
        return mix(gradSet, gradNight, t);
    } else {
        return mix(SKY_NIGHT_BOT, SKY_NIGHT_TOP, uv.y);
    }
}

void main() {
    float aspect = u_Resolution.x / u_Resolution.y;
    vec2 uv = gl_FragCoord.xy / u_Resolution.xy;
    vec2 p = uv * 2.0 - 1.0;
    p.x *= aspect;

    float SUN_PEAK = 0.75;
    float MOON_PEAK = 0.8;
    float DEEP_HORIZON = -1.2;
    float SUNSET_TIME = 0.4;

    float celestialMask = smoothstep(HORIZON_Y - 0.05, HORIZON_Y + 0.05, uv.y);
    vec3 color = vec3(0.0);

    if (u_TimeOfDay < SUNSET_TIME) {
        float t = u_TimeOfDay / SUNSET_TIME;

        vec2 sunPos = vec2(0.0, mix(DEEP_HORIZON, SUN_PEAK, cos(t * 1.5708)));
        vec3 sunColor;

        if (t < 0.6) {
            sunColor = mix(C_SUN_HIGH, C_SUN_SET, t/0.6);
        } else {
            sunColor = mix(C_SUN_SET, vec3(1.0, 0.5, 0.0), (t - 0.6) / 0.4);
        }

        float d = length(p - sunPos);
        float sunSize = 0.12;

        float core = smoothstep(sunSize + 0.005, sunSize - 0.005, d);
        float glow = exp(-4.0 * d) * 0.5;
        float rim = exp(-12.0 * abs(d - sunSize)) * 0.3;

        color += (sunColor * core + sunColor * glow + sunColor * rim) * celestialMask;
    }
    else {
        float t = (u_TimeOfDay - SUNSET_TIME) / (1.0 - SUNSET_TIME);

        vec2 moonPos = vec2(0.0, mix(DEEP_HORIZON, MOON_PEAK, sin(t * PI)));
        vec3 moonColor = mix(C_MOON_RED, C_MOON_WHITE, smoothstep(0.0, 0.25, t));

        float moonRadius = 0.068;
        float dMoon = length(p - moonPos);

        float core = smoothstep(moonRadius + 0.005, moonRadius - 0.005, dMoon);
        float glow = exp(-4.0 * dMoon) * 0.4;
        float halo = exp(-8.0 * abs(dMoon - moonRadius)) * 0.3;

        vec3 moonRender = moonColor * core + (C_MOON_MYSTIC * 0.8) * glow + C_MOON_MYSTIC * halo;

        color += moonRender * celestialMask;
    }

    vec3 sky = getSkyColor(uv, u_TimeOfDay);
    vec3 finalColor = sky + color;

    float mountScale = 1.0;
    float v = 1.0 - (uv.y / mountScale);

    if (v >= 0.0 && v <= 1.0) {
        float blend = 0.5 + 0.5 * sin(u_Time * 0.5);

        vec2 texUV = vec2(uv.x, v);
        vec4 sample1 = texture2D(u_Texture1, texUV);
        vec4 sample2 = texture2D(u_Texture2, texUV);
        vec4 warriorSample = mix(sample1, sample2, blend);
        vec3 warriorColor = warriorSample.rgb;

        if (u_TimeOfDay < SUNSET_TIME) {
            float t = u_TimeOfDay / SUNSET_TIME;
            float boost = smoothstep(1.0, 0.7, t);
            warriorColor = adjustSaturation(warriorColor, 0.25 * boost);
            warriorColor = (warriorColor - 0.5) * (1.0 + 0.1 * boost) + 0.5;
        }

        if (u_TimeOfDay > SUNSET_TIME) {
            float nightFactor = smoothstep(SUNSET_TIME, SUNSET_TIME + 0.2, u_TimeOfDay);
            float brightness = mix(1.0, 0.85, nightFactor);
            warriorColor *= brightness;
        }

        float r = warriorSample.r;
        float g = warriorSample.g;
        float b = warriorSample.b;

        float fireCondition = step(0.6, r) * step(0.25, g) * step(b + 0.25, r);

        float nightFactor = smoothstep(SUNSET_TIME, SUNSET_TIME + 0.2, u_TimeOfDay);
        float pulse = 1.0 + 0.1 * sin(u_Time * 5.0 + uv.y * 10.0);
        vec3 fireGlow = vec3(1.0, 0.4, 0.0);

        warriorColor += fireGlow * nightFactor * pulse * 0.4 * fireCondition;

        float cleanAlpha = smoothstep(0.6, 0.98, warriorSample.a);
        warriorColor *= smoothstep(0.3, 1.0, warriorSample.a);

        finalColor = mix(finalColor, warriorColor, cleanAlpha);
    }

    gl_FragColor = vec4(finalColor, 1.0);
}
