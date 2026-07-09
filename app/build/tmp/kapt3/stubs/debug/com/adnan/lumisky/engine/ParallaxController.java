package com.adnan.lumisky.engine;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u0007\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\t\b\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J0\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u00052\u0006\u0010\n\u001a\u00020\u00052\b\u0010\u000b\u001a\u0004\u0018\u00010\f2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u0010R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0011"}, d2 = {"Lcom/adnan/lumisky/engine/ParallaxController;", "", "<init>", "()V", "currentX", "", "currentY", "update", "", "targetX", "targetY", "definition", "Lcom/adnan/lumisky/definition/WallpaperDefinition;", "state", "Lcom/adnan/lumisky/engine/SceneState;", "frameState", "Lcom/adnan/lumisky/engine/MutableRenderFrameState;", "app_debug"})
public final class ParallaxController {
    private float currentX = 0.0F;
    private float currentY = 0.0F;
    
    @javax.inject.Inject()
    public ParallaxController() {
        super();
    }
    
    public final void update(float targetX, float targetY, @org.jetbrains.annotations.Nullable()
    com.adnan.lumisky.definition.WallpaperDefinition definition, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.SceneState state, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.MutableRenderFrameState frameState) {
    }
}