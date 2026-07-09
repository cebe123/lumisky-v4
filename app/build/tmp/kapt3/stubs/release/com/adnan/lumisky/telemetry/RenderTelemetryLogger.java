package com.adnan.lumisky.telemetry;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\t\b\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tJ\u000e\u0010\n\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\u000bJ\u0006\u0010\f\u001a\u00020\u0007J\u0010\u0010\r\u001a\u00020\u000b2\u0006\u0010\b\u001a\u00020\tH\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000e"}, d2 = {"Lcom/adnan/lumisky/telemetry/RenderTelemetryLogger;", "", "<init>", "()V", "rateLimiter", "Lcom/adnan/lumisky/telemetry/TelemetryRateLimiter;", "logFallback", "", "event", "Lcom/adnan/lumisky/telemetry/RenderFallbackEvent;", "log", "Lcom/adnan/lumisky/telemetry/RenderTelemetryEvent;", "clear", "toTelemetryEvent", "app_release"})
public final class RenderTelemetryLogger {
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.telemetry.TelemetryRateLimiter rateLimiter = null;
    
    @javax.inject.Inject()
    public RenderTelemetryLogger() {
        super();
    }
    
    public final void logFallback(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.telemetry.RenderFallbackEvent event) {
    }
    
    public final void log(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.telemetry.RenderTelemetryEvent event) {
    }
    
    public final void clear() {
    }
    
    private final com.adnan.lumisky.telemetry.RenderTelemetryEvent toTelemetryEvent(com.adnan.lumisky.telemetry.RenderFallbackEvent event) {
        return null;
    }
}