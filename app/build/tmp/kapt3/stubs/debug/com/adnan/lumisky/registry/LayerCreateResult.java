package com.adnan.lumisky.registry;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\bv\u0018\u00002\u00020\u0001:\u0003\u0002\u0003\u0004\u0082\u0001\u0003\u0005\u0006\u0007\u00a8\u0006\b\u00c0\u0006\u0003"}, d2 = {"Lcom/adnan/lumisky/registry/LayerCreateResult;", "", "Created", "UnknownType", "CreateFailed", "Lcom/adnan/lumisky/registry/LayerCreateResult$CreateFailed;", "Lcom/adnan/lumisky/registry/LayerCreateResult$Created;", "Lcom/adnan/lumisky/registry/LayerCreateResult$UnknownType;", "app_debug"})
public abstract interface LayerCreateResult {
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0003\n\u0002\b\u0010\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\'\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\u0006\u0010\u0007\u001a\u00020\b\u00a2\u0006\u0004\b\t\u0010\nJ\t\u0010\u0012\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0013\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0014\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\u0015\u001a\u00020\bH\u00c6\u0003J1\u0010\u0016\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\bH\u00c6\u0001J\u0013\u0010\u0017\u001a\u00020\u00062\b\u0010\u0018\u001a\u0004\u0018\u00010\u0019H\u00d6\u0003J\t\u0010\u001a\u001a\u00020\u001bH\u00d6\u0001J\t\u0010\u001c\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\fR\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011\u00a8\u0006\u001d"}, d2 = {"Lcom/adnan/lumisky/registry/LayerCreateResult$CreateFailed;", "Lcom/adnan/lumisky/registry/LayerCreateResult;", "layerId", "", "type", "required", "", "cause", "", "<init>", "(Ljava/lang/String;Ljava/lang/String;ZLjava/lang/Throwable;)V", "getLayerId", "()Ljava/lang/String;", "getType", "getRequired", "()Z", "getCause", "()Ljava/lang/Throwable;", "component1", "component2", "component3", "component4", "copy", "equals", "other", "", "hashCode", "", "toString", "app_debug"})
    public static final class CreateFailed implements com.adnan.lumisky.registry.LayerCreateResult {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String layerId = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String type = null;
        private final boolean required = false;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.Throwable cause = null;
        
        public CreateFailed(@org.jetbrains.annotations.NotNull()
        java.lang.String layerId, @org.jetbrains.annotations.NotNull()
        java.lang.String type, boolean required, @org.jetbrains.annotations.NotNull()
        java.lang.Throwable cause) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getLayerId() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getType() {
            return null;
        }
        
        public final boolean getRequired() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.Throwable getCause() {
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
        
        public final boolean component3() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.Throwable component4() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.adnan.lumisky.registry.LayerCreateResult.CreateFailed copy(@org.jetbrains.annotations.NotNull()
        java.lang.String layerId, @org.jetbrains.annotations.NotNull()
        java.lang.String type, boolean required, @org.jetbrains.annotations.NotNull()
        java.lang.Throwable cause) {
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
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B\u000f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\t\u0010\b\u001a\u00020\u0003H\u00c6\u0003J\u0013\u0010\t\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\n\u001a\u00020\u000b2\b\u0010\f\u001a\u0004\u0018\u00010\rH\u00d6\u0003J\t\u0010\u000e\u001a\u00020\u000fH\u00d6\u0001J\t\u0010\u0010\u001a\u00020\u0011H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007\u00a8\u0006\u0012"}, d2 = {"Lcom/adnan/lumisky/registry/LayerCreateResult$Created;", "Lcom/adnan/lumisky/registry/LayerCreateResult;", "layer", "Lcom/adnan/lumisky/layers/RenderLayer;", "<init>", "(Lcom/adnan/lumisky/layers/RenderLayer;)V", "getLayer", "()Lcom/adnan/lumisky/layers/RenderLayer;", "component1", "copy", "equals", "", "other", "", "hashCode", "", "toString", "", "app_debug"})
    public static final class Created implements com.adnan.lumisky.registry.LayerCreateResult {
        @org.jetbrains.annotations.NotNull()
        private final com.adnan.lumisky.layers.RenderLayer layer = null;
        
        public Created(@org.jetbrains.annotations.NotNull()
        com.adnan.lumisky.layers.RenderLayer layer) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.adnan.lumisky.layers.RenderLayer getLayer() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.adnan.lumisky.layers.RenderLayer component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.adnan.lumisky.registry.LayerCreateResult.Created copy(@org.jetbrains.annotations.NotNull()
        com.adnan.lumisky.layers.RenderLayer layer) {
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
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\r\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u001f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\u0004\b\u0007\u0010\bJ\t\u0010\u000e\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u000f\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0010\u001a\u00020\u0006H\u00c6\u0003J\'\u0010\u0011\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u0006H\u00c6\u0001J\u0013\u0010\u0012\u001a\u00020\u00062\b\u0010\u0013\u001a\u0004\u0018\u00010\u0014H\u00d6\u0003J\t\u0010\u0015\u001a\u00020\u0016H\u00d6\u0001J\t\u0010\u0017\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\nR\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\r\u00a8\u0006\u0018"}, d2 = {"Lcom/adnan/lumisky/registry/LayerCreateResult$UnknownType;", "Lcom/adnan/lumisky/registry/LayerCreateResult;", "layerId", "", "type", "required", "", "<init>", "(Ljava/lang/String;Ljava/lang/String;Z)V", "getLayerId", "()Ljava/lang/String;", "getType", "getRequired", "()Z", "component1", "component2", "component3", "copy", "equals", "other", "", "hashCode", "", "toString", "app_debug"})
    public static final class UnknownType implements com.adnan.lumisky.registry.LayerCreateResult {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String layerId = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String type = null;
        private final boolean required = false;
        
        public UnknownType(@org.jetbrains.annotations.NotNull()
        java.lang.String layerId, @org.jetbrains.annotations.NotNull()
        java.lang.String type, boolean required) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getLayerId() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getType() {
            return null;
        }
        
        public final boolean getRequired() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component2() {
            return null;
        }
        
        public final boolean component3() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.adnan.lumisky.registry.LayerCreateResult.UnknownType copy(@org.jetbrains.annotations.NotNull()
        java.lang.String layerId, @org.jetbrains.annotations.NotNull()
        java.lang.String type, boolean required) {
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