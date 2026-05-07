#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif

uniform vec2 u_SunPos;
uniform float u_AspectRatio;
uniform float u_DrawSun;
uniform sampler2D u_MoonTexture;
uniform sampler2D u_ForegroundTexture;
uniform vec2 u_MoonPos;
uniform float u_IsNight;
uniform float u_Minute;
uniform float u_Sunset;
uniform float u_Sunrise;
uniform float u_SolarNoon;
uniform float u_NightAmount;
uniform float u_HorizonY;
uniform float u_HasStars;
uniform float u_Time;

varying vec2 v_TexCoord;
varying vec2 v_Uv;

const vec3 MORNING_TOP = vec3(0.90, 0.30, 0.24);
const vec3 MORNING_HORIZON = vec3(1.00, 0.72, 0.48);
const vec3 NOON_TOP = vec3(0.149, 0.635, 0.973);
const vec3 NOON_HORIZON = vec3(0.76, 0.90, 1.00);
const vec3 EVENING_TOP = vec3(0.88, 0.26, 0.22);
const vec3 EVENING_HORIZON = vec3(1.00, 0.64, 0.42);
const vec3 NIGHT_TOP = vec3(0.045, 0.068, 0.118);
const vec3 NIGHT_HORIZON = vec3(0.10, 0.15, 0.24);
const vec3 SUN_CORE = vec3(1.00, 0.98, 0.92);
const vec3 SUN_HALO = vec3(1.00, 0.58, 0.20);
const vec3 MOON_HALO = vec3(0.62, 0.73, 1.00);
const float SUN_RADIUS = 0.0888;
const float MOON_RADIUS = 0.174;

float hash(vec2 p) {
	return fract(1e4 * sin(17.0 * p.x + p.y * 0.1) * (0.1 + abs(sin(p.y * 13.0 + p.x))));
}

float noise(vec2 x) {
	vec2 i = floor(x);
	vec2 f = fract(x);
	float a = hash(i);
	float b = hash(i + vec2(1.0, 0.0));
	float c = hash(i + vec2(0.0, 1.0));
	float d = hash(i + vec2(1.0, 1.0));
	vec2 u = f * f * (3.0 - 2.0 * f);
	return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
}

float resolvePeakAlignedProgress(float minute, float startMinute, float peakMinute, float endMinute) {
	if (endMinute <= startMinute) {
		return 0.5;
	}

	float safePeak = clamp(peakMinute, startMinute, endMinute);
	if (safePeak <= startMinute || safePeak >= endMinute) {
		return clamp((minute - startMinute) / (endMinute - startMinute), 0.0, 1.0);
	}

	if (minute <= safePeak) {
		float firstHalf = max(safePeak - startMinute, 1.0);
		return clamp(((minute - startMinute) / firstHalf) * 0.5, 0.0, 0.5);
	}

	float secondHalf = max(endMinute - safePeak, 1.0);
	return clamp(0.5 + ((minute - safePeak) / secondHalf) * 0.5, 0.5, 1.0);
}

float circleMask(vec2 uv, vec2 center, float radius) {
	vec2 delta = uv - center;
	delta.y /= u_AspectRatio;
	return smoothstep(radius, radius * 0.82, length(delta));
}

float softGlow(vec2 uv, vec2 center, float radius) {
	vec2 delta = uv - center;
	delta.y /= u_AspectRatio;
	float dist = length(delta);
	return pow(max(0.0, 1.0 - dist / radius), 2.6);
}

float getStars(vec2 uv) {
	float theta = 0.63;
	float cs = cos(theta);
	float sn = sin(theta);
	vec2 rotatedUv = vec2(uv.x * cs - uv.y * sn, uv.x * sn + uv.y * cs);
	vec2 starGrid = rotatedUv * vec2(430.0, 360.0) + vec2(41.0, 17.0);
	vec2 starCell = floor(starGrid);
	vec2 starLocal = fract(starGrid) - 0.5;
	vec2 randomOffset = vec2(
		hash(starCell + vec2(5.0, 13.0)),
		hash(starCell + vec2(17.0, 3.0))
	) - 0.5;
	vec2 delta = starLocal - randomOffset * 0.82;
	float presence = step(0.77, hash(starCell + vec2(29.0, 31.0)));
	float core = smoothstep(0.12, 0.0, length(delta));
	float cross = max(
		smoothstep(0.055, 0.0, abs(delta.x)) * smoothstep(0.18, 0.0, abs(delta.y)),
		smoothstep(0.055, 0.0, abs(delta.y)) * smoothstep(0.18, 0.0, abs(delta.x))
	);
	float shape = max(core, cross * 0.42);
	float phase = hash(starCell + vec2(7.0, 19.0)) * 6.28318;
	float speed = 0.52 + hash(starCell + vec2(13.0, 5.0)) * 1.18;
	float pulse = pow(sin(u_Time * speed + phase) * 0.5 + 0.5, 5.0);
	float steady = 0.74 + hash(starCell + vec2(3.0, 11.0)) * 0.34;
	float shimmer = 0.20 + pulse * 2.35;
	float selector = step(0.5, hash(starCell + vec2(23.0, 29.0)));
	return presence * shape * mix(steady, shimmer, selector);
}

vec4 sampleForegroundSoftened(vec2 uv) {
	vec4 fg = texture2D(u_ForegroundTexture, uv);
	vec4 fgNear = texture2D(u_ForegroundTexture, vec2(uv.x, min(uv.y + 0.010, 1.0)));
	vec4 fgDeep = texture2D(u_ForegroundTexture, vec2(uv.x, min(uv.y + 0.026, 1.0)));
	float detailMix = smoothstep(0.10, 0.55, fg.a);
	float softAlpha = fg.a * smoothstep(0.10, 0.42, fg.a);
	vec3 edgeColor = mix(fgDeep.rgb, fgNear.rgb, 0.55);
	vec3 softenedColor = mix(edgeColor, fg.rgb, detailMix);
	softenedColor = mix(softenedColor, edgeColor, (1.0 - detailMix) * 0.16);
	return vec4(softenedColor, softAlpha);
}

void main() {
	vec2 uv = v_TexCoord;
	vec2 skyUv = v_Uv;
	vec4 fg = sampleForegroundSoftened(uv);
	float horizonY = clamp(u_HorizonY, 0.0, 1.0);
	float skyHorizonY = 1.0 - horizonY;
	float horizonClip = 1.0 - smoothstep(horizonY - 0.010, horizonY + 0.030, uv.y);

	float solarProgress = resolvePeakAlignedProgress(u_Minute, u_Sunrise, u_SolarNoon, u_Sunset);
	vec3 dayTop = solarProgress < 0.5
		? mix(MORNING_TOP, NOON_TOP, smoothstep(0.0, 0.5, solarProgress))
		: mix(NOON_TOP, EVENING_TOP, smoothstep(0.5, 1.0, solarProgress));
	vec3 dayHorizon = solarProgress < 0.5
		? mix(MORNING_HORIZON, NOON_HORIZON, smoothstep(0.0, 0.5, solarProgress))
		: mix(NOON_HORIZON, EVENING_HORIZON, smoothstep(0.5, 1.0, solarProgress));

	float skyHeightMix = smoothstep(max(skyHorizonY, 0.0), 1.0, skyUv.y);
	vec3 sky = mix(dayHorizon, dayTop, pow(skyHeightMix, 0.86));
	vec3 nightSky = mix(NIGHT_HORIZON, NIGHT_TOP, pow(skyHeightMix, 0.90));
	sky = mix(sky, nightSky, u_NightAmount);

	float sunDistanceX = abs(uv.x - u_SunPos.x);
	float sunHorizonGlow = exp(-sunDistanceX * 4.4) * smoothstep(horizonY - 0.34, horizonY, u_SunPos.y);
	sky += SUN_HALO * sunHorizonGlow * (1.0 - u_NightAmount) * 0.24;

	float moonDistanceX = abs(uv.x - u_MoonPos.x);
	float moonHorizonGlow = exp(-moonDistanceX * 4.8) * smoothstep(horizonY - 0.34, horizonY, u_MoonPos.y);
	sky += MOON_HALO * moonHorizonGlow * u_NightAmount * 0.08;

	float starVisibility = smoothstep(skyHorizonY + 0.01, skyHorizonY + 0.26, skyUv.y);
	float stars = getStars(skyUv);
	sky += vec3(0.96, 0.98, 1.0) * stars * u_NightAmount * starVisibility * u_HasStars * 1.18;

	if (u_DrawSun > 0.5 && u_IsNight < 0.5) {
		vec2 sunDelta = uv - u_SunPos;
		sunDelta.y /= u_AspectRatio;
		float sunDist = length(sunDelta);
		float sunDisk = smoothstep(SUN_RADIUS, SUN_RADIUS - 0.012, sunDist);
		float sunHalo = exp(-sunDist * 7.2) * 0.54;
		sky += SUN_HALO * sunHalo * horizonClip;
		sky = mix(sky, SUN_CORE, sunDisk * horizonClip);
	}

	if (u_IsNight > 0.5) {
		vec2 moonDelta = uv - u_MoonPos;
		moonDelta.y /= u_AspectRatio;
		vec2 moonUv = moonDelta / MOON_RADIUS + vec2(0.5);
		vec4 moonTex = texture2D(u_MoonTexture, moonUv);
		float moonMask = circleMask(uv, u_MoonPos, MOON_RADIUS * 0.94);
		moonTex *= moonMask;
		float moonGlow = softGlow(uv, u_MoonPos, 0.312) * 0.32;
		sky += MOON_HALO * moonGlow * horizonClip;
		sky = mix(sky, moonTex.rgb, clamp(moonTex.a + moonGlow, 0.0, 1.0) * horizonClip);
	}

	float boundaryAtmosphere = 1.0 - smoothstep(0.04, 0.18, fg.a);
	vec3 fgColor = fg.rgb;
	float nightDarkening = u_NightAmount * smoothstep(0.10, 0.90, fg.a);
	fgColor = mix(fgColor, fgColor * vec3(0.76, 0.79, 0.85), nightDarkening * 0.22);
	fgColor = mix(fgColor, sky, boundaryAtmosphere * 0.84);

	vec3 finalColor = mix(sky, fgColor, fg.a);
	gl_FragColor = vec4(finalColor, 1.0);
}
