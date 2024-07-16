package io.appmetrica.analytics.adrevenue.admob.v23.impl.processor;

import androidx.annotation.NonNull;
import com.google.android.gms.ads.AdValue;
import com.google.android.gms.ads.AdView;
import io.appmetrica.analytics.adrevenue.admob.v23.impl.AdRevenueConverter;
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext;

public class BannerAdMobAdRevenueProcessor extends BaseAdMobAdRevenueProcessor {

    private static final String TAG = "[BannerAdMobAdRevenueProcessor]";

    public BannerAdMobAdRevenueProcessor(
        @NonNull AdRevenueConverter adRevenueConverter,
        @NonNull ClientContext clientContext
    ) {
        super(adRevenueConverter, clientContext);
    }

    @Override
    public boolean process(Object... values) {
        boolean isArgumentsHasClasses = ReflectionUtils.isArgumentsOfClasses(
            values,
            AdValue.class,
            AdView.class
        );
        if (!isArgumentsHasClasses) {
            return false;
        }

        AdValue adValue = (AdValue) values[0];
        AdView adView = (AdView) values[1];

        report(adRevenueConverter.convertBanner(adValue, adView));
        DebugLogger.INSTANCE.info(TAG, "AdRevenue was reported");
        return true;
    }
}
