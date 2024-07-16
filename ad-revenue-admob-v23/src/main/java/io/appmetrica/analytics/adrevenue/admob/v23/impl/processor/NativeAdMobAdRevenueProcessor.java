package io.appmetrica.analytics.adrevenue.admob.v23.impl.processor;

import androidx.annotation.NonNull;
import com.google.android.gms.ads.AdValue;
import com.google.android.gms.ads.nativead.NativeAd;
import io.appmetrica.analytics.adrevenue.admob.v23.impl.AdRevenueConverter;
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext;

public class NativeAdMobAdRevenueProcessor extends BaseAdMobAdRevenueProcessor {

    private static final String TAG = "[NativeAdMobAdRevenueProcessor]";

    public NativeAdMobAdRevenueProcessor(
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
            NativeAd.class
        );
        if (!isArgumentsHasClasses) {
            return false;
        }

        AdValue adValue = (AdValue) values[0];
        NativeAd nativeAd = (NativeAd) values[1];

        report(adRevenueConverter.convertNativeAd(adValue, nativeAd));
        DebugLogger.INSTANCE.info(TAG, "AdRevenue was reported");
        return true;
    }
}
