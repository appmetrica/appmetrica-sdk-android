package io.appmetrica.analytics.impl.referrer.service;

import io.appmetrica.analytics.impl.referrer.common.ReferrerChosenListener;
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class ReferrerManagerTest extends CommonTest {

    @Mock
    private ReferrerHolder referrerHolder;
    @Mock
    private ReferrerChosenListener firstReferrerChosenListener;
    @Mock
    private ReferrerChosenListener secondReferrerChosenListener;
    private ReferrerManager referrerManager;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        referrerManager = new ReferrerManager(referrerHolder);
    }

    @Test
    public void subscribedToReferrerHolder() {
        ArgumentCaptor<ReferrerListenerNotifier> listenerCaptor = ArgumentCaptor.forClass(ReferrerListenerNotifier.class);
        verify(referrerHolder).subscribe(listenerCaptor.capture());
        ReferrerListenerNotifier listenerNotifier = listenerCaptor.getValue();
        assertThat(listenerNotifier).isInstanceOf(SimpleReferrerListenerNotifier.class);
        assertThat(listenerNotifier.getListener()).isSameAs(referrerManager);
    }

    @Test
    public void referrerIsRequested() {
        verify(referrerHolder).retrieveReferrerIfNeeded();
    }

    @Test
    public void handleReferrerThenAddTwoListeners() {
        ReferrerInfo referrerInfo = mock(ReferrerInfo.class);
        referrerManager.handleReferrer(referrerInfo);
        referrerManager.addOneShotListener(firstReferrerChosenListener);
        referrerManager.addOneShotListener(secondReferrerChosenListener);
        verify(firstReferrerChosenListener).onReferrerChosen(referrerInfo);
        verify(secondReferrerChosenListener).onReferrerChosen(referrerInfo);
    }

    @Test
    public void handleNullReferrerThenAddTwoListeners() {
        ReferrerInfo referrerInfo = mock(ReferrerInfo.class);
        referrerManager.handleReferrer(null);
        referrerManager.addOneShotListener(firstReferrerChosenListener);
        referrerManager.addOneShotListener(secondReferrerChosenListener);
        verify(firstReferrerChosenListener).onReferrerChosen(null);
        verify(secondReferrerChosenListener).onReferrerChosen(null);
    }

    @Test
    public void addTwoListenersThenHandleReferrer() {
        ReferrerInfo referrerInfo = mock(ReferrerInfo.class);
        referrerManager.addOneShotListener(firstReferrerChosenListener);
        referrerManager.addOneShotListener(secondReferrerChosenListener);
        verifyNoMoreInteractions(firstReferrerChosenListener, secondReferrerChosenListener);
        referrerManager.handleReferrer(referrerInfo);
        verify(firstReferrerChosenListener).onReferrerChosen(referrerInfo);
        verify(secondReferrerChosenListener).onReferrerChosen(referrerInfo);
    }

    @Test
    public void addTwoListenersThenHandleNullReferrer() {
        referrerManager.addOneShotListener(firstReferrerChosenListener);
        referrerManager.addOneShotListener(secondReferrerChosenListener);
        verifyNoMoreInteractions(firstReferrerChosenListener, secondReferrerChosenListener);
        referrerManager.handleReferrer(null);
        verify(firstReferrerChosenListener).onReferrerChosen(null);
        verify(secondReferrerChosenListener).onReferrerChosen(null);
    }

    @Test
    public void addOneListenerHandleReferrerAndAddAnother() {
        ReferrerInfo referrerInfo = mock(ReferrerInfo.class);
        referrerManager.addOneShotListener(firstReferrerChosenListener);
        verifyNoMoreInteractions(firstReferrerChosenListener);
        referrerManager.handleReferrer(referrerInfo);
        verify(firstReferrerChosenListener).onReferrerChosen(referrerInfo);
        referrerManager.addOneShotListener(secondReferrerChosenListener);
        verify(secondReferrerChosenListener).onReferrerChosen(referrerInfo);
    }

    @Test
    public void handleReferrerTwice() {
        ReferrerInfo referrerInfo = mock(ReferrerInfo.class);
        referrerManager.addOneShotListener(firstReferrerChosenListener);
        verifyNoMoreInteractions(firstReferrerChosenListener);
        referrerManager.handleReferrer(referrerInfo);
        referrerManager.handleReferrer(referrerInfo);
        verify(firstReferrerChosenListener, times(1)).onReferrerChosen(referrerInfo);
    }

    @Test
    public void handleReferrerTwiceFirstIsNull() {
        ReferrerInfo referrerInfo = mock(ReferrerInfo.class);
        referrerManager.addOneShotListener(firstReferrerChosenListener);
        verifyNoMoreInteractions(firstReferrerChosenListener);
        referrerManager.handleReferrer(null);
        referrerManager.handleReferrer(referrerInfo);
        verify(firstReferrerChosenListener, times(1)).onReferrerChosen(null);
        verify(firstReferrerChosenListener, never()).onReferrerChosen(referrerInfo);
    }
}
