#version 300 es
precision highp float;
in vec2 v_TexCoord;
out vec4 fragColor;
uniform vec2 u_Resolution;
uniform float u_MoonRadius;
uniform float u_MoonGlowRadius;
uniform float u_MoonIntensity;
uniform float u_MoonPhase;
uniform vec2 u_Parallax;
void main(){
    vec2 p=v_TexCoord-vec2(0.5);
    p.x*=u_Resolution.x/u_Resolution.y;
    float d=length(p);
    float disc=1.0-smoothstep(u_MoonRadius*0.95,u_MoonRadius,d);
    float offset=(1.0-clamp(u_MoonPhase,0.0,1.0))*u_MoonRadius*1.55;
    float shadow=1.0-smoothstep(u_MoonRadius*0.95,u_MoonRadius,length(p-vec2(offset,0.0)));
    float lit=disc*(u_MoonPhase>0.98?1.0:1.0-shadow);
    float glow=(1.0-smoothstep(u_MoonRadius,u_MoonGlowRadius,d))*0.22;
    float a=clamp((lit+glow)*u_MoonIntensity,0.0,1.0);
    vec3 c=mix(vec3(0.70,0.76,0.93),vec3(0.94,0.93,0.84),lit);
    fragColor=vec4(c*a,a);
}
