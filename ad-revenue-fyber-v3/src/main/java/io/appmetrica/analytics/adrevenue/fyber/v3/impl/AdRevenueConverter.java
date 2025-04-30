package io.appmetrica.analytics.adrevenue.fyber.v3.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.fyber.fairbid.ads.ImpressionData;
import com.fyber.fairbid.ads.PlacementType;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.AdRevenueConstants;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenue;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdType;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

public class AdRevenueConverter {

    public ModuleAdRevenue convert(@NonNull final ImpressionData data) {
        PlacementType type = data.getPlacementType();
        return new ModuleAdRevenue(
            BigDecimal.valueOf(WrapUtils.getFiniteDoubleOrDefault(data.getNetPayout(), 0)), // adRevenue
            Currency.getInstance(data.getCurrency()), // currency
            convert(type), // adType
            data.getDemandSource(), // adNetwork
            data.getCreativeId(), // adUnitId
            null, // adUnitName
            null, // adPlacementId
            null, // adPlacementName
            data.getPriceAccuracy().toString(), // precision
            composePayload(type), // payload
            false // autoCollected
        );
    }

    private Map<String, String> composePayload(@Nullable PlacementType type) {
        Map<String, String> payload = new HashMap<>();
        payload.put(AdRevenueConstants.SOURCE_KEY, Constants.AD_REVENUE_SOURCE_IDENTIFIER);
        payload.put(AdRevenueConstants.ORIGINAL_SOURCE_KEY, Constants.MODULE_ID);
        payload.put(AdRevenueConstants.ORIGINAL_AD_TYPE_KEY, type == null ? "null" : type.name());
        return payload;
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
