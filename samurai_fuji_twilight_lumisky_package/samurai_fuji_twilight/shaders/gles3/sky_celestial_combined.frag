#version 300 es
precision highp float;
in vec2 v_TexCoord;
out vec4 fragColor;
uniform sampler2D u_SliceA;
uniform sampler2D u_SliceB;
uniform vec2 u_Resolution;
uniform float u_SliceMix;
uniform float u_DayProgress;
uniform float u_Sunrise;
uniform float u_Sunset;
uniform float u_TwilightWidth;
uniform float u_StarIntensity;
uniform float u_SunRadius;
uniform float u_SunGlowRadius;
uniform float u_SunIntensity;
uniform float u_MoonRadius;
uniform float u_MoonGlowRadius;
uniform float u_MoonIntensity;
uniform float u_MoonPhase;
uniform vec2 u_Parallax;
uniform float u_SkyStyleMix;
uniform float u_CloudOpacity;
uniform float u_HorizonGlowIntensity;
uniform float u_ColorThemeMix;
uniform vec3 u_TintMulA;
uniform vec3 u_TintAddA;
uniform vec3 u_TintMulB;
uniform vec3 u_TintAddB;
float curve(float t){ return t*t*(3.0-2.0*t); }
vec3 applyTint(vec3 c, vec3 mul, vec3 add){ return clamp(c*mul+add,0.0,1.0); }
void main(){
    vec2 uv=clamp(v_TexCoord,vec2(0.0),vec2(1.0));
    vec3 a=applyTint(texture(u_SliceA,uv).rgb,u_TintMulA,u_TintAddA);
    vec3 b=applyTint(texture(u_SliceB,uv).rgb,u_TintMulB,u_TintAddB);
    float t=curve(clamp(u_SliceMix,0.0,1.0));
    vec3 c=mix(a,b,t);
    fragColor=vec4(c,1.0);
}
