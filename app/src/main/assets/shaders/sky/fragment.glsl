#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif

uniform vec2 u_Resolution;
uniform float u_Time;
uniform vec2 u_SunPos;
uniform float u_DrawSun;
uniform float u_IsNight;
uniform float u_NightAmount;
uniform vec2 u_MoonPos;
uniform vec2 u_Parallax;
uniform sampler2D u_ForegroundTexture;
uniform sampler2D u_CloudTexture;

#define iResolution u_Resolution

varying vec2 v_TexCoord;

const float gunesBoyut = 0.05;
const float gunesParlama = 0.5;
const vec3 gunesMerkezRenk = vec3(1.0, 0.98, 0.9);
const vec3 gunesHaleRenk = vec3(1.0, 0.5, 0.1);
const float castleTextureAspect = 1350.0 / 2400.0;
const float cloudTextureAspect = 3344.0 / 941.0;
const float cloudTargetHeight = 0.34;
const float cloudMinWidth = 1.18;
const float cloudTopSpeed = 0.014;
const float cloudLowerSpeed = 0.020;

// Zoom factor for castle texture: larger than max tilt offset
// to prevent texture edges from being visible during parallax.
// Max horizontal offset = 0.85 * 0.12 = 0.102 => zoom 1.30 gives ~0.115 centered margin per side.
const float castleZoom = 1.30;

vec2 legacySunPosToFragUv(vec2 sunPos) {
    return vec2(sunPos.x, 1.0 - sunPos.y);
}

vec4 sampleCastleTexture(vec2 parallaxOffset) {
    float screenAspect = iResolution.x / iResolution.y;
    vec2 castleUv;

    if (screenAspect <= castleTextureAspect) {
        float displayedHeight = screenAspect / castleTextureAspect;
        float topInset = 1.0 - displayedHeight;
        castleUv = vec2(
            v_TexCoord.x + parallaxOffset.x,
            (v_TexCoord.y + parallaxOffset.y - topInset) / displayedHeight
        );
    } else {
        float displayedWidth = castleTextureAspect / screenAspect;
        float sideInset = (1.0 - displayedWidth) * 0.5;
        castleUv = vec2(
            (v_TexCoord.x + parallaxOffset.x - sideInset) / displayedWidth,
            v_TexCoord.y + parallaxOffset.y
        );
    }

    // Apply zoom: scale UV around center so all edges have equal margin.
    castleUv = vec2(
        (castleUv.x - 0.5) / castleZoom + 0.5,
        (castleUv.y - 0.5) / castleZoom + 0.5
    );

    if (castleUv.x < 0.0 || castleUv.x > 1.0 || castleUv.y < 0.0 || castleUv.y > 1.0) {
        return vec4(0.0);
    }

    vec4 castle = texture2D(u_ForegroundTexture, castleUv);
    float blackKey = max(max(castle.r, castle.g), castle.b);
    float visibleMask = smoothstep(0.02, 0.08, blackKey);
    castle.a *= visibleMask;
    return castle;
}

vec4 sampleCloudTexture(float speed, float verticalOffset, vec2 parallaxOffset) {
    float screenAspect = iResolution.x / iResolution.y;
    float displayedWidth = max(cloudMinWidth, (cloudTextureAspect * cloudTargetHeight) / screenAspect);
    float displayedHeight = displayedWidth * screenAspect / cloudTextureAspect;

    // Continuous horizontal scroll (unbounded)
    float horizontalOffset = u_Time * speed;
    float rawX = (v_TexCoord.x + horizontalOffset + parallaxOffset.x) / displayedWidth;
    float rawY = (v_TexCoord.y - verticalOffset + parallaxOffset.y) / displayedHeight;

    // Vertical bounds check only
    if (rawY < 0.0 || rawY > 1.0) {
        return vec4(0.0);
    }

    // Seamless horizontal wrapping: sample two half-period-offset
    // copies and cross-fade so the seam is never visible.
    float wx  = fract(rawX);
    float wx2 = fract(rawX + 0.5);
    float edgeDist = min(wx, 1.0 - wx);
    float blendFactor = smoothstep(0.0, 0.10, edgeDist);

    vec4 sample1 = texture2D(u_CloudTexture, vec2(wx,  rawY));
    vec4 sample2 = texture2D(u_CloudTexture, vec2(wx2, rawY));
    vec4 cloud = mix(sample2, sample1, blendFactor);

    float alphaMask = smoothstep(0.01, 0.08, cloud.a);
    cloud.a *= alphaMask;
    return cloud;
}

vec4 applyCloudLight(vec4 cloud, float gunesMesafe, vec2 p, vec2 posGunes, float gunesGorunurluk) {
    float bulutIsik = exp(-gunesMesafe * 2.4) * gunesGorunurluk;
    float bulutIsinCizgisi = exp(-abs(p.x - posGunes.x) * 4.8) *
        smoothstep(posGunes.y - 0.28, posGunes.y + 0.10, p.y) *
        gunesGorunurluk;
    float bulutAydinlanma = clamp((bulutIsik * 0.7) + (bulutIsinCizgisi * 0.3), 0.0, 1.0);
    cloud.rgb = mix(cloud.rgb, vec3(1.0, 0.93, 0.84), bulutAydinlanma * 0.22);
    return cloud;
}

vec2 resolveParallaxOffset(float horizontalScale, float verticalScale) {
    return vec2(
        u_Parallax.x * horizontalScale,
        u_Parallax.y * verticalScale
    );
}

// ─── Procedural Moon ───

float hash21(vec2 p) {
    p = fract(p * vec2(123.34, 345.45));
    p += dot(p, p + 34.345);
    return fract(p.x * p.y);
}

float moonNoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);

    float a = hash21(i);
    float b = hash21(i + vec2(1.0, 0.0));
    float c = hash21(i + vec2(0.0, 1.0));
    float d = hash21(i + vec2(1.0, 1.0));

    vec2 u = f * f * (3.0 - 2.0 * f);

    return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
}

float moonSurface(vec2 p) {
    float n = 0.0;
    n += moonNoise(p * 3.0) * 0.6;
    n += moonNoise(p * 6.0) * 0.3;
    n += moonNoise(p * 12.0) * 0.1;
    return n;
}

// Draw a procedural moon with subtle glow at the given center (in p-space).
vec3 drawMoon(vec2 p, vec2 center) {
    vec2 d = p - center;
    float r = length(d);
    float radius = 0.036;

    vec3 col = vec3(0.0);

    float mask = smoothstep(radius, radius - 0.001, r);

    if (mask > 0.0) {
        vec2 mp = d / radius;

        float z = sqrt(max(0.0, 1.0 - dot(mp, mp)));
        vec3 n = normalize(vec3(mp, z));

        vec3 lightDir = normalize(vec3(-0.7, 0.25, 0.6));

        float diff = max(dot(n, lightDir), 0.0);
        float shade = 0.3 + 0.7 * diff;

        float s = moonSurface(mp * 2.5);
        float maria = smoothstep(0.45, 0.75, s);

        float albedo = 0.8;
        albedo -= maria * 0.12;
        albedo += (s - 0.5) * 0.08;

        float limb = 1.0 - smoothstep(0.7, 1.0, length(mp));
        float finalShade = shade * (0.85 + 0.15 * limb);

        vec3 moon = vec3(0.88, 0.89, 0.9) * albedo * finalShade;
        moon *= mix(vec3(0.92, 0.95, 1.0), vec3(1.0), diff);

        col = moon * mask;
    }

    // Near glow
    float glow = smoothstep(radius + 0.08, radius, r);
    glow = pow(glow, 2.0);

    vec3 glowColor = vec3(0.6, 0.7, 1.0);
    col += glow * glowColor * 0.55;

    // Far ambient glow for extra light scatter
    float farGlow = smoothstep(radius + 0.35, radius, r);
    farGlow = pow(farGlow, 2.0);
    col += farGlow * glowColor * 0.25;

    return col;
}

void mainImage(out vec4 fragColor, in vec2 fragCoord)
{
    vec2 uv = fragCoord / iResolution.xy;
    vec2 p = (fragCoord - 0.5 * iResolution.xy) / iResolution.y;
    float aspect = iResolution.x / iResolution.y;

    vec2 gunesKonum = legacySunPosToFragUv(u_SunPos);
    vec2 posGunes = vec2(gunesKonum.x * aspect, gunesKonum.y) - vec2(0.5 * aspect, 0.5);
    float gunesMesafe = length(p - posGunes);
    float gunesGorunurluk = clamp(u_DrawSun * (1.0 - u_IsNight), 0.0, 1.0);

    vec3 horizonColor = vec3(0.9, 0.45, 0.25);
    vec3 zenithColor = vec3(0.1, 0.25, 0.6);
    float gradientY = smoothstep(0.0, 1.2, uv.y);
    vec3 finalColor = mix(horizonColor, zenithColor, gradientY);

    float parlamaMaske = 1.0 - smoothstep(gunesBoyut, gunesBoyut + gunesParlama, gunesMesafe);
    parlamaMaske = parlamaMaske * parlamaMaske * gunesGorunurluk;

    float diskMaske = 1.0 - smoothstep(gunesBoyut - 0.005, gunesBoyut + 0.015, gunesMesafe);
    diskMaske *= gunesGorunurluk;

    finalColor += gunesHaleRenk * parlamaMaske * 1.5;
    finalColor = mix(finalColor, gunesMerkezRenk, diskMaske);

    // Procedural moon (follows celestial motion, only visible at night)
    if (u_IsNight > 0.0) {
        vec2 ayKonum = legacySunPosToFragUv(u_MoonPos);
        vec2 moonCenter = vec2(ayKonum.x * aspect, ayKonum.y) - vec2(0.5 * aspect, 0.5);
        vec3 moonColor = drawMoon(p, moonCenter);
        finalColor += moonColor * u_IsNight;
    }

    // Environmental darkening at night (smooth transition)
    float envBrightness = mix(1.0, 0.15, u_NightAmount);

    vec2 ustBulutParallax = resolveParallaxOffset(0.035, 0.008);
    vec2 altBulutParallax = resolveParallaxOffset(0.060, 0.014);
    vec2 castleParallax = resolveParallaxOffset(0.12, 0.028);

    vec4 ustBulut = applyCloudLight(
        sampleCloudTexture(cloudTopSpeed, 0.0, ustBulutParallax),
        gunesMesafe,
        p,
        posGunes,
        gunesGorunurluk
    );
    ustBulut.rgb *= envBrightness;
    finalColor = mix(finalColor, ustBulut.rgb, ustBulut.a);

    vec4 altBulut = applyCloudLight(
        sampleCloudTexture(cloudLowerSpeed, 0.18, altBulutParallax),
        gunesMesafe,
        p,
        posGunes,
        gunesGorunurluk
    );
    altBulut.rgb *= envBrightness;
    finalColor = mix(finalColor, altBulut.rgb, altBulut.a);

    vec4 castle = sampleCastleTexture(castleParallax);
    castle.rgb *= envBrightness;
    finalColor = mix(finalColor, castle.rgb, castle.a);

    fragColor = vec4(finalColor, 1.0);
}

void main() {
    vec4 color;
    mainImage(color, gl_FragCoord.xy);
    gl_FragColor = color;
}
