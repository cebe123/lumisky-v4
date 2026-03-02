# Preview Render Optimization Report

## Scope

Bu rapor Home ekranindaki su maliyet alanlarini kapsar:

- `GLSurfaceView` olusturma ve ilk render
- shader/texture yukleri
- snapshot decode
- kart ustundeki canli preview oturum degisimleri

## Findings

### GLSurfaceView creation and first render

Mevcut akista fokus degistikce yeni `PreviewRendererSurfaceView` ve yeni `PreviewGlRenderer`
olusturuluyor. Bu su maliyetleri uretir:

- yeni EGL/surface olusumu
- render thread aktivasyonu
- ilk frame warmup maliyeti

### Shader and texture loads

`RenderAssetCache` cache kullaniyor ama cold miss aninda ayni asset birden fazla istekten
eszamanli gelirse ayni dosya birden fazla kez okunabilir.

### Snapshot decode

`SnapshotPreviewAssetLoader` iki asamali decode yapiyor (`inJustDecodeBounds` + sample decode),
ama ayni bitmap birden fazla coroutine tarafindan ayni anda istenirse decode yarisi olabilir.

### Live preview session churn

Fokus degisimleri artik tek karta inmis olsa da session baslangici hala yeni render oturumu
maliyeti tasiyor.

## Applied In This Pass

1. Snapshot decode icin in-flight dedup
2. Fragment ve texture load icin in-flight dedup
3. `GLSurfaceView` lifecycle davranisini API 34+ icin attachment tabanli hale getirme
4. Ilk fokus warmup frame sayisini azaltma

## Recommended Next Phase

1. Tek kalici GL host + config retarget
2. GPU tarafinda texture/program reuse
3. Snapshot decode icin `inBitmap` pool
4. Gec baslatilan focus debounce ve idle-start render
