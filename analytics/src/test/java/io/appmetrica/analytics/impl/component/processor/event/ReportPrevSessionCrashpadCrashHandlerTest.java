package io.appmetrica.analytics.impl.component.processor.event;

import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.EventSaver;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ReportPrevSessionCrashpadCrashHandlerTest extends CommonTest {

    @Mock
    private ComponentUnit mComponentUnit;
    @Mock
    private CounterReport report;
    @Mock
    private EventSaver mEventSaver;
    private ReportPrevSessionCrashpadCrashHandler mHandler;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mComponentUnit.getEventSaver()).thenReturn(mEventSaver);
        mHandler = new ReportPrevSessionCrashpadCrashHandler(mComponentUnit);
    }

    @Test
    public void testProcess() {
        mHandler.process(report);
        verify(mEventSaver).saveReportFromPrevSession(report);
    }
}
