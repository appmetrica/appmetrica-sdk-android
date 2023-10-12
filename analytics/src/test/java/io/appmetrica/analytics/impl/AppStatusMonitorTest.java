package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.testutils.ClientExecutorProviderStub;
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AppStatusMonitorTest extends CommonTest {

    @Mock
    private AppStatusMonitor.Observer mObserver;
    @Mock
    private AppStatusMonitor.Observer mSecondObserver;
    @Mock
    private AppStatusMonitor.Observer mThirdObserver;
    @Mock
    private ICommonExecutor mExecutor;
    @Mock
    private ClientServiceLocator mClientServiceLocator;

    private AppStatusMonitor mMonitor;

    private static final long SESSION_TIMEOUT = 12L;

    @Rule
    public final ClientServiceLocatorRule mClientServiceLocatorRule = new ClientServiceLocatorRule();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        stubExecuteDelayed(SESSION_TIMEOUT);

        mMonitor = new AppStatusMonitor(SESSION_TIMEOUT, mExecutor);
        when(mClientServiceLocator.getClientExecutorProvider()).thenReturn(new ClientExecutorProviderStub());
    }

    @Test
    public void testDefaultConstructor() {
        mMonitor = new AppStatusMonitor(SESSION_TIMEOUT);
        assertThat(mMonitor.getExecutor()).isNotNull();
        assertThat(mMonitor.getDefaultSessionTimeout()).isEqualTo(SESSION_TIMEOUT);
    }

    @Test
    public void testResume() {
        mMonitor.registerObserver(mObserver);
        mMonitor.resume();
        verify(mExecutor).remove(any(Runnable.class));
        verify(mObserver).onResume();
    }

    @Test
    public void testPause() {
        mMonitor.registerObserver(mObserver);
        mMonitor.pause();
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void testResumeTwice() {
        mMonitor.registerObserver(mObserver);
        mMonitor.resume();
        mMonitor.resume();
        verify(mExecutor).remove(any(Runnable.class));
        verify(mObserver).onResume();
    }

    @Test
    public void testPauseTwice() {
        mMonitor.registerObserver(mObserver);
        mMonitor.pause();
        mMonitor.pause();
        verifyNoMoreInteractions(mExecutor, mObserver);
    }

    @Test
    public void testPauseAfterResume() {
        mMonitor.registerObserver(mObserver);
        mMonitor.resume();
        mMonitor.pause();
        InOrder inOrder = inOrder(mObserver, mExecutor);
        inOrder.verify(mExecutor).remove(any(Runnable.class));
        inOrder.verify(mObserver).onResume();
        inOrder.verify(mExecutor).executeDelayed(any(Runnable.class), eq(SESSION_TIMEOUT));
        inOrder.verify(mObserver).onPause();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testResumeAfterPause() {
        mMonitor.registerObserver(mObserver);
        mMonitor.pause();
        mMonitor.resume();
        verify(mExecutor).remove(any(Runnable.class));
        verify(mObserver).onResume();
    }

    @Test
    public void testMultipleObservers() {
        mMonitor.registerObserver(mObserver);
        mMonitor.registerObserver(mSecondObserver);
        mMonitor.registerObserver(mThirdObserver);
        mMonitor.resume();
        verify(mObserver).onResume();
        verify(mSecondObserver).onResume();
        verify(mThirdObserver).onResume();
    }

    @Test
    public void testObserversUnregister() {
        mMonitor.registerObserver(mObserver);
        mMonitor.resume();
        mMonitor.unregisterObserver(mObserver);
        mMonitor.pause();
        verify(mObserver).onResume();
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void testObserverWithCustomTimeout() {
        long customTimeout = 4342L;
        stubExecuteDelayed(customTimeout);
        mMonitor.registerObserver(mObserver, customTimeout);
        mMonitor.resume();
        mMonitor.pause();
        verify(mObserver).onPause();
    }

    @Test
    public void testObserverWithStickyWhenPaused() {
        mMonitor.pause();
        mMonitor.registerObserver(mObserver, /* sticky */ true);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void testObserverWithStickyWhenResumed() {
        mMonitor.resume();
        mMonitor.registerObserver(mObserver, /* sticky */ true);
        verify(mObserver).onResume();
    }

    private void stubExecuteDelayed(long timeout) {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                ((Runnable) invocation.getArgument(0)).run();
                return null;
            }
        }).when(mExecutor).executeDelayed(any(Runnable.class), eq(timeout));

    }
}
