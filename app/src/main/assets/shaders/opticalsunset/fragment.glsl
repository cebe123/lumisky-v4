precision mediump float;

varying vec2 v_TexCoord;

uniform vec2 u_SunPos;
uniform vec3 u_SunColor;
uniform float u_AspectRatio;
uniform float u_DrawSun;
uniform vec2 u_MoonPos;
uniform float u_IsNight;
uniform float u_Minute;
uniform float u_CloudOffset;
uniform float u_CloudAlpha;
uniform float u_Sunset;
uniform float u_Sunrise;
uniform float u_NightAmount;
uniform float u_HasAtmosphere;
uniform float u_HasFlare;
uniform float u_HasStars;
uniform float u_FlareIntensity;

const vec3 C_SUN_HIGH = vec3(1.0, 1.0, 0.9);
const vec3 C_SUN_SET = vec3(1.0, 0.3, 0.05);
const vec3 SKY_DAY_TOP = vec3(0.2, 0.5, 0.9);
const vec3 SKY_DAY_BOT = vec3(0.6, 0.8, 0.95);
const vec3 SKY_SET_TOP = vec3(0.1, 0.15, 0.35);
const vec3 SKY_SET_MID = vec3(0.8, 0.5, 0.3);
const vec3 SKY_SET_BOT = vec3(0.95, 0.2, 0.05);
const vec3 SKY_NIGHT_TOP = vec3(0.0, 0.0, 0.08);
const vec3 SKY_NIGHT_BOT = vec3(0.01, 0.02, 0.15);
const vec3 C_MOON_RED = vec3(0.8, 0.3, 0.1);
const vec3 C_MOON_WHITE = vec3(0.9, 0.9, 0.95);

float hash(vec2 p) {
	p = fract(p * vec2(123.34, 456.21));
	p += dot(p, p + 45.32);
	return fract(p.x * p.y);
}

float noise(vec2 p) {
	vec2 i = floor(p);
	vec2 f = fract(p);
	f = f * f * (3.0 - 2.0 * f);
	float a = hash(i);
	float b = hash(i + vec2(1.0, 0.0));
	float c = hash(i + vec2(0.0, 1.0));
	float d = hash(i + vec2(1.0, 1.0));
	return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

float fbm(vec2 p) {
	float v = 0.0;
	float amp = 0.5;
	for (int i = 0; i < 4; i++) {
		v += noise(p) * amp;
		p *= 2.1;
		amp *= 0.5;
	}
	return v;
}

void main() {
	float sunriseBlend = smoothstep(u_Sunrise - 80.0, u_Sunrise + 40.0, u_Minute);
	float sunsetBlend = smoothstep(u_Sunset - 40.0, u_Sunset + 80.0, u_Minute);
	float daylight = clamp(sunriseBlend - sunsetBlend, 0.0, 1.0);

	float sunriseBand = 1.0 - smoothstep(0.0, 120.0, abs(u_Minute - u_Sunrise));
	float sunsetBand = 1.0 - smoothstep(0.0, 140.0, abs(u_Minute - u_Sunset));
	float warmMix = max(sunriseBand, sunsetBand) * (1.0 - u_NightAmount * 0.7);

	vec3 daySky = mix(SKY_DAY_BOT, SKY_DAY_TOP, pow(v_TexCoord.y, 0.8));
	vec3 sunsetSky = mix(SKY_SET_BOT, SKY_SET_MID, smoothstep(0.0, 0.5, v_TexCoord.y));
	sunsetSky = mix(sunsetSky, SKY_SET_TOP, smoothstep(0.45, 1.0, v_TexCoord.y));
	vec3 nightSky = mix(SKY_NIGHT_BOT, SKY_NIGHT_TOP, pow(v_TexCoord.y, 0.7));

	vec3 color = mix(nightSky, daySky, daylight);
	if (u_HasAtmosphere > 0.5) {
		color = mix(color, sunsetSky, warmMix * smoothstep(0.0, 1.0, v_TexCoord.y + 0.05));
		float horizonHaze = exp(-12.0 * max(v_TexCoord.y, 0.0));
		color += vec3(0.25, 0.14, 0.08) * horizonHaze * warmMix * 0.45;
	}

	vec2 sunDelta = v_TexCoord - u_SunPos;
	sunDelta.y /= max(0.001, u_AspectRatio);
	float sunDist = length(sunDelta);
	float sunDisk = smoothstep(0.07, 0.0, sunDist);
	float sunHalo = exp(-sunDist * 10.0);
	vec3 sunColor = mix(C_SUN_HIGH, C_SUN_SET, warmMix);
	sunColor = mix(sunColor, u_SunColor, 0.5);
	float sunVisible = u_DrawSun * clamp(1.0 - u_NightAmount * 0.85, 0.0, 1.0);
	color += sunColor * sunHalo * 0.30 * sunVisible;
	color = mix(color, sunColor, sunDisk * sunVisible);
	if (u_HasFlare > 0.5 && u_FlareIntensity > 0.001) {
		float flareCore = exp(-sunDist * 20.0);
		float flareStreak = exp(-(abs(sunDelta.y) * 14.0 + abs(sunDelta.x) * 2.5));
		float flare = (flareCore + flareStreak * 0.7) * u_FlareIntensity * sunVisible;
		color += sunColor * flare * 0.55;
	}

	vec2 moonDelta = v_TexCoord - u_MoonPos;
	moonDelta.y /= max(0.001, u_AspectRatio);
	float moonDist = length(moonDelta);
	float moonDisk = smoothstep(0.05, 0.0, moonDist);
	float moonHalo = exp(-moonDist * 14.0);
	float moonVisible = clamp(max(u_NightAmount, u_IsNight), 0.0, 1.0);
	vec3 moonColor = mix(C_MOON_RED, C_MOON_WHITE, smoothstep(0.0, 1.0, moonVisible));
	color += moonColor * moonHalo * 0.24 * moonVisible;
	color = mix(color, moonColor, moonDisk * moonVisible);

	float nightStars = smoothstep(0.45, 1.0, u_NightAmount);
	vec2 starCell = floor(v_TexCoord * vec2(180.0, 110.0));
	float starSeed = hash(starCell);
	float star = step(0.9925, starSeed);
	float twinkle = 0.5 + 0.5 * sin(u_Minute * 0.12 + starSeed * 23.0);
	color += vec3(1.0) * star * twinkle * nightStars * u_HasStars * smoothstep(0.2, 1.0, v_TexCoord.y);

	if (u_HasAtmosphere > 0.5) {
		vec2 cloudUv = vec2(v_TexCoord.x * 2.0 + u_CloudOffset * 3.0, v_TexCoord.y * 3.0);
		float cloudField = fbm(cloudUv + vec2(0.0, u_Minute * 0.0005));
		float cloudMask = smoothstep(0.58, 0.78, cloudField) * smoothstep(0.12, 0.92, v_TexCoord.y);
		vec3 cloudColor = mix(vec3(0.96, 0.94, 0.90), vec3(0.28, 0.30, 0.37), u_NightAmount);
		color = mix(color, cloudColor, cloudMask * u_CloudAlpha * 0.55);
	}

	float duneHeight = 0.14
		+ 0.03 * sin(v_TexCoord.x * 6.2831853 * 1.3)
		+ 0.02 * noise(vec2(v_TexCoord.x * 12.0, 0.25));
	float groundMask = 1.0 - smoothstep(duneHeight - 0.01, duneHeight + 0.01, v_TexCoord.y);
	vec3 groundDay = vec3(0.42, 0.30, 0.18);
	vec3 groundNight = vec3(0.05, 0.05, 0.08);
	vec3 ground = mix(groundNight, groundDay, clamp(daylight * 0.85 + warmMix * 0.15, 0.0, 1.0));
	color = mix(color, ground, groundMask);

	gl_FragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
