#version 300 es
precision mediump float;
in vec2 v_Uv;
out vec4 outColor;
uniform vec3 u_TopColor;
uniform vec3 u_BottomColor;
uniform float u_DayProgress;

vec3 getSkyColor(vec3 night, vec3 sunrise, vec3 day, vec3 sunset, float progress, float height) {
    vec3 col;
    if (progress < 0.25) {
        float t = progress / 0.25;
        col = mix(night, sunrise, t);
    } else if (progress < 0.5) {
        float t = (progress - 0.25) / 0.25;
        col = mix(sunrise, day, t);
    } else if (progress < 0.75) {
        float t = (progress - 0.5) / 0.25;
        col = mix(day, sunset, t);
    } else {
        float t = (progress - 0.75) / 0.25;
        col = mix(sunset, night, t);
    }
    return mix(col * 0.5, col, height);
}

void main() {
    vec3 nightTop = vec3(0.05, 0.10, 0.24);
    vec3 nightBottom = vec3(0.12, 0.18, 0.34);
    
    vec3 sunriseTop = vec3(0.2, 0.1, 0.3);
    vec3 sunriseBottom = vec3(1.0, 0.45, 0.2);
    
    vec3 dayTop = vec3(0.1, 0.4, 0.8);
    vec3 dayBottom = vec3(0.5, 0.8, 1.0);
    
    vec3 sunsetTop = vec3(0.3, 0.1, 0.4);
    vec3 sunsetBottom = vec3(1.0, 0.3, 0.1);
    
    vec3 topColor = getSkyColor(nightTop, sunriseTop, dayTop, sunsetTop, u_DayProgress, 1.0);
    vec3 bottomColor = getSkyColor(nightBottom, sunriseBottom, dayBottom, sunsetBottom, u_DayProgress, 0.0);
    
    float hasTopOverride = step(0.001, dot(abs(u_TopColor), vec3(1.0)));
    float hasBottomOverride = step(0.001, dot(abs(u_BottomColor), vec3(1.0)));
    topColor = mix(topColor, u_TopColor, hasTopOverride * 0.3);
    bottomColor = mix(bottomColor, u_BottomColor, hasBottomOverride * 0.3);
    
    outColor = vec4(mix(bottomColor, topColor, v_Uv.y), 1.0);
}
