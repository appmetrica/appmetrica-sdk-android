package io.appmetrica.analytics.adrevenue.ironsource.v7.impl;

import com.ironsource.mediationsdk.impressionData.ImpressionData;
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
            .checkField("payload", new HashMap<String, String>() {{
                put(AdRevenueConstants.ORIGINAL_SOURCE_KEY, "ad-revenue-ironsource-v7");
                put(AdRevenueConstants.ORIGINAL_AD_TYPE_KEY, "Interstitial");
                put(AdRevenueConstants.SOURCE_KEY, "ironsource");
            }})
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
            .checkField("payload", new HashMap<String, String>() {{
                put(AdRevenueConstants.ORIGINAL_SOURCE_KEY, "ad-revenue-ironsource-v7");
                put(AdRevenueConstants.ORIGINAL_AD_TYPE_KEY, "Interstitial");
                put(AdRevenueConstants.SOURCE_KEY, "ironsource");
            }})
            .checkField("autoCollected", true)
            .checkAll();
    }

    @Test
    public void convertAndCheckAutoAdType() {
        when(data.getAdUnit()).thenReturn(null);
        final ModuleAdRevenue adRevenueNull = converter.convert(data);
        assertThat(adRevenueNull.getAdType()).isNull();
        assertThat(adRevenueNull.getPayload()).isEqualTo(
            new HashMap<String, String>() {{
                put(AdRevenueConstants.ORIGINAL_SOURCE_KEY, "ad-revenue-ironsource-v7");
                put(AdRevenueConstants.ORIGINAL_AD_TYPE_KEY, "null");
                put(AdRevenueConstants.SOURCE_KEY, "ironsource");
            }}
        );

        when(data.getAdUnit()).thenReturn("Rewarded Video");
        final ModuleAdRevenue adRevenueRewarded = converter.convert(data);
        assertThat(adRevenueRewarded.getAdType()).isEqualTo(ModuleAdType.REWARDED);
        assertThat(adRevenueRewarded.getPayload()).isEqualTo(
            new HashMap<String, String>() {{
                put(AdRevenueConstants.ORIGINAL_SOURCE_KEY, "ad-revenue-ironsource-v7");
                put(AdRevenueConstants.ORIGINAL_AD_TYPE_KEY, "Rewarded Video");
                put(AdRevenueConstants.SOURCE_KEY, "ironsource");
            }}
        );

        when(data.getAdUnit()).thenReturn("Interstitial");
        final ModuleAdRevenue adRevenueInterstitial = converter.convert(data);
        assertThat(adRevenueInterstitial.getAdType()).isEqualTo(ModuleAdType.INTERSTITIAL);
        assertThat(adRevenueInterstitial.getPayload()).isEqualTo(
            new HashMap<String, String>() {{
                put(AdRevenueConstants.ORIGINAL_SOURCE_KEY, "ad-revenue-ironsource-v7");
                put(AdRevenueConstants.ORIGINAL_AD_TYPE_KEY, "Interstitial");
                put(AdRevenueConstants.SOURCE_KEY, "ironsource");
            }}
        );

        when(data.getAdUnit()).thenReturn("Banner");
        final ModuleAdRevenue adRevenueBanner = converter.convert(data);
        assertThat(adRevenueBanner.getAdType()).isEqualTo(ModuleAdType.BANNER);
        assertThat(adRevenueBanner.getPayload()).isEqualTo(
            new HashMap<String, String>() {{
                put(AdRevenueConstants.ORIGINAL_SOURCE_KEY, "ad-revenue-ironsource-v7");
                put(AdRevenueConstants.ORIGINAL_AD_TYPE_KEY, "Banner");
                put(AdRevenueConstants.SOURCE_KEY, "ironsource");
            }}
        );

        when(data.getAdUnit()).thenReturn("Some string");
        final ModuleAdRevenue adRevenueOther = converter.convert(data);
        assertThat(adRevenueOther.getAdType()).isEqualTo(ModuleAdType.OTHER);
        assertThat(adRevenueOther.getPayload()).isEqualTo(
            new HashMap<String, String>() {{
                put(AdRevenueConstants.ORIGINAL_SOURCE_KEY, "ad-revenue-ironsource-v7");
                put(AdRevenueConstants.ORIGINAL_AD_TYPE_KEY, "Some string");
                put(AdRevenueConstants.SOURCE_KEY, "ironsource");
            }}
        );
    }
}
