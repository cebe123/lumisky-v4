package com.adnan.lumisky.preview;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0011\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\u0006\u0010\u0006\u001a\u00020\u0007R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\b"}, d2 = {"Lcom/adnan/lumisky/preview/CardPreviewController;", "", "surfaceController", "Lcom/adnan/lumisky/preview/PreviewSurfaceController;", "<init>", "(Lcom/adnan/lumisky/preview/PreviewSurfaceController;)V", "shouldRenderLive", "", "app_release"})
public final class CardPreviewController {
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.preview.PreviewSurfaceController surfaceController = null;
    
    @javax.inject.Inject()
    public CardPreviewController(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.preview.PreviewSurfaceController surfaceController) {
        super();
    }
    
    public final boolean shouldRenderLive() {
        return false;
    }
}