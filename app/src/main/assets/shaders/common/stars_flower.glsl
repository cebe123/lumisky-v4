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
uniform float u_HasStars;

float hash(vec2 p) {
    return fract(1e4 * sin(17.0 * p.x + p.y * 0.1) * (0.1 + abs(sin(p.y * 13.0 + p.x))));
}

float getStars(vec2 uv) {
    float theta = 0.63;
    float cs = cos(theta);
    float sn = sin(theta);
    vec2 rotatedUv = vec2(uv.x * cs - uv.y * sn, uv.x * sn + uv.y * cs);
    vec2 starGrid = rotatedUv * vec2(430.0, 360.0) + vec2(41.0, 17.0);
    vec2 starCell = floor(starGrid);
    vec2 starLocal = fract(starGrid) - 0.5;
    vec2 randomOffset = vec2(
        hash(starCell + vec2(5.0, 13.0)),
        hash(starCell + vec2(17.0, 3.0))
    ) - 0.5;
    vec2 delta = starLocal - randomOffset * 0.82;
    float presence = step(0.735, hash(starCell + vec2(29.0, 31.0)));
    float core = smoothstep(0.12, 0.0, length(delta));
    float cross = max(
        smoothstep(0.055, 0.0, abs(delta.x)) * smoothstep(0.18, 0.0, abs(delta.y)),
        smoothstep(0.055, 0.0, abs(delta.y)) * smoothstep(0.18, 0.0, abs(delta.x))
    );
    float shape = max(core, cross * 0.42);
    float microStars = step(0.88, hash(starCell + vec2(43.0, 59.0))) *
        smoothstep(0.07, 0.0, length(delta)) * 0.34;
    float phase = hash(starCell + vec2(7.0, 19.0)) * 6.28318;
    float speed = 0.52 + hash(starCell + vec2(13.0, 5.0)) * 1.18;
    float pulse = pow(sin(u_Time * speed + phase) * 0.5 + 0.5, 5.0);
    float steady = 0.74 + hash(starCell + vec2(3.0, 11.0)) * 0.34;
    float shimmer = 0.20 + pulse * 2.35;
    float selector = step(0.5, hash(starCell + vec2(23.0, 29.0)));
    return (presence * shape * mix(steady, shimmer, selector)) + microStars;
}

void main() {
    vec2 uv = v_TexCoord;
    float starVisibility = smoothstep(u_HorizonY + 0.01, u_HorizonY + 0.26, uv.y);
    float stars = getStars(uv);
    vec3 color = vec3(0.96, 0.98, 1.0) * stars * u_NightAmount * starVisibility * u_HasStars * 1.18;
    float alpha = clamp(stars * starVisibility * u_NightAmount * u_HasStars * u_StarOpacity, 0.0, 1.0);
    gl_FragColor = vec4(color, alpha);
}
