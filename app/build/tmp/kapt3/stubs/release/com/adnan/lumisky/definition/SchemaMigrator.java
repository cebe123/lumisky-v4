package com.adnan.lumisky.definition;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\t\b\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tJ\u0010\u0010\n\u001a\u00020\t2\u0006\u0010\u000b\u001a\u00020\tH\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082D\u00a2\u0006\u0002\n\u0000\u00a8\u0006\f"}, d2 = {"Lcom/adnan/lumisky/definition/SchemaMigrator;", "", "<init>", "()V", "latestVersion", "", "migrateToLatest", "Lcom/adnan/lumisky/definition/SchemaMigrationResult;", "rawJson", "Lkotlinx/serialization/json/JsonObject;", "migrateOldSchemaToV5", "oldJson", "app_release"})
public final class SchemaMigrator {
    private final int latestVersion = 5;
    
    @javax.inject.Inject()
    public SchemaMigrator() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.definition.SchemaMigrationResult migrateToLatest(@org.jetbrains.annotations.NotNull()
    kotlinx.serialization.json.JsonObject rawJson) {
        return null;
    }
    
    private final kotlinx.serialization.json.JsonObject migrateOldSchemaToV5(kotlinx.serialization.json.JsonObject oldJson) {
        return null;
    }
}