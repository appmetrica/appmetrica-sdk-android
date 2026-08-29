package io.appmetrica.analytics.impl.component.processor.event;

import io.appmetrica.analytics.impl.CoreServiceEvent;
import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.EventSaver;
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.gradle.testutils.CommonTest;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReportFirstHandlerTest extends CommonTest {

    @Mock
    private ComponentUnit mComponent;
    @Mock
    private VitalComponentDataProvider vitalComponentDataProvider;
    private ReportFirstHandler mReportFirstHandler;
    @Mock
    private ReportRequestConfig mConfig;
    @Mock
    private EventSaver mEventSaver;

    @Before
    public void setUp() throws JSONException {
        MockitoAnnotations.openMocks(this);
        when(vitalComponentDataProvider.isInitEventDone()).thenReturn(false);
        when(vitalComponentDataProvider.isFirstEventDone()).thenReturn(false);
        mReportFirstHandler = new ReportFirstHandler(mComponent, vitalComponentDataProvider);
        when(mComponent.getFreshReportRequestConfig()).thenReturn(mConfig);
        when(mComponent.getEventSaver()).thenReturn(mEventSaver);
    }

    @Test
    public void testProcessShouldDoNothingIfFirstEventAlreadySend() {
        when(vitalComponentDataProvider.isFirstEventDone()).thenReturn(true);
        mReportFirstHandler.process(new CoreServiceEvent());
        verify(mEventSaver, never()).identifyAndSaveReport(any(CoreServiceEvent.class));
        verify(vitalComponentDataProvider, never()).setFirstEventDone(anyBoolean());
    }

    @Test
    public void testProcessShouldNotSentEventIfInitStateExists() {
        when(vitalComponentDataProvider.isInitEventDone()).thenReturn(true);
        mReportFirstHandler.process(new CoreServiceEvent());
        verify(mEventSaver, never()).identifyAndSaveReport(any(CoreServiceEvent.class));
    }

    @Test
    public void testProcessShouldSaveFirstEventDoneIfInitStateExists() {
        when(vitalComponentDataProvider.isInitEventDone()).thenReturn(true);
        mReportFirstHandler.process(new CoreServiceEvent());
        verify(vitalComponentDataProvider, times(1)).setFirstEventDone(true);
    }

    @Test
    public void testProcessShouldSendFirstEventIfFirstEventAndInitNotSentYet() {
        mReportFirstHandler.process(new CoreServiceEvent());

        ArgumentMatcher<CoreServiceEvent> firstEventMatcher = new ArgumentMatcher<CoreServiceEvent>() {
            @Override
            public boolean matches(CoreServiceEvent serviceEvent) {
                return serviceEvent.getType() == InternalEvents.EVENT_TYPE_FIRST_ACTIVATION.getTypeId();
            }
        };
        verify(mEventSaver, times(1)).identifyAndSaveFirstEventReport(argThat(firstEventMatcher));
        verify(vitalComponentDataProvider, times(1)).setFirstEventDone(true);
    }

    @Test
    public void testEventValueIsEmpty() {
        mReportFirstHandler.process(new CoreServiceEvent());
        verify(mEventSaver).identifyAndSaveFirstEventReport(argThat(new ArgumentMatcher<CoreServiceEvent>() {
            @Override
            public boolean matches(CoreServiceEvent serviceEvent) {
                return serviceEvent.getValue() == null || serviceEvent.getValue().isEmpty();
            }
        }));
    }
}
