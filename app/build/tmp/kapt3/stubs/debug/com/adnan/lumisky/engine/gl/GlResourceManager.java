package com.adnan.lumisky.engine.gl;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000V\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B!\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0004\b\b\u0010\tJ\u0006\u0010(\u001a\u00020)J\u0006\u0010*\u001a\u00020)R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0011\u0010\u0010\u001a\u00020\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0011\u0010\u0014\u001a\u00020\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0011\u0010\u0018\u001a\u00020\u0019\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u001bR\u0011\u0010\u001c\u001a\u00020\u001d\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001fR\u0011\u0010 \u001a\u00020!\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010#R\u0011\u0010$\u001a\u00020%\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010\'\u00a8\u0006+"}, d2 = {"Lcom/adnan/lumisky/engine/gl/GlResourceManager;", "", "context", "Landroid/content/Context;", "shaderRegistry", "Lcom/adnan/lumisky/registry/ShaderRegistry;", "releaseQueue", "Lcom/adnan/lumisky/engine/gl/GlReleaseQueue;", "<init>", "(Landroid/content/Context;Lcom/adnan/lumisky/registry/ShaderRegistry;Lcom/adnan/lumisky/engine/gl/GlReleaseQueue;)V", "getContext", "()Landroid/content/Context;", "getShaderRegistry", "()Lcom/adnan/lumisky/registry/ShaderRegistry;", "getReleaseQueue", "()Lcom/adnan/lumisky/engine/gl/GlReleaseQueue;", "programs", "Lcom/adnan/lumisky/engine/gl/ShaderProgramPool;", "getPrograms", "()Lcom/adnan/lumisky/engine/gl/ShaderProgramPool;", "textures", "Lcom/adnan/lumisky/engine/gl/TexturePool;", "getTextures", "()Lcom/adnan/lumisky/engine/gl/TexturePool;", "framebuffers", "Lcom/adnan/lumisky/engine/gl/FramebufferPool;", "getFramebuffers", "()Lcom/adnan/lumisky/engine/gl/FramebufferPool;", "meshes", "Lcom/adnan/lumisky/engine/gl/MeshRegistry;", "getMeshes", "()Lcom/adnan/lumisky/engine/gl/MeshRegistry;", "bitmapPool", "Lcom/adnan/lumisky/engine/gl/BitmapPool;", "getBitmapPool", "()Lcom/adnan/lumisky/engine/gl/BitmapPool;", "binaryCache", "Lcom/adnan/lumisky/engine/gl/ShaderBinaryCache;", "getBinaryCache", "()Lcom/adnan/lumisky/engine/gl/ShaderBinaryCache;", "onContextLost", "", "release", "app_debug"})
public final class GlResourceManager {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.registry.ShaderRegistry shaderRegistry = null;
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.engine.gl.GlReleaseQueue releaseQueue = null;
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.engine.gl.ShaderProgramPool programs = null;
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.engine.gl.TexturePool textures = null;
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.engine.gl.FramebufferPool framebuffers = null;
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.engine.gl.MeshRegistry meshes = null;
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.engine.gl.BitmapPool bitmapPool = null;
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.engine.gl.ShaderBinaryCache binaryCache = null;
    
    public GlResourceManager(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.registry.ShaderRegistry shaderRegistry, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.gl.GlReleaseQueue releaseQueue) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final android.content.Context getContext() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.registry.ShaderRegistry getShaderRegistry() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.engine.gl.GlReleaseQueue getReleaseQueue() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.engine.gl.ShaderProgramPool getPrograms() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.engine.gl.TexturePool getTextures() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.engine.gl.FramebufferPool getFramebuffers() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.engine.gl.MeshRegistry getMeshes() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.engine.gl.BitmapPool getBitmapPool() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.engine.gl.ShaderBinaryCache getBinaryCache() {
        return null;
    }
    
    public final void onContextLost() {
    }
    
    public final void release() {
    }
}