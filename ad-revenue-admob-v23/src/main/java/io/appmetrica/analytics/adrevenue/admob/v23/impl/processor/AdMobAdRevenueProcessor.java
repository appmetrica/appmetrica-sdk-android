package io.appmetrica.analytics.adrevenue.admob.v23.impl.processor;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.adrevenue.admob.v23.impl.AdRevenueConverter;
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenueProcessor;
import java.util.ArrayList;
import java.util.List;

public class AdMobAdRevenueProcessor implements ModuleAdRevenueProcessor {

    @NonNull
    private final List<ModuleAdRevenueProcessor> processors;

    public AdMobAdRevenueProcessor(
        @NonNull AdRevenueConverter converter,
        @NonNull ClientContext clientContext
    ) {
        processors = new ArrayList<>();
        processors.add(new BannerAdMobAdRevenueProcessor(converter, clientContext));
        processors.add(new InterstitialAdMobAdRevenueProcessor(converter, clientContext));
        processors.add(new NativeAdMobAdRevenueProcessor(converter, clientContext));
        processors.add(new RewardedAdMobAdRevenueProcessor(converter, clientContext));
        processors.add(new RewardedInterstitialAdMobAdRevenueProcessor(converter, clientContext));
        processors.add(new AppOpenAdMobAdRevenueProcessor(converter, clientContext));
    }

    @Override
    public boolean process(Object... values) {
        for (ModuleAdRevenueProcessor processor : processors) {
            boolean result = processor.process(values);
            if (result) {
                return true;
            }
        }
        return false;
    }

    @NonNull
    @Override
    public String getDescription() {
        return "AdMob";
    }
}
