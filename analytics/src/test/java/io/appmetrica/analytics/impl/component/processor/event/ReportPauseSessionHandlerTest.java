package io.appmetrica.analytics.impl.component.processor.event;

import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.session.SessionManagerStateMachine;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ReportPauseSessionHandlerTest extends CommonTest {
    private ComponentUnit mComponent;
    private SessionManagerStateMachine mSessionManager;
    private ReportPauseForegroundSessionHandler mReportPauseSessionHandler;

    @Before
    public void setUp() {
        mComponent = mock(ComponentUnit.class);
        mSessionManager = mock(SessionManagerStateMachine.class);
        when(mComponent.getSessionManager()).thenReturn(mSessionManager);
        mReportPauseSessionHandler = new ReportPauseForegroundSessionHandler(mComponent);
    }

    @Test
    public void testProcessShouldUpdateAliveTime() {
        mReportPauseSessionHandler.process(new CounterReport());

        verify(mSessionManager, times(1)).heartbeat(any(CounterReport.class));
    }

    @Test
    public void testProcessShouldNotBreakProcessing() {
        assertThat(mReportPauseSessionHandler.process(new CounterReport())).isFalse();
    }
}
