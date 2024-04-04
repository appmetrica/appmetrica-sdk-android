package io.appmetrica.analytics.impl.adrevenue

import io.appmetrica.analytics.AdRevenue
import io.appmetrica.analytics.ModulesFacade
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.AutoAdRevenue
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import io.appmetrica.analytics.testutils.staticRule
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class AppMetricaAutoAdRevenueReporterTest : CommonTest() {

    private val autoAdRevenue: AutoAdRevenue = mock()
    private val adRevenue: AdRevenue = mock()

    @get:Rule
    val converterRule = MockedConstructionRule(AutoAdRevenueConverter::class.java) { mock, _ ->
        whenever(mock.convert(autoAdRevenue)).thenReturn(adRevenue)
    }
    @get:Rule
    val modulesFacadeRule = staticRule<ModulesFacade>()

    private val reporter by setUp { AppMetricaAutoAdRevenueReporter() }

    @Test
    fun reportAutoAdRevenue() {
        reporter.reportAutoAdRevenue(autoAdRevenue)

        modulesFacadeRule.staticMock.verify {
            ModulesFacade.reportAdRevenue(adRevenue)
        }
    }
}
