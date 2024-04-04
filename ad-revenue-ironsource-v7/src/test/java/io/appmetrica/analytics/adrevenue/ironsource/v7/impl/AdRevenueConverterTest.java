package io.appmetrica.analytics.adrevenue.ironsource.v7.impl;

import com.ironsource.mediationsdk.impressionData.ImpressionData;
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
        final AutoAdRevenue autoAdRevenue = converter.convert(data);

        ObjectPropertyAssertions(autoAdRevenue)
            .checkField("adRevenue", BigDecimal.valueOf(revenue))
            .checkField("currency", Currency.getInstance("USD"))
            .checkField("adType", AutoAdType.INTERSTITIAL)
            .checkField("adNetwork", adNetwork)
            .checkFieldIsNull("adUnitId")
            .checkFieldIsNull("adUnitName")
            .checkFieldIsNull("adPlacementId")
            .checkField("adPlacementName", placement)
            .checkField("precision", precision)
            .checkFieldIsNull("payload")
            .checkAll();
    }

    @Test
    public void convertIfRevenueIsIllegal() {
        when(data.getRevenue()).thenReturn(Double.NaN);
        final AutoAdRevenue autoAdRevenue = converter.convert(data);

        ObjectPropertyAssertions(autoAdRevenue)
            .checkField("adRevenue", BigDecimal.valueOf(0.0))
            .checkField("currency", Currency.getInstance("USD"))
            .checkField("adType", AutoAdType.INTERSTITIAL)
            .checkField("adNetwork", adNetwork)
            .checkFieldIsNull("adUnitId")
            .checkFieldIsNull("adUnitName")
            .checkFieldIsNull("adPlacementId")
            .checkField("adPlacementName", placement)
            .checkField("precision", precision)
            .checkFieldIsNull("payload")
            .checkAll();
    }

    @Test
    public void convertAndCheckAutoAdType() {
        when(data.getAdUnit()).thenReturn(null);
        final AutoAdRevenue autoAdRevenueNull = converter.convert(data);
        assertThat(autoAdRevenueNull.getAdType()).isNull();

        when(data.getAdUnit()).thenReturn("Rewarded Video");
        final AutoAdRevenue autoAdRevenueRewarded = converter.convert(data);
        assertThat(autoAdRevenueRewarded.getAdType()).isEqualTo(AutoAdType.REWARDED);

        when(data.getAdUnit()).thenReturn("Interstitial");
        final AutoAdRevenue autoAdRevenueInterstitial = converter.convert(data);
        assertThat(autoAdRevenueInterstitial.getAdType()).isEqualTo(AutoAdType.INTERSTITIAL);

        when(data.getAdUnit()).thenReturn("Banner");
        final AutoAdRevenue autoAdRevenueBanner = converter.convert(data);
        assertThat(autoAdRevenueBanner.getAdType()).isEqualTo(AutoAdType.BANNER);

        when(data.getAdUnit()).thenReturn("Some string");
        final AutoAdRevenue autoAdRevenueOther = converter.convert(data);
        assertThat(autoAdRevenueOther.getAdType()).isEqualTo(AutoAdType.OTHER);
    }
}
