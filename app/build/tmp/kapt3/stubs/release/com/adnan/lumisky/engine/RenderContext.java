package com.adnan.lumisky.engine;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\b\n\u0002\u0010\u0007\n\u0002\b\u0005\n\u0002\u0010\u0014\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\b\n\u0002\u0010\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010 \u001a\u00020!2\u0006\u0010\"\u001a\u00020\u0018R\u001a\u0010\u0004\u001a\u00020\u0005X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\tR\u001a\u0010\n\u001a\u00020\u0005X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\u0007\"\u0004\b\f\u0010\tR\u001a\u0010\r\u001a\u00020\u000eX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000f\u0010\u0010\"\u0004\b\u0011\u0010\u0012R\u0011\u0010\u0013\u001a\u00020\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u001a\u0010\u0017\u001a\u00020\u0018X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0019\u0010\u001a\"\u0004\b\u001b\u0010\u001cR\u001a\u0010\u001d\u001a\u00020\u000eX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001e\u0010\u0010\"\u0004\b\u001f\u0010\u0012\u00a8\u0006#"}, d2 = {"Lcom/adnan/lumisky/engine/RenderContext;", "", "<init>", "()V", "width", "", "getWidth", "()I", "setWidth", "(I)V", "height", "getHeight", "setHeight", "aspect", "", "getAspect", "()F", "setAspect", "(F)V", "projectionMatrix", "", "getProjectionMatrix", "()[F", "frameTimeNanos", "", "getFrameTimeNanos", "()J", "setFrameTimeNanos", "(J)V", "deltaTimeSeconds", "getDeltaTimeSeconds", "setDeltaTimeSeconds", "update", "", "timeNanos", "app_release"})
public final class RenderContext {
    private int width = 0;
    private int height = 0;
    private float aspect = 1.0F;
    @org.jetbrains.annotations.NotNull()
    private final float[] projectionMatrix = null;
    private long frameTimeNanos = 0L;
    private float deltaTimeSeconds = 0.0F;
    
    public RenderContext() {
        super();
    }
    
    public final int getWidth() {
        return 0;
    }
    
    public final void setWidth(int p0) {
    }
    
    public final int getHeight() {
        return 0;
    }
    
    public final void setHeight(int p0) {
    }
    
    public final float getAspect() {
        return 0.0F;
    }
    
    public final void setAspect(float p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final float[] getProjectionMatrix() {
        return null;
    }
    
    public final long getFrameTimeNanos() {
        return 0L;
    }
    
    public final void setFrameTimeNanos(long p0) {
    }
    
    public final float getDeltaTimeSeconds() {
        return 0.0F;
    }
    
    public final void setDeltaTimeSeconds(float p0) {
    }
    
    public final void update(long timeNanos) {
    }
}