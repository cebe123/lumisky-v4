package com.adnan.lumisky.definition;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0011\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\u000e\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000bJ\u000e\u0010\f\u001a\u00020\r2\u0006\u0010\n\u001a\u00020\u000bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000e"}, d2 = {"Lcom/adnan/lumisky/definition/WallpaperDefinitionParser;", "", "schemaMigrator", "Lcom/adnan/lumisky/definition/SchemaMigrator;", "<init>", "(Lcom/adnan/lumisky/definition/SchemaMigrator;)V", "json", "Lkotlinx/serialization/json/Json;", "parseCatalog", "Lcom/adnan/lumisky/definition/CatalogDefinition;", "jsonString", "", "parseWallpaper", "Lcom/adnan/lumisky/definition/WallpaperParseResult;", "app_release"})
public final class WallpaperDefinitionParser {
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.definition.SchemaMigrator schemaMigrator = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.serialization.json.Json json = null;
    
    @javax.inject.Inject()
    public WallpaperDefinitionParser(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.definition.SchemaMigrator schemaMigrator) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.definition.CatalogDefinition parseCatalog(@org.jetbrains.annotations.NotNull()
    java.lang.String jsonString) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.definition.WallpaperParseResult parseWallpaper(@org.jetbrains.annotations.NotNull()
    java.lang.String jsonString) {
        return null;
    }
}