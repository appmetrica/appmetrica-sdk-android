package io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl

import android.os.Bundle
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenue
import io.appmetrica.analytics.modulesapi.internal.common.InternalClientModuleFacade
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule.Companion.constructionRule
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal

internal class AppLovinIlrdReporterTest : CommonTest() {

    private val facade: InternalClientModuleFacade = mock()
    private val clientContext: ClientContext = mock {
        on { internalClientModuleFacade } doReturn facade
    }

    @get:Rule
    val converterRule = constructionRule<AppLovinIlrdConverter>()

    private val reporter by setUp { AppLovinIlrdReporter(clientContext) }

    private fun converter() = converterRule.constructionMock.constructed().first()

    private fun stubConverter(bundle: Bundle, revenue: BigDecimal): ModuleAdRevenue {
        val adRevenue: ModuleAdRevenue = mock { on { adRevenue } doReturn revenue }
        whenever(converter().convert(bundle)).thenReturn(adRevenue)
        return adRevenue
    }

    @Test
    fun onIlrdReceivedReportsAdRevenue() {
        val bundle: Bundle = mock()
        val adRevenue = stubConverter(bundle, BigDecimal.valueOf(1.5))

        reporter.onIlrdReceived("id1", bundle)

        verify(facade).reportAdRevenue(adRevenue)
    }

    @Test
    fun onIlrdReceivedReportsZeroAndNegativeRevenue() {
        val bundleZero: Bundle = mock()
        val bundleNeg: Bundle = mock()
        val adRevenueZero = stubConverter(bundleZero, BigDecimal.ZERO)
        val adRevenueNeg = stubConverter(bundleNeg, BigDecimal.valueOf(-1.0))

        reporter.onIlrdReceived("id1", bundleZero)
        reporter.onIlrdReceived("id2", bundleNeg)

        verify(facade).reportAdRevenue(adRevenueZero)
        verify(facade).reportAdRevenue(adRevenueNeg)
    }

    @Test
    fun onIlrdReceivedDeduplicatesSameId() {
        val bundle: Bundle = mock()
        val adRevenue = stubConverter(bundle, BigDecimal.valueOf(1.0))

        reporter.onIlrdReceived("dup-id", bundle)
        reporter.onIlrdReceived("dup-id", bundle)

        verify(facade).reportAdRevenue(adRevenue)
    }

    @Test
    fun onIlrdReceivedEvictsOldestWhenCacheFull() {
        val bundle: Bundle = mock()
        val adRevenue = stubConverter(bundle, BigDecimal.valueOf(1.0))

        // Fill cache with DEDUPLICATION_CACHE_SIZE + 1 unique ids so that "id-1" gets evicted
        for (i in 1..(Constants.DEDUPLICATION_CACHE_SIZE + 1)) {
            reporter.onIlrdReceived("id-$i", bundle)
        }
        // Now "id-1" was evicted — its repeated delivery should be accepted
        reporter.onIlrdReceived("id-1", bundle)

        // DEDUPLICATION_CACHE_SIZE + 1 unique ids + 1 re-accepted id-1
        verify(facade, times(Constants.DEDUPLICATION_CACHE_SIZE + 2))
            .reportAdRevenue(adRevenue)
    }

    @Test
    fun onIlrdReceivedHandlesConverterException() {
        val bundle: Bundle = mock()
        whenever(converter().convert(bundle)).thenThrow(RuntimeException("boom"))

        reporter.onIlrdReceived("id1", bundle)

        verify(facade, never()).reportAdRevenue(any())
    }
}
