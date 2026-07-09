package com.adnan.lumisky.device;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0006\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u0013\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\u0006\u0010\n\u001a\u00020\u000bJ\u0006\u0010\f\u001a\u00020\u000bJ\u001a\u0010\r\u001a\u0004\u0018\u00010\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0010H\u0002J\u001a\u0010\u0012\u001a\u0004\u0018\u00010\u00132\b\b\u0002\u0010\u0014\u001a\u00020\u0015H\u0087@\u00a2\u0006\u0002\u0010\u0016R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0006\u001a\u0004\u0018\u00010\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0017"}, d2 = {"Lcom/adnan/lumisky/device/DeviceLocationProvider;", "", "context", "Landroid/content/Context;", "<init>", "(Landroid/content/Context;)V", "locationManager", "Landroid/location/LocationManager;", "fusedLocationClient", "Lcom/google/android/gms/location/FusedLocationProviderClient;", "hasLocationPermission", "", "isLocationEnabled", "resolveCityOrDistrict", "", "latitude", "", "longitude", "readLastKnownSnapshot", "Lcom/adnan/lumisky/data/DeviceLocationSnapshot;", "nowEpochMs", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_release"})
public final class DeviceLocationProvider {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.Nullable()
    private final android.location.LocationManager locationManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.google.android.gms.location.FusedLocationProviderClient fusedLocationClient = null;
    
    @javax.inject.Inject()
    public DeviceLocationProvider(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    public final boolean hasLocationPermission() {
        return false;
    }
    
    public final boolean isLocationEnabled() {
        return false;
    }
    
    private final java.lang.String resolveCityOrDistrict(double latitude, double longitude) {
        return null;
    }
    
    @android.annotation.SuppressLint(value = {"MissingPermission"})
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object readLastKnownSnapshot(long nowEpochMs, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.adnan.lumisky.data.DeviceLocationSnapshot> $completion) {
        return null;
    }
}