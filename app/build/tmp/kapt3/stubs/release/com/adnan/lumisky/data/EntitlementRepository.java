package com.adnan.lumisky.data;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\t\b\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003B\u0011\b\u0016\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0004\b\u0002\u0010\u0006J\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00050\bR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\t"}, d2 = {"Lcom/adnan/lumisky/data/EntitlementRepository;", "", "<init>", "()V", "debugPremiumOverride", "", "(Z)V", "isPremiumPurchased", "Lkotlinx/coroutines/flow/Flow;", "app_release"})
public final class EntitlementRepository {
    private boolean debugPremiumOverride = false;
    
    @javax.inject.Inject()
    public EntitlementRepository() {
        super();
    }
    
    public EntitlementRepository(boolean debugPremiumOverride) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.lang.Boolean> isPremiumPurchased() {
        return null;
    }
}