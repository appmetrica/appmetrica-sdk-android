package io.appmetrica.analytics.adrevenue.admob.v23.impl;

import com.google.android.gms.ads.AdValue;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdapterResponseInfo;
import com.google.android.gms.ads.ResponseInfo;
import com.google.android.gms.ads.nativead.NativeAd;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenue;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdType;
import io.appmetrica.analytics.testutils.CommonTest;
import java.math.BigDecimal;
import java.util.Currency;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class AdRevenueConverterTest extends CommonTest {

    private final String currencyCode = "RUB";
    private final long valueMicros = 42_000_000L;
    private final double value = valueMicros / 1_000_000.0;
    private final int precisionType = 2;
    private final String adUnitId = "some_adUnitId";
    private final String adPlacementId = "some_adPlacementId";
    private final String adPlacementName = "some_adPlacementName";
    private final String adNetwork = "some_adNetwork";

    @Mock
    private AdValue adValue;
    @Mock
    private AdView adView;
    @Mock
    private NativeAd nativeAd;
    @Mock
    private ResponseInfo responseInfo;
    @Mock
    private AdapterResponseInfo adapterResponseInfo;

    private final AdRevenueConverter adRevenueConverter = new AdRevenueConverter();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        when(responseInfo.getLoadedAdapterResponseInfo()).thenReturn(adapterResponseInfo);

        when(adValue.getCurrencyCode()).thenReturn(currencyCode);
        when(adValue.getValueMicros()).thenReturn(valueMicros);
        when(adValue.getPrecisionType()).thenReturn(precisionType);

        when(adView.getAdUnitId()).thenReturn(adUnitId);
        when(adView.getResponseInfo()).thenReturn(responseInfo);

        when(nativeAd.getResponseInfo()).thenReturn(responseInfo);

        when(adapterResponseInfo.getAdapterClassName()).thenReturn(adNetwork);
        when(adapterResponseInfo.getAdSourceInstanceId()).thenReturn(adPlacementId);
        when(adapterResponseInfo.getAdSourceInstanceName()).thenReturn(adPlacementName);
    }

    @Test
    public void convertBanner() {
        ModuleAdRevenue adRevenue = adRevenueConverter.convertBanner(adValue, adView);
        ObjectPropertyAssertions(adRevenue)
            .checkField("adRevenue", BigDecimal.valueOf(value))
            .checkField("currency", Currency.getInstance(currencyCode))
            .checkField("adType", ModuleAdType.BANNER)
            .checkField("adNetwork", adNetwork)
            .checkField("adUnitId", adUnitId)
            .checkFieldIsNull("adUnitName")
            .checkField("adPlacementId", adPlacementId)
            .checkField("adPlacementName", adPlacementName)
            .checkField("precision", "PUBLISHER_PROVIDED")
            .checkFieldIsNull("payload")
            .checkField("autoCollected", false)
            .checkAll();
    }

    @Test
    public void convertBannerIfResponseInfoIsNull() {
        when(adView.getResponseInfo()).thenReturn(null);

        ModuleAdRevenue adRevenue = adRevenueConverter.convertBanner(adValue, adView);
        ObjectPropertyAssertions(adRevenue)
            .checkField("adRevenue", BigDecimal.valueOf(value))
            .checkField("currency", Currency.getInstance(currencyCode))
            .checkField("adType", ModuleAdType.BANNER)
            .checkFieldIsNull("adNetwork")
            .checkField("adUnitId", adUnitId)
            .checkFieldIsNull("adUnitName")
            .checkFieldIsNull("adPlacementId")
            .checkFieldIsNull("adPlacementName")
            .checkField("precision", "PUBLISHER_PROVIDED")
            .checkFieldIsNull("payload")
            .checkField("autoCollected", false)
            .checkAll();
    }

    @Test
    public void convertBannerIfLoadedAdapterResponseInfoIsNull() {
        when(responseInfo.getLoadedAdapterResponseInfo()).thenReturn(null);

        ModuleAdRevenue adRevenue = adRevenueConverter.convertBanner(adValue, adView);
        ObjectPropertyAssertions(adRevenue)
            .checkField("adRevenue", BigDecimal.valueOf(value))
            .checkField("currency", Currency.getInstance(currencyCode))
            .checkField("adType", ModuleAdType.BANNER)
            .checkFieldIsNull("adNetwork")
            .checkField("adUnitId", adUnitId)
            .checkFieldIsNull("adUnitName")
            .checkFieldIsNull("adPlacementId")
            .checkFieldIsNull("adPlacementName")
            .checkField("precision", "PUBLISHER_PROVIDED")
            .checkFieldIsNull("payload")
            .checkField("autoCollected", false)
            .checkAll();
    }

    @Test
    public void convertInterstitialAd() {
        ModuleAdRevenue adRevenue = adRevenueConverter.convertInterstitialAd(adValue, adView);
        ObjectPropertyAssertions(adRevenue)
            .checkField("adRevenue", BigDecimal.valueOf(value))
            .checkField("currency", Currency.getInstance(currencyCode))
            .checkField("adType", ModuleAdType.INTERSTITIAL)
            .checkField("adNetwork", adNetwork)
            .checkField("adUnitId", adUnitId)
            .checkFieldIsNull("adUnitName")
            .checkField("adPlacementId", adPlacementId)
            .checkField("adPlacementName", adPlacementName)
            .checkField("precision", "PUBLISHER_PROVIDED")
            .checkFieldIsNull("payload")
            .checkField("autoCollected", false)
            .checkAll();
    }

    @Test
    public void convertInterstitialAdIfResponseInfoIsNull() {
        when(adView.getResponseInfo()).thenReturn(null);

        ModuleAdRevenue adRevenue = adRevenueConverter.convertInterstitialAd(adValue, adView);
        ObjectPropertyAssertions(adRevenue)
            .checkField("adRevenue", BigDecimal.valueOf(value))
            .checkField("currency", Currency.getInstance(currencyCode))
            .checkField("adType", ModuleAdType.INTERSTITIAL)
            .checkFieldIsNull("adNetwork")
            .checkField("adUnitId", adUnitId)
            .checkFieldIsNull("adUnitName")
            .checkFieldIsNull("adPlacementId")
            .checkFieldIsNull("adPlacementName")
            .checkField("precision", "PUBLISHER_PROVIDED")
            .checkFieldIsNull("payload")
            .checkField("autoCollected", false)
            .checkAll();
    }

    @Test
    public void convertInterstitialAdIfLoadedAdapterResponseInfoIsNull() {
        when(responseInfo.getLoadedAdapterResponseInfo()).thenReturn(null);

        ModuleAdRevenue adRevenue = adRevenueConverter.convertInterstitialAd(adValue, adView);
        ObjectPropertyAssertions(adRevenue)
            .checkField("adRevenue", BigDecimal.valueOf(value))
            .checkField("currency", Currency.getInstance(currencyCode))
            .checkField("adType", ModuleAdType.INTERSTITIAL)
            .checkFieldIsNull("adNetwork")
            .checkField("adUnitId", adUnitId)
            .checkFieldIsNull("adUnitName")
            .checkFieldIsNull("adPlacementId")
            .checkFieldIsNull("adPlacementName")
            .checkField("precision", "PUBLISHER_PROVIDED")
            .checkFieldIsNull("payload")
            .checkField("autoCollected", false)
            .checkAll();
    }

    @Test
    public void convertRewardedAd() {
        ModuleAdRevenue adRevenue = adRevenueConverter.convertRewardedAd(adValue, adView);
        ObjectPropertyAssertions(adRevenue)
            .checkField("adRevenue", BigDecimal.valueOf(value))
            .checkField("currency", Currency.getInstance(currencyCode))
            .checkField("adType", ModuleAdType.REWARDED)
            .checkField("adNetwork", adNetwork)
            .checkField("adUnitId", adUnitId)
            .checkFieldIsNull("adUnitName")
            .checkField("adPlacementId", adPlacementId)
            .checkField("adPlacementName", adPlacementName)
            .checkField("precision", "PUBLISHER_PROVIDED")
            .checkFieldIsNull("payload")
            .checkField("autoCollected", false)
            .checkAll();
    }

    @Test
    public void convertRewardedAdIfResponseInfoIsNull() {
        when(adView.getResponseInfo()).thenReturn(null);

        ModuleAdRevenue adRevenue = adRevenueConverter.convertRewardedAd(adValue, adView);
        ObjectPropertyAssertions(adRevenue)
            .checkField("adRevenue", BigDecimal.valueOf(value))
            .checkField("currency", Currency.getInstance(currencyCode))
            .checkField("adType", ModuleAdType.REWARDED)
            .checkFieldIsNull("adNetwork")
            .checkField("adUnitId", adUnitId)
            .checkFieldIsNull("adUnitName")
            .checkFieldIsNull("adPlacementId")
            .checkFieldIsNull("adPlacementName")
            .checkField("precision", "PUBLISHER_PROVIDED")
            .checkFieldIsNull("payload")
            .checkField("autoCollected", false)
            .checkAll();
    }

    @Test
    public void convertRewardedAdIfLoadedAdapterResponseInfoIsNull() {
        when(responseInfo.getLoadedAdapterResponseInfo()).thenReturn(null);

        ModuleAdRevenue adRevenue = adRevenueConverter.convertRewardedAd(adValue, adView);
        ObjectPropertyAssertions(adRevenue)
            .checkField("adRevenue", BigDecimal.valueOf(value))
            .checkField("currency", Currency.getInstance(currencyCode))
            .checkField("adType", ModuleAdType.REWARDED)
            .checkFieldIsNull("adNetwork")
            .checkField("adUnitId", adUnitId)
            .checkFieldIsNull("adUnitName")
            .checkFieldIsNull("adPlacementId")
            .checkFieldIsNull("adPlacementName")
            .checkField("precision", "PUBLISHER_PROVIDED")
            .checkFieldIsNull("payload")
            .checkField("autoCollected", false)
            .checkAll();
    }

    @Test
    public void convertRewardedInterstitialAd() {
        ModuleAdRevenue adRevenue = adRevenueConverter.convertRewardedInterstitialAd(adValue, adView);
        ObjectPropertyAssertions(adRevenue)
            .checkField("adRevenue", BigDecimal.valueOf(value))
            .checkField("currency", Currency.getInstance(currencyCode))
            .checkField("adType", ModuleAdType.OTHER)
            .checkField("adNetwork", adNetwork)
            .checkField("adUnitId", adUnitId)
            .checkFieldIsNull("adUnitName")
            .checkField("adPlacementId", adPlacementId)
            .checkField("adPlacementName", adPlacementName)
            .checkField("precision", "PUBLISHER_PROVIDED")
            .checkFieldIsNull("payload")
            .checkField("autoCollected", false)
            .checkAll();
    }

    @Test
    public void convertRewardedInterstitialAdIfResponseInfoIsNull() {
        when(adView.getResponseInfo()).thenReturn(null);

        ModuleAdRevenue adRevenue = adRevenueConverter.convertRewardedInterstitialAd(adValue, adView);
        ObjectPropertyAssertions(adRevenue)
            .checkField("adRevenue", BigDecimal.valueOf(value))
            .checkField("currency", Currency.getInstance(currencyCode))
            .checkField("adType", ModuleAdType.OTHER)
            .checkFieldIsNull("adNetwork")
            .checkField("adUnitId", adUnitId)
            .checkFieldIsNull("adUnitName")
            .checkFieldIsNull("adPlacementId")
            .checkFieldIsNull("adPlacementName")
            .checkField("precision", "PUBLISHER_PROVIDED")
            .checkFieldIsNull("payload")
            .checkField("autoCollected", false)
            .checkAll();
    }

    @Test
    public void convertRewardedInterstitialAdIfLoadedAdapterResponseInfoIsNull() {
        when(responseInfo.getLoadedAdapterResponseInfo()).thenReturn(null);

        ModuleAdRevenue adRevenue = adRevenueConverter.convertRewardedInterstitialAd(adValue, adView);
        ObjectPropertyAssertions(adRevenue)
            .checkField("adRevenue", BigDecimal.valueOf(value))
            .checkField("currency", Currency.getInstance(currencyCode))
            .checkField("adType", ModuleAdType.OTHER)
            .checkFieldIsNull("adNetwork")
            .checkField("adUnitId", adUnitId)
            .checkFieldIsNull("adUnitName")
            .checkFieldIsNull("adPlacementId")
            .checkFieldIsNull("adPlacementName")
            .checkField("precision", "PUBLISHER_PROVIDED")
            .checkFieldIsNull("payload")
            .checkField("autoCollected", false)
            .checkAll();
    }

    @Test
    public void convertNativeAd() {
        ModuleAdRevenue adRevenue = adRevenueConverter.convertNativeAd(adValue, nativeAd);
        ObjectPropertyAssertions(adRevenue)
            .checkField("adRevenue", BigDecimal.valueOf(value))
            .checkField("currency", Currency.getInstance(currencyCode))
            .checkField("adType", ModuleAdType.NATIVE)
            .checkField("adNetwork", adNetwork)
            .checkFieldIsNull("adUnitId")
            .checkFieldIsNull("adUnitName")
            .checkField("adPlacementId", adPlacementId)
            .checkField("adPlacementName", adPlacementName)
            .checkField("precision", "PUBLISHER_PROVIDED")
            .checkFieldIsNull("payload")
            .checkField("autoCollected", false)
            .checkAll();
    }

    @Test
    public void convertNativeAdIfResponseInfoIsNull() {
        when(nativeAd.getResponseInfo()).thenReturn(null);

        ModuleAdRevenue adRevenue = adRevenueConverter.convertNativeAd(adValue, nativeAd);
        ObjectPropertyAssertions(adRevenue)
            .checkField("adRevenue", BigDecimal.valueOf(value))
            .checkField("currency", Currency.getInstance(currencyCode))
            .checkField("adType", ModuleAdType.NATIVE)
            .checkFieldIsNull("adNetwork")
            .checkFieldIsNull("adUnitId")
            .checkFieldIsNull("adUnitName")
            .checkFieldIsNull("adPlacementId")
            .checkFieldIsNull("adPlacementName")
            .checkField("precision", "PUBLISHER_PROVIDED")
            .checkFieldIsNull("payload")
            .checkField("autoCollected", false)
            .checkAll();
    }

    @Test
    public void convertNativeAdIfLoadedAdapterResponseInfoIsNull() {
        when(responseInfo.getLoadedAdapterResponseInfo()).thenReturn(null);

        ModuleAdRevenue adRevenue = adRevenueConverter.convertNativeAd(adValue, nativeAd);
        ObjectPropertyAssertions(adRevenue)
            .checkField("adRevenue", BigDecimal.valueOf(value))
            .checkField("currency", Currency.getInstance(currencyCode))
            .checkField("adType", ModuleAdType.NATIVE)
            .checkFieldIsNull("adNetwork")
            .checkFieldIsNull("adUnitId")
            .checkFieldIsNull("adUnitName")
            .checkFieldIsNull("adPlacementId")
            .checkFieldIsNull("adPlacementName")
            .checkField("precision", "PUBLISHER_PROVIDED")
            .checkFieldIsNull("payload")
            .checkField("autoCollected", false)
            .checkAll();
    }

    @Test
    public void convertPrecision() {
        when(adValue.getPrecisionType()).thenReturn(-1);
        assertThat(adRevenueConverter.convertBanner(adValue, adView).getPrecision())
            .isEqualTo("");

        when(adValue.getPrecisionType()).thenReturn(0);
        assertThat(adRevenueConverter.convertBanner(adValue, adView).getPrecision())
            .isEqualTo("UNKNOWN");

        when(adValue.getPrecisionType()).thenReturn(1);
        assertThat(adRevenueConverter.convertBanner(adValue, adView).getPrecision())
            .isEqualTo("ESTIMATED");

        when(adValue.getPrecisionType()).thenReturn(2);
        assertThat(adRevenueConverter.convertBanner(adValue, adView).getPrecision())
            .isEqualTo("PUBLISHER_PROVIDED");

        when(adValue.getPrecisionType()).thenReturn(3);
        assertThat(adRevenueConverter.convertBanner(adValue, adView).getPrecision())
            .isEqualTo("PRECISE");

        when(adValue.getPrecisionType()).thenReturn(4);
        assertThat(adRevenueConverter.convertBanner(adValue, adView).getPrecision())
            .isEqualTo("");
    }
}