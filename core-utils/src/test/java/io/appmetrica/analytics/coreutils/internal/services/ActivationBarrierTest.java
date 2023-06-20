package io.appmetrica.analytics.coreutils.internal.services;

import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider;
import java.util.Arrays;
import java.util.Collection;
import org.junit.After;
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
public class ActivationBarrierTest {

    @Mock
    private SystemTimeProvider mTimeProvider;
    @Mock
    private ICommonExecutor mExecutor;
    @Mock
    private ActivationBarrier.IActivationBarrierCallback mCallback;

    private long mActivationTimestamp;
    private long mRequestTimestamp;
    private long mRequestedWaiting;
    private long mExpectedDelay;

    private ActivationBarrier mActivationBarrier;

    public ActivationBarrierTest(long activationTimestamp,
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
        mActivationBarrier = new ActivationBarrier(mTimeProvider);
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
    public static class ActivationBarrierHelperTest {

        private final long delay = 5000;
        private ActivationBarrier.ActivationBarrierHelper helper;

        @Mock
        private ActivationBarrier activationBarrier;
        @Mock
        private Runnable runnable;
        @Mock
        private ICommonExecutor executor;
        @Mock
        private UtilityServiceLocator mockedUtilityServiceLocator;
        @Captor
        private ArgumentCaptor<Runnable> runnableCaptor;

        private UtilityServiceLocator originalUtilityServiceLocator;

        @Before
        public void setUp() {
            MockitoAnnotations.openMocks(this);
            originalUtilityServiceLocator = UtilityServiceLocator.Companion.getInstance();
            UtilityServiceLocator.Companion.setInstance(mockedUtilityServiceLocator);
            when(mockedUtilityServiceLocator.getActivationBarrier()).thenReturn(activationBarrier);
            helper = new ActivationBarrier.ActivationBarrierHelper(runnable, activationBarrier);
        }

        @After
        public void tearDown() {
            UtilityServiceLocator.Companion.setInstance(originalUtilityServiceLocator);
        }

        @Test
        public void subscribeIfNeeded() {
            doAnswer(new Answer() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    ActivationBarrier.IActivationBarrierCallback callback = invocation.getArgument(2);
                    callback.onWaitFinished();
                    return null;
                }
            }).when(activationBarrier).subscribe(eq(delay), same(executor), any(ActivationBarrier.IActivationBarrierCallback.class));
            helper.subscribeIfNeeded(delay, executor);
            verify(activationBarrier).subscribe(eq(delay), same(executor), any(ActivationBarrier.IActivationBarrierCallback.class));
            verify(runnable, times(1)).run();
            helper.subscribeIfNeeded(delay, executor);
            verifyNoMoreInteractions(activationBarrier);
            verify(executor).execute(runnableCaptor.capture());
            runnableCaptor.getValue().run();
            verify(runnable, times(2)).run();
        }
    }
}
