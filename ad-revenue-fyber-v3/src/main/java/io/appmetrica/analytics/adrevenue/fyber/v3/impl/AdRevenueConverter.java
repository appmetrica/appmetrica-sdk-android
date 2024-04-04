package io.appmetrica.analytics.adrevenue.fyber.v3.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.fyber.fairbid.ads.ImpressionData;
import com.fyber.fairbid.ads.PlacementType;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.AutoAdRevenue;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.AutoAdType;
import java.math.BigDecimal;
import java.util.Currency;

public class AdRevenueConverter {

    public AutoAdRevenue convert(@NonNull final ImpressionData data) {
        return new AutoAdRevenue(
            BigDecimal.valueOf(getFiniteDoubleOrDefault(data.getNetPayout(), 0)), // adRevenue
            Currency.getInstance(data.getCurrency()), // currency
            convert(data.getPlacementType()), // adType
            data.getDemandSource(), // adNetwork
            data.getCreativeId(), // adUnitId
            null, // adUnitName
            null, // adPlacementId
            null, // adPlacementName
            data.getPriceAccuracy().toString(), // precision
            null // payload
        );
    }

    @Nullable
    private AutoAdType convert(@Nullable final PlacementType type) {
        if (type == null) {
            return null;
        }
        switch (type) {
            case BANNER: return AutoAdType.BANNER;
            case REWARDED: return AutoAdType.REWARDED;
            case INTERSTITIAL: return AutoAdType.INTERSTITIAL;
            default: return AutoAdType.OTHER;
        }
    }

    private double getFiniteDoubleOrDefault(double input, double fallback) {
        return Double.isNaN(input) || Double.isInfinite(input) ? fallback : input;
    }
}
