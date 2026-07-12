precision highp float;
attribute vec2 a_Position;
attribute vec2 a_TexCoord;
varying vec2 v_TexCoord;
uniform vec2 u_Parallax;
void main() {
    v_TexCoord = a_TexCoord + u_Parallax;
    gl_Position = vec4(a_Position, 0.0, 1.0);
}
