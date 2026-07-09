package com.adnan.lumisky.engine.gl;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010%\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\u0018\u00002\u00020\u0001B\u000f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\u0018\u0010\n\u001a\u00020\t2\u0006\u0010\u000b\u001a\u00020\b2\b\b\u0002\u0010\f\u001a\u00020\rJ\u0018\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u000b\u001a\u00020\b2\u0006\u0010\f\u001a\u00020\rH\u0002J\u0010\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u000fH\u0002J\u0006\u0010\u0013\u001a\u00020\u0014R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\t0\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0015"}, d2 = {"Lcom/adnan/lumisky/engine/gl/TexturePool;", "", "manager", "Lcom/adnan/lumisky/engine/gl/GlResourceManager;", "<init>", "(Lcom/adnan/lumisky/engine/gl/GlResourceManager;)V", "pool", "", "", "Lcom/adnan/lumisky/engine/gl/GlTexture;", "get", "path", "quality", "Lcom/adnan/lumisky/definition/QualityTier;", "loadBitmap", "Landroid/graphics/Bitmap;", "uploadTexture", "", "bitmap", "clear", "", "app_release"})
public final class TexturePool {
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.engine.gl.GlResourceManager manager = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Map<java.lang.String, com.adnan.lumisky.engine.gl.GlTexture> pool = null;
    
    public TexturePool(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.gl.GlResourceManager manager) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.engine.gl.GlTexture get(@org.jetbrains.annotations.NotNull()
    java.lang.String path, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.definition.QualityTier quality) {
        return null;
    }
    
    private final android.graphics.Bitmap loadBitmap(java.lang.String path, com.adnan.lumisky.definition.QualityTier quality) {
        return null;
    }
    
    private final int uploadTexture(android.graphics.Bitmap bitmap) {
        return 0;
    }
    
    public final void clear() {
    }
}