package io.appmetrica.analytics

import io.appmetrica.analytics.impl.attribution.ExternalAttributionType
import io.appmetrica.analytics.impl.attribution.JSONObjectExternalAttribution
import io.appmetrica.analytics.impl.attribution.MapExternalAttribution
import io.appmetrica.analytics.impl.attribution.NullExternalAttribution
import io.appmetrica.analytics.impl.attribution.ObjectExternalAttribution
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test

internal class ExternalAttributionsTest : CommonTest() {

    @get:Rule
    val mapExternalAttributionRule = MockedConstructionRule(MapExternalAttribution::class.java)
    @get:Rule
    val objectExternalAttributionRule = MockedConstructionRule(ObjectExternalAttribution::class.java)
    @get:Rule
    val jsonObjectExternalAttributionRule = MockedConstructionRule(JSONObjectExternalAttribution::class.java)
    @get:Rule
    val nullExternalAttributionRule = MockedConstructionRule(NullExternalAttribution::class.java)

    @Test
    fun appsflyer() {
        val value = mapOf<String, Any>()
        val attribution = ExternalAttributions.appsflyer(value)

        assertThat(attribution).isEqualTo(mapExternalAttributionRule.constructionMock.constructed().first())
        assertThat(mapExternalAttributionRule.argumentInterceptor.flatArguments()).containsExactly(
            ExternalAttributionType.APPSFLYER,
            value
        )
    }

    @Test
    fun appsflyerIfNull() {
        val attribution = ExternalAttributions.appsflyer(null)

        assertThat(attribution).isEqualTo(nullExternalAttributionRule.constructionMock.constructed().first())
        assertThat(nullExternalAttributionRule.argumentInterceptor.flatArguments()).containsExactly(
            ExternalAttributionType.APPSFLYER
        )
    }

    @Test
    fun adjust() {
        val value = object {}
        val attribution = ExternalAttributions.adjust(value)

        assertThat(attribution).isEqualTo(objectExternalAttributionRule.constructionMock.constructed().first())
        assertThat(objectExternalAttributionRule.argumentInterceptor.flatArguments()).containsExactly(
            ExternalAttributionType.ADJUST,
            value
        )
    }

    @Test
    fun adjustIfNull() {
        val attribution = ExternalAttributions.adjust(null)

        assertThat(attribution).isEqualTo(nullExternalAttributionRule.constructionMock.constructed().first())
        assertThat(nullExternalAttributionRule.argumentInterceptor.flatArguments()).containsExactly(
            ExternalAttributionType.ADJUST
        )
    }

    @Test
    fun kochava() {
        val value = JSONObject()
        val attribution = ExternalAttributions.kochava(value)

        assertThat(attribution).isEqualTo(jsonObjectExternalAttributionRule.constructionMock.constructed().first())
        assertThat(jsonObjectExternalAttributionRule.argumentInterceptor.flatArguments()).containsExactly(
            ExternalAttributionType.KOCHAVA,
            value
        )
    }

    @Test
    fun kochavaIfNull() {
        val attribution = ExternalAttributions.kochava(null)

        assertThat(attribution).isEqualTo(nullExternalAttributionRule.constructionMock.constructed().first())
        assertThat(nullExternalAttributionRule.argumentInterceptor.flatArguments()).containsExactly(
            ExternalAttributionType.KOCHAVA
        )
    }

    @Test
    fun tenjin() {
        val value = mapOf<String, String>()
        val attribution = ExternalAttributions.tenjin(value)

        assertThat(attribution).isEqualTo(mapExternalAttributionRule.constructionMock.constructed().first())
        assertThat(mapExternalAttributionRule.argumentInterceptor.flatArguments()).containsExactly(
            ExternalAttributionType.TENJIN,
            value
        )
    }

    @Test
    fun tenjinIfNull() {
        val attribution = ExternalAttributions.tenjin(null)

        assertThat(attribution).isEqualTo(nullExternalAttributionRule.constructionMock.constructed().first())
        assertThat(nullExternalAttributionRule.argumentInterceptor.flatArguments()).containsExactly(
            ExternalAttributionType.TENJIN
        )
    }

    @Test
    fun airbridge() {
        val value = mapOf<String, String>()
        val attribution = ExternalAttributions.airbridge(value)

        assertThat(attribution).isEqualTo(mapExternalAttributionRule.constructionMock.constructed().first())
        assertThat(mapExternalAttributionRule.argumentInterceptor.flatArguments()).containsExactly(
            ExternalAttributionType.AIRBRIDGE,
            value
        )
    }

    @Test
    fun airbridgeIfNull() {
        val attribution = ExternalAttributions.airbridge(null)

        assertThat(attribution).isEqualTo(nullExternalAttributionRule.constructionMock.constructed().first())
        assertThat(nullExternalAttributionRule.argumentInterceptor.flatArguments()).containsExactly(
            ExternalAttributionType.AIRBRIDGE
        )
    }

    @Test
    fun singular() {
        val value = mapOf<String, Any>()
        val attribution = ExternalAttributions.singular(value)

        assertThat(attribution).isEqualTo(mapExternalAttributionRule.constructionMock.constructed().first())
        assertThat(mapExternalAttributionRule.argumentInterceptor.flatArguments()).containsExactly(
            ExternalAttributionType.SINGULAR,
            value
        )
    }

    @Test
    fun singularIfNull() {
        val attribution = ExternalAttributions.singular(null)

        assertThat(attribution).isEqualTo(nullExternalAttributionRule.constructionMock.constructed().first())
        assertThat(nullExternalAttributionRule.argumentInterceptor.flatArguments()).containsExactly(
            ExternalAttributionType.SINGULAR
        )
    }
}
