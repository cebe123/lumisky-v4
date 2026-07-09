package com.adnan.lumisky.telemetry;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\bv\u0018\u00002\u00020\u0001:\u0006\u0002\u0003\u0004\u0005\u0006\u0007\u0082\u0001\u0006\b\t\n\u000b\f\r\u00a8\u0006\u000e\u00c0\u0006\u0003"}, d2 = {"Lcom/adnan/lumisky/telemetry/RenderTelemetryEvent;", "", "ShaderCompileFailed", "AssetMissing", "FallbackActivated", "ContextRestoreFailed", "FrameBudgetExceeded", "ThermalEmergencyDegrade", "Lcom/adnan/lumisky/telemetry/RenderTelemetryEvent$AssetMissing;", "Lcom/adnan/lumisky/telemetry/RenderTelemetryEvent$ContextRestoreFailed;", "Lcom/adnan/lumisky/telemetry/RenderTelemetryEvent$FallbackActivated;", "Lcom/adnan/lumisky/telemetry/RenderTelemetryEvent$FrameBudgetExceeded;", "Lcom/adnan/lumisky/telemetry/RenderTelemetryEvent$ShaderCompileFailed;", "Lcom/adnan/lumisky/telemetry/RenderTelemetryEvent$ThermalEmergencyDegrade;", "app_debug"})
public abstract interface RenderTelemetryEvent {
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\r\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B!\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\b\u0010\u0005\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\u0004\b\u0006\u0010\u0007J\t\u0010\f\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\r\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010\u000e\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J)\u0010\u000f\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u0003H\u00c6\u0001J\u0013\u0010\u0010\u001a\u00020\u00112\b\u0010\u0012\u001a\u0004\u0018\u00010\u0013H\u00d6\u0003J\t\u0010\u0014\u001a\u00020\u0015H\u00d6\u0001J\t\u0010\u0016\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\tR\u0013\u0010\u0005\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\t\u00a8\u0006\u0017"}, d2 = {"Lcom/adnan/lumisky/telemetry/RenderTelemetryEvent$AssetMissing;", "Lcom/adnan/lumisky/telemetry/RenderTelemetryEvent;", "wallpaperId", "", "pathHash", "assetPack", "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "getWallpaperId", "()Ljava/lang/String;", "getPathHash", "getAssetPack", "component1", "component2", "component3", "copy", "equals", "", "other", "", "hashCode", "", "toString", "app_debug"})
    public static final class AssetMissing implements com.adnan.lumisky.telemetry.RenderTelemetryEvent {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String wallpaperId = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String pathHash = null;
        @org.jetbrains.annotations.Nullable()
        private final java.lang.String assetPack = null;
        
        public AssetMissing(@org.jetbrains.annotations.NotNull()
        java.lang.String wallpaperId, @org.jetbrains.annotations.NotNull()
        java.lang.String pathHash, @org.jetbrains.annotations.Nullable()
        java.lang.String assetPack) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getWallpaperId() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getPathHash() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String getAssetPack() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String component3() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.adnan.lumisky.telemetry.RenderTelemetryEvent.AssetMissing copy(@org.jetbrains.annotations.NotNull()
        java.lang.String wallpaperId, @org.jetbrains.annotations.NotNull()
        java.lang.String pathHash, @org.jetbrains.annotations.Nullable()
        java.lang.String assetPack) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\n\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0005\u0010\u0006J\t\u0010\n\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u000b\u001a\u00020\u0003H\u00c6\u0003J\u001d\u0010\f\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\r\u001a\u00020\u000e2\b\u0010\u000f\u001a\u0004\u0018\u00010\u0010H\u00d6\u0003J\t\u0010\u0011\u001a\u00020\u0012H\u00d6\u0001J\t\u0010\u0013\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\b\u00a8\u0006\u0014"}, d2 = {"Lcom/adnan/lumisky/telemetry/RenderTelemetryEvent$ContextRestoreFailed;", "Lcom/adnan/lumisky/telemetry/RenderTelemetryEvent;", "wallpaperId", "", "glRenderer", "<init>", "(Ljava/lang/String;Ljava/lang/String;)V", "getWallpaperId", "()Ljava/lang/String;", "getGlRenderer", "component1", "component2", "copy", "equals", "", "other", "", "hashCode", "", "toString", "app_debug"})
    public static final class ContextRestoreFailed implements com.adnan.lumisky.telemetry.RenderTelemetryEvent {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String wallpaperId = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String glRenderer = null;
        
        public ContextRestoreFailed(@org.jetbrains.annotations.NotNull()
        java.lang.String wallpaperId, @org.jetbrains.annotations.NotNull()
        java.lang.String glRenderer) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getWallpaperId() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getGlRenderer() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.adnan.lumisky.telemetry.RenderTelemetryEvent.ContextRestoreFailed copy(@org.jetbrains.annotations.NotNull()
        java.lang.String wallpaperId, @org.jetbrains.annotations.NotNull()
        java.lang.String glRenderer) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\r\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u001f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0006\u0010\u0007J\t\u0010\f\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\r\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u000e\u001a\u00020\u0003H\u00c6\u0003J\'\u0010\u000f\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\u0010\u001a\u00020\u00112\b\u0010\u0012\u001a\u0004\u0018\u00010\u0013H\u00d6\u0003J\t\u0010\u0014\u001a\u00020\u0015H\u00d6\u0001J\t\u0010\u0016\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\tR\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\t\u00a8\u0006\u0017"}, d2 = {"Lcom/adnan/lumisky/telemetry/RenderTelemetryEvent$FallbackActivated;", "Lcom/adnan/lumisky/telemetry/RenderTelemetryEvent;", "wallpaperId", "", "fallbackType", "reason", "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "getWallpaperId", "()Ljava/lang/String;", "getFallbackType", "getReason", "component1", "component2", "component3", "copy", "equals", "", "other", "", "hashCode", "", "toString", "app_debug"})
    public static final class FallbackActivated implements com.adnan.lumisky.telemetry.RenderTelemetryEvent {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String wallpaperId = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String fallbackType = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String reason = null;
        
        public FallbackActivated(@org.jetbrains.annotations.NotNull()
        java.lang.String wallpaperId, @org.jetbrains.annotations.NotNull()
        java.lang.String fallbackType, @org.jetbrains.annotations.NotNull()
        java.lang.String reason) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getWallpaperId() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getFallbackType() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getReason() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component3() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.adnan.lumisky.telemetry.RenderTelemetryEvent.FallbackActivated copy(@org.jetbrains.annotations.NotNull()
        java.lang.String wallpaperId, @org.jetbrains.annotations.NotNull()
        java.lang.String fallbackType, @org.jetbrains.annotations.NotNull()
        java.lang.String reason) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0007\n\u0000\n\u0002\u0018\u0002\n\u0002\b\r\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u001f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0004\b\b\u0010\tJ\t\u0010\u0010\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0011\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0012\u001a\u00020\u0007H\u00c6\u0003J\'\u0010\u0013\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u0007H\u00c6\u0001J\u0013\u0010\u0014\u001a\u00020\u00152\b\u0010\u0016\u001a\u0004\u0018\u00010\u0017H\u00d6\u0003J\t\u0010\u0018\u001a\u00020\u0019H\u00d6\u0001J\t\u0010\u001a\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000f\u00a8\u0006\u001b"}, d2 = {"Lcom/adnan/lumisky/telemetry/RenderTelemetryEvent$FrameBudgetExceeded;", "Lcom/adnan/lumisky/telemetry/RenderTelemetryEvent;", "wallpaperId", "", "p95FrameMs", "", "qualityTier", "Lcom/adnan/lumisky/definition/QualityTier;", "<init>", "(Ljava/lang/String;FLcom/adnan/lumisky/definition/QualityTier;)V", "getWallpaperId", "()Ljava/lang/String;", "getP95FrameMs", "()F", "getQualityTier", "()Lcom/adnan/lumisky/definition/QualityTier;", "component1", "component2", "component3", "copy", "equals", "", "other", "", "hashCode", "", "toString", "app_debug"})
    public static final class FrameBudgetExceeded implements com.adnan.lumisky.telemetry.RenderTelemetryEvent {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String wallpaperId = null;
        private final float p95FrameMs = 0.0F;
        @org.jetbrains.annotations.NotNull()
        private final com.adnan.lumisky.definition.QualityTier qualityTier = null;
        
        public FrameBudgetExceeded(@org.jetbrains.annotations.NotNull()
        java.lang.String wallpaperId, float p95FrameMs, @org.jetbrains.annotations.NotNull()
        com.adnan.lumisky.definition.QualityTier qualityTier) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getWallpaperId() {
            return null;
        }
        
        public final float getP95FrameMs() {
            return 0.0F;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.adnan.lumisky.definition.QualityTier getQualityTier() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component1() {
            return null;
        }
        
        public final float component2() {
            return 0.0F;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.adnan.lumisky.definition.QualityTier component3() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.adnan.lumisky.telemetry.RenderTelemetryEvent.FrameBudgetExceeded copy(@org.jetbrains.annotations.NotNull()
        java.lang.String wallpaperId, float p95FrameMs, @org.jetbrains.annotations.NotNull()
        com.adnan.lumisky.definition.QualityTier qualityTier) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\r\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u001f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0006\u0010\u0007J\t\u0010\f\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\r\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u000e\u001a\u00020\u0003H\u00c6\u0003J\'\u0010\u000f\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\u0010\u001a\u00020\u00112\b\u0010\u0012\u001a\u0004\u0018\u00010\u0013H\u00d6\u0003J\t\u0010\u0014\u001a\u00020\u0015H\u00d6\u0001J\t\u0010\u0016\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\tR\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\t\u00a8\u0006\u0017"}, d2 = {"Lcom/adnan/lumisky/telemetry/RenderTelemetryEvent$ShaderCompileFailed;", "Lcom/adnan/lumisky/telemetry/RenderTelemetryEvent;", "shaderRef", "", "glRenderer", "logHash", "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "getShaderRef", "()Ljava/lang/String;", "getGlRenderer", "getLogHash", "component1", "component2", "component3", "copy", "equals", "", "other", "", "hashCode", "", "toString", "app_debug"})
    public static final class ShaderCompileFailed implements com.adnan.lumisky.telemetry.RenderTelemetryEvent {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String shaderRef = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String glRenderer = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String logHash = null;
        
        public ShaderCompileFailed(@org.jetbrains.annotations.NotNull()
        java.lang.String shaderRef, @org.jetbrains.annotations.NotNull()
        java.lang.String glRenderer, @org.jetbrains.annotations.NotNull()
        java.lang.String logHash) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getShaderRef() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getGlRenderer() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getLogHash() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component3() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.adnan.lumisky.telemetry.RenderTelemetryEvent.ShaderCompileFailed copy(@org.jetbrains.annotations.NotNull()
        java.lang.String shaderRef, @org.jetbrains.annotations.NotNull()
        java.lang.String glRenderer, @org.jetbrains.annotations.NotNull()
        java.lang.String logHash) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\r\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0002\b\u0003\b\u0086\b\u0018\u00002\u00020\u0001B\u001f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0005\u00a2\u0006\u0004\b\u0007\u0010\bJ\t\u0010\u000e\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u000f\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0010\u001a\u00020\u0005H\u00c6\u0003J\'\u0010\u0011\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u0005H\u00c6\u0001J\u0013\u0010\u0012\u001a\u00020\u00132\b\u0010\u0014\u001a\u0004\u0018\u00010\u0015H\u00d6\u0003J\t\u0010\u0016\u001a\u00020\u0005H\u00d6\u0001J\t\u0010\u0017\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0011\u0010\u0006\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\f\u00a8\u0006\u0018"}, d2 = {"Lcom/adnan/lumisky/telemetry/RenderTelemetryEvent$ThermalEmergencyDegrade;", "Lcom/adnan/lumisky/telemetry/RenderTelemetryEvent;", "wallpaperId", "", "thermalStatus", "", "appliedSceneMaxFps", "<init>", "(Ljava/lang/String;II)V", "getWallpaperId", "()Ljava/lang/String;", "getThermalStatus", "()I", "getAppliedSceneMaxFps", "component1", "component2", "component3", "copy", "equals", "", "other", "", "hashCode", "toString", "app_debug"})
    public static final class ThermalEmergencyDegrade implements com.adnan.lumisky.telemetry.RenderTelemetryEvent {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String wallpaperId = null;
        private final int thermalStatus = 0;
        private final int appliedSceneMaxFps = 0;
        
        public ThermalEmergencyDegrade(@org.jetbrains.annotations.NotNull()
        java.lang.String wallpaperId, int thermalStatus, int appliedSceneMaxFps) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getWallpaperId() {
            return null;
        }
        
        public final int getThermalStatus() {
            return 0;
        }
        
        public final int getAppliedSceneMaxFps() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component1() {
            return null;
        }
        
        public final int component2() {
            return 0;
        }
        
        public final int component3() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.adnan.lumisky.telemetry.RenderTelemetryEvent.ThermalEmergencyDegrade copy(@org.jetbrains.annotations.NotNull()
        java.lang.String wallpaperId, int thermalStatus, int appliedSceneMaxFps) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
}