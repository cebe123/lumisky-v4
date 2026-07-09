package com.adnan.lumisky.registry;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010%\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0005\b\u0007\u0018\u00002\u00020\u0001B\t\b\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u001e\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u00062\u0006\u0010\u000b\u001a\u00020\u00062\u0006\u0010\f\u001a\u00020\u0006J\u0010\u0010\r\u001a\u0004\u0018\u00010\u00072\u0006\u0010\n\u001a\u00020\u0006R\u001a\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00070\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000e"}, d2 = {"Lcom/adnan/lumisky/registry/ShaderRegistry;", "", "<init>", "()V", "shaders", "", "", "Lcom/adnan/lumisky/registry/ShaderSourceInfo;", "register", "", "ref", "vertexPath", "fragmentPath", "getSourceInfo", "app_release"})
public final class ShaderRegistry {
    @org.jetbrains.annotations.NotNull()
    private final java.util.Map<java.lang.String, com.adnan.lumisky.registry.ShaderSourceInfo> shaders = null;
    
    @javax.inject.Inject()
    public ShaderRegistry() {
        super();
    }
    
    public final void register(@org.jetbrains.annotations.NotNull()
    java.lang.String ref, @org.jetbrains.annotations.NotNull()
    java.lang.String vertexPath, @org.jetbrains.annotations.NotNull()
    java.lang.String fragmentPath) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.adnan.lumisky.registry.ShaderSourceInfo getSourceInfo(@org.jetbrains.annotations.NotNull()
    java.lang.String ref) {
        return null;
    }
}