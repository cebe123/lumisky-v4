# Snapshot Preview Integration Plan

## Goal

Home ekranindaki wallpaper kartlari, GL preview daha render etmeden once statik snapshot gostersin.
Boylece:

- kartlar bos/skeleton gorunmesin,
- ilk scroll deneyimi daha dolu gorunsun,
- uygulama ici kaydirma sirasinda GL yuk olusmadan da akici hissedilsin.

Kapsam sadece `app` modulu icindeki Home preview kartlari ile sinirlidir.

## Confirmed Decisions

- Snapshotlar uygulama icine asset olarak girecek.
- Snapshot kaynaklari sadece localde tutulacak, git'e dahil edilmeyecek.
- Statik snapshot, render ilk goruntuyu verene kadar gorunecek.
- Render hazir olunca statik snapshot yerini canli preview'a birakacak.
- Snapshotlar `GenerateWallpaperZenithSnapshots.cmd` uzerinden uretilip dogrudan APK'ya girecek kaynak konuma tasinacak.
- Sadece ana ekrandaki preview kartlari degisecek.
- Gecis olabildigince akici olacak.
- PNG ve WebP dosyalari ayri klasorlerde tutulacak.
- APK'ya sadece WebP dosyalari girecek.

## Recommended Asset Strategy

### Source Asset Location

PNG ve WebP ayri klasorlerde tutulmali.

Onerilen kesin klasor yapisi:

- local PNG kaynaklari: `snapshot-output/zenith-png/`
- uygulamanin okuyacagi local WebP assetleri: `app/src/main/assets/previews/zenith/`

Bu secim su nedenlerle en dusuk riskli yaklasimdir:

- mevcut asset pipeline zaten `app/src/main/assets/**` altini paketleme kaynagi olarak kullaniyor,
- `prepareFilteredAssets` ve `convertWallpaperTexturesToWebp` zinciri bozulmaz,
- yeni bir build kaynagi tanimlamaya gerek kalmaz.
- PNG kaynagi `assets` disinda oldugu icin APK'ya yanlislikla dahil olmaz.

### Packaging Format

Uygulama tarafi sadece WebP tukecek.

Bu repo icin guncel strateji:

- export edilen ham kaynak: `snapshot-output/zenith-png/*.png`
- script tarafinda PNG -> WebP donusumu hemen otomatik yapilir
- donusen dosya hedefi: `app/src/main/assets/previews/zenith/*.webp`
- runtime okuma: `previews/zenith/<wallpaperId>.webp`
- paketlenen son dosya: `build/generated/filteredAssets/main/previews/zenith/*.webp`

Bu sayede:

- PNG dosyalari asset klasorune hic girmez,
- APK boyutu gereksiz sismez,
- runtime sadece son format olan WebP ile calisir.

### WebP Quality

WebP bu kullanim icin uygundur ve bu senaryo icin tavsiye edilen formattir.

- Home kartlari tam ekran degil; bu nedenle iyi kalite ayarli WebP genelde yeterince net gorunur.
- Preview karti icin iyi kalite ayarli WebP genelde PNG'den ayirt edilmesi zor seviyede gorunur.
- Script tarafindaki PNG -> WebP donusumu, mevcut build task'indaki `compressionQuality = 0.90f` seviyesine yakin tutulmalidir.
- Yuksek coznurluklu telefonlarda kaliteyi korumak icin tek kritik nokta, kaynagin yeterince buyuk olmasidir.

### Resolution Strategy

Ilk fazda tek bir yuksek coznurluklu kaynak seti ile baslanmali.

Oneri:

- exporter, cihaz yogunluguna bagli degil sabit piksel hedefiyle snapshot uretsin,
- hedef coznurluk karttan daha buyuk olsun (ornegin yaklasik `1080x2160` sinifinda bir portre export),
- runtime decode sirasinda kart boyutuna gore downsample edilsin.

Neden:

- tek set yonetmesi daha kolaydir,
- asset eslestirme karmasiklasmaz,
- kaydirma performansi, dogru decode/sample yapildigi surece tek buyuk kaynakla da korunabilir.

Ancak ikinci faz iyilestirme olarak, gerekirse yogunluk varyanti eklenebilir:

- `previews/zenith/standard/`
- `previews/zenith/high/`

Bu yalnizca gercek cihaz testinde bellek veya kalite sorunu gorulurse dusunulmeli.

## Planned File Ownership

- `app`
  - Home kart UI gecisi
  - snapshot asset yukleme/caching
  - `GenerateWallpaperZenithSnapshots.cmd` ile asset senkronizasyonu
- `engine`
  - sadece gerekli ise "first frame rendered" bildirimi icin ufak callback genisletmesi

`core`, `wallpaper` veya wallpaper apply flow bu is icin degistirilmemeli.

## Implementation Plan

### Phase 1: Normalize Snapshot Export For App Assets

`GenerateWallpaperZenithSnapshots.cmd` su davranisa cekilecek:

- guncel APK'yi olusturup cihaza kurmaya devam eder,
- cihazda snapshotlari once PNG olarak uretir,
- cihazdan cektigi dosyalari `snapshot-output/zenith-png/` altina alir,
- numarali export adlarini `wallpaperId` tabanli local PNG adlarina normalize eder,
- PNG uretiminden hemen sonra tum dosyalari otomatik olarak WebP'ye donusturur,
- olusan WebP dosyalarini `app/src/main/assets/previews/zenith/` altina yazar,
- ayni isimde mevcut PNG veya WebP varsa ustune en guncel dosyayi yazar,
- eski WebP assetlerini temizler,
- tamamlandiginda `app/src/main/assets/previews/zenith/` icinde sadece guncel `.webp` dosyalari kalir.

Hedef dosya isimleri:

- local PNG: `snapshot-output/zenith-png/pixel_forest.png`
- package edilecek WebP: `app/src/main/assets/previews/zenith/pixel_forest.webp`
- package edilecek WebP: `app/src/main/assets/previews/zenith/classic_sun.webp`
- package edilecek WebP: `app/src/main/assets/previews/zenith/solar_horizon.webp`

Bu, runtime tarafinda katalog `config.id` ile dogrudan eslestirme saglar.

Not:

- Su anki numarali dosya isimleri (`01-...`) gecici export icin uygun, fakat runtime mapping icin gereksiz bagimlilik yaratir.
- Bu nedenle PNG cekme ve WebP yazma asamasinda id tabanli yeniden adlandirma planlanmali.
- `snapshot-output/zenith-png/` ve `app/src/main/assets/previews/zenith/` local artifact olarak dusunulmeli; git'e alinmamalari icin `.gitignore` kapsamina alinmalidir.

### Phase 2: Add Snapshot Asset Loader And Cache

`app` modulu icine Home preview icin ayri bir hafif asset loader eklenmeli.

Onerilen yeni sorumluluk:

- `SnapshotPreviewAssetLoader` veya benzeri bir sinif

Sorumluluklari:

- `config.id` -> `previews/zenith/<id>.webp` yolunu cozmeyi denemek
- asset'i arka planda decode etmek
- kart boyutuna gore downsample etmek
- `LruCache<String, Bitmap>` ile bellek ici cache tutmak
- komsu kartlar icin prewarm destegi vermek

Bu loader, mevcut `RenderAssetCache` ile karistirilmamali; amac farkli:

- `RenderAssetCache`: shader/texture verisi
- yeni loader: Home UI icin statik preview bitmap'i

### Phase 3: Replace Skeleton Placeholder With Snapshot Placeholder

`HomeScreen.kt` icindeki `WallpaperCard` akisi degisecek:

- `PreviewPlaceholderFrame(...)` artik sadece son fallback olacak,
- once snapshot varsa o gosterilecek,
- snapshot yoksa mevcut skeleton placeholder kullanilacak.

Onerilen katman sirasi:

1. statik snapshot
2. render hazir degilse snapshot gorunur
3. render ilk frame ile hazir olunca canli preview ustten fade ile gorunur
4. snapshot altta kisa sure kalip sonra tamamen gizlenir

Bu sayede kullanici karti ilk anda dolu gorur, ama GL preview acildiginda ani pop olmaz.

### Phase 4: Add First-Frame Ready Signal

Su an `FocusedWallpaperPreview` sadece view olusturuyor; UI tarafi render'in gorunur hale gelip gelmedigini bilmiyor.

Bu nedenle kucuk bir "first frame rendered" sinyali eklenmeli:

- `PreviewGlRenderer` ilk basarili draw sonrasi callback tetikleyebilmeli
- `PreviewRendererSurfaceView` bunu UI'ya tasiyabilmeli
- `FocusedWallpaperPreview` Compose state uzerinden "live preview hazir" bilgisini alabilmeli

Bu sinyal geldikten sonra snapshot -> live preview gecisi baslatilir.

### Phase 5: Smooth Transition

Gecis ani sert olmamali.

Onerilen UI davranisi:

- snapshot ilk anda tamamen opak
- ilk frame hazir olunca canli preview `alpha` ile 0 -> 1 fade
- snapshot paralelde 1 -> 0 fade
- sure: kisa ve akici (yaklasik `120ms` - `180ms`)

Bu, istenen "olabildigince akici" hedefini karsilar.

### Phase 6: Performance Safeguards

Akicilik icin su kurallar uygulanmali:

- decode islemi ana thread'de yapilmamali
- kart olcusune uygun sample size kullanilmali
- sadece gorunen/komsu kartlar prewarm edilmeli
- bitmap cache boyutu sinirli olmali
- snapshot varsa GL preview olusana kadar bos alan birakilmamali

Ek olarak:

- `isPreparedNeighbor` ve mevcut komsu on-hazirlama mantigi snapshot prewarm icin de kullanilabilir
- boylece scroll sirasinda hem shader cache hem snapshot cache birlikte isler

## Validation Plan

Uygulamaya almadan once su adimlar uygulanmali:

1. `GenerateWallpaperZenithSnapshots.cmd` calistirilip `snapshot-output/zenith-png/` ve `app/src/main/assets/previews/zenith/` dogru dosyalarla doluyor mu kontrol edilecek.
2. `:app:assembleDebug` calistirilacak.
3. APK cihaza yuklenip Home ekrani ilk acilisinda kartlar render oncesi snapshot gosteriyor mu kontrol edilecek.
4. Yatay ve dikey kaydirma sirasinda jank azalmasi gozlemlenecek.
5. Render geldiginde snapshot -> live preview fade gecisi dogru mu kontrol edilecek.
6. Snapshot eksik oldugunda mevcut skeleton fallback'in bozulmadan calistigi dogrulanacak.

## Risks To Watch

- Snapshot export su an cihazdan cihaza farkli coznurluk uretebilir; entegrasyon oncesi sabit piksel export hedefi tercih edilmeli.
- Cok buyuk tekil bitmap decode edilirse bellek baskisi olusabilir; sample ederek decode etmek zorunlu.
- Asset isimleri katalog `id` ile birebir eslesmezse kartlar yanlis snapshot gosterebilir.
- Home ekraninda ayni anda cok fazla GL + bitmap yuklenirse prewarm penceresi dar tutulmali.
- PNG -> WebP donusumu script tarafinda tutarli yonetilmezse eski/yanlis WebP dosyalari asset klasorunde kalabilir; yazmadan once temizleme zorunlu olmalidir.

## Success Criteria

Asagidaki durum saglandiginda is tamamlanmis sayilacak:

- Home kartlari skeleton yerine dogru statik wallpaper snapshotini gosteriyor.
- Canli preview gelene kadar kart bos kalmiyor.
- Canli preview geldigi anda gecis sert degil, akici.
- Scroll performansi mevcut duruma gore daha stabil.
- Asset pipeline (`preBuild -> prepareFilteredAssets -> convertWallpaperTexturesToWebp`) bozulmuyor.
