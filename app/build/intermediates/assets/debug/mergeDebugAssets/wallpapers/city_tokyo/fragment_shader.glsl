#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif

uniform float u_Time;
uniform vec2 u_Resolution;
uniform float u_Minute;
uniform float u_Sunrise;
uniform float u_Sunset;
uniform float u_SolarNoon;
uniform sampler2D u_Texture; // City Mask Texture
uniform float u_CloudOffset;
uniform float u_CloudAlpha;
uniform float u_HasStars;

uniform vec2 u_TouchPosition;
uniform float u_TouchTime;
uniform float u_WindSpeed;
// --- Şehir Ayarlama Değişkenleri (Uniforms) ---
// Bunlar Kotlin kodundan (ThemeRepository) şehir bazlı ince ayar yapılmasını sağlar
uniform float u_CityZoom;             // Şehir genişliğini ekrana sığacak şekilde ölçekler (örn: 0.85)
uniform float u_CityVerticalOffset;   // Şehri ufuk çizgisine göre yukarı/aşağı taşır (örn: 0.04)
uniform float u_CityHorizontalOffset; // Şehri sola/sağa kaydırır (örn: 0.05)

varying vec2 v_Uv;

const vec3 C_NIGHT_SKY_TOP = vec3(0.005, 0.015, 0.04); // DAHA KOYU (Eskiden 0.01, 0.03, 0.06)
const vec3 C_NIGHT_SKY_HORIZON = vec3(0.02, 0.06, 0.12); // DAHA KOYU (Eskiden 0.05, 0.12, 0.20)
const vec3 C_NIGHT_OCEAN_BASE = vec3(0.01, 0.02, 0.05); // DAHA KOYU (Eskiden 0.02, 0.05, 0.10)
const vec3 C_MOON_CORE = vec3(0.95, 0.95, 0.98);
const vec3 C_MOON_HALO = vec3(0.6, 0.7, 0.8);

const vec3 C_DAY_SKY_TOP = vec3(0.1, 0.4, 0.8);
const vec3 C_DAY_SKY_HORIZON = vec3(0.6, 0.8, 0.95);
const vec3 C_DAY_OCEAN_BASE = vec3(0.05, 0.2, 0.5);

const vec3 C_SUN_CORE = vec3(1.0, 0.98, 0.92);
const vec3 C_SUN_HALO = vec3(1.0, 0.55, 0.15);

const float HORIZON_Y = 0.48; // Slightly lower for better fit
const float CELESTIAL_HORIZON = 0.35;
const float CELESTIAL_PEAK = 0.89;
const float SUN_RADIUS = 0.070;
const float MOON_RADIUS = 0.038; // %10 Küçültüldü (0.043 -> 0.038)

float hash(vec2 p) { return fract(1e4 * sin(17.0 * p.x + p.y * 0.1) * (0.1 + abs(sin(p.y * 13.0 + p.x)))); }
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
float fbm(vec2 x) {
    float v = 0.0;
    float a = 0.5;
    vec2 shift = vec2(100);
    mat2 rot = mat2(cos(0.5), sin(0.5), -sin(0.5), cos(0.50));
    for (int i = 0; i < 4; ++i) {
        v += a * noise(x);
        x = rot * x * 2.0 + shift;
        a *= 0.5;
    }
    return v;
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
float getStars(vec2 uv) {
    float theta = 0.63;
    float cs = cos(theta);
    float sn = sin(theta);
    vec2 rotatedUV = vec2(uv.x * cs - uv.y * sn, uv.x * sn + uv.y * cs);
    vec2 starGrid = rotatedUV * vec2(78.0, 52.0) + vec2(11.0, 23.0);
    vec2 starCell = floor(starGrid);
    vec2 starLocal = fract(starGrid) - 0.5;
    vec2 randomOffset = vec2(
        hash(starCell + vec2(5.0, 13.0)),
        hash(starCell + vec2(17.0, 3.0))
    ) - 0.5;
    vec2 delta = starLocal - randomOffset * 0.72;

    float selector = hash(starCell + vec2(29.0, 31.0));
    float presence = step(0.9979, selector);
    float largeStar = step(0.9994, selector);
    float core = 1.0 - smoothstep(0.0, mix(0.075, 0.115, largeStar), length(delta));
    float cross = max(
        1.0 - smoothstep(0.0, 0.030, abs(delta.x)) * 1.0 - smoothstep(0.0, 0.120, abs(delta.y)),
        1.0 - smoothstep(0.0, 0.030, abs(delta.y)) * 1.0 - smoothstep(0.0, 0.120, abs(delta.x))
    ) * largeStar * 0.35;
    float phase = hash(starCell + vec2(7.0, 19.0)) * 6.28318;
    float speed = 0.36 + hash(starCell + vec2(13.0, 5.0)) * 0.58;
    float pulse = pow(sin(u_Time * speed + phase) * 0.5 + 0.5, 4.0);
    float twinkle = 0.66 + pulse * 0.42;

    return presence * max(core, cross) * twinkle;
}

void main() {
    float aspectRatio = u_Resolution.x / u_Resolution.y;
    vec2 uv = v_Uv;

    // Time transition
    float transitionDur = 60.0;
    float isDay = smoothstep(u_Sunrise - transitionDur, u_Sunrise + transitionDur, u_Minute) *
    (1.0 - smoothstep(u_Sunset - transitionDur, u_Sunset + transitionDur, u_Minute));

    // Celestial positions
    float celestialY = -0.3;
    float celestialX = 0.5;

    if (u_Minute >= u_Sunrise && u_Minute <= u_Sunset) {
        float progress = resolvePeakAlignedProgress(u_Minute, u_Sunrise, u_SolarNoon, u_Sunset);
        float amplitude = CELESTIAL_PEAK - CELESTIAL_HORIZON;
        celestialY = CELESTIAL_HORIZON + sin(progress * 3.14159) * amplitude;
        celestialX = 0.5;
    } else {
        float nightStart = u_Sunset;
        float nightEnd = u_Sunrise + 1440.0;
        float current = u_Minute;
        if (current < u_Sunrise) current += 1440.0;
        float nightDuration = nightEnd - nightStart;
        float progress = (current - nightStart) / nightDuration;
        float amplitude = CELESTIAL_PEAK - CELESTIAL_HORIZON;
        celestialY = CELESTIAL_HORIZON + sin(progress * 3.14159) * amplitude;
        celestialX = 0.5;
    }

    vec2 bodyPos = vec2(celestialX, celestialY);
    vec2 aspectUV = uv;
    aspectUV.x *= aspectRatio;
    vec2 aspectBodyPos = bodyPos;
    aspectBodyPos.x *= aspectRatio;

    // Sky Colors
    vec3 skyTop = mix(C_NIGHT_SKY_TOP, C_DAY_SKY_TOP, isDay);
    vec3 sunsetOrange = vec3(1.0, 0.5, 0.2);
    float sunLowIntensity = 1.0 - smoothstep(HORIZON_Y, HORIZON_Y + 0.35, celestialY);
    float distToSunX = abs(aspectUV.x - aspectBodyPos.x);
    float horizontalGlow = exp(-distToSunX * 2.0);
    float horizonGlowMask = sunLowIntensity * horizontalGlow;

    vec3 dayHorizonBase = C_DAY_SKY_HORIZON;
    dayHorizonBase = mix(dayHorizonBase, sunsetOrange, horizonGlowMask * 0.9);

    vec3 moonGlowColor = vec3(0.4, 0.6, 0.9);
    float moonLowIntensity = 1.0 - smoothstep(HORIZON_Y, HORIZON_Y + 0.6, celestialY);
    vec3 nightHorizonBase = C_NIGHT_SKY_HORIZON;
    float nightGlowMask = moonLowIntensity * horizontalGlow;
    nightHorizonBase = mix(nightHorizonBase, moonGlowColor, nightGlowMask * 0.5);

    vec3 skyHorizon = mix(nightHorizonBase, dayHorizonBase, isDay);
    vec3 bodyCore = mix(C_MOON_CORE, C_SUN_CORE, isDay);
    vec3 bodyHalo = mix(C_MOON_HALO, C_SUN_HALO, isDay);
    vec3 oceanBase = mix(C_NIGHT_OCEAN_BASE, C_DAY_OCEAN_BASE, isDay);

    vec3 finalColor = vec3(0.0);
    float radius = isDay > 0.5 ? SUN_RADIUS : MOON_RADIUS;

    // City Mask Sampling
    vec2 maskUv;
    maskUv.x = 1.0 - uv.x; // Flip X for 180 rotation
    
    // --- Şehir Maskesi Örnekleme ve Konumlandırma ---
    // 1. Yakınlaştırma: Kenarlara ulaşmak için genişliği düzeltir (Responsive)
    float cityHorizontalZoom = 0.75; 
    
    // 2. Yatay Kaydırma: Belirli şehirleri ortalar (Londra gibi)
    float horizontalShift = 0.0; 
    maskUv.x = (maskUv.x - 0.5 - horizontalShift) * cityHorizontalZoom + 0.5;
    
    // 3. Dikey Konumlandırma: Şehir tabanını ufuk çizgisine gömer
    float cityVerticalScale = 1.6; 
    float cityVerticalOffset = 0.04; 
    maskUv.y = 1.0 - (uv.y - HORIZON_Y + cityVerticalOffset) * cityVerticalScale;
    
    vec4 mask = texture2D(u_Texture, maskUv);
    
    float isCity = 0.0;
    if (maskUv.y >= 0.0 && maskUv.y <= 1.0 && maskUv.x >= 0.0 && maskUv.x <= 1.0) {
        isCity = 1.0 - smoothstep(0.3, 0.7, mask.r); 
    }
    
    // Şehir altındaki geçişi yumuşat (ufkun altında yok olmasını sağla)
    // Şeffaflık uygulayarak keskin bitişi engelle
    float fadeStart = HORIZON_Y - 0.02;
    float fadeEnd = HORIZON_Y - 0.15;
    float bottomAlpha = smoothstep(fadeEnd, fadeStart, uv.y);
    isCity *= bottomAlpha;

    if (uv.y >= HORIZON_Y) {
        float skyT = (uv.y - HORIZON_Y) / (1.0 - HORIZON_Y);
        skyT = pow(skyT, 0.8);
        finalColor = mix(skyHorizon, skyTop, skyT);

        // Sun/Moon
        float dist = distance(aspectUV, aspectBodyPos);
        float disk = 1.0 - smoothstep(radius - 0.01, radius, dist);
        
        // Ay halesi (glow) azaltıldı
        // isDay 0 ise (gece), çarpanı düşür
        float haloIntensity = isDay > 0.5 ? 0.55 : 0.47; // Gece %20 azaltıldı (0.55 -> 0.44)
        
        float haloStr = exp(-dist * 6.0) * haloIntensity;
        finalColor += bodyHalo * haloStr;
        finalColor = mix(finalColor, bodyCore, disk);
        // Clouds
        float cloudMask = 0.0;
        if (uv.y > HORIZON_Y && u_CloudAlpha > 0.0) {
            vec2 cloudUV = uv;
            cloudUV.x += u_CloudOffset + u_Time * 0.005 * u_WindSpeed;
            float cloudNoise = fbm(cloudUV * 3.0);
            cloudMask = smoothstep(0.46, 0.78, cloudNoise);
            cloudMask *= smoothstep(HORIZON_Y, HORIZON_Y + 0.2, uv.y);
            cloudMask *= u_CloudAlpha;
        }
        
        // --- Yıldızları Uygula (Sadece Gece) ---
        // Sadece gece görünür (1.0 - isDay)
        float nightFactor = 1.0 - isDay;
        
        // Sadece gökyüzünde (ufuktan yukarıda)
        float starVis = smoothstep(HORIZON_Y, HORIZON_Y + 0.3, uv.y);
        
        // Bulutların arkasında kalsın (1.0 - cloudMask)
        float starVal = 0.0;
        if (u_HasStars > 0.0 && nightFactor > 0.0) {
            starVal = getStars(uv);
        }
        
        // Final yıldız rengi katkısı
        vec3 starColor = vec3(0.9, 0.95, 1.0) * starVal * nightFactor * starVis * (1.0 - clamp(cloudMask * 2.2, 0.0, 1.0));
        finalColor += starColor;

        vec3 cloudColor = mix(vec3(0.35), vec3(1.0), isDay * 0.8 + 0.2);
        finalColor = mix(finalColor, cloudColor, cloudMask * 0.4);

    } else {
        // Ocean / Water Reflection
        float oceanT = (HORIZON_Y - uv.y) / HORIZON_Y;
        vec3 oceanTop = skyHorizon;
        vec3 base = mix(oceanTop, oceanBase, pow(oceanT, 0.6));
        float horizonFog = 1.0 - smoothstep(0.0, 0.1, oceanT);
        finalColor = mix(base, skyHorizon, horizonFog * 0.5);

        float stretchFactor = 1.4;
        
        // Yansıma pozisyonunu yukarı taşıma
        // Yansımanın başlangıç noktasını ufka daha yakın/yukarı çekmek için ofset eklemiyoruz ama
        // Yansıyan UV'nin Y bileşenini manipüle edebiliriz.
        // reflectedY formülü: HORIZON_Y + (HORIZON_Y - uv.y) / stretchFactor
        // Bunu biraz yukarı çekmek için ufuk çizgisinden daha "uzak"mış gibi davranabiliriz veya ofset ekleyebiliriz.
        // Yansımanın "Tepe Noktasını" yukarı çekmek -> reflectionShape içindeki distR hesaplamasını etkiler.
        // reflectedY değerini KÜÇÜLTMEK (ekran koordinatında yukarı = daha küçük Y, ama burada UV.y 0 altta)
        // Bekle, UV'de 1.0 üstte, 0.0 altta. Horizon 0.48. Okyanus 0.0-0.48 arası.
        // ReflectedY: 0.48 + (0.48 - uv.y)/1.4. Örneğin uv.y=0.48 -> refY=0.48. uv.y=0.0 -> refY=0.48+0.34=0.82.
        // Tepe noktasını yukarı çekmek demek, yansımanın görsel olarak daha yukarıda (ufka daha yakın?) başlaması mı?
        // Yoksa yansımanın boyunun uzaması mı? "Yansımanın tepe noktasını daha yukarı çıkart" -> Muhtemelen yansımanın
        // gökyüzündeki cisme daha yakın olmasını istiyor (aradaki boşluğu azaltmak).
        // Cisim (Ay) yukarıda, Yansıma aşağıda. Yansımanın "başı" ufukta.
        // reflectionShape hesaplanırken kullanılan `reflectedY` değerini biraz azaltırsak (daha aşağı çekersek - hayır)
        // Cisim Yüksekte (örn 0.8). Yansıma hesaplanan Y (örn 0.6).
        // Eğer Yansımayı YUKARI (0.7'ye) çekmek istiyorsak, reflectedY değerini ARTIRMALIYIZ.
        // Daha önce 0.05 eklemiştik, yetmedi. Daha büyük bir değer deneyelim.
        // Ayrıca Yansımanın "boyunu" (stretchFactor) biraz azaltarak (uzatarak) daha geniş alana yayabiliriz.
        
        float reflOffset = 0.20; // Yansımayı iyice yukarı taşı (0.05 -> 0.20)
        float reflectedY = HORIZON_Y + (HORIZON_Y - uv.y) / stretchFactor + reflOffset;
        float ripple = sin((uv.y * 150.0) - (u_Time * 0.8)) * 0.003;
        ripple += cos((uv.y * 50.0) + (u_Time * 0.5)) * 0.002;
        ripple *= smoothstep(0.0, 0.2, oceanT) * (1.0 + oceanT * 2.0);

        vec2 reflectUV = vec2(uv.x + ripple, reflectedY);
        reflectUV.x *= aspectRatio;
        float distR = distance(reflectUV, aspectBodyPos);
        float reflectionShape = 1.0 - smoothstep(radius - 0.04, radius + 0.02, distR);
        float bodyVis = smoothstep(HORIZON_Y - 0.1, HORIZON_Y + 0.1, celestialY);
        float bottomMask = 1.0 - smoothstep(0.6, 1.0, oceanT);
        float depthFade = exp(-oceanT * 0.4);

        vec3 reflectionCol = mix(bodyHalo, bodyCore, 0.6);
        vec3 glowReflection = (isDay > 0.5 ? sunsetOrange : moonGlowColor) * horizonGlowMask * 0.6 * exp(-oceanT * 2.5);
        finalColor += glowReflection;
        finalColor = mix(finalColor, reflectionCol, reflectionShape * 0.5 * bodyVis * depthFade * bottomMask);
        
        // Şehir Yansıması (City Reflection)
        // Yansıma katsayısı ve uzatma artırıldı
        float cityRefl = 0.0;
        vec2 reflMaskUv;
        
        // Yansıma dalgalanması (ripple)
        reflMaskUv.x = 1.0 - (uv.x + ripple); 
        reflMaskUv.x = (reflMaskUv.x - 0.5 - horizontalShift) * cityHorizontalZoom + 0.5;
        
        // Uzatma Faktörü (Stretch): 1.0 = normal, >1.0 = sıkıştırılmış, <1.0 = uzatılmış
        // Eskiden dolaylı olarak scale faktörü ile yapılıyordu, şimdi dikey ölçeği azaltarak uzatıyoruz
        reflMaskUv.y = 1.0 - (HORIZON_Y - uv.y + cityVerticalOffset) * (cityVerticalScale * 0.75); // %25 daha uzun
        
        if (reflMaskUv.y >= 0.0 && reflMaskUv.y <= 1.0 && reflMaskUv.x >= 0.0 && reflMaskUv.x <= 1.0) {
            vec4 maskR = texture2D(u_Texture, reflMaskUv);
            cityRefl = 1.0 - smoothstep(0.4, 0.6, maskR.r);
        }
        
        // Yansıma Rengi ve Görünürlük
        // depthFade azaltılarak yansımanın daha uzağa gitmesi sağlandı
        vec3 cityReflCol = mix(vec3(0.02), vec3(0.12), isDay * 0.5); // Gece yansıması biraz daha belirgin
        finalColor = mix(finalColor, cityReflCol, cityRefl * 0.5 * exp(-oceanT * 0.3)); // Görünürlük 0.3 -> 0.5, fade azaldı
    }

    // ŞEHİR SİLÜETİNİ UYGULA (Ön Plan Katmanı)
    // Gökyüzü ve Okyanustan sonra uygulanır, z-index olarak ufkun üzerindedir
    vec3 cityColor = mix(vec3(0.02, 0.03, 0.05), vec3(0.1, 0.12, 0.15), isDay * 0.3);
    finalColor = mix(finalColor, cityColor, isCity);

    // Alt Kısımda Responsive Siyah Fade (Şehirler için İyileştirilmiş)
    float bottomFade = smoothstep(0.0, 0.4, v_Uv.y);
    finalColor *= bottomFade;

    float vignette = 1.0 - length(v_Uv - 0.5) * 0.35;
    finalColor *= vignette;

    // --- Son İşleme (Post-Processing) ---
    // Optimizasyon: Tamamen siyah olan pikselleri at (Discard)
    // Ancak bottomFade sadece karartıyor, tamamen 0 olduğu yerde discard edebiliriz.
    // v_Uv 0.0'a çok yakınsa zaten siyahtır.
    
    // GEREKSİZ ÇİZİMLERİ TESPİT ET (Culling/Discard)
    if (finalColor.r < 0.001 && finalColor.g < 0.001 && finalColor.b < 0.001) {
        discard;
    }
    
    gl_FragColor = vec4(finalColor, 1.0);
}
