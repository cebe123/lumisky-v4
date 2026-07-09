package com.adnan.lumisky.engine.gl;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0007\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u0000 \u00122\u00020\u0001:\u0001\u0012B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0005\u0010\u0006J\b\u0010\n\u001a\u00020\u000bH\u0016J\u0006\u0010\f\u001a\u00020\u000bJ\u0006\u0010\r\u001a\u00020\u000bJ\u0018\u0010\u000e\u001a\u00020\u000b2\u0006\u0010\u000f\u001a\u00020\u00102\b\b\u0002\u0010\u0011\u001a\u00020\u0003R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\b\u00a8\u0006\u0013"}, d2 = {"Lcom/adnan/lumisky/engine/gl/GlBuffer;", "Lcom/adnan/lumisky/engine/gl/GlResource;", "bufferId", "", "target", "<init>", "(II)V", "getBufferId", "()I", "getTarget", "release", "", "bind", "unbind", "setData", "data", "Ljava/nio/Buffer;", "usage", "Companion", "app_release"})
public final class GlBuffer implements com.adnan.lumisky.engine.gl.GlResource {
    private final int bufferId = 0;
    private final int target = 0;
    @org.jetbrains.annotations.NotNull()
    public static final com.adnan.lumisky.engine.gl.GlBuffer.Companion Companion = null;
    
    public GlBuffer(int bufferId, int target) {
        super();
    }
    
    public final int getBufferId() {
        return 0;
    }
    
    public final int getTarget() {
        return 0;
    }
    
    @java.lang.Override()
    public void release() {
    }
    
    public final void bind() {
    }
    
    public final void unbind() {
    }
    
    public final void setData(@org.jetbrains.annotations.NotNull()
    java.nio.Buffer data, int usage) {
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007\u00a8\u0006\b"}, d2 = {"Lcom/adnan/lumisky/engine/gl/GlBuffer$Companion;", "", "<init>", "()V", "create", "Lcom/adnan/lumisky/engine/gl/GlBuffer;", "target", "", "app_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.adnan.lumisky.engine.gl.GlBuffer create(int target) {
            return null;
        }
    }
}