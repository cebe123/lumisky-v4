#version 300 es
precision highp float;
in vec2 v_TexCoord;
out vec4 fragColor;
uniform vec2 u_Resolution;
uniform float u_SunRadius;
uniform float u_SunGlowRadius;
uniform float u_SunIntensity;
uniform vec2 u_Parallax;
void main(){
    vec2 p=v_TexCoord-vec2(0.5);
    p.x*=u_Resolution.x/u_Resolution.y;
    float d=length(p);
    float disc=1.0-smoothstep(u_SunRadius*0.94,u_SunRadius,d);
    float glow=(1.0-smoothstep(u_SunRadius,u_SunGlowRadius,d))*0.35;
    vec3 c=mix(vec3(1.0,0.69,0.28),vec3(1.0,0.98,0.78),disc);
    float a=clamp((disc+glow)*u_SunIntensity,0.0,1.0);
    fragColor=vec4(c*a,a);
}
