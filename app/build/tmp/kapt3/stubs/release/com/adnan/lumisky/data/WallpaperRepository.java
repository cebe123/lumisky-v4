package com.adnan.lumisky.data;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B9\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u0012\u0006\u0010\f\u001a\u00020\r\u00a2\u0006\u0004\b\u000e\u0010\u000fJ\u000e\u0010\u0016\u001a\u00020\u0017H\u0086@\u00a2\u0006\u0002\u0010\u0018J\u0018\u0010\u0019\u001a\u0004\u0018\u00010\u001a2\u0006\u0010\u001b\u001a\u00020\u001cH\u0086@\u00a2\u0006\u0002\u0010\u001dR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0011\u0010\n\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0011\u0010\f\u001a\u00020\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015\u00a8\u0006\u001e"}, d2 = {"Lcom/adnan/lumisky/data/WallpaperRepository;", "", "catalogDataSource", "Lcom/adnan/lumisky/data/WallpaperCatalogDataSource;", "localDataSource", "Lcom/adnan/lumisky/data/LocalWallpaperDataSource;", "parser", "Lcom/adnan/lumisky/definition/WallpaperDefinitionParser;", "settings", "Lcom/adnan/lumisky/data/SettingsRepository;", "entitlement", "Lcom/adnan/lumisky/data/EntitlementRepository;", "downloads", "Lcom/adnan/lumisky/data/AssetDownloadRepository;", "<init>", "(Lcom/adnan/lumisky/data/WallpaperCatalogDataSource;Lcom/adnan/lumisky/data/LocalWallpaperDataSource;Lcom/adnan/lumisky/definition/WallpaperDefinitionParser;Lcom/adnan/lumisky/data/SettingsRepository;Lcom/adnan/lumisky/data/EntitlementRepository;Lcom/adnan/lumisky/data/AssetDownloadRepository;)V", "getSettings", "()Lcom/adnan/lumisky/data/SettingsRepository;", "getEntitlement", "()Lcom/adnan/lumisky/data/EntitlementRepository;", "getDownloads", "()Lcom/adnan/lumisky/data/AssetDownloadRepository;", "getCatalog", "Lcom/adnan/lumisky/definition/CatalogDefinition;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getDefinition", "Lcom/adnan/lumisky/definition/WallpaperDefinition;", "wallpaperId", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_release"})
public final class WallpaperRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.data.WallpaperCatalogDataSource catalogDataSource = null;
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.data.LocalWallpaperDataSource localDataSource = null;
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.definition.WallpaperDefinitionParser parser = null;
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.data.SettingsRepository settings = null;
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.data.EntitlementRepository entitlement = null;
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.data.AssetDownloadRepository downloads = null;
    
    @javax.inject.Inject()
    public WallpaperRepository(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.data.WallpaperCatalogDataSource catalogDataSource, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.data.LocalWallpaperDataSource localDataSource, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.definition.WallpaperDefinitionParser parser, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.data.SettingsRepository settings, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.data.EntitlementRepository entitlement, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.data.AssetDownloadRepository downloads) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.data.SettingsRepository getSettings() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.data.EntitlementRepository getEntitlement() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.data.AssetDownloadRepository getDownloads() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getCatalog(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.adnan.lumisky.definition.CatalogDefinition> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getDefinition(@org.jetbrains.annotations.NotNull()
    java.lang.String wallpaperId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.adnan.lumisky.definition.WallpaperDefinition> $completion) {
        return null;
    }
}