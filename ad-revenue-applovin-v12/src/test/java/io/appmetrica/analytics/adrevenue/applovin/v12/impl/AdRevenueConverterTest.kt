package io.appmetrica.analytics.adrevenue.applovin.v12.impl

import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdFormat
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkConfiguration
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.AdRevenueConstants
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenue
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdType
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.util.Currency

internal class AdRevenueConverterTest : CommonTest() {

    private val revenue = 123.1234
    private val format = MaxAdFormat.REWARDED
    private val networkName = "some_networkName"
    private val placement = "some_placement"
    private val networkPlacement = "some_networkPlacement"
    private val revenuePrecision = "some_revenuePrecision"
    private val countryCode = "RU"

    private val maxAd: MaxAd = mock {
        on { revenue } doReturn revenue
        on { format } doReturn format
        on { networkName } doReturn networkName
        on { placement } doReturn placement
        on { networkPlacement } doReturn networkPlacement
        on { revenuePrecision } doReturn revenuePrecision
    }
    private val configuration: AppLovinSdkConfiguration = mock {
        on { countryCode } doReturn countryCode
    }
    private val appLovinSdk: AppLovinSdk = mock {
        on { configuration } doReturn configuration
    }

    private val converter = AdRevenueConverter()

    @Test
    fun convert() {
        val adRevenue = converter.convert(maxAd, appLovinSdk)
        ObjectPropertyAssertions(adRevenue)
            .checkField("adRevenue", BigDecimal.valueOf(revenue))
            .checkField("currency", Currency.getInstance("USD"))
            .checkField("adType", ModuleAdType.REWARDED)
            .checkField("adNetwork", "some_networkName")
            .checkFieldIsNull("adUnitId")
            .checkFieldIsNull("adUnitName")
            .checkField("adPlacementId", "some_networkPlacement")
            .checkField("adPlacementName", "some_placement")
            .checkField("precision", "some_revenuePrecision")
            .checkField(
                "payload",
                mapOf(
                    "countryCode" to "RU",
                    "original_ad_type" to "REWARDED",
                    "original_source" to "ad-revenue-applovin-v12",
                    AdRevenueConstants.SOURCE_KEY to "applovin"
                )
            )
            .checkField("autoCollected", false)
            .checkAll()
    }

    @Test
    fun convertIfNetPayoutIsIllegal() {
        whenever(maxAd.revenue).thenReturn(Double.NaN)
        val adRevenue: ModuleAdRevenue = converter.convert(maxAd, appLovinSdk)
        ObjectPropertyAssertions(adRevenue)
            .checkField("adRevenue", BigDecimal.valueOf(0.0))
            .checkField("currency", Currency.getInstance("USD"))
            .checkField("adType", ModuleAdType.REWARDED)
            .checkField("adNetwork", "some_networkName")
            .checkFieldIsNull("adUnitId")
            .checkFieldIsNull("adUnitName")
            .checkField("adPlacementId", "some_networkPlacement")
            .checkField("adPlacementName", "some_placement")
            .checkField("precision", "some_revenuePrecision")
            .checkField(
                "payload",
                mapOf(
                    "countryCode" to "RU",
                    "original_ad_type" to "REWARDED",
                    "original_source" to "ad-revenue-applovin-v12",
                    AdRevenueConstants.SOURCE_KEY to "applovin"
                )
            )
            .checkField("autoCollected", false)
            .checkAll()
    }

    @Test
    fun convertAndCheckAutoAdType() {
        whenever(maxAd.format).thenReturn(null)
        converter.convert(maxAd, appLovinSdk).also { adRevenue ->
            assertThat(adRevenue.adType).isNull()
            assertThat(adRevenue.payload).isEqualTo(
                mapOf(
                    "countryCode" to "RU",
                    "original_ad_type" to "null",
                    "original_source" to "ad-revenue-applovin-v12",
                    AdRevenueConstants.SOURCE_KEY to "applovin"
                )
            )
        }

        whenever(maxAd.format).thenReturn(MaxAdFormat.BANNER)
        converter.convert(maxAd, appLovinSdk).also { adRevenue ->
            assertThat(adRevenue.adType).isEqualTo(ModuleAdType.BANNER)
            assertThat(adRevenue.payload).isEqualTo(
                mapOf(
                    "countryCode" to "RU",
                    "original_ad_type" to "BANNER",
                    "original_source" to "ad-revenue-applovin-v12",
                    AdRevenueConstants.SOURCE_KEY to "applovin"
                )
            )
        }

        whenever(maxAd.format).thenReturn(MaxAdFormat.MREC)
        converter.convert(maxAd, appLovinSdk).also { adRevenue ->
            assertThat(adRevenue.adType).isEqualTo(ModuleAdType.MREC)
            assertThat(adRevenue.payload).isEqualTo(
                mapOf(
                    "countryCode" to "RU",
                    "original_ad_type" to "MREC",
                    "original_source" to "ad-revenue-applovin-v12",
                    AdRevenueConstants.SOURCE_KEY to "applovin"
                )
            )
        }

        whenever(maxAd.format).thenReturn(MaxAdFormat.LEADER)
        converter.convert(maxAd, appLovinSdk).also { adRevenue ->
            assertThat(adRevenue.adType).isEqualTo(ModuleAdType.OTHER)
            assertThat(adRevenue.payload).isEqualTo(
                mapOf(
                    "countryCode" to "RU",
                    "original_ad_type" to "LEADER",
                    "original_source" to "ad-revenue-applovin-v12",
                    AdRevenueConstants.SOURCE_KEY to "applovin"
                )
            )
        }

        whenever(maxAd.format).thenReturn(MaxAdFormat.INTERSTITIAL)
        converter.convert(maxAd, appLovinSdk).also { adRevenue ->
            assertThat(adRevenue.adType).isEqualTo(ModuleAdType.INTERSTITIAL)
            assertThat(adRevenue.payload).isEqualTo(
                mapOf(
                    "countryCode" to "RU",
                    "original_ad_type" to "INTER",
                    "original_source" to "ad-revenue-applovin-v12",
                    AdRevenueConstants.SOURCE_KEY to "applovin"
                )
            )
        }

        whenever(maxAd.format).thenReturn(MaxAdFormat.APP_OPEN)
        converter.convert(maxAd, appLovinSdk).also { adRevenue ->
            assertThat(adRevenue.adType).isEqualTo(ModuleAdType.OTHER)
            assertThat(adRevenue.payload).isEqualTo(
                mapOf(
                    "countryCode" to "RU",
                    "original_ad_type" to "APPOPEN",
                    "original_source" to "ad-revenue-applovin-v12",
                    AdRevenueConstants.SOURCE_KEY to "applovin"
                )
            )
        }

        whenever(maxAd.format).thenReturn(MaxAdFormat.REWARDED)
        converter.convert(maxAd, appLovinSdk).also { adRevenue ->
            assertThat(adRevenue.adType).isEqualTo(ModuleAdType.REWARDED)
            assertThat(adRevenue.payload).isEqualTo(
                mapOf(
                    "countryCode" to "RU",
                    "original_ad_type" to "REWARDED",
                    "original_source" to "ad-revenue-applovin-v12",
                    AdRevenueConstants.SOURCE_KEY to "applovin"
                )
            )
        }

        whenever(maxAd.format).thenReturn(MaxAdFormat.REWARDED_INTERSTITIAL)
        converter.convert(maxAd, appLovinSdk).also { adRevenue ->
            assertThat(adRevenue.adType).isEqualTo(ModuleAdType.OTHER)
            assertThat(adRevenue.payload).isEqualTo(
                mapOf(
                    "countryCode" to "RU",
                    "original_ad_type" to "REWARDED_INTER",
                    "original_source" to "ad-revenue-applovin-v12",
                    AdRevenueConstants.SOURCE_KEY to "applovin"
                )
            )
        }

        whenever(maxAd.format).thenReturn(MaxAdFormat.NATIVE)
        converter.convert(maxAd, appLovinSdk).also { adRevenue ->
            assertThat(adRevenue.adType).isEqualTo(ModuleAdType.NATIVE)
            assertThat(adRevenue.payload).isEqualTo(
                mapOf(
                    "countryCode" to "RU",
                    "original_ad_type" to "NATIVE",
                    "original_source" to "ad-revenue-applovin-v12",
                    AdRevenueConstants.SOURCE_KEY to "applovin"
                )
            )
        }
    }
}
