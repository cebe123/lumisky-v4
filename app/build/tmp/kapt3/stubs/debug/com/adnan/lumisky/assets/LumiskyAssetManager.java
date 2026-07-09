package com.adnan.lumisky.assets;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\u001b\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0004\b\u0006\u0010\u0007J\u001a\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u000bJ\u001a\u0010\r\u001a\u00020\u000b2\u0006\u0010\n\u001a\u00020\u000b2\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u000bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000e"}, d2 = {"Lcom/adnan/lumisky/assets/LumiskyAssetManager;", "", "context", "Landroid/content/Context;", "assetPackResolver", "Lcom/adnan/lumisky/assets/AssetPackResolver;", "<init>", "(Landroid/content/Context;Lcom/adnan/lumisky/assets/AssetPackResolver;)V", "openAsset", "Ljava/io/InputStream;", "path", "", "assetPack", "getAssetPath", "app_debug"})
public final class LumiskyAssetManager {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.assets.AssetPackResolver assetPackResolver = null;
    
    @javax.inject.Inject()
    public LumiskyAssetManager(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.assets.AssetPackResolver assetPackResolver) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.io.InputStream openAsset(@org.jetbrains.annotations.NotNull()
    java.lang.String path, @org.jetbrains.annotations.Nullable()
    java.lang.String assetPack) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getAssetPath(@org.jetbrains.annotations.NotNull()
    java.lang.String path, @org.jetbrains.annotations.Nullable()
    java.lang.String assetPack) {
        return null;
    }
}