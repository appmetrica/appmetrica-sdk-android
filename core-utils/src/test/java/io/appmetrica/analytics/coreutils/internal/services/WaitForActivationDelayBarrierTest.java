package io.appmetrica.analytics.coreutils.internal.services;

import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.coreapi.internal.servicecomponents.ActivationBarrierCallback;
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class WaitForActivationDelayBarrierTest extends CommonTest {

    @Mock
    private SystemTimeProvider mTimeProvider;
    @Mock
    private ICommonExecutor mExecutor;
    @Mock
    private ActivationBarrierCallback mCallback;

    private long mActivationTimestamp;
    private long mRequestTimestamp;
    private long mRequestedWaiting;
    private long mExpectedDelay;

    private WaitForActivationDelayBarrier mActivationBarrier;

    public WaitForActivationDelayBarrierTest(long activationTimestamp,
                                             long requestTimestamp,
                                             long requestedWaiting,
                                             long expectedDelay) {
        mActivationTimestamp = activationTimestamp;
        mRequestTimestamp = requestTimestamp;
        mRequestedWaiting = requestedWaiting;
        mExpectedDelay = expectedDelay;
    }

    @ParameterizedRobolectricTestRunner.Parameters(name = "#{index}: Expected delay = {3} for activation " +
            "timestamp = {0}, requested timestamp = {1} and requested waiting = {2}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {10000L, 10000L, 100L, 100L},
                {10000L, 10500L, 1000L, 500L},
                {10000L, 11000L, 500L, 0L},
                {10000L, 10000L, 0L, 0L},
                {10000L, 11000L, 0L, 0L},
                {10000L, 10000L, -1L, 0L},
                {10000L, 11000L, -1L, 0L}
        });
    }

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mTimeProvider.currentTimeMillis()).thenReturn(mActivationTimestamp);
        mActivationBarrier = new WaitForActivationDelayBarrier(mTimeProvider);
    }

    @Test
    public void testRequestWaiting() {
        mActivationBarrier.activate();
        when(mTimeProvider.currentTimeMillis()).thenReturn(mRequestTimestamp);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((Runnable) invocation.getArgument(0)).run();
                return null;
            }
        }).when(mExecutor).executeDelayed(any(Runnable.class), anyLong());
        mActivationBarrier.subscribe(mRequestedWaiting, mExecutor, mCallback);
        verify(mExecutor).executeDelayed(any(Runnable.class), eq(mExpectedDelay));
        verify(mCallback).onWaitFinished();
    }

    @RunWith(RobolectricTestRunner.class)
    public static class WaitForActivationDelayBarrierHelperTest extends CommonTest {

        private final long delay = 5000;
        private WaitForActivationDelayBarrier.ActivationBarrierHelper helper;

        @Mock
        private WaitForActivationDelayBarrier activationBarrier;
        @Mock
        private Runnable runnable;
        @Mock
        private ICommonExecutor executor;
        @Mock
        private UtilityServiceProvider mockedUtilityServiceProvider;
        @Captor
        private ArgumentCaptor<Runnable> runnableCaptor;
        @Mock
        private UtilityServiceProvider originalUtilityServiceProvider;

        @Before
        public void setUp() {
            MockitoAnnotations.openMocks(this);
            when(mockedUtilityServiceProvider.getActivationBarrier()).thenReturn(activationBarrier);
            helper = new WaitForActivationDelayBarrier.ActivationBarrierHelper(runnable, activationBarrier);
        }

        @Test
        public void subscribeIfNeeded() {
            doAnswer(new Answer() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    ActivationBarrierCallback callback = invocation.getArgument(2);
                    callback.onWaitFinished();
                    return null;
                }
            }).when(activationBarrier).subscribe(eq(delay), same(executor), any(ActivationBarrierCallback.class));
            helper.subscribeIfNeeded(delay, executor);
            verify(activationBarrier).subscribe(eq(delay), same(executor), any(ActivationBarrierCallback.class));
            verify(runnable, times(1)).run();
            helper.subscribeIfNeeded(delay, executor);
            verifyNoMoreInteractions(activationBarrier);
            verify(executor).execute(runnableCaptor.capture());
            runnableCaptor.getValue().run();
            verify(runnable, times(2)).run();
        }
    }
}
