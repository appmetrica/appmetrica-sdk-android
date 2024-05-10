package io.appmetrica.analytics.impl;

import android.os.Bundle;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.service.commands.ReportToServiceCallable;
import io.appmetrica.analytics.impl.service.commands.ServiceCallableFactory;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ReportsSenderTest extends CommonTest {

    @Mock
    private AppMetricaConnector appMetricaConnector;
    @Mock
    private ServiceCallableFactory serviceCallableFactory;
    @Mock
    private ICommonExecutor executor;
    @Mock
    private ReportToServiceCallable crashCallable;
    @Mock
    private ReportToServiceCallable reportCallable;
    @Mock
    private ReportToServiceCallable typedReportCallable;
    @Mock
    private ReportToServiceCallable resumeUserSessionCallable;
    @Mock
    private ReportToServiceCallable pauseUserSessionCallable;

    private ReportsSender reportsSender;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(serviceCallableFactory.createCrashCallable(any(ReportToSend.class))).thenReturn(crashCallable);
        when(serviceCallableFactory.createReportCallable(any(ReportToSend.class))).thenReturn(reportCallable);
        when(serviceCallableFactory.createTypedReportCallable(anyInt(), any(Bundle.class))).thenReturn(typedReportCallable);
        when(serviceCallableFactory.createResumeUseSessionCallable(any(ProcessConfiguration.class))).thenReturn(resumeUserSessionCallable);
        when(serviceCallableFactory.createPauseUseSessionCallable(any(ProcessConfiguration.class))).thenReturn(pauseUserSessionCallable);

        reportsSender = new ReportsSender(appMetricaConnector, serviceCallableFactory, executor);
    }

    @Test
    public void queueReportWithEvent() {
        final ReportToSend reportToSend = mock(ReportToSend.class);
        when(reportToSend.isCrashReport()).thenReturn(false);

        reportsSender.queueReport(reportToSend);

        verify(serviceCallableFactory).createReportCallable(reportToSend);
        verifyNoMoreInteractions(serviceCallableFactory);

        verify(executor).submit(reportCallable);
        verifyNoMoreInteractions(executor);
    }

    @Test
    public void queueReportWithCrashEvent() {
        final ReportToSend reportToSend = mock(ReportToSend.class);
        when(reportToSend.isCrashReport()).thenReturn(true);

        reportsSender.queueReport(reportToSend);

        verify(serviceCallableFactory).createCrashCallable(reportToSend);
        verifyNoMoreInteractions(serviceCallableFactory);

        verify(executor).submit(crashCallable);
        verifyNoMoreInteractions(executor);
    }

    @Test
    public void sendCrashIfConnectedAndExecuted() {
        final ReportToSend reportToSend = mock(ReportToSend.class);
        when(appMetricaConnector.isConnected()).thenReturn(true);
        when(crashCallable.isExecuted()).thenReturn(true);

        reportsSender.sendCrash(reportToSend);

        verify(serviceCallableFactory).createCrashCallable(reportToSend);
        verifyNoMoreInteractions(serviceCallableFactory);

        verify(executor).submit(crashCallable);
        verifyNoMoreInteractions(executor);

        verify(crashCallable, never()).call();
    }

    @Test
    public void sendCrashIfConnectedAndNotExecuted() {
        final ReportToSend reportToSend = mock(ReportToSend.class);
        when(appMetricaConnector.isConnected()).thenReturn(true);
        when(crashCallable.isExecuted()).thenReturn(false);

        reportsSender.sendCrash(reportToSend);

        verify(serviceCallableFactory).createCrashCallable(reportToSend);
        verifyNoMoreInteractions(serviceCallableFactory);

        verify(executor).submit(crashCallable);
        verifyNoMoreInteractions(executor);

        verify(crashCallable).call();
    }

    @Test
    public void sendCrashIfNotConnectedAndNotExecuted() {
        final ReportToSend reportToSend = mock(ReportToSend.class);
        when(appMetricaConnector.isConnected()).thenReturn(false);
        when(crashCallable.isExecuted()).thenReturn(false);

        reportsSender.sendCrash(reportToSend);

        verify(serviceCallableFactory).createCrashCallable(reportToSend);
        verifyNoMoreInteractions(serviceCallableFactory);

        verify(executor, never()).submit(crashCallable);

        verify(crashCallable).call();
    }

    @Test
    public void reportData() {
        final int type = 42;
        final Bundle bundle = mock(Bundle.class);

        reportsSender.reportData(type, bundle);

        verify(serviceCallableFactory).createTypedReportCallable(type, bundle);
        verifyNoMoreInteractions(serviceCallableFactory);

        verify(executor).submit(typedReportCallable);
        verifyNoMoreInteractions(executor);
    }

    @Test
    public void queueResumeUserSession() {
        final ProcessConfiguration processConfiguration = mock(ProcessConfiguration.class);

        reportsSender.queueResumeUserSession(processConfiguration);

        verify(serviceCallableFactory).createResumeUseSessionCallable(processConfiguration);
        verifyNoMoreInteractions(serviceCallableFactory);

        verify(executor).submit(resumeUserSessionCallable);
        verifyNoMoreInteractions(executor);
    }

    @Test
    public void queuePauseUserSession() {
        final ProcessConfiguration processConfiguration = mock(ProcessConfiguration.class);

        reportsSender.queuePauseUserSession(processConfiguration);

        verify(serviceCallableFactory).createPauseUseSessionCallable(processConfiguration);
        verifyNoMoreInteractions(serviceCallableFactory);

        verify(executor).submit(pauseUserSessionCallable);
        verifyNoMoreInteractions(executor);
    }
}
