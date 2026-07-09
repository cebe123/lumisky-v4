package com.adnan.lumisky.data;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0019\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0004\b\u0006\u0010\u0007J\u0006\u0010\b\u001a\u00020\tR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\n"}, d2 = {"Lcom/adnan/lumisky/data/WallpaperCatalogDataSource;", "", "localDataSource", "Lcom/adnan/lumisky/data/LocalWallpaperDataSource;", "parser", "Lcom/adnan/lumisky/definition/WallpaperDefinitionParser;", "<init>", "(Lcom/adnan/lumisky/data/LocalWallpaperDataSource;Lcom/adnan/lumisky/definition/WallpaperDefinitionParser;)V", "loadCatalog", "Lcom/adnan/lumisky/definition/CatalogDefinition;", "app_debug"})
public final class WallpaperCatalogDataSource {
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.data.LocalWallpaperDataSource localDataSource = null;
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.definition.WallpaperDefinitionParser parser = null;
    
    @javax.inject.Inject()
    public WallpaperCatalogDataSource(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.data.LocalWallpaperDataSource localDataSource, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.definition.WallpaperDefinitionParser parser) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.definition.CatalogDefinition loadCatalog() {
        return null;
    }
}