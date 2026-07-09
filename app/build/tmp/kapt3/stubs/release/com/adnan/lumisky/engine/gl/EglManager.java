package com.adnan.lumisky.engine.gl;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0006\u0010\f\u001a\u00020\rJ\u000e\u0010\u000e\u001a\u00020\r2\u0006\u0010\u000f\u001a\u00020\u0010J\u0016\u0010\u0011\u001a\u00020\r2\u0006\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u0013J\u0006\u0010\u0015\u001a\u00020\rJ\u0006\u0010\u0016\u001a\u00020\rJ\u0006\u0010\u0017\u001a\u00020\u0018J\u0006\u0010\u0019\u001a\u00020\rJ\u0006\u0010\u001a\u001a\u00020\rR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\n\u001a\u0004\u0018\u00010\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001b"}, d2 = {"Lcom/adnan/lumisky/engine/gl/EglManager;", "", "<init>", "()V", "eglDisplay", "Landroid/opengl/EGLDisplay;", "eglContext", "Landroid/opengl/EGLContext;", "eglSurface", "Landroid/opengl/EGLSurface;", "eglConfig", "Landroid/opengl/EGLConfig;", "initialize", "", "createSurface", "surfaceHolder", "Landroid/view/SurfaceHolder;", "createOffscreenSurface", "width", "", "height", "makeCurrent", "makeUncurrent", "swapBuffers", "", "destroySurface", "release", "app_release"})
public final class EglManager {
    @org.jetbrains.annotations.NotNull()
    private android.opengl.EGLDisplay eglDisplay;
    @org.jetbrains.annotations.NotNull()
    private android.opengl.EGLContext eglContext;
    @org.jetbrains.annotations.NotNull()
    private android.opengl.EGLSurface eglSurface;
    @org.jetbrains.annotations.Nullable()
    private android.opengl.EGLConfig eglConfig;
    
    public EglManager() {
        super();
    }
    
    public final void initialize() {
    }
    
    public final void createSurface(@org.jetbrains.annotations.NotNull()
    android.view.SurfaceHolder surfaceHolder) {
    }
    
    public final void createOffscreenSurface(int width, int height) {
    }
    
    public final void makeCurrent() {
    }
    
    public final void makeUncurrent() {
    }
    
    public final boolean swapBuffers() {
        return false;
    }
    
    public final void destroySurface() {
    }
    
    public final void release() {
    }
}