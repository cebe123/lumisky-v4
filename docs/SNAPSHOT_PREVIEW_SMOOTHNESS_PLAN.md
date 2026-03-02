# Snapshot Preview Smoothness Plan

## Goal

Home ekranindaki snapshot preview -> live render gecisini daha stabil hale getirmek:

- WebP snapshot kart cercevesine birebir otursun
- render basladiginda kadraj veya olcek "ziplamasi" olmasin
- ilk gorunen canli frame "cold" bir frame gibi drop hissi vermesin
- ilk scroll ve kart acilisi daha hizli hissettirsin

## Root Causes

### 1. Frame ratio mismatch

Home kart boyutu ile snapshot export boyutu ayni oranla uretilmiyor.

- Home kart yuksekligi dinamik oran + ekstra scale ile hesaplanir
- snapshot export ise ayri bir ekran oranindan turetilir

Bu durum snapshot ile canli preview arasinda crop/kadraj farki olusturur.

### 2. Surface transition behavior

Home preview, `GLSurfaceView` tabanli oldugu icin Compose `Image` ile ayni katman davranisini vermez.
Bu yuzden canli preview `alpha` fade bazen beklenen kadar yumusak gorunmez.

### 3. First-frame handoff too early

Ilk draw edilen frame her zaman kullaniciya gosterilmesi gereken "stabil" frame degildir.
Shader, texture ve surface ilk aktivasyonunda ilk 1-2 frame daha sert gorunebilir.

## Implementation Order

### Step 1: Unify Home frame geometry

`app` modulu icinde ortak bir helper ile Home kart preview oranini tek yerden hesapla.

- Home kart bu helper ile yukseklik hesaplasin
- `ZenithSnapshotActivity` export boyutunu ayni oranla uretsin
- snapshot ile runtime preview ayni cerceve geometriyi kullansin

Bu adim ziplama etkisinin en buyuk kaynagini azaltir.

### Step 2: Make snapshot fill the frame exactly

Snapshot asset, kart cercevesine tam oturacak sekilde gosterilmeli.

- oranlar eslestikten sonra gereksiz crop kaldirilmali
- snapshot runtime tarafinda tam frame kaplamali

Bu sayede render geldiginde yeni bir crop/zoom algisi olusmaz.

### Step 3: Switch to snapshot-only fade handoff

Canli `GLSurfaceView` preview icin alpha fade yerine:

- canli preview altta hazirlanir
- snapshot ustte opak kalir
- stabil frame hazir oldugunda sadece snapshot fade-out olur

Bu yaklasim `GLSurfaceView` katman davranisi nedeniyle daha dusuk risklidir.

### Step 4: Gate handoff on stable frames

Ilk draw yerine birkac draw sonra "ready" kabul edilmeli.

- en az 2-3 frame say
- sonra UI thread uzerinde bir tur daha bekle
- ondan sonra snapshot gizlenmeye baslasin

Bu, cold-frame drop hissini azaltir.

### Step 5: Keep snapshot decode warm

Static preview akisi daha hizli hissedilsin diye:

- ilk gorunen kartlar icin snapshot decode onceden yapilmali
- komsu kartlar mevcut prewarm penceresi ile islenmeli
- cache hit olabildigince artirilmali

## Applied Now

Bu turda uygulanacak dusuk riskli adimlar:

1. Ortak Home preview frame oranini helper'a tasimak
2. Snapshot export boyutunu ayni frame oranina cekmek
3. Home handoff'u snapshot-only fade modeline cevirmek
4. Handoff'u stabil frame esigine baglamak

## Future Phase

Sorun devam ederse bir sonraki faz:

- Home preview icin `GLSurfaceView` yerine `TextureView` tabanli host

Bu adim daha buyuk degisikliktir ve ayrica ele alinmalidir.

## Validation

1. `GenerateWallpaperZenithSnapshots.cmd` ile yeni snapshot seti uret
2. `.\gradlew :app:assembleDebug`
3. Home kartlarinda snapshot tam cerceve doluyor mu kontrol et
4. Live preview basladiginda crop/zoom ziplamasi kaldi mi kontrol et
5. Gecis aninda sadece snapshot fade-out oluyor mu kontrol et
6. Farkli ekran oranlarinda test et
