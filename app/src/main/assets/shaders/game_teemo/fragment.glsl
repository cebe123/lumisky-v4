precision mediump float;

uniform vec2 u_SunPos;
uniform vec2 u_MoonPos;
uniform float u_DrawSun;
uniform float u_IsNight;
uniform float u_NightAmount;
uniform float u_Time;
uniform float u_Minute;
uniform float u_SolarNoon;
uniform float u_AspectRatio;

uniform sampler2D u_ForegroundTexture;

varying vec2 v_TexCoord;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
}

float fbm(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 4; i++) {
        v += a * noise(p);
        p = p * 2.03 + vec2(21.3, 9.2);
        a *= 0.5;
    }
    return v;
}

float luma(vec3 c) {
    return dot(c, vec3(0.299, 0.587, 0.114));
}

float sat(vec3 c) {
    float hi = max(c.r, max(c.g, c.b));
    float lo = min(c.r, min(c.g, c.b));
    return hi - lo;
}

float softCircle(vec2 uv, vec2 center, float radius, float feather) {
    vec2 d = uv - center;
    d.x *= u_AspectRatio;
    float dist = length(d);
    return smoothstep(radius + feather, radius - feather, dist);
}

void main() {
    vec2 uv = v_TexCoord;
    vec4 fg = texture2D(u_ForegroundTexture, uv);

    float t = u_Time;
    float dayFactor = u_DrawSun * (1.0 - u_IsNight);
    float nightFactor = max(u_IsNight, u_NightAmount);

    float skyByAlpha = 1.0 - fg.a;
    float skyByColor = smoothstep(0.80, 0.98, luma(fg.rgb)) * (1.0 - smoothstep(0.10, 0.30, sat(fg.rgb)));
    float skyMask = clamp(max(skyByAlpha, skyByColor), 0.0, 1.0);

    vec3 dayTop = vec3(1.00, 0.73, 0.46);
    vec3 dayBottom = vec3(1.00, 0.54, 0.25);
    vec3 nightTop = vec3(0.06, 0.10, 0.27);
    vec3 nightBottom = vec3(0.11, 0.20, 0.43);

    float horizon = smoothstep(0.15, 0.80, uv.y);
    vec3 daySky = mix(dayBottom, dayTop, horizon);
    vec3 nightSky = mix(nightBottom, nightTop, horizon);
    vec3 sky = mix(daySky, nightSky, nightFactor);

    float sunZenithDelta = abs(u_Minute - u_SolarNoon);
    float sunZenithLock = 1.0 - smoothstep(0.0, 45.0, sunZenithDelta);
    float sunY = clamp(u_SunPos.y * 0.86 + 0.08, 0.10, 0.90);
    sunY = mix(sunY, clamp(u_SunPos.y, 0.10, 0.90), sunZenithLock);
    vec2 sunPos = vec2(0.33, sunY);
    vec2 moonPos = vec2(0.33, clamp(u_MoonPos.y * 0.86 + 0.08, 0.10, 0.90));

    float sunDisk = softCircle(uv, sunPos, 0.125, 0.010);
    float sunCore = softCircle(uv, sunPos, 0.080, 0.020);
    float sunHalo = softCircle(uv, sunPos, 0.240, 0.110);

    vec2 sunVec = uv - sunPos;
    sunVec.x *= u_AspectRatio;
    float sunDist = length(sunVec);
    float ray = pow(max(0.0, 1.0 - sunDist / 0.35), 2.5);
    float cloudBands = sin((uv.y - sunPos.y) * 180.0 + sin(uv.x * 20.0 + t * 0.15) * 3.0);
    float bandMask = smoothstep(0.25, 0.95, cloudBands) * smoothstep(0.28, 0.06, sunDist);

    vec3 sunOuter = vec3(1.0, 0.58, 0.22);
    vec3 sunInner = vec3(1.0, 0.95, 0.72);
    vec3 sunColor = mix(sunOuter, sunInner, sunCore);
    vec3 sunLayer = sunColor * sunDisk;
    sunLayer += vec3(1.0, 0.73, 0.30) * sunHalo * 0.55;
    sunLayer += vec3(1.0, 0.50, 0.24) * ray * 0.30;
    sunLayer += vec3(1.0, 0.86, 0.55) * bandMask * 0.23;
    sunLayer *= dayFactor;

    vec2 moonDelta = uv - moonPos;
    moonDelta.x *= u_AspectRatio;
    float moonDist = length(moonDelta);

    float moonOuter = smoothstep(0.170, 0.145, moonDist);
    vec2 innerCenter = moonPos + vec2(0.058, 0.0);
    vec2 moonInnerDelta = uv - innerCenter;
    moonInnerDelta.x *= u_AspectRatio;
    float moonInner = smoothstep(0.158, 0.136, length(moonInnerDelta));
    float crescent = clamp(moonOuter - moonInner, 0.0, 1.0);

    vec2 moonUv = (uv - moonPos) * vec2(u_AspectRatio, 1.0) / 0.22 + vec2(0.5);
    float moonPattern = fbm(moonUv * 5.5 + vec2(0.0, t * 0.01));
    float moonPattern2 = fbm(moonUv * 10.0 + vec2(13.0, 7.0));
    vec3 moonBase = vec3(0.53, 0.84, 0.98);
    vec3 moonBright = vec3(0.80, 0.95, 1.00);
    vec3 moonColor = mix(moonBase, moonBright, moonPattern * 0.65 + moonPattern2 * 0.35);

    float moonRim = smoothstep(0.030, 0.0, abs(moonDist - 0.146));
    float moonAura = softCircle(uv, moonPos, 0.28, 0.16);
    vec3 moonLayer = moonColor * crescent;
    moonLayer += vec3(0.64, 0.90, 1.0) * moonRim * 0.50;
    moonLayer += vec3(0.38, 0.68, 0.95) * moonAura * 0.35;
    moonLayer *= nightFactor;

    vec3 stars = vec3(0.0);
    float starField = step(0.996, noise(uv * vec2(420.0, 380.0) + vec2(2.0, 4.0)));
    float starTwinkle = 0.4 + 0.6 * sin(t * 2.0 + uv.x * 120.0 + uv.y * 80.0);
    stars += vec3(0.9, 0.95, 1.0) * starField * starTwinkle * nightFactor * smoothstep(0.25, 0.90, uv.y);

    vec3 skyWithCelestial = sky + sunLayer + moonLayer + stars;

    float sunSpill = dayFactor * smoothstep(0.9, 0.2, sunDist) * (1.0 - skyMask);
    float moonSpill = nightFactor * smoothstep(0.8, 0.25, moonDist) * (1.0 - skyMask);
    vec3 foreground = fg.rgb;
    foreground += vec3(1.0, 0.48, 0.18) * sunSpill * 0.18;
    foreground += vec3(0.45, 0.68, 0.95) * moonSpill * 0.10;

    vec3 color = mix(foreground, skyWithCelestial, skyMask);
    gl_FragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
