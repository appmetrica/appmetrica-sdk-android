package io.appmetrica.analytics.adrevenue.fyber.v3.impl;

import androidx.annotation.NonNull;
import com.fyber.fairbid.ads.ImpressionData;
import io.appmetrica.analytics.coreutils.internal.logger.LoggerStorage;
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils;
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenueProcessor;

public class FyberAdRevenueProcessor implements ModuleAdRevenueProcessor {

    @NonNull
    private final AdRevenueConverter converter;
    @NonNull
    private final ClientContext clientContext;

    public FyberAdRevenueProcessor(
        @NonNull AdRevenueConverter converter,
        @NonNull ClientContext clientContext
    ) {
        this.converter = converter;
        this.clientContext = clientContext;
    }

    @Override
    public boolean process(Object... values) {
        boolean isArgumentsHasClasses = ReflectionUtils.isArgumentsOfClasses(
            values,
            ImpressionData.class
        );
        if (!isArgumentsHasClasses) {
            return false;
        }

        ImpressionData impressionData = (ImpressionData) values[0];

        clientContext.getInternalClientModuleFacade()
            .reportAdRevenue(converter.convert(impressionData));
        LoggerStorage.getMainPublicOrAnonymousLogger().info(
            "Ad Revenue from Fyber was reported"
        );
        return true;
    }

    @NonNull
    @Override
    public String getDescription() {
        return "Fyber";
    }
}
