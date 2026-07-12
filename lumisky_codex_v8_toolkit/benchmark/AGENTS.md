# Benchmark modülü kuralları

- Macrobenchmark: app launch, catalog scroll, detail/fullscreen preview açılışı.
- Live wallpaper GPU/pil için Perfetto, AGI, batterystats ve gerçek cihaz harness kullanılır.
- CPU submission süresini GPU completion süresi olarak adlandırma.
- Senaryolar fullscreen, katalog ve live için ayrı raporlanır.
- En az iki GPU ailesi ve yüksek yenilemeli bir gerçek cihaz hedeflenir.
- Baseline ile karşılaştırılmayan performans iyileşmesi iddiası yazma.
- Test kodu production davranışını değiştirmemelidir.
