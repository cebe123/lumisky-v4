#version 300 es
precision highp float;
in vec2 v_TexCoord;
out vec4 fragColor;
uniform vec2 u_Resolution;
uniform float u_DayProgress;
uniform float u_Sunrise;
uniform float u_Sunset;
uniform float u_TwilightWidth;
uniform float u_SkyStyleMix;
uniform float u_HorizonGlowIntensity;
uniform float u_ColorThemeMix;
vec3 gradient(vec2 uv) {
    float horizon = pow(1.0 - abs(uv.y - 0.56), 2.2);
    vec3 nightTop = vec3(0.035,0.055,0.16);
    vec3 nightBottom = vec3(0.20,0.12,0.27);
    vec3 dayTop = vec3(0.75,0.16,0.15);
    vec3 dayBottom = vec3(1.0,0.55,0.20);
    float daylight = smoothstep(u_Sunrise-u_TwilightWidth,u_Sunrise+u_TwilightWidth,u_DayProgress)
                   * (1.0-smoothstep(u_Sunset-u_TwilightWidth,u_Sunset+u_TwilightWidth,u_DayProgress));
    vec3 top = mix(nightTop, dayTop, daylight);
    vec3 bottom = mix(nightBottom, dayBottom, daylight);
    vec3 c = mix(bottom, top, smoothstep(0.0,1.0,uv.y));
    c += vec3(1.0,0.33,0.08)*horizon*u_HorizonGlowIntensity*daylight;
    return c;
}
void main(){ fragColor=vec4(gradient(v_TexCoord),1.0); }
