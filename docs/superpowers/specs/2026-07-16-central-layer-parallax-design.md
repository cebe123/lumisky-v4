# Merkezi Katman Parallax Tasarımı

## Amaç

Parallax etkinleştirilmiş bir wallpaper içindeki aktif katmanlara, manifestteki katman sırasına göre otomatik derinlik vermek. En arkadaki katman çok hafif, en öndeki katman en güçlü hareket eder. Wallpaper ve katman değerleri manifestten değiştirilebilir kalır.

## Kapsam

- Sistem yalnız `parallax.enabled=true` olan wallpaperlarda devreye girer.
- Otomatik sıralama `CompiledLayerGraph.layersByIndex` içindeki aktif katmanların çizim sırasını (`renderPass`, `zIndex`, declaration order) kullanır.
- Birden fazla katmanda varsayılan depth doğrusal olarak `0.05..1.0` aralığına dağıtılır.
- Tek katmanlı wallpaper tam giriş (`1.0`) alır; shader kendi alt katman katsayılarını uygulayabilir.
- Bir katmanda `parallax.depth` açıkça verilmişse otomatik değer yerine bu değer kullanılır ve `0..1` aralığına sıkıştırılır.
- Wallpaper seviyesindeki `maxOffsetX`, `maxOffsetY` ve `smoothing` global hareket miktarı ve yumuşatmayı override etmeye devam eder.

## Mimari

Saf bir `LayerParallaxDepthResolver`, scene oluşturulurken her compiled layer için nihai depth değerini hesaplar. `SceneFactory`, layer registry'ye parsed modeli değiştirmeden, nihai depth'i taşıyan bir `LayerDefinition.copy(...)` verir. Hesaplama scene hazırlanırken bir kez yapılır; render/update hot path içinde koleksiyon, JSON veya index hesabı yapılmaz.

`TextureLayer` ve `ShaderLayer` mevcut `parallaxDepth` çarpanını kullanmayı sürdürür. `TimeSliceTextureLayer` içindeki sabit sıfır kaldırılır ve aynı nihai depth uygulanır. GL çağrıları yalnız mevcut render thread yolunda kalır.

## Samurai ve Castle

`samurai_fuji_twilight` katmanlarında manuel depth bulunmadığı için merkezi dağılım otomatik uygulanır: zaman dilimli gökyüzü `0.05`, ara katmanlar kademeli, samurai katmanı `1.0` olur.

`sky` (Castle Fantasy) tek bir shader layer içerir. Bu katman `depth=1.0` override alır. Shader içindeki üst bulut, alt bulut ve kale katsayıları korunur; böylece shaderın kendi arka-ön sıralaması merkezi giriş üzerinde çalışır.

## Güvenlik ve Performans

- Depth her zaman `0..1` aralığında tutulur.
- Görünmez wallpaper için mevcut render/sensor kapısı değişmez.
- Parallax komutları mevcut latest-wins mailbox ve GL-thread render yolunu kullanır.
- Yeni dependency, per-frame allocation veya wallpaper'a özel renderer koşulu eklenmez.

## Doğrulama

- Resolver: ilk/orta/son index dağılımı, tek katman ve explicit override birim testleri.
- SceneFactory: resolved definition'ın registry'ye aktarılması testi.
- TimeSliceTextureLayer: sabit sıfır yerine layer depth kullandığını doğrulayan test.
- `validateWallpaperDefinitions`, ilgili unit testler ve `assembleDebug`.
- Bağlı cihazda Samurai ve Castle için eğim sırasında arka katmanın en az, ön katmanın en çok hareket ettiğinin görsel kontrolü.
