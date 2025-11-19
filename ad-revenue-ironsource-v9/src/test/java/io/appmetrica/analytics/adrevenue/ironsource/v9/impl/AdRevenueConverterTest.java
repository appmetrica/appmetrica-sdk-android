package io.appmetrica.analytics.adrevenue.ironsource.v9.impl;

import com.unity3d.mediation.impression.LevelPlayImpressionData;
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
    private final String adUnit = "interstitial";
    private final String adNetwork = "adNetwork";
    private final String placement = "placement";
    private final String precision = "precision";
    private final String unitId = "unitId";
    private final String unitName = "unitName";
    private final LevelPlayImpressionData data = mock(LevelPlayImpressionData.class);

    private final AdRevenueConverter converter = new AdRevenueConverter();

    @Before
    public void setUp() {
        when(data.getRevenue()).thenReturn(revenue);
        when(data.getAdFormat()).thenReturn(adUnit);
        when(data.getAdNetwork()).thenReturn(adNetwork);
        when(data.getPlacement()).thenReturn(placement);
        when(data.getPrecision()).thenReturn(precision);
        when(data.getMediationAdUnitName()).thenReturn(unitName);
        when(data.getMediationAdUnitId()).thenReturn(unitId);
    }

    @Test
    public void convert() {
        final ModuleAdRevenue adRevenue = converter.convert(data);

        ObjectPropertyAssertions(adRevenue)
            .checkField("adRevenue", BigDecimal.valueOf(revenue))
            .checkField("currency", Currency.getInstance("USD"))
            .checkField("adType", ModuleAdType.INTERSTITIAL)
            .checkField("adNetwork", adNetwork)
            .checkField("adUnitId", unitId)
            .checkField("adUnitName", unitName)
            .checkFieldIsNull("adPlacementId")
            .checkField("adPlacementName", placement)
            .checkField("precision", precision)
            .checkField("payload", new HashMap<String, String>() {{
                put(AdRevenueConstants.ORIGINAL_SOURCE_KEY, "ad-revenue-ironsource-v9");
                put(AdRevenueConstants.ORIGINAL_AD_TYPE_KEY, "interstitial");
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
            .checkField("adUnitId", unitId)
            .checkField("adUnitName", unitName)
            .checkFieldIsNull("adPlacementId")
            .checkField("adPlacementName", placement)
            .checkField("precision", precision)
            .checkField("payload", new HashMap<String, String>() {{
                put(AdRevenueConstants.ORIGINAL_SOURCE_KEY, "ad-revenue-ironsource-v9");
                put(AdRevenueConstants.ORIGINAL_AD_TYPE_KEY, "interstitial");
                put(AdRevenueConstants.SOURCE_KEY, "ironsource");
            }})
            .checkField("autoCollected", true)
            .checkAll();
    }

    @Test
    public void convertAndCheckAutoAdType() {
        when(data.getAdFormat()).thenReturn(null);
        final ModuleAdRevenue adRevenueNull = converter.convert(data);
        assertThat(adRevenueNull.getAdType()).isNull();
        assertThat(adRevenueNull.getPayload()).isEqualTo(
            new HashMap<String, String>() {{
                put(AdRevenueConstants.ORIGINAL_SOURCE_KEY, "ad-revenue-ironsource-v9");
                put(AdRevenueConstants.ORIGINAL_AD_TYPE_KEY, "null");
                put(AdRevenueConstants.SOURCE_KEY, "ironsource");
            }}
        );

        when(data.getAdFormat()).thenReturn("rewarded_video");
        final ModuleAdRevenue adRevenueRewarded = converter.convert(data);
        assertThat(adRevenueRewarded.getAdType()).isEqualTo(ModuleAdType.REWARDED);
        assertThat(adRevenueRewarded.getPayload()).isEqualTo(
            new HashMap<String, String>() {{
                put(AdRevenueConstants.ORIGINAL_SOURCE_KEY, "ad-revenue-ironsource-v9");
                put(AdRevenueConstants.ORIGINAL_AD_TYPE_KEY, "rewarded_video");
                put(AdRevenueConstants.SOURCE_KEY, "ironsource");
            }}
        );

        when(data.getAdFormat()).thenReturn("interstitial");
        final ModuleAdRevenue adRevenueInterstitial = converter.convert(data);
        assertThat(adRevenueInterstitial.getAdType()).isEqualTo(ModuleAdType.INTERSTITIAL);
        assertThat(adRevenueInterstitial.getPayload()).isEqualTo(
            new HashMap<String, String>() {{
                put(AdRevenueConstants.ORIGINAL_SOURCE_KEY, "ad-revenue-ironsource-v9");
                put(AdRevenueConstants.ORIGINAL_AD_TYPE_KEY, "interstitial");
                put(AdRevenueConstants.SOURCE_KEY, "ironsource");
            }}
        );

        when(data.getAdFormat()).thenReturn("banner");
        final ModuleAdRevenue adRevenueBanner = converter.convert(data);
        assertThat(adRevenueBanner.getAdType()).isEqualTo(ModuleAdType.BANNER);
        assertThat(adRevenueBanner.getPayload()).isEqualTo(
            new HashMap<String, String>() {{
                put(AdRevenueConstants.ORIGINAL_SOURCE_KEY, "ad-revenue-ironsource-v9");
                put(AdRevenueConstants.ORIGINAL_AD_TYPE_KEY, "banner");
                put(AdRevenueConstants.SOURCE_KEY, "ironsource");
            }}
        );

        when(data.getAdFormat()).thenReturn("Some string");
        final ModuleAdRevenue adRevenueOther = converter.convert(data);
        assertThat(adRevenueOther.getAdType()).isEqualTo(ModuleAdType.OTHER);
        assertThat(adRevenueOther.getPayload()).isEqualTo(
            new HashMap<String, String>() {{
                put(AdRevenueConstants.ORIGINAL_SOURCE_KEY, "ad-revenue-ironsource-v9");
                put(AdRevenueConstants.ORIGINAL_AD_TYPE_KEY, "Some string");
                put(AdRevenueConstants.SOURCE_KEY, "ironsource");
            }}
        );
    }
}
