package com.adnan.lumisky.assets;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fJ\u0016\u0010\r\u001a\u00020\f2\u0006\u0010\u000e\u001a\u00020\f2\u0006\u0010\u000f\u001a\u00020\fR\u001e\u0010\u0006\u001a\u00020\u00052\u0006\u0010\u0004\u001a\u00020\u0005@BX\u0086\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007R\u001e\u0010\b\u001a\u00020\u00052\u0006\u0010\u0004\u001a\u00020\u0005@BX\u0086\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\u0007\u00a8\u0006\u0010"}, d2 = {"Lcom/adnan/lumisky/assets/TextureFormatResolver;", "", "<init>", "()V", "value", "", "isAstcSupported", "()Z", "isEtc2Supported", "initialize", "", "extensions", "", "selectBestFormat", "preferredAstcPath", "fallbackPath", "app_debug"})
public final class TextureFormatResolver {
    private static boolean isAstcSupported = false;
    private static boolean isEtc2Supported = true;
    @org.jetbrains.annotations.NotNull()
    public static final com.adnan.lumisky.assets.TextureFormatResolver INSTANCE = null;
    
    private TextureFormatResolver() {
        super();
    }
    
    public final boolean isAstcSupported() {
        return false;
    }
    
    public final boolean isEtc2Supported() {
        return false;
    }
    
    public final void initialize(@org.jetbrains.annotations.NotNull()
    java.lang.String extensions) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String selectBestFormat(@org.jetbrains.annotations.NotNull()
    java.lang.String preferredAstcPath, @org.jetbrains.annotations.NotNull()
    java.lang.String fallbackPath) {
        return null;
    }
}