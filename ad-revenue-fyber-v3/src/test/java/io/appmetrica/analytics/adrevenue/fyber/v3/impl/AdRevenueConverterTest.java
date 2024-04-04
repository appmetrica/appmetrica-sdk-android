package io.appmetrica.analytics.adrevenue.fyber.v3.impl;

import com.fyber.fairbid.ads.ImpressionData;
import com.fyber.fairbid.ads.PlacementType;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.AutoAdRevenue;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.AutoAdType;
import io.appmetrica.analytics.testutils.CommonTest;
import java.math.BigDecimal;
import java.util.Currency;
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
        final AutoAdRevenue autoAdRevenue = converter.convert(data);

        ObjectPropertyAssertions(autoAdRevenue)
            .checkField("adRevenue", BigDecimal.valueOf(netPayout))
            .checkField("currency", Currency.getInstance(currency))
            .checkField("adType", AutoAdType.REWARDED)
            .checkField("adNetwork", demandSource)
            .checkField("adUnitId", creativeId)
            .checkFieldIsNull("adUnitName")
            .checkFieldIsNull("adPlacementId")
            .checkFieldIsNull("adPlacementName")
            .checkField("precision", priceAccuracy.toString())
            .checkFieldIsNull("payload")
            .checkAll();
    }

    @Test
    public void convertIfNetPayoutIsIllegal() {
        when(data.getNetPayout()).thenReturn(Double.NaN);
        final AutoAdRevenue autoAdRevenue = converter.convert(data);

        ObjectPropertyAssertions(autoAdRevenue)
            .checkField("adRevenue", BigDecimal.valueOf(0.0))
            .checkField("currency", Currency.getInstance(currency))
            .checkField("adType", AutoAdType.REWARDED)
            .checkField("adNetwork", demandSource)
            .checkField("adUnitId", creativeId)
            .checkFieldIsNull("adUnitName")
            .checkFieldIsNull("adPlacementId")
            .checkFieldIsNull("adPlacementName")
            .checkField("precision", priceAccuracy.toString())
            .checkFieldIsNull("payload")
            .checkAll();
    }

    @Test
    public void convertAndCheckAutoAdType() {
        when(data.getPlacementType()).thenReturn(null);
        final AutoAdRevenue autoAdRevenueNull = converter.convert(data);
        assertThat(autoAdRevenueNull.getAdType()).isNull();

        when(data.getPlacementType()).thenReturn(PlacementType.REWARDED);
        final AutoAdRevenue autoAdRevenueRewarded = converter.convert(data);
        assertThat(autoAdRevenueRewarded.getAdType()).isEqualTo(AutoAdType.REWARDED);

        when(data.getPlacementType()).thenReturn(PlacementType.INTERSTITIAL);
        final AutoAdRevenue autoAdRevenueInterstitial = converter.convert(data);
        assertThat(autoAdRevenueInterstitial.getAdType()).isEqualTo(AutoAdType.INTERSTITIAL);

        when(data.getPlacementType()).thenReturn(PlacementType.BANNER);
        final AutoAdRevenue autoAdRevenueBanner = converter.convert(data);
        assertThat(autoAdRevenueBanner.getAdType()).isEqualTo(AutoAdType.BANNER);
    }
}
