# Onceliklendirilmis Gelistirme Sirasi

Bu siralama, incelenen dosyalar arasinda uygulama kararliligi, kullanici etkisi, mimari borc ve degisiklik getirisi birlikte degerlendirilerek hazirlandi.

1. `WallpaperRenderController.kt` - Render thread/state sahipligini netlestir; surface, visibility ve config state yarismalarini azalt.
2. `WallpaperRenderEngine.kt` - `attachSurface`/`setConfig` basarisizliklarini ust katmana bildiren temiz EGL state modeli kur.
3. `WallpaperEglSession.kt` - EGL partial-init cleanup, release sirasi ve hata loglarini saglamlastir.
4. `PreviewSkyProgram.kt` - Texture unit cakismalarini ve GL resource lifecycle belirsizligini gider.
5. `MainActivity.kt` - Wallpaper apply flow, ViewModel sahipligi ve repository lifecycle'ini ayir; atomik apply guard ekle.
6. `SunTimesRepository.kt` - Callback thread contract, timeout ve executor lifecycle sorunlarini coz.
7. `LocationSunTimesCoordinator.kt` - Release sonrasi async callback guard'lari ve repository sahipligini netlestir.
8. `WallpaperConfigStore.kt` - JSON codec'i ayir, schema/version ve decode hata gorunurlugu ekle.
9. `HomeViewModel.kt` - Gercek lifecycle ViewModel'e gec; settings snapshot dil bug'ini ve release sonrasi post riskini duzelt.
10. `HomeScreen.kt` - Büyük composable dosyasini bol; snapshot yuklemeyi IO-safe yap ve focus akisini sadelestir.
11. `SettingsScreen.kt` - Permission tekrarini ve uzun dialog render maliyetini azalt; dil seceneklerini resource tabanli yap.
12. `WallpaperRestoreReceiver.kt` - Boot/unlock duplicate restore guard'i ve Direct Boot/user-unlocked sinirlarini netlestir.
13. `WallpaperSelectionState.kt` - Lock-screen override clear islemini Lumisky aktiflik guard'i ve daha acik result modeliyle guvenli hale getir.
14. `LastKnownLocationProvider.kt` - Location callback thread contract, cancelable current request ve passive listener race'ini duzelt.
15. `PreviewGlRenderer.kt` - UI/GL thread contract ve adaptive quality/FPS policy ayrimini netlestir.
16. `PreviewRendererSurfaceView.kt` - GL thread callback, parallax lifecycle ve silent `queueEvent` hatalarini duzelt.
17. `SkyRenderer.kt` - Mutable frame state buffer contract'i ve hash kapsam sorunlarini gider.
18. `city/fragment_shader.glsl` - `u_HorizonY` kullan, day/night helper ve shader maliyet azaltimi yap.
19. `TiltParallaxTracker.kt` - Sensor callback thread'i, lifecycle dispatch ve sensor delay politikasini netlestir.
20. `WallpaperManifestCatalogSource.kt` - Parser hata loglari, enum/numeric validasyon ve duplicate id kontrollerini ekle.
21. `WallpaperManifest.kt` - DTO enum/wire format ve numeric validator politikasini netlestir.
22. `wallpapers/index.json` - Schema/version, duplicate id ve metadata/siralama contract'i ekle.
23. `lighthouse/fragment.glsl` - Henuz incelenmedi; bu siralama disinda kalir.

## Ilk Uygulama Dalgasi

En yuksek getirili ilk dalga:

1. Render lifecycle guvenligi: `WallpaperRenderController.kt`, `WallpaperRenderEngine.kt`, `WallpaperEglSession.kt`.
2. GL program/resource guvenligi: `PreviewSkyProgram.kt`.
3. Wallpaper apply ve app lifecycle guvenligi: `MainActivity.kt`.
4. Sun-times/location async guvenligi: `SunTimesRepository.kt`, `LocationSunTimesCoordinator.kt`.

## Not

`lighthouse/fragment.glsl` henuz tekil inceleme sirasinda acilmadigi icin bilincli olarak onceliklendirme disinda tutuldu. Inceleme tamamlandiktan sonra bu siralama guncellenmeli.
