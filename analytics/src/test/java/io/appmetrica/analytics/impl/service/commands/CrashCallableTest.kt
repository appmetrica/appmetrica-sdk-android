package io.appmetrica.analytics.impl.service.commands

import android.content.Context
import android.content.Intent
import android.os.ResultReceiver
import io.appmetrica.analytics.impl.AppMetricaConnector
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.ReportToSend
import io.appmetrica.analytics.impl.ReporterEnvironment
import io.appmetrica.analytics.impl.ServiceUtils
import io.appmetrica.analytics.impl.ShouldDisconnectFromServiceChecker
import io.appmetrica.analytics.impl.client.ProcessConfiguration
import io.appmetrica.analytics.impl.crash.CrashToFileWriter
import io.appmetrica.analytics.impl.crash.jvm.client.AppMetricaUncaughtExceptionHandler
import io.appmetrica.analytics.internal.CounterConfiguration
import io.appmetrica.analytics.internal.IAppMetricaService
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class CrashCallableTest : CommonTest() {

    private val appMetricaProcess = "AppMetrica process"
    private val anotherProcess = "Another process"
    private val context: Context = mock()
    private val service: IAppMetricaService = mock()
    private val appMetricaConnector: AppMetricaConnector = mock {
        on { service } doReturn service
    }
    private val shouldDisconnectFromServiceChecker: ShouldDisconnectFromServiceChecker = mock()
    private var reportToSend: ReportToSend = mock()
    private val intent: Intent = mock()

    private lateinit var crashToFileWriter: CrashToFileWriter

    @get:Rule
    val serviceUtilsRule = MockedStaticRule(ServiceUtils::class.java)

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    @get:Rule
    val mockedRule = MockedConstructionRule(CrashToFileWriter::class.java) { mock, mockContext ->
        crashToFileWriter = mock
    }

    private lateinit var crashCallable: CrashCallable

    @Before
    fun setUp() {
        crashCallable = CrashCallable(
            context,
            appMetricaConnector,
            shouldDisconnectFromServiceChecker,
            reportToSend
        )

        whenever(ClientServiceLocator.getInstance().appMetricaServiceProcessDetector.processName(context))
            .thenReturn(appMetricaProcess)

        val reporterEnvironment = mock<ReporterEnvironment>()
        val processConfiguration = ProcessConfiguration(context, mock<ResultReceiver>())
        val counterConfiguration = CounterConfiguration()
        whenever(reporterEnvironment.processConfiguration).thenReturn(processConfiguration)
        whenever(reporterEnvironment.reporterConfiguration).thenReturn(counterConfiguration)

        whenever(reportToSend.report).thenReturn(CounterReport())
        whenever(reportToSend.environment).thenReturn(reporterEnvironment)

        whenever(ServiceUtils.getOwnMetricaServiceIntent(context)).thenReturn(intent)
        AppMetricaUncaughtExceptionHandler.reset()
    }

    @Test
    fun construction() {
        assertThat(mockedRule.constructionMock.constructed()).hasSize(1)
    }

    @Test
    fun appMetricaProcess() {
        whenever(ClientServiceLocator.getInstance().currentProcessDetector.getProcessName())
            .thenReturn(appMetricaProcess)

        crashCallable.call()

        verify(appMetricaConnector, never()).service
        verify(crashToFileWriter).writeToFile(reportToSend)
    }

    @Test
    fun tryToExecuteTwice() {
        whenever(ClientServiceLocator.getInstance().currentProcessDetector.getProcessName())
            .thenReturn(appMetricaProcess)

        crashCallable.call()

        verify(appMetricaConnector, never()).service
        verify(crashToFileWriter).writeToFile(reportToSend)

        crashCallable.call()
        verifyNoMoreInteractions(crashToFileWriter)
    }

    @Test
    fun nonAppMetricaProcess() {
        whenever(ClientServiceLocator.getInstance().currentProcessDetector.getProcessName())
            .thenReturn(anotherProcess)

        crashCallable.call()

        verify(appMetricaConnector).service
        verify(crashToFileWriter, never()).writeToFile(reportToSend)
    }
}
