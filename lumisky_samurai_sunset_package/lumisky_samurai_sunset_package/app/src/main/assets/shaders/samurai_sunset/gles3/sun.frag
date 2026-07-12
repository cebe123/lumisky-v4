#version 300 es
precision highp float;
in vec2 v_Uv;
out vec4 fragColor;

uniform vec2 u_Resolution;
uniform vec2 u_SunPosition;
uniform vec3 u_SunColor;
uniform float u_SunAlpha;

float circleMask(vec2 uv, vec2 center, float radius, float feather) {
    float d = length(uv - center);
    return 1.0 - smoothstep(radius - feather, radius + feather, d);
}

vec4 renderSun(vec2 uv) {
    float core = circleMask(uv, u_SunPosition, 0.112, 0.004);
    float glow = exp(-18.0 * max(length(uv - u_SunPosition) - 0.08, 0.0)) * 0.22;
    float alpha = clamp((core + glow) * u_SunAlpha, 0.0, 1.0);
    return vec4(u_SunColor * (core + glow), alpha);
}

void main() { fragColor = renderSun(v_Uv); }
