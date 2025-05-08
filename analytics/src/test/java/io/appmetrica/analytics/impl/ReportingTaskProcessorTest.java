package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.impl.startup.executor.StartupExecutor;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class ReportingTaskProcessorTest extends CommonTest {

    @Rule
    public final GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

    ReportingTaskProcessor reportingTaskProcessor;
    @Mock
    private ReportRequestConfig requestConfig;
    @Mock
    private ComponentUnit componentUnit;
    @Mock
    private ICommonExecutor mCommonExecutor;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        reportingTaskProcessor = new ReportingTaskProcessor(
            componentUnit,
            mock(StartupExecutor.class),
            mCommonExecutor
        );
        doReturn(requestConfig).when(componentUnit).getFreshReportRequestConfig();
    }

    @Test
    public void testPostNewTask() {
        int dispatchPeriod = 100;
        doReturn(dispatchPeriod).when(requestConfig).getDispatchPeriod();
        reportingTaskProcessor.scheduleFlushTask();
        verify(mCommonExecutor).executeDelayed(
            reportingTaskProcessor.getFlushRunnable(),
            TimeUnit.SECONDS.toMillis(dispatchPeriod)
        );
    }

    @Test
    public void testRemoveTask() {
        reportingTaskProcessor.cancelFlushTask();
        verify(mCommonExecutor).remove(reportingTaskProcessor.getFlushRunnable());
    }

    @Test
    public void scheduleFlushTaskNow() {
        reportingTaskProcessor.scheduleFlushTaskNow();
        verify(mCommonExecutor).executeDelayed(reportingTaskProcessor.getFlushRunnable(), TimeUnit.SECONDS.toMillis(1));
    }
}
