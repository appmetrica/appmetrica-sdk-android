package io.appmetrica.analytics

import android.content.Context
import io.appmetrica.analytics.impl.proxy.ModulesProxy
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

class ModulesFacadeTest : CommonTest() {

    private val proxy: ModulesProxy = mock()
    private val moduleEvent: ModuleEvent = mock()

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    @Before
    fun setUp() {
        ModulesFacade.setProxy(proxy)
    }

    @Test
    fun setAdvIdentifiersTracking() {
        ModulesFacade.setAdvIdentifiersTracking(true)
        verify(proxy).setAdvIdentifiersTracking(true)
        verifyNoMoreInteractions(proxy)
    }

    @Test
    fun reportEvent() {
        ModulesFacade.reportEvent(moduleEvent)
        verify(proxy).reportEvent(moduleEvent)
        verifyNoMoreInteractions(proxy)
    }

    @Test
    fun reportExternalAttribution() {
        val source = 200500
        val value = "some value"
        ModulesFacade.reportExternalAttribution(source, value)
        verify(proxy).reportExternalAttribution(source, value)
        verifyNoMoreInteractions(proxy)
    }

    @Test
    fun sendEventsBuffer() {
        ModulesFacade.sendEventsBuffer()
        verify(proxy).sendEventsBuffer()
        verifyNoMoreInteractions(proxy)
    }

    @Test
    fun iifaForTrue() {
        verifyIifa(true)
    }

    @Test
    fun iifaForFalse() {
        verifyIifa(false)
    }

    @Test
    fun getModuleReporter() {
        val reporter: IModuleReporter = mock()
        val appContext: Context = mock()
        val context: Context = mock {
            on { applicationContext } doReturn appContext
        }
        val apiKey = "some api key"
        whenever(proxy.getReporter(context, apiKey)).thenReturn(reporter)

        assertThat(ModulesFacade.getModuleReporter(context, apiKey)).isSameAs(reporter)
    }

    @Test
    fun setSessionExtra() {
        val key = "Key"
        val value = ByteArray(5) { it.toByte() }
        ModulesFacade.setSessionExtra(key, value)
        verify(proxy).setSessionExtra(key, value)
        verifyNoMoreInteractions(proxy)
    }

    @Test
    fun reportAdRevenue() {
        val adRevenue: AdRevenue = mock()
        ModulesFacade.reportAdRevenue(adRevenue)
        verify(proxy).reportAdRevenue(adRevenue, true)
        verifyNoMoreInteractions(proxy)
    }

    @Test
    fun reportAdRevenueIfAutoCollectedIsTrue() {
        val adRevenue: AdRevenue = mock()
        ModulesFacade.reportAdRevenue(adRevenue, true)
        verify(proxy).reportAdRevenue(adRevenue, true)
        verifyNoMoreInteractions(proxy)
    }

    @Test
    fun reportAdRevenueIfAutoCollectedIsFalse() {
        val adRevenue: AdRevenue = mock()
        ModulesFacade.reportAdRevenue(adRevenue, false)
        verify(proxy).reportAdRevenue(adRevenue, false)
        verifyNoMoreInteractions(proxy)
    }

    @Test
    fun reportAdRevenueWithAutoCollectedFlag() {
        val adRevenue: AdRevenue = mock()
        ModulesFacade.reportAdRevenue(adRevenue, false)
        verify(proxy).reportAdRevenue(adRevenue, false)
        verifyNoMoreInteractions(proxy)
    }

    @Test
    fun subscribeForAutoCollectedData() {
        val context: Context = mock()
        val apiKey = "some api key"
        ModulesFacade.subscribeForAutoCollectedData(context, apiKey)
        verify(proxy).subscribeForAutoCollectedData(context, apiKey)
        verifyNoMoreInteractions(proxy)
    }

    private fun verifyIifa(returnValue: Boolean) {
        whenever(proxy.isActivatedForApp()).thenReturn(returnValue)
        assertThat(ModulesFacade.isActivatedForApp()).isEqualTo(returnValue)
        verify(proxy).isActivatedForApp()
        verifyNoMoreInteractions(proxy)
    }
}
