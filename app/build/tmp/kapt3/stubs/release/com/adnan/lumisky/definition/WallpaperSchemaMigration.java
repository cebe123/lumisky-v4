package com.adnan.lumisky.definition;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J\u0010\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\tH&R\u0012\u0010\u0002\u001a\u00020\u0003X\u00a6\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0004\u0010\u0005R\u0012\u0010\u0006\u001a\u00020\u0003X\u00a6\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0007\u0010\u0005\u00a8\u0006\u000b\u00c0\u0006\u0003"}, d2 = {"Lcom/adnan/lumisky/definition/WallpaperSchemaMigration;", "", "fromVersion", "", "getFromVersion", "()I", "toVersion", "getToVersion", "migrate", "Lkotlinx/serialization/json/JsonObject;", "rawJson", "app_release"})
public abstract interface WallpaperSchemaMigration {
    
    public abstract int getFromVersion();
    
    public abstract int getToVersion();
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.serialization.json.JsonObject migrate(@org.jetbrains.annotations.NotNull()
    kotlinx.serialization.json.JsonObject rawJson);
}