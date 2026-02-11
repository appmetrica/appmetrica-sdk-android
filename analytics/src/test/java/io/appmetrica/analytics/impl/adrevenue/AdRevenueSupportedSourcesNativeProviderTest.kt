package io.appmetrica.analytics.impl.adrevenue

import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONArray
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.whenever
import org.skyscreamer.jsonassert.JSONAssert

internal class AdRevenueSupportedSourcesNativeProviderTest : CommonTest() {

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    private val ironSource = "ironsource"
    private val fyber = "fyber"
    private val admob = "admob"

    private val adRevenueCollectorsSourceIds = mutableListOf(ironSource, fyber, admob)

    private val provider by setUp { AdRevenueSupportedSourcesNativeProvider() }

    @Before
    fun setUp() {
        whenever(ClientServiceLocator.getInstance().modulesController.adRevenueCollectorsSourceIds)
            .thenReturn(adRevenueCollectorsSourceIds)
    }

    @Test
    fun metaInfo() {
        JSONAssert.assertEquals(
            provider.metaInfo,
            JSONArray().put("yandex").put(ironSource).put(fyber).put(admob),
            true
        )
    }

    @Test
    fun `metaInfo if exception occurs`() {
        whenever(ClientServiceLocator.getInstance().modulesController.adRevenueCollectorsSourceIds)
            .thenThrow(RuntimeException())
        assertThat(provider.metaInfo).isNull()
    }
}
