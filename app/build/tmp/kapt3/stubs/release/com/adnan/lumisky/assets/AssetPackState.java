package com.adnan.lumisky.assets;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\bv\u0018\u00002\u00020\u0001:\n\u0002\u0003\u0004\u0005\u0006\u0007\b\t\n\u000b\u0082\u0001\n\f\r\u000e\u000f\u0010\u0011\u0012\u0013\u0014\u0015\u00a8\u0006\u0016\u00c0\u0006\u0003"}, d2 = {"Lcom/adnan/lumisky/assets/AssetPackState;", "", "NotRequired", "NotInstalled", "Pending", "Downloading", "Transferring", "Installed", "Failed", "RequiresWifiConfirmation", "Canceled", "Removed", "Lcom/adnan/lumisky/assets/AssetPackState$Canceled;", "Lcom/adnan/lumisky/assets/AssetPackState$Downloading;", "Lcom/adnan/lumisky/assets/AssetPackState$Failed;", "Lcom/adnan/lumisky/assets/AssetPackState$Installed;", "Lcom/adnan/lumisky/assets/AssetPackState$NotInstalled;", "Lcom/adnan/lumisky/assets/AssetPackState$NotRequired;", "Lcom/adnan/lumisky/assets/AssetPackState$Pending;", "Lcom/adnan/lumisky/assets/AssetPackState$Removed;", "Lcom/adnan/lumisky/assets/AssetPackState$RequiresWifiConfirmation;", "Lcom/adnan/lumisky/assets/AssetPackState$Transferring;", "app_release"})
public abstract interface AssetPackState {
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003\u00a8\u0006\u0004"}, d2 = {"Lcom/adnan/lumisky/assets/AssetPackState$Canceled;", "Lcom/adnan/lumisky/assets/AssetPackState;", "<init>", "()V", "app_release"})
    public static final class Canceled implements com.adnan.lumisky.assets.AssetPackState {
        @org.jetbrains.annotations.NotNull()
        public static final com.adnan.lumisky.assets.AssetPackState.Canceled INSTANCE = null;
        
        private Canceled() {
            super();
        }
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0007\n\u0002\b\u0007\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B\u000f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\t\u0010\b\u001a\u00020\u0003H\u00c6\u0003J\u0013\u0010\t\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\n\u001a\u00020\u000b2\b\u0010\f\u001a\u0004\u0018\u00010\rH\u00d6\u0003J\t\u0010\u000e\u001a\u00020\u000fH\u00d6\u0001J\t\u0010\u0010\u001a\u00020\u0011H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007\u00a8\u0006\u0012"}, d2 = {"Lcom/adnan/lumisky/assets/AssetPackState$Downloading;", "Lcom/adnan/lumisky/assets/AssetPackState;", "progress", "", "<init>", "(F)V", "getProgress", "()F", "component1", "copy", "equals", "", "other", "", "hashCode", "", "toString", "", "app_release"})
    public static final class Downloading implements com.adnan.lumisky.assets.AssetPackState {
        private final float progress = 0.0F;
        
        public Downloading(float progress) {
            super();
        }
        
        public final float getProgress() {
            return 0.0F;
        }
        
        public final float component1() {
            return 0.0F;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.adnan.lumisky.assets.AssetPackState.Downloading copy(float progress) {
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
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\f\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B\u001b\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0004\b\u0006\u0010\u0007J\t\u0010\r\u001a\u00020\u0003H\u00c6\u0003J\u0010\u0010\u000e\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003\u00a2\u0006\u0002\u0010\u000bJ$\u0010\u000f\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005H\u00c6\u0001\u00a2\u0006\u0002\u0010\u0010J\u0013\u0010\u0011\u001a\u00020\u00122\b\u0010\u0013\u001a\u0004\u0018\u00010\u0014H\u00d6\u0003J\t\u0010\u0015\u001a\u00020\u0005H\u00d6\u0001J\t\u0010\u0016\u001a\u00020\u0017H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0015\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\n\n\u0002\u0010\f\u001a\u0004\b\n\u0010\u000b\u00a8\u0006\u0018"}, d2 = {"Lcom/adnan/lumisky/assets/AssetPackState$Failed;", "Lcom/adnan/lumisky/assets/AssetPackState;", "reason", "Lcom/adnan/lumisky/assets/AssetPackFailureReason;", "errorCode", "", "<init>", "(Lcom/adnan/lumisky/assets/AssetPackFailureReason;Ljava/lang/Integer;)V", "getReason", "()Lcom/adnan/lumisky/assets/AssetPackFailureReason;", "getErrorCode", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "component1", "component2", "copy", "(Lcom/adnan/lumisky/assets/AssetPackFailureReason;Ljava/lang/Integer;)Lcom/adnan/lumisky/assets/AssetPackState$Failed;", "equals", "", "other", "", "hashCode", "toString", "", "app_release"})
    public static final class Failed implements com.adnan.lumisky.assets.AssetPackState {
        @org.jetbrains.annotations.NotNull()
        private final com.adnan.lumisky.assets.AssetPackFailureReason reason = null;
        @org.jetbrains.annotations.Nullable()
        private final java.lang.Integer errorCode = null;
        
        public Failed(@org.jetbrains.annotations.NotNull()
        com.adnan.lumisky.assets.AssetPackFailureReason reason, @org.jetbrains.annotations.Nullable()
        java.lang.Integer errorCode) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.adnan.lumisky.assets.AssetPackFailureReason getReason() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.Integer getErrorCode() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.adnan.lumisky.assets.AssetPackFailureReason component1() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.Integer component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.adnan.lumisky.assets.AssetPackState.Failed copy(@org.jetbrains.annotations.NotNull()
        com.adnan.lumisky.assets.AssetPackFailureReason reason, @org.jetbrains.annotations.Nullable()
        java.lang.Integer errorCode) {
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
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003\u00a8\u0006\u0004"}, d2 = {"Lcom/adnan/lumisky/assets/AssetPackState$Installed;", "Lcom/adnan/lumisky/assets/AssetPackState;", "<init>", "()V", "app_release"})
    public static final class Installed implements com.adnan.lumisky.assets.AssetPackState {
        @org.jetbrains.annotations.NotNull()
        public static final com.adnan.lumisky.assets.AssetPackState.Installed INSTANCE = null;
        
        private Installed() {
            super();
        }
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003\u00a8\u0006\u0004"}, d2 = {"Lcom/adnan/lumisky/assets/AssetPackState$NotInstalled;", "Lcom/adnan/lumisky/assets/AssetPackState;", "<init>", "()V", "app_release"})
    public static final class NotInstalled implements com.adnan.lumisky.assets.AssetPackState {
        @org.jetbrains.annotations.NotNull()
        public static final com.adnan.lumisky.assets.AssetPackState.NotInstalled INSTANCE = null;
        
        private NotInstalled() {
            super();
        }
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003\u00a8\u0006\u0004"}, d2 = {"Lcom/adnan/lumisky/assets/AssetPackState$NotRequired;", "Lcom/adnan/lumisky/assets/AssetPackState;", "<init>", "()V", "app_release"})
    public static final class NotRequired implements com.adnan.lumisky.assets.AssetPackState {
        @org.jetbrains.annotations.NotNull()
        public static final com.adnan.lumisky.assets.AssetPackState.NotRequired INSTANCE = null;
        
        private NotRequired() {
            super();
        }
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003\u00a8\u0006\u0004"}, d2 = {"Lcom/adnan/lumisky/assets/AssetPackState$Pending;", "Lcom/adnan/lumisky/assets/AssetPackState;", "<init>", "()V", "app_release"})
    public static final class Pending implements com.adnan.lumisky.assets.AssetPackState {
        @org.jetbrains.annotations.NotNull()
        public static final com.adnan.lumisky.assets.AssetPackState.Pending INSTANCE = null;
        
        private Pending() {
            super();
        }
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003\u00a8\u0006\u0004"}, d2 = {"Lcom/adnan/lumisky/assets/AssetPackState$Removed;", "Lcom/adnan/lumisky/assets/AssetPackState;", "<init>", "()V", "app_release"})
    public static final class Removed implements com.adnan.lumisky.assets.AssetPackState {
        @org.jetbrains.annotations.NotNull()
        public static final com.adnan.lumisky.assets.AssetPackState.Removed INSTANCE = null;
        
        private Removed() {
            super();
        }
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003\u00a8\u0006\u0004"}, d2 = {"Lcom/adnan/lumisky/assets/AssetPackState$RequiresWifiConfirmation;", "Lcom/adnan/lumisky/assets/AssetPackState;", "<init>", "()V", "app_release"})
    public static final class RequiresWifiConfirmation implements com.adnan.lumisky.assets.AssetPackState {
        @org.jetbrains.annotations.NotNull()
        public static final com.adnan.lumisky.assets.AssetPackState.RequiresWifiConfirmation INSTANCE = null;
        
        private RequiresWifiConfirmation() {
            super();
        }
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0007\n\u0002\b\u0007\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B\u000f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\t\u0010\b\u001a\u00020\u0003H\u00c6\u0003J\u0013\u0010\t\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\n\u001a\u00020\u000b2\b\u0010\f\u001a\u0004\u0018\u00010\rH\u00d6\u0003J\t\u0010\u000e\u001a\u00020\u000fH\u00d6\u0001J\t\u0010\u0010\u001a\u00020\u0011H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007\u00a8\u0006\u0012"}, d2 = {"Lcom/adnan/lumisky/assets/AssetPackState$Transferring;", "Lcom/adnan/lumisky/assets/AssetPackState;", "progress", "", "<init>", "(F)V", "getProgress", "()F", "component1", "copy", "equals", "", "other", "", "hashCode", "", "toString", "", "app_release"})
    public static final class Transferring implements com.adnan.lumisky.assets.AssetPackState {
        private final float progress = 0.0F;
        
        public Transferring(float progress) {
            super();
        }
        
        public final float getProgress() {
            return 0.0F;
        }
        
        public final float component1() {
            return 0.0F;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.adnan.lumisky.assets.AssetPackState.Transferring copy(float progress) {
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