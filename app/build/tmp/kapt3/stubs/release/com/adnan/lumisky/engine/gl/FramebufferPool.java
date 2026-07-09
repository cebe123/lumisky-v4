package com.adnan.lumisky.engine.gl;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u000f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\u0016\u0010\t\u001a\u00020\b2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u000bJ\u000e\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\bJ\u0006\u0010\u0010\u001a\u00020\u000eR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0011"}, d2 = {"Lcom/adnan/lumisky/engine/gl/FramebufferPool;", "", "manager", "Lcom/adnan/lumisky/engine/gl/GlResourceManager;", "<init>", "(Lcom/adnan/lumisky/engine/gl/GlResourceManager;)V", "pool", "", "Lcom/adnan/lumisky/engine/gl/GlFramebuffer;", "obtain", "width", "", "height", "recycle", "", "framebuffer", "clear", "app_release"})
public final class FramebufferPool {
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.engine.gl.GlResourceManager manager = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.adnan.lumisky.engine.gl.GlFramebuffer> pool = null;
    
    public FramebufferPool(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.gl.GlResourceManager manager) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.engine.gl.GlFramebuffer obtain(int width, int height) {
        return null;
    }
    
    public final void recycle(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.gl.GlFramebuffer framebuffer) {
    }
    
    public final void clear() {
    }
}