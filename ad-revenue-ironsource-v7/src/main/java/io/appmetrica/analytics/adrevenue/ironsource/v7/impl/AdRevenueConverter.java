package io.appmetrica.analytics.adrevenue.ironsource.v7.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.ironsource.mediationsdk.impressionData.ImpressionData;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenue;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdType;
import java.math.BigDecimal;
import java.util.Currency;

public class AdRevenueConverter {

    private static final String REWARDED = "Rewarded Video";
    private static final String INTERSTITIAL = "Interstitial";
    private static final String BANNER = "Banner";

    public ModuleAdRevenue convert(@NonNull final ImpressionData data) {
        return new ModuleAdRevenue(
            BigDecimal.valueOf(WrapUtils.getFiniteDoubleOrDefault(data.getRevenue(), 0)), // adRevenue
            Currency.getInstance("USD"), // currency
            convert(data.getAdUnit()), // adType
            data.getAdNetwork(), // adNetwork
            null, // adUnitId
            null, // adUnitName
            null, // adPlacementId
            data.getPlacement(), // adPlacementName
            data.getPrecision(), // precision
            null, // payload
            true // autoCollected
        );
    }

    @Nullable
    private ModuleAdType convert(@Nullable final String type) {
        if (type == null) {
            return null;
        }
        switch (type) {
            case REWARDED: return ModuleAdType.REWARDED;
            case INTERSTITIAL: return ModuleAdType.INTERSTITIAL;
            case BANNER: return ModuleAdType.BANNER;
            default: return ModuleAdType.OTHER;
        }
    }
}
