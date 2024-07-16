package io.appmetrica.analytics.adrevenue.admob.v23.impl.processor;

import androidx.annotation.NonNull;
import com.google.android.gms.ads.AdValue;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.rewarded.RewardedAd;
import io.appmetrica.analytics.adrevenue.admob.v23.impl.AdRevenueConverter;
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext;

public class RewardedAdMobAdRevenueProcessor extends BaseAdMobAdRevenueProcessor {

    private static final String TAG = "[RewardedAdMobAdRevenueProcessor]";

    public RewardedAdMobAdRevenueProcessor(
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
            AdView.class,
            RewardedAd.class
        );
        if (!isArgumentsHasClasses) {
            return false;
        }

        AdValue adValue = (AdValue) values[0];
        AdView adView = (AdView) values[1];

        report(adRevenueConverter.convertRewardedAd(adValue, adView));
        DebugLogger.INSTANCE.info(TAG, "AdRevenue was reported");
        return true;
    }
}
