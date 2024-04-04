package io.appmetrica.analytics.adrevenue.ironsource.v7.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.ironsource.mediationsdk.impressionData.ImpressionData;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.AutoAdRevenue;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.AutoAdType;
import java.math.BigDecimal;
import java.util.Currency;

public class AdRevenueConverter {

    private static final String REWARDED = "Rewarded Video";
    private static final String INTERSTITIAL = "Interstitial";
    private static final String BANNER = "Banner";

    public AutoAdRevenue convert(@NonNull final ImpressionData data) {
        return new AutoAdRevenue(
            BigDecimal.valueOf(getFiniteDoubleOrDefault(data.getRevenue(), 0)), // adRevenue
            Currency.getInstance("USD"), // currency
            convert(data.getAdUnit()), // adType
            data.getAdNetwork(), // adNetwork
            null, // adUnitId
            null, // adUnitName
            null, // adPlacementId
            data.getPlacement(), // adPlacementName
            data.getPrecision(), // precision
            null // payload
        );
    }

    @Nullable
    private AutoAdType convert(@Nullable final String type) {
        if (type == null) {
            return null;
        }
        switch (type) {
            case REWARDED: return AutoAdType.REWARDED;
            case INTERSTITIAL: return AutoAdType.INTERSTITIAL;
            case BANNER: return AutoAdType.BANNER;
            default: return AutoAdType.OTHER;
        }
    }

    private double getFiniteDoubleOrDefault(double input, double fallback) {
        return Double.isNaN(input) || Double.isInfinite(input) ? fallback : input;
    }
}
