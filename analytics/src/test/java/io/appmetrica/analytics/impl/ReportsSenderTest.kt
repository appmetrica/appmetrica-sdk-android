package io.appmetrica.analytics.impl

import android.os.Bundle
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.impl.client.ProcessConfiguration
import io.appmetrica.analytics.impl.service.commands.ReportToServiceCallable
import io.appmetrica.analytics.impl.service.commands.ServiceCallableFactory
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

internal class ReportsSenderTest : CommonTest() {

    private val appMetricaConnector: AppMetricaConnector = mock()
    private val executor: ICommonExecutor = mock()
    private val crashCallable: ReportToServiceCallable = mock()
    private val reportCallable: ReportToServiceCallable = mock()
    private val typedReportCallable: ReportToServiceCallable = mock()
    private val resumeUserSessionCallable: ReportToServiceCallable = mock()
    private val pauseUserSessionCallable: ReportToServiceCallable = mock()

    private val serviceCallableFactory: ServiceCallableFactory = mock {
        on { createCrashCallable(any()) } doReturn crashCallable
        on { createReportCallable(any()) } doReturn reportCallable
        on { createTypedReportCallable(any(), any()) } doReturn typedReportCallable
        on { createResumeUseSessionCallable(any()) } doReturn resumeUserSessionCallable
        on { createPauseUseSessionCallable(any()) } doReturn pauseUserSessionCallable
    }

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    private lateinit var reportsSender: ReportsSender

    @Before
    fun setUp() {
        whenever(ClientServiceLocator.getInstance().clientExecutorProvider.reportSenderExecutor)
            .thenReturn(executor)

        reportsSender = ReportsSender(appMetricaConnector, serviceCallableFactory)
    }

    @Test
    fun queueReportWithEvent() {
        val reportToSend = mock<ReportToSend>()
        whenever(reportToSend.isCrashReport).thenReturn(false)

        reportsSender.queueReport(reportToSend)

        verify(serviceCallableFactory).createReportCallable(reportToSend)
        verifyNoMoreInteractions(serviceCallableFactory)

        verify(executor).submit(reportCallable)
        verifyNoMoreInteractions(executor)
    }

    @Test
    fun queueReportWithCrashEvent() {
        val reportToSend = mock<ReportToSend>()
        whenever(reportToSend.isCrashReport).thenReturn(true)

        reportsSender.queueReport(reportToSend)

        verify(serviceCallableFactory).createCrashCallable(reportToSend)
        verifyNoMoreInteractions(serviceCallableFactory)

        verify(executor).submit(crashCallable)
        verifyNoMoreInteractions(executor)
    }

    @Test
    fun sendCrashIfConnectedAndExecuted() {
        val reportToSend = mock<ReportToSend>()
        whenever(appMetricaConnector.isConnected).thenReturn(true)
        whenever(crashCallable.isExecuted).thenReturn(true)

        reportsSender.sendCrash(reportToSend)

        verify(serviceCallableFactory).createCrashCallable(reportToSend)
        verifyNoMoreInteractions(serviceCallableFactory)

        verify(executor).submit(crashCallable)
        verifyNoMoreInteractions(executor)

        verify(crashCallable, never()).call()
    }

    @Test
    fun sendCrashIfConnectedAndNotExecuted() {
        val reportToSend = mock<ReportToSend>()
        whenever(appMetricaConnector.isConnected).thenReturn(true)
        whenever(crashCallable.isExecuted).thenReturn(false)

        reportsSender.sendCrash(reportToSend)

        verify(serviceCallableFactory).createCrashCallable(reportToSend)
        verifyNoMoreInteractions(serviceCallableFactory)

        verify(executor).submit(crashCallable)
        verifyNoMoreInteractions(executor)

        verify(crashCallable).call()
    }

    @Test
    fun sendCrashIfNotConnectedAndNotExecuted() {
        val reportToSend = mock<ReportToSend>()
        whenever(appMetricaConnector.isConnected).thenReturn(false)
        whenever(crashCallable.isExecuted).thenReturn(false)

        reportsSender.sendCrash(reportToSend)

        verify(serviceCallableFactory).createCrashCallable(reportToSend)
        verifyNoMoreInteractions(serviceCallableFactory)

        verify(executor, never()).submit(crashCallable)

        verify(crashCallable).call()
    }

    @Test
    fun reportData() {
        val type = 42
        val bundle = mock<Bundle>()

        reportsSender.reportData(type, bundle)

        verify(serviceCallableFactory).createTypedReportCallable(type, bundle)
        verifyNoMoreInteractions(serviceCallableFactory)

        verify(executor).submit(typedReportCallable)
        verifyNoMoreInteractions(executor)
    }

    @Test
    fun queueResumeUserSession() {
        val processConfiguration = mock<ProcessConfiguration>()

        reportsSender.queueResumeUserSession(processConfiguration)

        verify(serviceCallableFactory).createResumeUseSessionCallable(processConfiguration)
        verifyNoMoreInteractions(serviceCallableFactory)

        verify(executor).submit(resumeUserSessionCallable)
        verifyNoMoreInteractions(executor)
    }

    @Test
    fun queuePauseUserSession() {
        val processConfiguration = mock<ProcessConfiguration>()

        reportsSender.queuePauseUserSession(processConfiguration)

        verify(serviceCallableFactory).createPauseUseSessionCallable(processConfiguration)
        verifyNoMoreInteractions(serviceCallableFactory)

        verify(executor).submit(pauseUserSessionCallable)
        verifyNoMoreInteractions(executor)
    }
}
