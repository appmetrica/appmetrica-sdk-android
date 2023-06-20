package io.appmetrica.analytics.impl.events;

import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class EventTriggerTest extends CommonTest {

    @Mock
    private EventsFlusher mEventsFlusher;
    @Mock
    private EventCondition mFirstEventCondition;
    @Mock
    private EventCondition mSecondEventCondition;
    private EventTrigger mEventTrigger;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mEventTrigger = new EventTrigger(Arrays.asList(mFirstEventCondition, mSecondEventCondition), mEventsFlusher);
    }

    @Test
    public void testBothConditionsNotMet() {
        when(mFirstEventCondition.isConditionMet()).thenReturn(false);
        when(mSecondEventCondition.isConditionMet()).thenReturn(false);
        mEventTrigger.trigger();
        verify(mEventsFlusher, never()).flushEvents();
    }

    @Test
    public void testOnlyFirstConditionMet() {
        when(mFirstEventCondition.isConditionMet()).thenReturn(true);
        when(mSecondEventCondition.isConditionMet()).thenReturn(false);
        mEventTrigger.trigger();
        verify(mEventsFlusher).flushEvents();
    }

    @Test
    public void testOnlySecondConditionMet() {
        when(mFirstEventCondition.isConditionMet()).thenReturn(false);
        when(mSecondEventCondition.isConditionMet()).thenReturn(true);
        mEventTrigger.trigger();
        verify(mEventsFlusher).flushEvents();
    }

    @Test
    public void testBothConditionsMet() {
        when(mFirstEventCondition.isConditionMet()).thenReturn(true);
        when(mSecondEventCondition.isConditionMet()).thenReturn(true);
        mEventTrigger.trigger();
        verify(mEventsFlusher).flushEvents();
    }

    @Test
    public void testEmptyConditions() {
        EventTrigger eventTrigger = new EventTrigger(Collections.EMPTY_LIST, mEventsFlusher);
        eventTrigger.trigger();
        verify(mEventsFlusher).flushEvents();
    }

    @Test
    public void testTriggerDisabledThenEnabled() {
        when(mFirstEventCondition.isConditionMet()).thenReturn(true);
        when(mSecondEventCondition.isConditionMet()).thenReturn(true);
        mEventTrigger.disableTrigger();
        mEventTrigger.trigger();
        verify(mEventsFlusher, never()).flushEvents();
        mEventTrigger.enableTrigger();
        mEventTrigger.trigger();
        verify(mEventsFlusher).flushEvents();
    }
}
