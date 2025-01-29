package io.appmetrica.analytics.adrevenue.fyber.v3.impl;

import com.fyber.fairbid.ads.ImpressionData;
import com.fyber.fairbid.ads.PlacementType;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.AdRevenueConstants;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenue;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdType;
import io.appmetrica.analytics.testutils.CommonTest;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AdRevenueConverterTest extends CommonTest {

    private final double netPayout = 123.1234;
    private final String currency = "RUB";
    private final PlacementType placementType = PlacementType.REWARDED;
    private final ImpressionData.PriceAccuracy priceAccuracy = ImpressionData.PriceAccuracy.PREDICTED;
    private final String demandSource = "demandSource";
    private final String creativeId = "creativeId";

    private final ImpressionData data = mock(ImpressionData.class);

    private final AdRevenueConverter converter = new AdRevenueConverter();

    @Before
    public void setUp() {
        when(data.getNetPayout()).thenReturn(netPayout);
        when(data.getCurrency()).thenReturn(currency);
        when(data.getPlacementType()).thenReturn(placementType);
        when(data.getPriceAccuracy()).thenReturn(priceAccuracy);
        when(data.getDemandSource()).thenReturn(demandSource);
        when(data.getCreativeId()).thenReturn(creativeId);
    }

    @Test
    public void convert() {
        final ModuleAdRevenue adRevenue = converter.convert(data);

        ObjectPropertyAssertions(adRevenue)
            .checkField("adRevenue", BigDecimal.valueOf(netPayout))
            .checkField("currency", Currency.getInstance(currency))
            .checkField("adType", ModuleAdType.REWARDED)
            .checkField("adNetwork", demandSource)
            .checkField("adUnitId", creativeId)
            .checkFieldIsNull("adUnitName")
            .checkFieldIsNull("adPlacementId")
            .checkFieldIsNull("adPlacementName")
            .checkField("precision", priceAccuracy.toString())
            .checkField("payload", new HashMap<String, String>() {{
                put(AdRevenueConstants.ORIGINAL_SOURCE_KEY, "ad-revenue-fyber-v3");
                put(AdRevenueConstants.ORIGINAL_AD_TYPE_KEY, "REWARDED");
            }})
            .checkField("autoCollected", true)
            .checkAll();
    }

    @Test
    public void convertIfNetPayoutIsIllegal() {
        when(data.getNetPayout()).thenReturn(Double.NaN);
        final ModuleAdRevenue adRevenue = converter.convert(data);

        ObjectPropertyAssertions(adRevenue)
            .checkField("adRevenue", BigDecimal.valueOf(0.0))
            .checkField("currency", Currency.getInstance(currency))
            .checkField("adType", ModuleAdType.REWARDED)
            .checkField("adNetwork", demandSource)
            .checkField("adUnitId", creativeId)
            .checkFieldIsNull("adUnitName")
            .checkFieldIsNull("adPlacementId")
            .checkFieldIsNull("adPlacementName")
            .checkField("precision", priceAccuracy.toString())
            .checkField("payload", new HashMap<String, String>() {{
                put(AdRevenueConstants.ORIGINAL_SOURCE_KEY, "ad-revenue-fyber-v3");
                put(AdRevenueConstants.ORIGINAL_AD_TYPE_KEY, "REWARDED");
            }})
            .checkField("autoCollected", true)
            .checkAll();
    }

    @Test
    public void convertAndCheckAutoAdType() {
        when(data.getPlacementType()).thenReturn(null);
        final ModuleAdRevenue adRevenueNull = converter.convert(data);
        assertThat(adRevenueNull.getAdType()).isNull();
        assertThat(adRevenueNull.getPayload()).isEqualTo(
            new HashMap<String, String>() {{
                put(AdRevenueConstants.ORIGINAL_SOURCE_KEY, "ad-revenue-fyber-v3");
                put(AdRevenueConstants.ORIGINAL_AD_TYPE_KEY, "null");
            }}
        );

        when(data.getPlacementType()).thenReturn(PlacementType.REWARDED);
        final ModuleAdRevenue adRevenueRewarded = converter.convert(data);
        assertThat(adRevenueRewarded.getAdType()).isEqualTo(ModuleAdType.REWARDED);
        assertThat(adRevenueRewarded.getPayload()).isEqualTo(
            new HashMap<String, String>() {{
                put(AdRevenueConstants.ORIGINAL_SOURCE_KEY, "ad-revenue-fyber-v3");
                put(AdRevenueConstants.ORIGINAL_AD_TYPE_KEY, "REWARDED");
            }}
        );

        when(data.getPlacementType()).thenReturn(PlacementType.INTERSTITIAL);
        final ModuleAdRevenue adRevenueInterstitial = converter.convert(data);
        assertThat(adRevenueInterstitial.getAdType()).isEqualTo(ModuleAdType.INTERSTITIAL);
        assertThat(adRevenueInterstitial.getPayload()).isEqualTo(
            new HashMap<String, String>() {{
                put(AdRevenueConstants.ORIGINAL_SOURCE_KEY, "ad-revenue-fyber-v3");
                put(AdRevenueConstants.ORIGINAL_AD_TYPE_KEY, "INTERSTITIAL");
            }}
        );

        when(data.getPlacementType()).thenReturn(PlacementType.BANNER);
        final ModuleAdRevenue adRevenueBanner = converter.convert(data);
        assertThat(adRevenueBanner.getAdType()).isEqualTo(ModuleAdType.BANNER);
        assertThat(adRevenueBanner.getPayload()).isEqualTo(
            new HashMap<String, String>() {{
                put(AdRevenueConstants.ORIGINAL_SOURCE_KEY, "ad-revenue-fyber-v3");
                put(AdRevenueConstants.ORIGINAL_AD_TYPE_KEY, "BANNER");
            }}
        );
    }
}
