# V8 kısa referans — runtime/session

- Dış dünya yalnız immutable `RenderCommand` üretir.
- Mailbox FIFO + latest-wins coalescing uygular.
- RenderSession owner: state, frame demand, scheduler, parallax, EGL, resources, backend, telemetry.
- Mutable session bileşenleri singleton değildir.
- Dirty reasons: initial, resize, switch, parallax, timeline, video, particle, shader, daylight, quality, event.
- Görünmez state bütün frame/sensor/video işini durdurur.
- Scene prepare ayrı; first successful swap sonrası atomic commit.
- Animasyon monotonic clock kullanır; daylight wall-clock ayrı cache'tir.
