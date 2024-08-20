package io.appmetrica.analytics.adrevenue.admob.v23.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.ads.AdValue;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdapterResponseInfo;
import com.google.android.gms.ads.ResponseInfo;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenue;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdType;
import java.math.BigDecimal;
import java.util.Currency;

public class AdRevenueConverter {

    public ModuleAdRevenue convertBanner(@NonNull AdValue adValue, @NonNull AdView adView) {
        return constructModuleAdRevenue(
            adValue,
            ModuleAdType.BANNER,
            adView.getResponseInfo(),
            adView.getAdUnitId()
        );
    }

    public ModuleAdRevenue convertInterstitialAd(@NonNull AdValue adValue, @NonNull InterstitialAd ad) {
        return constructModuleAdRevenue(
            adValue,
            ModuleAdType.INTERSTITIAL,
            ad.getResponseInfo(),
            ad.getAdUnitId()
        );
    }

    public ModuleAdRevenue convertRewardedAd(@NonNull AdValue adValue, @NonNull RewardedAd ad) {
        return constructModuleAdRevenue(
            adValue,
            ModuleAdType.REWARDED,
            ad.getResponseInfo(),
            ad.getAdUnitId()
        );
    }

    public ModuleAdRevenue convertRewardedInterstitialAd(
        @NonNull AdValue adValue,
        @NonNull RewardedInterstitialAd ad
    ) {
        return constructModuleAdRevenue(
            adValue,
            ModuleAdType.OTHER,
            ad.getResponseInfo(),
            ad.getAdUnitId()
        );
    }

    public ModuleAdRevenue convertNativeAd(
        @NonNull AdValue adValue,
        @NonNull NativeAd nativeAd
    ) {
        return constructModuleAdRevenue(
            adValue,
            ModuleAdType.NATIVE,
            nativeAd.getResponseInfo(),
            null
        );
    }

    private ModuleAdRevenue constructModuleAdRevenue(
        @NonNull AdValue adValue,
        @NonNull ModuleAdType type,
        @Nullable ResponseInfo responseInfo,
        @Nullable String adUnitId
    ) {
        double adRevenue = WrapUtils.getFiniteDoubleOrDefault(
            adValue.getValueMicros() / 1_000_000.0,
            0.0
        );
        if (responseInfo != null) {
            AdapterResponseInfo adapterResponseInfo = responseInfo.getLoadedAdapterResponseInfo();
            if (adapterResponseInfo != null) {
                return new ModuleAdRevenue(
                    /* adRevenue = */ BigDecimal.valueOf(adRevenue),
                    /* currency = */ Currency.getInstance(adValue.getCurrencyCode()),
                    /* adType = */ type,
                    /* adNetwork = */ adapterResponseInfo.getAdapterClassName(),
                    /* adUnitId = */ adUnitId,
                    /* adUnitName = */ null,
                    /* adPlacementId = */ adapterResponseInfo.getAdSourceInstanceId(),
                    /* adPlacementName = */ adapterResponseInfo.getAdSourceInstanceName(),
                    /* precision = */ convertPrecision(adValue.getPrecisionType()),
                    /* payload = */ null,
                    /* autoCollected = */ false
                );
            }
        }
        return new ModuleAdRevenue(
            /* adRevenue = */ BigDecimal.valueOf(adRevenue),
            /* currency = */ Currency.getInstance(adValue.getCurrencyCode()),
            /* adType = */ type,
            /* adNetwork = */ null,
            /* adUnitId = */ adUnitId,
            /* adUnitName = */ null,
            /* adPlacementId = */ null,
            /* adPlacementName = */ null,
            /* precision = */ convertPrecision(adValue.getPrecisionType()),
            /* payload = */ null,
            /* autoCollected = */ false
        );
    }

    private String convertPrecision(int precision) {
        if (precision == AdValue.PrecisionType.UNKNOWN) {
            return "UNKNOWN";
        }
        if (precision == AdValue.PrecisionType.ESTIMATED) {
            return "ESTIMATED";
        }
        if (precision == AdValue.PrecisionType.PUBLISHER_PROVIDED) {
            return "PUBLISHER_PROVIDED";
        }
        if (precision == AdValue.PrecisionType.PRECISE) {
            return "PRECISE";
        }
        return "";
    }
}
