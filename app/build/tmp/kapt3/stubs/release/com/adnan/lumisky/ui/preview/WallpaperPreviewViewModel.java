package com.adnan.lumisky.ui.preview;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\b\u0007\u0018\u00002\u00020\u0001B\u0011\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\u000e\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u0013J\u000e\u0010\u0014\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u0013J\u0016\u0010\u0015\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u0013H\u0086@\u00a2\u0006\u0002\u0010\u0016R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0006\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\b0\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\t\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\b0\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0017\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000e0\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\f\u00a8\u0006\u0017"}, d2 = {"Lcom/adnan/lumisky/ui/preview/WallpaperPreviewViewModel;", "Landroidx/lifecycle/ViewModel;", "repository", "Lcom/adnan/lumisky/data/WallpaperRepository;", "<init>", "(Lcom/adnan/lumisky/data/WallpaperRepository;)V", "_definition", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/adnan/lumisky/definition/WallpaperDefinition;", "definition", "Lkotlinx/coroutines/flow/StateFlow;", "getDefinition", "()Lkotlinx/coroutines/flow/StateFlow;", "previewTimeSimulation", "", "getPreviewTimeSimulation", "loadWallpaper", "", "id", "", "setWallpaper", "setWallpaperNow", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_release"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class WallpaperPreviewViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.data.WallpaperRepository repository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.adnan.lumisky.definition.WallpaperDefinition> _definition = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.adnan.lumisky.definition.WallpaperDefinition> definition = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> previewTimeSimulation = null;
    
    @javax.inject.Inject()
    public WallpaperPreviewViewModel(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.data.WallpaperRepository repository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.adnan.lumisky.definition.WallpaperDefinition> getDefinition() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getPreviewTimeSimulation() {
        return null;
    }
    
    public final void loadWallpaper(@org.jetbrains.annotations.NotNull()
    java.lang.String id) {
    }
    
    public final void setWallpaper(@org.jetbrains.annotations.NotNull()
    java.lang.String id) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object setWallpaperNow(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}