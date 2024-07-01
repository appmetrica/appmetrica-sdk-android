package io.appmetrica.analytics.adrevenue.ironsource.v7.impl;

import com.ironsource.mediationsdk.impressionData.ImpressionData;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenue;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdType;
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

    private final double revenue = 123.1234;
    private final String adUnit = "Interstitial";
    private final String adNetwork = "adNetwork";
    private final String placement = "placement";
    private final String precision = "precision";

    private final ImpressionData data = mock(ImpressionData.class);

    private final AdRevenueConverter converter = new AdRevenueConverter();

    @Before
    public void setUp() {
        when(data.getRevenue()).thenReturn(revenue);
        when(data.getAdUnit()).thenReturn(adUnit);
        when(data.getAdNetwork()).thenReturn(adNetwork);
        when(data.getPlacement()).thenReturn(placement);
        when(data.getPrecision()).thenReturn(precision);
    }

    @Test
    public void convert() {
        final ModuleAdRevenue adRevenue = converter.convert(data);

        ObjectPropertyAssertions(adRevenue)
            .checkField("adRevenue", BigDecimal.valueOf(revenue))
            .checkField("currency", Currency.getInstance("USD"))
            .checkField("adType", ModuleAdType.INTERSTITIAL)
            .checkField("adNetwork", adNetwork)
            .checkFieldIsNull("adUnitId")
            .checkFieldIsNull("adUnitName")
            .checkFieldIsNull("adPlacementId")
            .checkField("adPlacementName", placement)
            .checkField("precision", precision)
            .checkFieldIsNull("payload")
            .checkField("autoCollected", true)
            .checkAll();
    }

    @Test
    public void convertIfRevenueIsIllegal() {
        when(data.getRevenue()).thenReturn(Double.NaN);
        final ModuleAdRevenue adRevenue = converter.convert(data);

        ObjectPropertyAssertions(adRevenue)
            .checkField("adRevenue", BigDecimal.valueOf(0.0))
            .checkField("currency", Currency.getInstance("USD"))
            .checkField("adType", ModuleAdType.INTERSTITIAL)
            .checkField("adNetwork", adNetwork)
            .checkFieldIsNull("adUnitId")
            .checkFieldIsNull("adUnitName")
            .checkFieldIsNull("adPlacementId")
            .checkField("adPlacementName", placement)
            .checkField("precision", precision)
            .checkFieldIsNull("payload")
            .checkField("autoCollected", true)
            .checkAll();
    }

    @Test
    public void convertAndCheckAutoAdType() {
        when(data.getAdUnit()).thenReturn(null);
        final ModuleAdRevenue adRevenueNull = converter.convert(data);
        assertThat(adRevenueNull.getAdType()).isNull();

        when(data.getAdUnit()).thenReturn("Rewarded Video");
        final ModuleAdRevenue adRevenueRewarded = converter.convert(data);
        assertThat(adRevenueRewarded.getAdType()).isEqualTo(ModuleAdType.REWARDED);

        when(data.getAdUnit()).thenReturn("Interstitial");
        final ModuleAdRevenue adRevenueInterstitial = converter.convert(data);
        assertThat(adRevenueInterstitial.getAdType()).isEqualTo(ModuleAdType.INTERSTITIAL);

        when(data.getAdUnit()).thenReturn("Banner");
        final ModuleAdRevenue adRevenueBanner = converter.convert(data);
        assertThat(adRevenueBanner.getAdType()).isEqualTo(ModuleAdType.BANNER);

        when(data.getAdUnit()).thenReturn("Some string");
        final ModuleAdRevenue adRevenueOther = converter.convert(data);
        assertThat(adRevenueOther.getAdType()).isEqualTo(ModuleAdType.OTHER);
    }
}
