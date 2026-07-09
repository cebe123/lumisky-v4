package com.adnan.lumisky.engine;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0011\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\u000e\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\n"}, d2 = {"Lcom/adnan/lumisky/engine/ShaderWarmupController;", "", "shaderSourceLoader", "Lcom/adnan/lumisky/assets/ShaderSourceLoader;", "<init>", "(Lcom/adnan/lumisky/assets/ShaderSourceLoader;)V", "warmup", "", "gl", "Lcom/adnan/lumisky/engine/gl/GlResourceManager;", "app_release"})
public final class ShaderWarmupController {
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.assets.ShaderSourceLoader shaderSourceLoader = null;
    
    @javax.inject.Inject()
    public ShaderWarmupController(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.assets.ShaderSourceLoader shaderSourceLoader) {
        super();
    }
    
    public final void warmup(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.gl.GlResourceManager gl) {
    }
}