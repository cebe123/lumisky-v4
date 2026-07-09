package com.adnan.lumisky.ui.catalog;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0002\b\n\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0007\n\u0002\b\u0006\n\u0002\u0010\t\n\u0002\b\n\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J6\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\u00072\u0006\u0010\t\u001a\u00020\u00072\u0006\u0010\n\u001a\u00020\u00072\u0006\u0010\u000b\u001a\u00020\u00052\u0006\u0010\f\u001a\u00020\u0005J&\u0010\r\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\u00072\u0006\u0010\t\u001a\u00020\u00072\u0006\u0010\n\u001a\u00020\u0007J\u0016\u0010\u000e\u001a\u00020\u00072\u0006\u0010\u000f\u001a\u00020\u00072\u0006\u0010\u0010\u001a\u00020\u0007J\u000e\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u0014J%\u0010\u0015\u001a\u00020\u00122\b\u0010\u0016\u001a\u0004\u0018\u00010\u00142\u0006\u0010\u0017\u001a\u00020\u00072\u0006\u0010\u0018\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\u0019J\u0006\u0010\u001a\u001a\u00020\u001bJ\u000e\u0010\u001c\u001a\u00020\u00142\u0006\u0010\u001d\u001a\u00020\u001bJ\u0016\u0010\u001e\u001a\u00020\u00052\u0006\u0010\u001f\u001a\u00020\u00052\u0006\u0010 \u001a\u00020\u0005J\u0016\u0010!\u001a\u00020\u00052\u0006\u0010\u000b\u001a\u00020\u00052\u0006\u0010\f\u001a\u00020\u0005R\u000e\u0010\"\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010#\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010$\u001a\u00020\u0014X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006%"}, d2 = {"Lcom/adnan/lumisky/ui/catalog/CatalogPreviewPolicy;", "", "<init>", "()V", "shouldRenderLivePreview", "", "sectionIndex", "", "activeSectionIndex", "itemIndex", "centeredItemIndex", "parentScrollInProgress", "rowScrollInProgress", "shouldMountLivePreview", "resolveActiveSectionIndex", "centeredIndex", "sectionCount", "formatLoopTime", "", "progress", "", "formatBadgeTime", "rendererDayProgress", "fallbackHour", "fallbackMinute", "(Ljava/lang/Float;II)Ljava/lang/String;", "livePreviewBadgeTickMillis", "", "loopProgressForElapsedMillis", "elapsedMillis", "shouldStartLivePreview", "showLivePreview", "warmupReady", "shouldRenderCardChrome", "MinutesPerHour", "MinutesPerDay", "LoopProgressPerSecond", "app_release"})
public final class CatalogPreviewPolicy {
    private static final int MinutesPerHour = 60;
    private static final int MinutesPerDay = 1440;
    private static final float LoopProgressPerSecond = 0.08333F;
    @org.jetbrains.annotations.NotNull()
    public static final com.adnan.lumisky.ui.catalog.CatalogPreviewPolicy INSTANCE = null;
    
    private CatalogPreviewPolicy() {
        super();
    }
    
    public final boolean shouldRenderLivePreview(int sectionIndex, int activeSectionIndex, int itemIndex, int centeredItemIndex, boolean parentScrollInProgress, boolean rowScrollInProgress) {
        return false;
    }
    
    public final boolean shouldMountLivePreview(int sectionIndex, int activeSectionIndex, int itemIndex, int centeredItemIndex) {
        return false;
    }
    
    public final int resolveActiveSectionIndex(int centeredIndex, int sectionCount) {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String formatLoopTime(float progress) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String formatBadgeTime(@org.jetbrains.annotations.Nullable()
    java.lang.Float rendererDayProgress, int fallbackHour, int fallbackMinute) {
        return null;
    }
    
    public final long livePreviewBadgeTickMillis() {
        return 0L;
    }
    
    public final float loopProgressForElapsedMillis(long elapsedMillis) {
        return 0.0F;
    }
    
    public final boolean shouldStartLivePreview(boolean showLivePreview, boolean warmupReady) {
        return false;
    }
    
    public final boolean shouldRenderCardChrome(boolean parentScrollInProgress, boolean rowScrollInProgress) {
        return false;
    }
}