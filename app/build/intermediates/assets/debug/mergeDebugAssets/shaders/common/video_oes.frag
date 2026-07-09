#version 300 es
#extension GL_OES_EGL_image_external_essl3 : require
precision mediump float;
in vec2 v_Uv;
out vec4 outColor;
uniform samplerExternalOES u_VideoTexture;
uniform mat4 u_VideoTransform;
void main() { vec2 uv = (u_VideoTransform * vec4(v_Uv, 0.0, 1.0)).xy; outColor = texture(u_VideoTexture, uv); }
