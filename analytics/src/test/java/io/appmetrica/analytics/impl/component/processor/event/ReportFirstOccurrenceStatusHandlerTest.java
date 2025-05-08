package io.appmetrica.analytics.impl.component.processor.event;

import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.FirstOccurrenceStatus;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.remarketing.EventFirstOccurrenceService;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ReportFirstOccurrenceStatusHandlerTest extends CommonTest {

    @Mock
    private ComponentUnit mComponentUnit;
    @Mock
    private EventFirstOccurrenceService mEventFirstOccurrenceService;

    private ReportFirstOccurrenceStatusHandler mReportFirstOccurrenceStatusHandler;

    private static final String EVENT_NAME = "Test event";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        mReportFirstOccurrenceStatusHandler = new ReportFirstOccurrenceStatusHandler(mComponentUnit, mEventFirstOccurrenceService);
    }

    @Test
    public void testMainConstructor() {
        when(mComponentUnit.getEventFirstOccurrenceService()).thenReturn(mEventFirstOccurrenceService);
        mReportFirstOccurrenceStatusHandler = new ReportFirstOccurrenceStatusHandler(mComponentUnit);
        assertThat(mReportFirstOccurrenceStatusHandler.getEventFirstOccurrenceService())
            .isEqualTo(mEventFirstOccurrenceService);
    }

    @Test
    public void testProcessUpdateEventFirstOccurrenceStatus() {
        CounterReport counterReport = new CounterReport();
        counterReport.setName(EVENT_NAME);
        FirstOccurrenceStatus status = FirstOccurrenceStatus.FIRST_OCCURRENCE;
        when(mEventFirstOccurrenceService.checkFirstOccurrence(EVENT_NAME)).thenReturn(status);
        mReportFirstOccurrenceStatusHandler.process(counterReport);
        assertThat(counterReport.getFirstOccurrenceStatus()).isEqualTo(status);
    }

    @Test
    public void testProcessDoesNotUpdateFirstOccurrenceStatusForEventWithEmptyEvent() {
        CounterReport counterReport = new CounterReport();
        when(mEventFirstOccurrenceService.checkFirstOccurrence(anyString()))
            .thenReturn(FirstOccurrenceStatus.FIRST_OCCURRENCE);
        mReportFirstOccurrenceStatusHandler.process(counterReport);
        assertThat(counterReport.getFirstOccurrenceStatus()).isEqualTo(FirstOccurrenceStatus.UNKNOWN);
    }

    @Test
    public void testProcessDoesNotBreakEventProcessing() {
        assertThat(mReportFirstOccurrenceStatusHandler.process(new CounterReport())).isFalse();
    }
}
