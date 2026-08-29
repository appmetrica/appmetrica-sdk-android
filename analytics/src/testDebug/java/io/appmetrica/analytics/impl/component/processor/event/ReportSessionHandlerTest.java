package io.appmetrica.analytics.impl.component.processor.event;

import io.appmetrica.analytics.impl.CoreServiceEvent;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.processor.ReportingReportProcessor;
import io.appmetrica.gradle.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
        CoreServiceEvent serviceEvent = new CoreServiceEvent();
        mReportSessionHandler.process(serviceEvent);

        ArgumentCaptor<CoreServiceEvent> arg = ArgumentCaptor.forClass(CoreServiceEvent.class);

        verify(mReportSessionProcessor, times(1)).process(arg.capture());
        assertThat(arg.getValue()).isEqualTo(serviceEvent);
    }
}
