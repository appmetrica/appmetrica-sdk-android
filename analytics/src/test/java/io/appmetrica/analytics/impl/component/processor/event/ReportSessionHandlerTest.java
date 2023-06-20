package io.appmetrica.analytics.impl.component.processor.event;

import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.processor.ReportingReportProcessor;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class ReportSessionHandlerTest extends CommonTest {

    private ReportingReportProcessor mReportSessionProcessor;
    private ReportSessionHandler mReportSessionHandler;

    @Before
    public void setUp() {
        ComponentUnit componentUnit = mock(ComponentUnit.class);
        mReportSessionProcessor = mock(ReportingReportProcessor.class);
        mReportSessionHandler = new ReportSessionHandler(componentUnit, mReportSessionProcessor);
    }

    @Test
    public void testProcessShouldInvokeReportSessionProcessor() {
        CounterReport report = new CounterReport();
        mReportSessionHandler.process(report);

        ArgumentCaptor<CounterReport> arg = ArgumentCaptor.forClass(CounterReport.class);

        verify(mReportSessionProcessor, times(1)).process(arg.capture());
        assertThat(arg.getValue()).isEqualTo(report);
    }
}
