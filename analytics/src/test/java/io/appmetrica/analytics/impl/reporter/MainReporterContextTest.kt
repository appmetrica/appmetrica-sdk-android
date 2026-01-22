package io.appmetrica.analytics.impl.reporter

import android.content.Context
import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.impl.AppStatusMonitor
import io.appmetrica.analytics.impl.MainReporterComponents
import io.appmetrica.analytics.impl.ReportsHandler
import io.appmetrica.analytics.impl.startup.StartupHelper
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class MainReporterContextTest : CommonTest() {

    private val deviceId = "DeviceId"
    private val context: Context = mock()
    private val appStatusMonitor: AppStatusMonitor = mock()

    private val startupHelper: StartupHelper = mock {
        on { deviceId } doReturn deviceId
    }

    private val reportsHandler: ReportsHandler = mock()

    private val mainReporterComponents: MainReporterComponents = mock {
        on { context } doReturn context
        on { appStatusMonitor } doReturn appStatusMonitor
        on { startupHelper } doReturn startupHelper
        on { reportsHandler } doReturn reportsHandler
    }

    private val config: AppMetricaConfig = mock()
    private val logger: PublicLogger = mock()

    private val mainReporterContext: MainReporterContext by setUp {
        MainReporterContext(mainReporterComponents, config, logger)
    }

    @Test
    fun applicationContext() {
        assertThat(mainReporterContext.applicationContext).isEqualTo(context)
    }

    @Test
    fun appStatusMonitor() {
        assertThat(mainReporterContext.appStatusMonitor).isEqualTo(appStatusMonitor)
    }

    @Test
    fun deviceId() {
        assertThat(mainReporterContext.deviceId).isEqualTo(deviceId)
    }

    @Test
    fun reportsHandler() {
        assertThat(mainReporterContext.reportsHandler).isEqualTo(reportsHandler)
    }
}
