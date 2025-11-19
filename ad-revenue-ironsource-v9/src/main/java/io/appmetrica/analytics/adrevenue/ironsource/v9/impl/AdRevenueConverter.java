package io.appmetrica.analytics.adrevenue.ironsource.v9.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.unity3d.mediation.impression.LevelPlayImpressionData;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.AdRevenueConstants;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenue;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdType;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.HashMap;

public class AdRevenueConverter {

    private static final String REWARDED = "rewarded_video";
    private static final String INTERSTITIAL = "interstitial";
    private static final String BANNER = "banner";

    public ModuleAdRevenue convert(@NonNull final LevelPlayImpressionData data) {
        String type = data.getAdFormat();
        return new ModuleAdRevenue(
            /* adRevenue */ BigDecimal.valueOf(WrapUtils.getFiniteDoubleOrDefaultNullable(data.getRevenue(), 0)),
            /* currency */ Currency.getInstance("USD"),
            /* adType */ convert(type),
            /* adNetwork */ data.getAdNetwork(),
            /* adUnitId */ data.getMediationAdUnitId(),
            /* adUnitName */ data.getMediationAdUnitName(),
            /* adPlacementId */ null,
            /* adPlacementName */ data.getPlacement(),
            /* precision */ data.getPrecision(),
            /* payload */ new HashMap<String, String>() {{
                put(AdRevenueConstants.ORIGINAL_SOURCE_KEY, Constants.MODULE_ID);
                put(AdRevenueConstants.ORIGINAL_AD_TYPE_KEY, type == null ? "null" : type);
                put(AdRevenueConstants.SOURCE_KEY, Constants.AD_REVENUE_SOURCE_IDENTIFIER);
            }},
            /* autoCollected */ true
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
