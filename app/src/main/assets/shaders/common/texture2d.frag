#version 300 es
precision mediump float;
in vec2 v_Uv;
out vec4 outColor;
uniform sampler2D u_Texture;
uniform vec2 u_ParallaxOffset;
void main() {
    vec2 safeScale = vec2(1.0) - (2.0 * abs(u_ParallaxOffset));
    vec2 safeUv = vec2(0.5) + ((v_Uv - vec2(0.5)) * safeScale) + u_ParallaxOffset;
    outColor = texture(u_Texture, safeUv);
}
