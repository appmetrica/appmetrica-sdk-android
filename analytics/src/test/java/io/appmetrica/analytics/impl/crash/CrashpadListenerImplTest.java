package io.appmetrica.analytics.impl.crash;

import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.impl.crash.ndk.crashpad.CrashpadCrashWatcher;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(RobolectricTestRunner.class)
public class CrashpadListenerImplTest extends CommonTest {

    @Mock
    private CrashpadCrashWatcher crashpadCrashWatcher;
    private CrashpadListenerImpl crashpadListener;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        crashpadListener = new CrashpadListenerImpl(crashpadCrashWatcher);
    }

    @Test
    public void addListeners() {
        Consumer<String> firstListener = mock(Consumer.class);
        Consumer<String> secondListener = mock(Consumer.class);
        Consumer<String> thirdListener = mock(Consumer.class);
        crashpadListener.addListener(firstListener);
        crashpadListener.addListener(secondListener);

        crashpadListener.onCreate();
        ArgumentCaptor<Consumer> listenerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(crashpadCrashWatcher).subscribe(listenerCaptor.capture());

        crashpadListener.removeListener(secondListener);
        crashpadListener.addListener(thirdListener);

        final String crash = "some crash";
        listenerCaptor.getValue().consume(crash);

        verify(firstListener).consume(crash);
        verifyZeroInteractions(secondListener);
        verify(thirdListener).consume(crash);
    }

    @Test
    public void onDestroyed() {
        crashpadListener.onCreate();
        ArgumentCaptor<Consumer> listenerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(crashpadCrashWatcher).subscribe(listenerCaptor.capture());

        Consumer listener = listenerCaptor.getValue();
        crashpadListener.onDestroy();
        verify(crashpadCrashWatcher).unsubscribe(listener);
    }
}
