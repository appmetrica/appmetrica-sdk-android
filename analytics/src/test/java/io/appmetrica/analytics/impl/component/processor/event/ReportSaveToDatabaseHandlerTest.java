package io.appmetrica.analytics.impl.component.processor.event;

import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.EventSaver;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReportSaveToDatabaseHandlerTest extends CommonTest {

    @Mock
    private ComponentUnit mComponent;
    @Mock
    private EventSaver mEventSaver;
    private ReportSaveToDatabaseHandler mReportSaveToDatabaseHandler;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mComponent.getEventSaver()).thenReturn(mEventSaver);
        mReportSaveToDatabaseHandler = new ReportSaveToDatabaseHandler(mComponent);
    }

    @Test
    public void testProcessShouldSaveReportToDatabase() {
        mReportSaveToDatabaseHandler.process(new CounterReport());

        ArgumentCaptor<CounterReport> arg = ArgumentCaptor.forClass(CounterReport.class);
        verify(mEventSaver, times(1)).identifyAndSaveReport(arg.capture());
    }

    @Test
    public void testProcessShouldNotBreakEventProcessing() {
        assertThat(mReportSaveToDatabaseHandler.process(new CounterReport())).isFalse();
    }
}
