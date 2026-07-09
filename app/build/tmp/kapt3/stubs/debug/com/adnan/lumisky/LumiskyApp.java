package com.adnan.lumisky;

@dagger.hilt.android.HiltAndroidApp()
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\b\u0010\n\u001a\u00020\u000bH\u0016R\u001e\u0010\u0004\u001a\u00020\u00058\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\t\u00a8\u0006\f"}, d2 = {"Lcom/adnan/lumisky/LumiskyApp;", "Landroid/app/Application;", "<init>", "()V", "thermalStateController", "Lcom/adnan/lumisky/device/ThermalStateController;", "getThermalStateController", "()Lcom/adnan/lumisky/device/ThermalStateController;", "setThermalStateController", "(Lcom/adnan/lumisky/device/ThermalStateController;)V", "onCreate", "", "app_debug"})
public final class LumiskyApp extends android.app.Application {
    @javax.inject.Inject()
    public com.adnan.lumisky.device.ThermalStateController thermalStateController;
    
    public LumiskyApp() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.device.ThermalStateController getThermalStateController() {
        return null;
    }
    
    public final void setThermalStateController(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.device.ThermalStateController p0) {
    }
    
    @java.lang.Override()
    public void onCreate() {
    }
}