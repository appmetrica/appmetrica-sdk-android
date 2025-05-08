package io.appmetrica.analytics.plugins

import io.appmetrica.analytics.impl.AppMetricaPluginsImpl
import io.appmetrica.analytics.impl.proxy.AppMetricaPluginsProxy
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify

class AppMetricaPluginsImplTest : CommonTest() {

    private val proxy: AppMetricaPluginsProxy = mock()
    private val errorDetails: PluginErrorDetails = mock()

    private val impl: AppMetricaPluginsImpl by setUp { AppMetricaPluginsImpl(proxy) }

    @Test
    fun reportUnhandledException() {
        impl.reportUnhandledException(errorDetails)
        verify(proxy).reportUnhandledException(errorDetails)
    }

    @Test
    fun reportError() {
        val message = "some message"
        impl.reportError(errorDetails, message)
        verify(proxy).reportError(errorDetails, message)
    }

    @Test
    fun reportErrorWithIdentifier() {
        val message = "some message"
        val identifier = "some id"
        impl.reportError(identifier, message, errorDetails)
        verify(proxy).reportError(identifier, message, errorDetails)
    }
}
