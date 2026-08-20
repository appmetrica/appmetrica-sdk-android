package io.appmetrica.analytics.impl.component.processor.session;

import io.appmetrica.analytics.impl.ServiceEvent;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.session.SessionManagerStateMachine;
import io.appmetrica.analytics.impl.events.ConditionalEventTrigger;
import io.appmetrica.gradle.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReportSessionStopDueCrashHandlerTest extends CommonTest {

    @Mock
    private ComponentUnit componentUnit;
    @Mock
    private SessionManagerStateMachine sessionManager;
    @Mock
    private ConditionalEventTrigger conditionalEventTrigger;
    private ReportSessionStopDueCrashHandler reportSessionStopDueCrashHandler;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(componentUnit.getSessionManager()).thenReturn(sessionManager);
        when(componentUnit.getEventTrigger()).thenReturn(conditionalEventTrigger);
        reportSessionStopDueCrashHandler = new ReportSessionStopDueCrashHandler(componentUnit);
    }

    @Test
    public void testSessionStopped() {
        ServiceEvent serviceEvent = new ServiceEvent();
        reportSessionStopDueCrashHandler.process(serviceEvent);

        verify(sessionManager, times(1)).stopCurrentSessionDueToCrash(serviceEvent);
    }

    @Test
    public void testProcessShouldTrigger() {
        reportSessionStopDueCrashHandler.process(new ServiceEvent());
        verify(conditionalEventTrigger).trigger();
    }

    @Test
    public void testProcessShouldBreakProcessing() {
        assertThat(reportSessionStopDueCrashHandler.process(new ServiceEvent())).isTrue();
    }

}
