package com.adnan.lumisky.engine.gl;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0010\u0002\n\u0002\b\u0003\u0018\u0000 \u00132\u00020\u0001:\u0001\u0013B)\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0003\u0012\u0006\u0010\u0007\u001a\u00020\u0003\u00a2\u0006\u0004\b\b\u0010\tJ\b\u0010\u0010\u001a\u00020\u0011H\u0016J\u0006\u0010\u0012\u001a\u00020\u0011R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0011\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000bR\u0011\u0010\u0007\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u000b\u00a8\u0006\u0014"}, d2 = {"Lcom/adnan/lumisky/engine/gl/GlFramebuffer;", "Lcom/adnan/lumisky/engine/gl/GlResource;", "framebufferId", "", "texture", "Lcom/adnan/lumisky/engine/gl/GlTexture;", "width", "height", "<init>", "(ILcom/adnan/lumisky/engine/gl/GlTexture;II)V", "getFramebufferId", "()I", "getTexture", "()Lcom/adnan/lumisky/engine/gl/GlTexture;", "getWidth", "getHeight", "release", "", "bind", "Companion", "app_release"})
public final class GlFramebuffer implements com.adnan.lumisky.engine.gl.GlResource {
    private final int framebufferId = 0;
    @org.jetbrains.annotations.Nullable()
    private final com.adnan.lumisky.engine.gl.GlTexture texture = null;
    private final int width = 0;
    private final int height = 0;
    @org.jetbrains.annotations.NotNull()
    public static final com.adnan.lumisky.engine.gl.GlFramebuffer.Companion Companion = null;
    
    public GlFramebuffer(int framebufferId, @org.jetbrains.annotations.Nullable()
    com.adnan.lumisky.engine.gl.GlTexture texture, int width, int height) {
        super();
    }
    
    public final int getFramebufferId() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.adnan.lumisky.engine.gl.GlTexture getTexture() {
        return null;
    }
    
    public final int getWidth() {
        return 0;
    }
    
    public final int getHeight() {
        return 0;
    }
    
    @java.lang.Override()
    public void release() {
    }
    
    public final void bind() {
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0016\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\u0007\u00a8\u0006\t"}, d2 = {"Lcom/adnan/lumisky/engine/gl/GlFramebuffer$Companion;", "", "<init>", "()V", "create", "Lcom/adnan/lumisky/engine/gl/GlFramebuffer;", "width", "", "height", "app_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.adnan.lumisky.engine.gl.GlFramebuffer create(int width, int height) {
            return null;
        }
    }
}