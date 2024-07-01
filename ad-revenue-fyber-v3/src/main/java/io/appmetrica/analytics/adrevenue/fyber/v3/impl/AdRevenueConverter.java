package io.appmetrica.analytics.adrevenue.fyber.v3.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.fyber.fairbid.ads.ImpressionData;
import com.fyber.fairbid.ads.PlacementType;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenue;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdType;
import java.math.BigDecimal;
import java.util.Currency;

public class AdRevenueConverter {

    public ModuleAdRevenue convert(@NonNull final ImpressionData data) {
        return new ModuleAdRevenue(
            BigDecimal.valueOf(WrapUtils.getFiniteDoubleOrDefault(data.getNetPayout(), 0)), // adRevenue
            Currency.getInstance(data.getCurrency()), // currency
            convert(data.getPlacementType()), // adType
            data.getDemandSource(), // adNetwork
            data.getCreativeId(), // adUnitId
            null, // adUnitName
            null, // adPlacementId
            null, // adPlacementName
            data.getPriceAccuracy().toString(), // precision
            null, // payload
            true // autoCollected
        );
    }

    @Nullable
    private ModuleAdType convert(@Nullable final PlacementType type) {
        if (type == null) {
            return null;
        }
        switch (type) {
            case BANNER: return ModuleAdType.BANNER;
            case REWARDED: return ModuleAdType.REWARDED;
            case INTERSTITIAL: return ModuleAdType.INTERSTITIAL;
            default: return ModuleAdType.OTHER;
        }
    }
}
