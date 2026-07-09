package com.adnan.lumisky.preview;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\t\b\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\u0005J\u000e\u0010\t\u001a\u00020\n2\u0006\u0010\b\u001a\u00020\u0005J\u0006\u0010\u000b\u001a\u00020\u0007R\u0010\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\f"}, d2 = {"Lcom/adnan/lumisky/preview/PreviewSurfaceController;", "", "<init>", "()V", "activeHolder", "Landroid/view/SurfaceHolder;", "claimSurface", "", "holder", "releaseSurface", "", "hasActiveSurface", "app_debug"})
public final class PreviewSurfaceController {
    @org.jetbrains.annotations.Nullable()
    private android.view.SurfaceHolder activeHolder;
    
    @javax.inject.Inject()
    public PreviewSurfaceController() {
        super();
    }
    
    public final boolean claimSurface(@org.jetbrains.annotations.NotNull()
    android.view.SurfaceHolder holder) {
        return false;
    }
    
    public final void releaseSurface(@org.jetbrains.annotations.NotNull()
    android.view.SurfaceHolder holder) {
    }
    
    public final boolean hasActiveSurface() {
        return false;
    }
}