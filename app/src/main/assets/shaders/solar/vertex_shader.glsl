attribute vec4 a_Position;
varying vec2 v_Uv;

void main() {
    gl_Position = a_Position;
    v_Uv = a_Position.xy * 0.5 + 0.5;
}
