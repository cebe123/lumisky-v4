attribute vec2 aPosition;
varying vec2 v_TexCoord;

void main() {
	v_TexCoord = (aPosition + 1.0) * 0.5;
	gl_Position = vec4(aPosition, 0.0, 1.0);
}
