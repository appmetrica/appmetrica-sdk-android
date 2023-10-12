package io.appmetrica.analytics.impl.component.processor.event;

import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.EventSaver;
import io.appmetrica.analytics.impl.component.remarketing.EventFirstOccurrenceService;
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider;
import io.appmetrica.analytics.impl.events.EventListener;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.impl.utils.ServerTime;
import io.appmetrica.analytics.testutils.CommonTest;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ApplySettingsFromActivationConfigHandlerTest extends CommonTest {

    @Mock
    private ComponentUnit mComponent;
    @Mock
    private VitalComponentDataProvider vitalComponentDataProvider;
    private ApplySettingsFromActivationConfigHandler mHandler;
    @Mock
    private ReportRequestConfig mConfig;
    @Mock
    private ServerTime mServerTime;
    @Mock
    private EventFirstOccurrenceService mEventFirstOccurrenceService;
    @Mock
    private EventSaver mEventSaver;
    @Mock
    private EventListener mEventListener;

    @Before
    public void setUp() throws JSONException {
        MockitoAnnotations.openMocks(this);
        when(vitalComponentDataProvider.isInitEventDone()).thenReturn(false);
        when(vitalComponentDataProvider.isFirstEventDone()).thenReturn(false);
        mHandler = new ApplySettingsFromActivationConfigHandler(mComponent, vitalComponentDataProvider, mServerTime);
        when(mComponent.getFreshReportRequestConfig()).thenReturn(mConfig);
        when(mComponent.getEventFirstOccurrenceService()).thenReturn(mEventFirstOccurrenceService);
        when(mComponent.getEventSaver()).thenReturn(mEventSaver);
        when(mComponent.getReportsListener()).thenReturn(mEventListener);
    }

    @Test
    public void processShouldNotBreakEventProcessing() {
        assertThat(mHandler.process(new CounterReport())).isFalse();
    }

    @Test
    public void nothingIsDoneIfFirstStateExists() {
        when(vitalComponentDataProvider.isFirstEventDone()).thenReturn(true);
        mHandler.process(new CounterReport());
        verifyNoMoreInteractions(mEventFirstOccurrenceService);
        verify(mConfig, never()).isFirstActivationAsUpdate();
    }

    @Test
    public void nothingIsDoneIfInitStateExists() {
        when(vitalComponentDataProvider.isInitEventDone()).thenReturn(true);
        mHandler.process(new CounterReport());
        verifyNoMoreInteractions(mEventFirstOccurrenceService);
        verify(mConfig, never()).isFirstActivationAsUpdate();
    }

    @Test
    public void processShouldDisableProbablyTimeFromPastCheckingIfFirstActivationIsUpdate() {
        doReturn(true).when(mConfig).isFirstActivationAsUpdate();
        mHandler.process(new CounterReport());
        verify(mServerTime).disableTimeDifferenceChecking();
    }

    @Test
    public void processShouldNotDisableProbablyTimeFromPastCheckingIfFistActivationIsNotUpdate() {
        doReturn(false).when(mConfig).isFirstActivationAsUpdate();
        mHandler.process(new CounterReport());
        verify(mServerTime, never()).disableTimeDifferenceChecking();
    }

    @Test
    public void processResetEventFirstOccurrenceService() {
        mHandler.process(new CounterReport());
        verify(mEventFirstOccurrenceService).reset();
    }
}
