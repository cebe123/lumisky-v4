package com.adnan.lumisky.registry;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\b\u0007\u0018\u00002\u00020\u0001B(\b\u0007\u0012\u001d\u0010\u0002\u001a\u0019\u0012\u0004\u0012\u00020\u0004\u0012\u000f\u0012\r\u0012\u0004\u0012\u00020\u00060\u0005\u00a2\u0006\u0002\b\u00070\u0003\u00a2\u0006\u0004\b\b\u0010\tJ\u0018\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\b\b\u0002\u0010\u000e\u001a\u00020\u000fR%\u0010\u0002\u001a\u0019\u0012\u0004\u0012\u00020\u0004\u0012\u000f\u0012\r\u0012\u0004\u0012\u00020\u00060\u0005\u00a2\u0006\u0002\b\u00070\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0010"}, d2 = {"Lcom/adnan/lumisky/registry/LayerRegistry;", "", "factories", "", "", "Ljavax/inject/Provider;", "Lcom/adnan/lumisky/registry/LayerFactory;", "Lkotlin/jvm/JvmSuppressWildcards;", "<init>", "(Ljava/util/Map;)V", "create", "Lcom/adnan/lumisky/registry/LayerCreateResult;", "definition", "Lcom/adnan/lumisky/definition/LayerDefinition;", "required", "", "app_release"})
public final class LayerRegistry {
    @org.jetbrains.annotations.NotNull()
    private final java.util.Map<java.lang.String, javax.inject.Provider<com.adnan.lumisky.registry.LayerFactory>> factories = null;
    
    @javax.inject.Inject()
    public LayerRegistry(@org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, javax.inject.Provider<com.adnan.lumisky.registry.LayerFactory>> factories) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.registry.LayerCreateResult create(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.definition.LayerDefinition definition, boolean required) {
        return null;
    }
}