package io.appmetrica.analytics.impl.component.processor.session;

import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.session.SessionManagerStateMachine;
import io.appmetrica.analytics.impl.events.ConditionalEventTrigger;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReportSessionStopHandlerTest extends CommonTest {

    @Mock
    private ComponentUnit componentUnit;
    @Mock
    private SessionManagerStateMachine sessionManager;
    @Mock
    private ConditionalEventTrigger conditionalEventTrigger;
    private ReportSessionStopHandler reportSessionStopHandler;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(componentUnit.getSessionManager()).thenReturn(sessionManager);
        when(componentUnit.getEventTrigger()).thenReturn(conditionalEventTrigger);
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
        verify(conditionalEventTrigger).trigger();
    }

    @Test
    public void testProcessShouldBreakProcessing() {
        assertThat(reportSessionStopHandler.process(new CounterReport())).isTrue();
    }

}
