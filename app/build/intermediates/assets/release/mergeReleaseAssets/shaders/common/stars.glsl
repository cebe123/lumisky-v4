#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif

varying vec2 v_TexCoord;

uniform float u_Time;
uniform float u_NightAmount;
uniform float u_HorizonY;
uniform float u_StarOpacity;

float hash21(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

void main() {
    vec2 uv = v_TexCoord;
    float skyMask = smoothstep(u_HorizonY + 0.10, u_HorizonY + 0.36, uv.y);
    float nightMask = smoothstep(0.08, 0.55, u_NightAmount);
    float starField = 0.0;

    vec2 grid = uv * vec2(72.0, 110.0);
    vec2 cell = floor(grid);
    vec2 local = fract(grid) - 0.5;
    float seed = hash21(cell);
    float size = mix(0.018, 0.045, hash21(cell + 7.3));
    float core = 1.0 - smoothstep(size * 0.25, size, length(local));
    float twinkle = 0.72 + 0.28 * sin(u_Time * 1.4 + seed * 31.0);
    starField += step(0.973, seed) * core * twinkle;

    vec3 color = vec3(0.88, 0.94, 1.0) * starField * u_StarOpacity;
    float alpha = clamp(starField * skyMask * nightMask * u_StarOpacity, 0.0, 1.0);
    gl_FragColor = vec4(color * skyMask * nightMask, alpha);
}
