package io.appmetrica.analytics.plugins

import io.appmetrica.analytics.impl.AppMetricaPluginsImpl
import io.appmetrica.analytics.impl.proxy.AppMetricaPluginsProxy
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify

class AppMetricaPluginsImplTest : CommonTest() {

    @Mock
    private lateinit var proxy: AppMetricaPluginsProxy
    @Mock
    private lateinit var errorDetails: PluginErrorDetails
    private lateinit var impl: AppMetricaPluginsImpl

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        impl = AppMetricaPluginsImpl(proxy)
    }

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
