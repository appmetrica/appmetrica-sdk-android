package io.appmetrica.analytics.impl.referrer.service;

import io.appmetrica.analytics.coreutils.internal.logger.LoggerStorage;
import io.appmetrica.analytics.impl.referrer.common.ReferrerChosenListener;
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ReferrerManagerTest extends CommonTest {

    @Mock
    private ReferrerHolder referrerHolder;
    private final String referrer = "Referrer from GP";
    private final ReferrerInfo.Source sourceGp = ReferrerInfo.Source.GP;
    private ReferrerInfo referrerInfo;
    @Mock
    private ReferrerChosenListener firstReferrerChosenListener;
    @Mock
    private ReferrerChosenListener secondReferrerChosenListener;
    private ReferrerManager referrerManager;

    @Mock
    private PublicLogger logger;

    @Rule
    public MockedStaticRule<LoggerStorage> loggerStorageMockedStatic = new MockedStaticRule<>(LoggerStorage.class);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(LoggerStorage.getMainPublicOrAnonymousLogger()).thenReturn(logger);
        referrerInfo = new ReferrerInfo(referrer, 100500L, 200500L, sourceGp);
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
        referrerManager.handleReferrer(referrerInfo);
        referrerManager.addOneShotListener(firstReferrerChosenListener);
        referrerManager.addOneShotListener(secondReferrerChosenListener);
        verify(firstReferrerChosenListener).onReferrerChosen(referrerInfo);
        verify(secondReferrerChosenListener).onReferrerChosen(referrerInfo);
    }

    @Test
    public void handleNullReferrerThenAddTwoListeners() {
        referrerManager.handleReferrer(null);
        referrerManager.addOneShotListener(firstReferrerChosenListener);
        referrerManager.addOneShotListener(secondReferrerChosenListener);
        verify(firstReferrerChosenListener).onReferrerChosen(null);
        verify(secondReferrerChosenListener).onReferrerChosen(null);
    }

    @Test
    public void addTwoListenersThenHandleReferrer() {
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
        referrerManager.addOneShotListener(firstReferrerChosenListener);
        verifyNoMoreInteractions(firstReferrerChosenListener);
        referrerManager.handleReferrer(referrerInfo);
        verify(firstReferrerChosenListener).onReferrerChosen(referrerInfo);
        referrerManager.addOneShotListener(secondReferrerChosenListener);
        verify(secondReferrerChosenListener).onReferrerChosen(referrerInfo);
    }

    @Test
    public void handleReferrerTwice() {
        referrerManager.addOneShotListener(firstReferrerChosenListener);
        verifyNoMoreInteractions(firstReferrerChosenListener);
        referrerManager.handleReferrer(referrerInfo);
        referrerManager.handleReferrer(referrerInfo);
        verify(firstReferrerChosenListener, times(1)).onReferrerChosen(referrerInfo);
    }

    @Test
    public void handleReferrerTwiceFirstIsNull() {
        referrerManager.addOneShotListener(firstReferrerChosenListener);
        verifyNoMoreInteractions(firstReferrerChosenListener);
        referrerManager.handleReferrer(null);
        referrerManager.handleReferrer(referrerInfo);
        verify(firstReferrerChosenListener, times(1)).onReferrerChosen(null);
        verify(firstReferrerChosenListener, never()).onReferrerChosen(referrerInfo);
    }

    @Test
    public void handleReferrerWritePublicLog() {
        referrerManager.handleReferrer(referrerInfo);
        verify(logger).info(
            argThat(new ArgumentMatcher<String>() {
                @Override
                public boolean matches(String argument) {
                    return argument.contains("Received referrer from source");
                }
            }),
            eq(sourceGp.value),
            eq(referrer)
        );
    }

    @Test
    public void handleReferrerWritePublicLogIfReferrerIsNull() {
        referrerManager.handleReferrer(null);
        verifyNoInteractions(logger);
    }
}
