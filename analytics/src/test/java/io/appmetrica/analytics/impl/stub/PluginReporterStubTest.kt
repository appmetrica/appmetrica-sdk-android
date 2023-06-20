package io.appmetrica.analytics.impl.stub

import io.appmetrica.analytics.plugins.PluginErrorDetails
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class PluginReporterStubTest : CommonTest() {

    @Mock
    private lateinit var errorDetails: PluginErrorDetails

    private val pluginReporterStub = PluginReporterStub()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun reportUnhandledException() {
        pluginReporterStub.reportUnhandledException(errorDetails)
        Mockito.verifyNoInteractions(errorDetails)
    }

    @Test
    fun reportError() {
        pluginReporterStub.reportError(errorDetails, "message")
        Mockito.verifyNoInteractions(errorDetails)
    }

    @Test
    fun reportErrorWithIdentifier() {
        pluginReporterStub.reportError("id", "message", errorDetails)
        Mockito.verifyNoInteractions(errorDetails)
    }
}
