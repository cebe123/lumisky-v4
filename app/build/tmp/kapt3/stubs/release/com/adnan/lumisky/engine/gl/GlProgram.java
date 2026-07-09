package com.adnan.lumisky.engine.gl;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0010%\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0010\u0007\n\u0002\b\u0006\n\u0002\u0010\u0014\n\u0000\u0018\u00002\u00020\u0001B\u000f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\b\u0010\u000b\u001a\u00020\fH\u0016J\u0006\u0010\r\u001a\u00020\fJ\u000e\u0010\u000e\u001a\u00020\u00032\u0006\u0010\u000f\u001a\u00020\nJ\u0016\u0010\u0010\u001a\u00020\f2\u0006\u0010\u000f\u001a\u00020\n2\u0006\u0010\u0011\u001a\u00020\u0012J\u0016\u0010\u0010\u001a\u00020\f2\u0006\u0010\u000f\u001a\u00020\n2\u0006\u0010\u0011\u001a\u00020\u0003J\u001e\u0010\u0010\u001a\u00020\f2\u0006\u0010\u000f\u001a\u00020\n2\u0006\u0010\u0013\u001a\u00020\u00122\u0006\u0010\u0014\u001a\u00020\u0012J&\u0010\u0010\u001a\u00020\f2\u0006\u0010\u000f\u001a\u00020\n2\u0006\u0010\u0013\u001a\u00020\u00122\u0006\u0010\u0014\u001a\u00020\u00122\u0006\u0010\u0015\u001a\u00020\u0012J.\u0010\u0010\u001a\u00020\f2\u0006\u0010\u000f\u001a\u00020\n2\u0006\u0010\u0013\u001a\u00020\u00122\u0006\u0010\u0014\u001a\u00020\u00122\u0006\u0010\u0015\u001a\u00020\u00122\u0006\u0010\u0016\u001a\u00020\u0012J\u0016\u0010\u0017\u001a\u00020\f2\u0006\u0010\u000f\u001a\u00020\n2\u0006\u0010\u0018\u001a\u00020\u0019R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007R\u001a\u0010\b\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u00030\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001a"}, d2 = {"Lcom/adnan/lumisky/engine/gl/GlProgram;", "Lcom/adnan/lumisky/engine/gl/GlResource;", "programId", "", "<init>", "(I)V", "getProgramId", "()I", "uniformLocations", "", "", "release", "", "use", "getUniformLocation", "name", "setUniform", "value", "", "x", "y", "z", "w", "setUniformMatrix", "matrix", "", "app_release"})
public final class GlProgram implements com.adnan.lumisky.engine.gl.GlResource {
    private final int programId = 0;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Map<java.lang.String, java.lang.Integer> uniformLocations = null;
    
    public GlProgram(int programId) {
        super();
    }
    
    public final int getProgramId() {
        return 0;
    }
    
    @java.lang.Override()
    public void release() {
    }
    
    public final void use() {
    }
    
    public final int getUniformLocation(@org.jetbrains.annotations.NotNull()
    java.lang.String name) {
        return 0;
    }
    
    public final void setUniform(@org.jetbrains.annotations.NotNull()
    java.lang.String name, float value) {
    }
    
    public final void setUniform(@org.jetbrains.annotations.NotNull()
    java.lang.String name, int value) {
    }
    
    public final void setUniform(@org.jetbrains.annotations.NotNull()
    java.lang.String name, float x, float y) {
    }
    
    public final void setUniform(@org.jetbrains.annotations.NotNull()
    java.lang.String name, float x, float y, float z) {
    }
    
    public final void setUniform(@org.jetbrains.annotations.NotNull()
    java.lang.String name, float x, float y, float z, float w) {
    }
    
    public final void setUniformMatrix(@org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    float[] matrix) {
    }
}