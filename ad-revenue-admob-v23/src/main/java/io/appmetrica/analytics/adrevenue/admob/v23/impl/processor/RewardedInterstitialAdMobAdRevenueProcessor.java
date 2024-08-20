package io.appmetrica.analytics.adrevenue.admob.v23.impl.processor;

import androidx.annotation.NonNull;
import com.google.android.gms.ads.AdValue;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import io.appmetrica.analytics.adrevenue.admob.v23.impl.AdRevenueConverter;
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext;

public class RewardedInterstitialAdMobAdRevenueProcessor extends BaseAdMobAdRevenueProcessor {

    private static final String TAG = "[RewardedInterstitialAdMobAdRevenueProcessor]";

    public RewardedInterstitialAdMobAdRevenueProcessor(
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
            RewardedInterstitialAd.class
        );
        if (!isArgumentsHasClasses) {
            return false;
        }

        AdValue adValue = (AdValue) values[0];
        RewardedInterstitialAd ad = (RewardedInterstitialAd) values[1];

        report(adRevenueConverter.convertRewardedInterstitialAd(adValue, ad));
        DebugLogger.INSTANCE.info(TAG, "AdRevenue was reported");
        return true;
    }
}
