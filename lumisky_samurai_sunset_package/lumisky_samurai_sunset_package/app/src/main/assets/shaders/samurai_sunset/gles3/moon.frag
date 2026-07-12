#version 300 es
precision highp float;
in vec2 v_Uv;
out vec4 fragColor;

uniform vec2 u_Resolution;
uniform vec2 u_MoonPosition;
uniform float u_MoonAlpha;
uniform float u_MoonPhase;

float circleMask(vec2 uv, vec2 center, float radius, float feather) {
    float d = length(uv - center);
    return 1.0 - smoothstep(radius - feather, radius + feather, d);
}

vec4 renderMoon(vec2 uv) {
    float disk = circleMask(uv, u_MoonPosition, 0.064, 0.004);
    float phaseOffset = mix(-0.045, 0.045, clamp(u_MoonPhase, 0.0, 1.0));
    float cut = circleMask(uv, u_MoonPosition + vec2(phaseOffset, 0.0), 0.061, 0.004);
    float phaseMask = mix(1.0 - cut, cut, step(0.5, u_MoonPhase));
    float crescent = disk * mix(phaseMask, 1.0, 1.0 - abs(u_MoonPhase * 2.0 - 1.0));
    float glow = exp(-22.0 * max(length(uv - u_MoonPosition) - 0.04, 0.0)) * 0.16;
    float alpha = clamp((crescent + glow) * u_MoonAlpha, 0.0, 1.0);
    return vec4(vec3(0.82, 0.88, 1.0) * (crescent + glow), alpha);
}

void main() { fragColor = renderMoon(v_Uv); }
