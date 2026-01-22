package io.appmetrica.analytics.impl.stub

import io.appmetrica.analytics.plugins.PluginErrorDetails
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock

internal class PluginReporterStubTest : CommonTest() {

    private val errorDetails: PluginErrorDetails = mock()

    private val pluginReporterStub = PluginReporterStub()

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
