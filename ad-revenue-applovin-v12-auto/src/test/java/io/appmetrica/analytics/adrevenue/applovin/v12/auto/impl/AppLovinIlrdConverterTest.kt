package io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl

import android.os.Bundle
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.AdRevenueConstants
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdType
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.Assertions.ObjectPropertyAssertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.math.BigDecimal
import java.util.Currency

internal class AppLovinIlrdConverterTest : CommonTest() {

    private val converter = AppLovinIlrdConverter()

    @Test
    fun convertAllFields() {
        val bundle: Bundle = mock {
            on { containsKey("revenue") } doReturn true
            on { getDouble("revenue") } doReturn 1.23
            on { getString("ad_format") } doReturn "BANNER"
            on { getString("network_name") } doReturn "some_network"
            on { getString("max_ad_unit_id") } doReturn "unit_id"
            on { getString("third_party_ad_placement_id") } doReturn "placement_id"
            on { getString("precision") } doReturn "publisher_defined"
        }

        val result = converter.convert(bundle)

        ObjectPropertyAssertions(result)
            .checkField("adRevenue", BigDecimal.valueOf(1.23))
            .checkField("currency", Currency.getInstance("USD"))
            .checkField("adType", ModuleAdType.BANNER)
            .checkField("adNetwork", "some_network")
            .checkField("adUnitId", "unit_id")
            .checkFieldIsNull("adUnitName")
            .checkField("adPlacementId", "placement_id")
            .checkFieldIsNull("adPlacementName")
            .checkField("precision", "publisher_defined")
            .checkField("autoCollected", true)
            .checkField(
                "payload",
                mapOf(
                    AdRevenueConstants.SOURCE_KEY to "applovin",
                    AdRevenueConstants.ORIGINAL_SOURCE_KEY to "ad-revenue-applovin-v12-auto",
                    AdRevenueConstants.ORIGINAL_AD_TYPE_KEY to "BANNER",
                )
            )
            .checkAll()
    }

    @RunWith(Parameterized::class)
    internal class AdFormatMappingTest(
        private val adFormat: String?,
        private val expectedAdType: ModuleAdType?,
        private val expectedOriginalAdType: String,
    ) : CommonTest() {

        private val converter = AppLovinIlrdConverter()

        companion object {
            @Parameterized.Parameters(name = "adFormat={0} -> {1}")
            @JvmStatic
            fun data(): Collection<Array<Any?>> = listOf(
                arrayOf("BANNER", ModuleAdType.BANNER, "BANNER"),
                arrayOf("MREC", ModuleAdType.MREC, "MREC"),
                arrayOf("NATIVE", ModuleAdType.NATIVE, "NATIVE"),
                arrayOf("INTER", ModuleAdType.INTERSTITIAL, "INTER"),
                arrayOf("REWARDED", ModuleAdType.REWARDED, "REWARDED"),
                arrayOf("REWARD", ModuleAdType.REWARDED, "REWARD"),
                arrayOf("APPOPEN", ModuleAdType.APP_OPEN, "APPOPEN"),
                arrayOf("UNKNOWN_FORMAT", ModuleAdType.OTHER, "UNKNOWN_FORMAT"),
                arrayOf(null, null, "null"),
            )
        }

        @Test
        fun convertAdFormat() {
            val bundle: Bundle = mock {
                on { containsKey("revenue") } doReturn true
                on { getDouble("revenue") } doReturn 1.0
                on { getString("ad_format") } doReturn adFormat
            }

            val result = converter.convert(bundle)

            assertThat(result.adType)
                .describedAs("ad_format=$adFormat should map to $expectedAdType")
                .isEqualTo(expectedAdType)
            assertThat(result.payload?.get(AdRevenueConstants.ORIGINAL_AD_TYPE_KEY))
                .describedAs("payload[ORIGINAL_AD_TYPE_KEY] for ad_format=$adFormat")
                .isEqualTo(expectedOriginalAdType)
        }
    }

    @RunWith(Parameterized::class)
    internal class RevenueBoundaryTest(
        private val revenuePresent: Boolean,
        private val inputRevenue: Double?,
        private val expectedRevenue: BigDecimal,
        private val expectedOriginalAdRevenue: String?,
    ) : CommonTest() {

        private val converter = AppLovinIlrdConverter()

        companion object {
            @Parameterized.Parameters(name = "present={0}, revenue={1} -> adRevenue={2}, original={3}")
            @JvmStatic
            fun data(): Collection<Array<Any?>> = listOf(
                arrayOf(false, null, BigDecimal.valueOf(0.0), "no-value"),
                arrayOf(true, Double.NaN, BigDecimal.valueOf(0.0), "NaN"),
                arrayOf(true, Double.POSITIVE_INFINITY, BigDecimal.valueOf(0.0), "Infinity"),
                arrayOf(true, Double.NEGATIVE_INFINITY, BigDecimal.valueOf(0.0), "-Infinity"),
                arrayOf(true, -1.0, BigDecimal.valueOf(-1.0), null),
                arrayOf(true, 0.0, BigDecimal.valueOf(0.0), null),
                arrayOf(true, 1.5, BigDecimal.valueOf(1.5), null),
            )
        }

        @Test
        fun convertRevenue() {
            val bundle: Bundle = mock {
                on { containsKey("revenue") } doReturn revenuePresent
                if (inputRevenue != null) {
                    on { getDouble("revenue") } doReturn inputRevenue
                }
                on { getString("ad_format") } doReturn "BANNER"
            }

            val result = converter.convert(bundle)

            assertThat(result.adRevenue)
                .describedAs("revenue=$inputRevenue should produce $expectedRevenue")
                .isEqualByComparingTo(expectedRevenue)
            assertThat(result.payload?.get(Constants.Payload.ORIGINAL_AD_REVENUE_KEY))
                .describedAs("original_ad_revenue should be $expectedOriginalAdRevenue")
                .isEqualTo(expectedOriginalAdRevenue)
        }
    }
}
