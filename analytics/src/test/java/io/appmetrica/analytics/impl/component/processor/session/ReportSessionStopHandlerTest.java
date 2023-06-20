package io.appmetrica.analytics.impl.component.processor.session;

import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.session.SessionManagerStateMachine;
import io.appmetrica.analytics.impl.events.EventTrigger;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ReportSessionStopHandlerTest extends CommonTest {

    @Mock
    private ComponentUnit componentUnit;
    @Mock
    private SessionManagerStateMachine sessionManager;
    @Mock
    private EventTrigger eventTrigger;
    private ReportSessionStopHandler reportSessionStopHandler;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(componentUnit.getSessionManager()).thenReturn(sessionManager);
        when(componentUnit.getEventTrigger()).thenReturn(eventTrigger);
        reportSessionStopHandler = new ReportSessionStopHandler(componentUnit);
    }

    @Test
    public void testSessionStopped() {
        CounterReport reportData = new CounterReport();
        reportSessionStopHandler.process(reportData);

        verify(sessionManager, times(1)).stopCurrentSessionDueToCrash(reportData);
    }

    @Test
    public void testProcessShouldTrigger() {
        reportSessionStopHandler.process(new CounterReport());
        verify(eventTrigger).trigger();
    }

    @Test
    public void testProcessShouldNotBreakProcessing() {
        assertThat(reportSessionStopHandler.process(new CounterReport())).isFalse();
    }

}
