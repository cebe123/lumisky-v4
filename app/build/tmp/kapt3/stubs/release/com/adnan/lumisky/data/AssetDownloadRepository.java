package com.adnan.lumisky.data;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\t\b\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0014\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\u0006\u0010\u0007\u001a\u00020\bJ\u000e\u0010\t\u001a\u00020\n2\u0006\u0010\u0007\u001a\u00020\b\u00a8\u0006\u000b"}, d2 = {"Lcom/adnan/lumisky/data/AssetDownloadRepository;", "", "<init>", "()V", "getDownloadState", "Lkotlinx/coroutines/flow/Flow;", "Lcom/adnan/lumisky/assets/AssetPackState;", "packName", "", "startDownload", "", "app_release"})
public final class AssetDownloadRepository {
    
    @javax.inject.Inject()
    public AssetDownloadRepository() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<com.adnan.lumisky.assets.AssetPackState> getDownloadState(@org.jetbrains.annotations.NotNull()
    java.lang.String packName) {
        return null;
    }
    
    public final void startDownload(@org.jetbrains.annotations.NotNull()
    java.lang.String packName) {
    }
}