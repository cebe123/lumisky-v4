# V8 kısa referans — location/daylight

Kaynak önceliği: uygun current fix -> uygun last-known -> persisted snapshot -> manual preset -> safe default.

- DEVICE tercihi snapshot yok/GPS kapalı diye MANUAL'a çevrilmez.
- Last-known provider kapalıyken izin/policy uygunsa kullanılabilir.
- Current request throttle/cancel/timeout içerir.
- Reverse geocoder daylight hesabını bloklamaz; async cache'tir.
- Timezone koordinata göre çözülür; cihaz timezone'u kör kaynak değildir.
- Date/time/timezone/location/provider değişimleri recalculation event üretir.
- Polar day, polar night ve normal state ayrı modellenir; 06:00/18:00 her durumda fallback değildir.
