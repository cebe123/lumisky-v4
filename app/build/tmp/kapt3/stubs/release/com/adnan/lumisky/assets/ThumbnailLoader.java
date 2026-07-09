package com.adnan.lumisky.assets;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0013\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\u0010\u0010\t\u001a\u0004\u0018\u00010\n2\u0006\u0010\u000b\u001a\u00020\fR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\b\u00a8\u0006\r"}, d2 = {"Lcom/adnan/lumisky/assets/ThumbnailLoader;", "", "context", "Landroid/content/Context;", "<init>", "(Landroid/content/Context;)V", "memoryCache", "Landroid/util/LruCache;", "Landroid/util/LruCache;", "load", "Landroid/graphics/Bitmap;", "path", "", "app_release"})
public final class ThumbnailLoader {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final android.util.LruCache<java.lang.String, android.graphics.Bitmap> memoryCache = null;
    
    @javax.inject.Inject()
    public ThumbnailLoader(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final android.graphics.Bitmap load(@org.jetbrains.annotations.NotNull()
    java.lang.String path) {
        return null;
    }
}