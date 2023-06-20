package io.appmetrica.analytics.impl.component.remarketing;

import io.appmetrica.analytics.impl.FirstOccurrenceStatus;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Set;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class EventFirstOccurrenceServiceTest extends CommonTest {

    private int mVersionCode = 213;
    private static final String EVENT_NAME = "Event name";
    private static final String SECOND_EVENT_NAME = "Second event name";

    @Mock
    private EventHashesStorage mEventHashesStorage;
    @Mock
    private EventHashes mEventHashesFromStorage;
    @Mock
    private Set<Integer> mEventNameHashSetFromStorage;

    private EventFirstOccurrenceService mEventFirstOccurrenceService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mEventFirstOccurrenceService = new EventFirstOccurrenceService(mEventHashesStorage, mVersionCode);
        when(mEventHashesStorage.read()).thenReturn(mEventHashesFromStorage);
        when(mEventHashesFromStorage.getLastVersionCode()).thenReturn(mVersionCode);
        when(mEventHashesFromStorage.getEventNameHashes()).thenReturn(mEventNameHashSetFromStorage);
    }

    @Test
    public void testMainConstructor() {
        mEventFirstOccurrenceService = new EventFirstOccurrenceService(mEventHashesStorage, mVersionCode);
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(mEventFirstOccurrenceService.getCurrentVersionCode()).isEqualTo(mVersionCode);
        softAssertions.assertThat(mEventFirstOccurrenceService.getEventHashesStorage()).isNotNull();
        softAssertions.assertAll();
    }

    @Test
    public void testCheckFirstOccurrenceReadDataFromStorage() {
        mEventFirstOccurrenceService.checkFirstOccurrence(EVENT_NAME);
        verify(mEventHashesStorage, times(1)).read();
        mEventFirstOccurrenceService.checkFirstOccurrence(SECOND_EVENT_NAME);
        verify(mEventHashesStorage, times(1)).read();
    }

    @Test
    public void testCheckFirstOccurrenceUpdateVersionIfChanged() {
        int newVersion = 999;
        when(mEventNameHashSetFromStorage.contains(anyInt())).thenReturn(true);
        mEventFirstOccurrenceService = new EventFirstOccurrenceService(mEventHashesStorage, newVersion);
        mEventFirstOccurrenceService.checkFirstOccurrence(EVENT_NAME);
        verify(mEventHashesFromStorage, times(1)).setLastVersionCode(newVersion);
        verify(mEventHashesStorage, times(1)).write(mEventHashesFromStorage);
    }

    @Test
    public void testCheckFirstOccurrenceDoesNotUpdateVersionIfDoesNotChanged() {
        when(mEventNameHashSetFromStorage.contains(anyInt())).thenReturn(true);
        mEventFirstOccurrenceService.checkFirstOccurrence(EVENT_NAME);
        verify(mEventHashesFromStorage, never()).setLastVersionCode(anyInt());
        verify(mEventHashesStorage, never()).write(any(EventHashes.class));
    }

    @Test
    public void testCheckFirstOccurrenceDoesNotUpdateDataIfEventExists() {
        when(mEventNameHashSetFromStorage.contains(anyInt())).thenReturn(true);
        mEventFirstOccurrenceService.checkFirstOccurrence(EVENT_NAME);
        verify(mEventHashesFromStorage, never()).addEventNameHash(anyInt());
        verify(mEventHashesStorage, never()).write(any(EventHashes.class));
    }

    @Test
    public void testCheckFirstOccurrenceUpdateDataIfEventDoesNotExists() {
        when(mEventNameHashSetFromStorage.contains(anyInt())).thenReturn(false);
        mEventFirstOccurrenceService.checkFirstOccurrence(EVENT_NAME);
        verify(mEventHashesFromStorage, times(1)).addEventNameHash(EVENT_NAME.hashCode());
        verify(mEventHashesStorage, times(1)).write(mEventHashesFromStorage);
    }

    @Test
    public void testCheckFirstOccurrenceForExistingEventReturnExpectedValue() {
        when(mEventNameHashSetFromStorage.contains(anyInt())).thenReturn(true);
        assertThat(mEventFirstOccurrenceService.checkFirstOccurrence(EVENT_NAME))
                .isEqualTo(FirstOccurrenceStatus.NON_FIRST_OCCURENCE);
    }

    @Test
    public void testCheckFirstOccurrenceForNewEventAfterResetReturnExpectedValue() {
        when(mEventNameHashSetFromStorage.contains(anyInt())).thenReturn(false);
        when(mEventHashesFromStorage.treatUnknownEventAsNew()).thenReturn(true);
        assertThat(mEventFirstOccurrenceService.checkFirstOccurrence(EVENT_NAME))
                .isEqualTo(FirstOccurrenceStatus.FIRST_OCCURRENCE);
    }

    @Test
    public void testCheckFirstOccurrenceForNewEventNotAfterResetReturnExpectedValue() {
        when(mEventNameHashSetFromStorage.contains(anyInt())).thenReturn(false);
        when(mEventHashesFromStorage.treatUnknownEventAsNew()).thenReturn(false);
        assertThat(mEventFirstOccurrenceService.checkFirstOccurrence(EVENT_NAME))
                .isEqualTo(FirstOccurrenceStatus.UNKNOWN);
    }

    @Test
    public void testCheckFirstOccurrenceForNewEventIfHashesLimitReachedReturnExpectedValue() {
        when(mEventNameHashSetFromStorage.contains(anyInt())).thenReturn(false);
        when(mEventHashesFromStorage.getHashesCountFromLastVersion()).thenReturn(1000);
        assertThat(mEventFirstOccurrenceService.checkFirstOccurrence(EVENT_NAME))
                .isEqualTo(FirstOccurrenceStatus.UNKNOWN);
        verify(mEventHashesFromStorage, times(1)).setTreatUnknownEventAsNew(false);
        verify(mEventHashesFromStorage, never()).addEventNameHash(anyInt());
        verify(mEventHashesStorage, times(1)).write(any(EventHashes.class));
    }

    @Test
    public void testCheckFirstOccurenceForFirstEventAfterLimitReached() {
        when(mEventNameHashSetFromStorage.contains(anyInt())).thenReturn(false);
        when(mEventHashesFromStorage.treatUnknownEventAsNew()).thenReturn(true);
        when(mEventHashesFromStorage.getHashesCountFromLastVersion()).thenReturn(1000);
        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                when(mEventHashesFromStorage.treatUnknownEventAsNew())
                        .thenReturn((Boolean) invocation.getArguments()[0]);
                return null;
            }
        }).when(mEventHashesFromStorage).setTreatUnknownEventAsNew(anyBoolean());
        assertThat(mEventFirstOccurrenceService.checkFirstOccurrence(EVENT_NAME))
                .isEqualTo(FirstOccurrenceStatus.FIRST_OCCURRENCE);
        assertThat(mEventFirstOccurrenceService.checkFirstOccurrence(SECOND_EVENT_NAME))
                .isEqualTo(FirstOccurrenceStatus.UNKNOWN);
    }

    @Test
    public void testResetReadDataIfNeeded() {
        mEventFirstOccurrenceService.reset();
        verify(mEventHashesStorage, times(1)).read();
    }

    @Test
    public void testResetDoesNotReadDataIfNoNeeded() {
        mEventFirstOccurrenceService.checkFirstOccurrence(EVENT_NAME);
        verify(mEventHashesStorage, times(1)).read();
        mEventFirstOccurrenceService.reset();
        verify(mEventHashesStorage, times(1)).read();
    }

    @Test
    public void testResetShouldResetEventHashesAndSave() {
        mEventFirstOccurrenceService.reset();
        verify(mEventHashesFromStorage, times(1)).clearEventHashes();
        verify(mEventHashesFromStorage, times(1)).setTreatUnknownEventAsNew(true);
        verify(mEventHashesStorage, times(1)).write(mEventHashesFromStorage);
    }
}
