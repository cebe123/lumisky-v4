#version 300 es
precision mediump float;
in vec2 v_Uv;
out vec4 outColor;
uniform sampler2D u_Texture;
uniform vec2 u_ParallaxOffset;
void main() {
    outColor = texture(u_Texture, v_Uv + u_ParallaxOffset);
}
