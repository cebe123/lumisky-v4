attribute vec2 a_Position;
varying vec2 v_Uv;

void main() {
    v_Uv = a_Position * 0.5 + 0.5;
    gl_Position = vec4(a_Position, 0.0, 1.0);
}
