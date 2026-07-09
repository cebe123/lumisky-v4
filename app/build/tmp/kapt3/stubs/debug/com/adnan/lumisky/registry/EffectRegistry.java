package com.adnan.lumisky.registry;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010%\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\b\u0007\u0018\u00002\u00020\u0001B\t\b\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0016\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u00062\u0006\u0010\u000b\u001a\u00020\u0007J\u0010\u0010\f\u001a\u0004\u0018\u00010\u00072\u0006\u0010\n\u001a\u00020\u0006R\u001a\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00070\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\r"}, d2 = {"Lcom/adnan/lumisky/registry/EffectRegistry;", "", "<init>", "()V", "effects", "", "", "Lcom/adnan/lumisky/registry/EffectConfig;", "register", "", "name", "config", "get", "app_debug"})
public final class EffectRegistry {
    @org.jetbrains.annotations.NotNull()
    private final java.util.Map<java.lang.String, com.adnan.lumisky.registry.EffectConfig> effects = null;
    
    @javax.inject.Inject()
    public EffectRegistry() {
        super();
    }
    
    public final void register(@org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.registry.EffectConfig config) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.adnan.lumisky.registry.EffectConfig get(@org.jetbrains.annotations.NotNull()
    java.lang.String name) {
        return null;
    }
}