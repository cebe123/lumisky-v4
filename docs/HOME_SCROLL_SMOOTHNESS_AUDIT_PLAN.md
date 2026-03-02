# Home Scroll Smoothness Audit Plan

## Fixed Regression

Home kartlarinda bazi preview'ler fokus oldugu halde oynamiyor gibi gorunuyordu.

Kok neden:

- `livePreviewReady` state'i her `isLive` degisiminde sifirlaniyordu
- ayni renderer oturumunda "ready" callback tek seferlik oldugu icin tekrar tetiklenmiyordu
- snapshot overlay opak kalip canli render'i gizliyordu

Bu turda state reset mantigi sadece yeni preview oturumu basladiginda calisacak sekilde daraltildi.

## Current Hotspots

### 1. Shader + texture prewarm cost

`RenderAssetCache.prewarmWallpaper(...)` tek wallpaper icin su yukleri yapiyor:

- fragment shader text load
- background texture load
- sun texture load
- moon texture load
- flare texture load

Home odak degistiginde ayni anda 5 adaya kadar prewarm yapiliyor.
Ilk cache miss aninda bu, scroll sonrasinda anlik I/O ve allocation pikleri olusturabilir.

### 2. Snapshot decode duplication

`SnapshotPreviewAssetLoader` su an cache yapiyor ama in-flight decode birlestirmiyor.
Bu nedenle ayni asset:

- kartin `produceState` akisi
- komsu kart `prewarm(...)` akisi

tarafindan yakin zamanda tekrar decode edilmeye calisabilir.

### 3. Forced warmup frames

`HOME_PREVIEW_ENABLE_WARMUP_FRAMES` degeri hala yuksek.
Bu, preview enable oldugunda ek GL frame zorlayip GPU uzerinde kisa sureli yuk olusturur.

### 4. Immediate render on attach

`requestRenderOnAttach = true` her preview host icin acik.
Bu, kart hazir oldugu anda en az bir GL frame ister.
Komsu kartlar icin de bu davranis aktif oldugu icin hizli kaydirmada gereksiz anlik render yukleri olusabilir.

### 5. Large category focus dispatch window

Home aktif kategori icin yukari katmana fazla sayida id gonderiyor.
Bu su an agir bir is tetiklemese de pencere gereksiz buyuk ve gelecekte ekstra is baglanirsa maliyet hizla buyur.

## Optimization Plan

### Phase 1: Low-risk reductions

1. `SnapshotPreviewAssetLoader` icine in-flight decode dedup ekle
2. `RenderAssetCache` icinde texture load icin in-flight dedup ekle
3. `HOME_PREVIEW_ENABLE_WARMUP_FRAMES` degerini cihaz testleriyle kademeli dusur
4. Snapshot `prewarm(...)` icinde zaten cache'de olan id'leri decode etmeye hic girme
5. Home focus dispatch penceresini ihtiyac kadar kucult

### Phase 2: Smarter scheduling

1. Yatay veya dikey scroll aktifken prewarm islerini ertele
2. Scroll durduktan sonra kisa bir idle debounce ile prewarm baslat
3. Performance mode `BATTERY` iken:
   - komsu preview sayisini azalt
   - prewarm yaricapini daralt
4. Live olmayan komsu preview icin gerekirse `requestRenderOnAttach` kosullu hale getir

### Phase 3: Rendering path hardening

1. Home preview icin `TextureView` tabanli host degerlendir
2. `GLSurfaceView` kaynakli katman/composition etkisini azalt
3. Snapshot -> live gecisinde tek compositing yoluna yaklas

### Phase 4: Measurement first

1. `Logger` ile su metrikleri ekle:
   - snapshot cache hit/miss
   - snapshot decode ms
   - texture cache hit/miss
   - prewarm batch suresi
   - first frame ve stable frame gecikmesi
2. Jank gorulen cihazlarda once metrik topla, sonra esikleri ayarla

## Recommended Next Implementation Order

1. In-flight dedup (`SnapshotPreviewAssetLoader`)
2. In-flight dedup (`RenderAssetCache`)
3. Warmup frame sayisini dusurup olc
4. Scroll-idle debounce ile prewarm zamanlamasini yumusat
5. Gerekirse `TextureView` fazina gec
