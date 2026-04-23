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
const float cloudTopSpeed = 0.010;
const float cloudLowerSpeed = 0.0065;

vec2 legacySunPosToFragUv(vec2 sunPos) {
    return vec2(sunPos.x, 1.0 - sunPos.y);
}

vec4 sampleCastleTexture() {
    float screenAspect = iResolution.x / iResolution.y;
    vec2 castleUv;

    if (screenAspect <= castleTextureAspect) {
        float displayedHeight = screenAspect / castleTextureAspect;
        float topInset = 1.0 - displayedHeight;
        castleUv = vec2(v_TexCoord.x, (v_TexCoord.y - topInset) / displayedHeight);
    } else {
        float displayedWidth = castleTextureAspect / screenAspect;
        float sideInset = (1.0 - displayedWidth) * 0.5;
        castleUv = vec2((v_TexCoord.x - sideInset) / displayedWidth, v_TexCoord.y);
    }

    if (castleUv.x < 0.0 || castleUv.x > 1.0 || castleUv.y < 0.0 || castleUv.y > 1.0) {
        return vec4(0.0);
    }

    vec4 castle = texture2D(u_ForegroundTexture, castleUv);
    float blackKey = max(max(castle.r, castle.g), castle.b);
    float visibleMask = smoothstep(0.02, 0.08, blackKey);
    castle.a *= visibleMask;
    return castle;
}

vec4 sampleCloudTexture(float speed, float verticalOffset) {
    float screenAspect = iResolution.x / iResolution.y;
    float displayedWidth = max(cloudMinWidth, (cloudTextureAspect * cloudTargetHeight) / screenAspect);
    float displayedHeight = displayedWidth * screenAspect / cloudTextureAspect;
    float horizontalTravel = max(displayedWidth - 1.0, 0.0);
    float horizontalOffset = fract(u_Time * speed) * horizontalTravel;
    vec2 cloudUv = vec2(
        (v_TexCoord.x + horizontalOffset) / displayedWidth,
        (v_TexCoord.y - verticalOffset) / displayedHeight
    );

    if (cloudUv.x < 0.0 || cloudUv.x > 1.0 || cloudUv.y < 0.0 || cloudUv.y > 1.0) {
        return vec4(0.0);
    }

    vec4 cloud = texture2D(u_CloudTexture, cloudUv);
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

void mainImage(out vec4 fragColor, in vec2 fragCoord)
{
    vec2 uv = fragCoord / iResolution.xy;
    vec2 p = (fragCoord - 0.5 * iResolution.xy) / iResolution.y;
    float aspect = iResolution.x / iResolution.y;

    vec3 horizonColor = vec3(0.9, 0.45, 0.25);
    vec3 zenithColor = vec3(0.1, 0.25, 0.6);
    float gradientY = smoothstep(0.0, 1.2, uv.y);
    vec3 finalColor = mix(horizonColor, zenithColor, gradientY);

    vec2 gunesKonum = legacySunPosToFragUv(u_SunPos);
    vec2 posGunes = vec2(gunesKonum.x * aspect, gunesKonum.y) - vec2(0.5 * aspect, 0.5);
    float gunesMesafe = length(p - posGunes);
    float gunesGorunurluk = clamp(u_DrawSun * (1.0 - u_IsNight), 0.0, 1.0);

    float parlamaMaske = 1.0 - smoothstep(gunesBoyut, gunesBoyut + gunesParlama, gunesMesafe);
    parlamaMaske = parlamaMaske * parlamaMaske * gunesGorunurluk;

    float diskMaske = 1.0 - smoothstep(gunesBoyut - 0.005, gunesBoyut + 0.015, gunesMesafe);
    diskMaske *= gunesGorunurluk;

    finalColor += gunesHaleRenk * parlamaMaske * 1.5;
    finalColor = mix(finalColor, gunesMerkezRenk, diskMaske);

    vec4 ustBulut = applyCloudLight(
        sampleCloudTexture(cloudTopSpeed, 0.0),
        gunesMesafe,
        p,
        posGunes,
        gunesGorunurluk
    );
    finalColor = mix(finalColor, ustBulut.rgb, ustBulut.a);

    vec4 altBulut = applyCloudLight(
        sampleCloudTexture(cloudLowerSpeed, 0.18),
        gunesMesafe,
        p,
        posGunes,
        gunesGorunurluk
    );
    finalColor = mix(finalColor, altBulut.rgb, altBulut.a);

    vec4 castle = sampleCastleTexture();
    finalColor = mix(finalColor, castle.rgb, castle.a);

    fragColor = vec4(finalColor, 1.0);
}

void main() {
    vec4 color;
    mainImage(color, gl_FragCoord.xy);
    gl_FragColor = color;
}
