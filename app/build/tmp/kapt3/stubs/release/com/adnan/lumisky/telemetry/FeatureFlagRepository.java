package com.adnan.lumisky.telemetry;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\t\b\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005J\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\b0\u00052\u0006\u0010\t\u001a\u00020\n\u00a8\u0006\u000b"}, d2 = {"Lcom/adnan/lumisky/telemetry/FeatureFlagRepository;", "", "<init>", "()V", "flags", "Lkotlinx/coroutines/flow/Flow;", "Lcom/adnan/lumisky/engine/RenderFeatureFlags;", "isFeatureEnabled", "", "name", "", "app_release"})
public final class FeatureFlagRepository {
    
    @javax.inject.Inject()
    public FeatureFlagRepository() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<com.adnan.lumisky.engine.RenderFeatureFlags> flags() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.lang.Boolean> isFeatureEnabled(@org.jetbrains.annotations.NotNull()
    java.lang.String name) {
        return null;
    }
}