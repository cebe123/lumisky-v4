package com.adnan.lumisky.telemetry;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000bJ\u0006\u0010\f\u001a\u00020\rR\u001e\u0010\u0004\u001a\u0012\u0012\u0004\u0012\u00020\u00060\u0005j\b\u0012\u0004\u0012\u00020\u0006`\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000e"}, d2 = {"Lcom/adnan/lumisky/telemetry/TelemetryRateLimiter;", "", "<init>", "()V", "seenThisSession", "Ljava/util/HashSet;", "", "Lkotlin/collections/HashSet;", "shouldReport", "", "event", "Lcom/adnan/lumisky/telemetry/RenderTelemetryEvent;", "clear", "", "app_release"})
public final class TelemetryRateLimiter {
    @org.jetbrains.annotations.NotNull()
    private final java.util.HashSet<java.lang.String> seenThisSession = null;
    
    public TelemetryRateLimiter() {
        super();
    }
    
    public final boolean shouldReport(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.telemetry.RenderTelemetryEvent event) {
        return false;
    }
    
    public final void clear() {
    }
}