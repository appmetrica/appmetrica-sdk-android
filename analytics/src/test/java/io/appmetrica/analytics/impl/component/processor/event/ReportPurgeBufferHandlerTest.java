package io.appmetrica.analytics.impl.component.processor.event;

import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ReportPurgeBufferHandlerTest extends CommonTest {
    private ComponentUnit mComponentUnit;
    private ReportPurgeBufferHandler mReportPurgeBufferHandler;

    @Before
    public void SetUp() {
        mComponentUnit = mock(ComponentUnit.class);
        mReportPurgeBufferHandler = new ReportPurgeBufferHandler(mComponentUnit);
    }

    @Test
    public void testProcessShouldFlushEvents() {
        mReportPurgeBufferHandler.process(new CounterReport());

        verify(mComponentUnit, times(1)).flushEvents();
    }

    @Test
    public void testProcessShouldNotBreakEventProcessing() {
        assertThat(mReportPurgeBufferHandler.process(new CounterReport())).isFalse();
    }
}
