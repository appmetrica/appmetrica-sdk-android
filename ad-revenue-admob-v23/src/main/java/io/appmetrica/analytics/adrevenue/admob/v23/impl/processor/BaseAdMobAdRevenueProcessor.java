package io.appmetrica.analytics.adrevenue.admob.v23.impl.processor;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.adrevenue.admob.v23.impl.AdRevenueConverter;
import io.appmetrica.analytics.coreutils.internal.logger.LoggerStorage;
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenue;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenueProcessor;

public abstract class BaseAdMobAdRevenueProcessor implements ModuleAdRevenueProcessor {

    @NonNull
    protected final AdRevenueConverter adRevenueConverter;
    @NonNull
    private final ClientContext clientContext;

    public BaseAdMobAdRevenueProcessor(
        @NonNull AdRevenueConverter adRevenueConverter,
        @NonNull ClientContext clientContext
    ) {
        this.adRevenueConverter = adRevenueConverter;
        this.clientContext = clientContext;
    }

    protected void report(@NonNull ModuleAdRevenue adRevenue) {
        clientContext.getModuleAdRevenueContext()
            .getAdRevenueReporter()
            .reportAutoAdRevenue(adRevenue);
        LoggerStorage.getMainPublicOrAnonymousLogger().info(
            "Ad Revenue from AdMob was reported"
        );
    }

    @NonNull
    @Override
    public String getDescription() {
        return "AdMob";
    }
}
