package io.appmetrica.analytics.impl.service.commands

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.ResultReceiver
import io.appmetrica.analytics.impl.AppMetricaConnector
import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.ReportToSend
import io.appmetrica.analytics.impl.ReporterEnvironment
import io.appmetrica.analytics.impl.ShouldDisconnectFromServiceChecker
import io.appmetrica.analytics.impl.client.ProcessConfiguration
import io.appmetrica.analytics.impl.crash.jvm.client.AppMetricaUncaughtExceptionHandler
import io.appmetrica.analytics.internal.CounterConfiguration
import io.appmetrica.analytics.internal.IAppMetricaService
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

internal class ReportCallableTest : CommonTest() {

    private val context: Context = mock()
    private val appMetricaConnector: AppMetricaConnector = mock()
    private val shouldDisconnectFromServiceChecker: ShouldDisconnectFromServiceChecker = mock()
    private val intent: Intent = mock()
    private val service: IAppMetricaService = mock()
    private val reportData: CounterReport = mock()
    private val reportBundle: Bundle = mock()
    private val serviceDataReporterType = 42

    private val processConfiguration = ProcessConfiguration(mock<Context>(), mock<ResultReceiver>())
    private val counterConfiguration = CounterConfiguration()

    private val reporterEnvironment: ReporterEnvironment = mock {
        on { processConfiguration } doReturn processConfiguration
        on { reporterConfiguration } doReturn counterConfiguration
    }

    private lateinit var reportCallable: ReportCallable

    @Before
    fun setUp() {
        val reportToSend = ReportToSend.newBuilder(reportData, reporterEnvironment)
            .withServiceDataReporterType(serviceDataReporterType)
            .build()
        reportCallable = ReportCallable(
            appMetricaConnector,
            shouldDisconnectFromServiceChecker,
            reportToSend
        )

        whenever(reportData.toBundle(any())).thenReturn(reportBundle)

        AppMetricaUncaughtExceptionHandler.reset()
    }

    @Test
    fun hasService() {
        whenever(appMetricaConnector.service).thenReturn(service)

        reportCallable.call()

        verify(service).reportData(serviceDataReporterType, reportBundle)
    }

    @Test
    fun noService() {
        whenever(appMetricaConnector.service).thenReturn(null, service)
        whenever(appMetricaConnector.isConnected).thenReturn(false, true)

        reportCallable.call()

        val inOrder = inOrder(appMetricaConnector, service)
        inOrder.verify(appMetricaConnector).bindService()
        inOrder.verify(service).reportData(serviceDataReporterType, reportBundle)
    }

    @Test
    fun noServiceAtAll() {
        whenever(appMetricaConnector.service).thenReturn(null)
        whenever(appMetricaConnector.isConnected).thenReturn(false)

        reportCallable.call()

        verify(appMetricaConnector, times(3)).bindService()
        verifyNoMoreInteractions(service)
    }
}
