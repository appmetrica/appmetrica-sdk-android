package io.appmetrica.analytics.impl.utils.executors;

import android.os.Handler;
import android.os.Looper;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ExecutorWrapperTest extends CommonTest {

    private static final String THREAD_NAME = "TestThread";

    @Mock
    private InterruptionSafeHandlerThread mHandlerThread;
    @Mock
    private Looper mLooper;
    @Mock
    private Handler mHandler;
    @Mock
    private Runnable mRunnable;
    @Mock
    private Object mCallableResult;

    private ExecutorWrapper mExecutorWrapper;

    private static final Long DELAY = 123123L;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(mHandlerThread.getLooper()).thenReturn(mLooper);
        mExecutorWrapper = new ExecutorWrapper(mHandlerThread, mLooper, mHandler);
    }

    @Test
    public void testDefaultConstructor() {
        mExecutorWrapper = new ExecutorWrapper(THREAD_NAME);
        assertThat(mExecutorWrapper.getLooper().getThread().getName()).startsWith(THREAD_NAME);
        assertThat(mExecutorWrapper.getLooper().getThread().getState()).isNotEqualTo(Thread.State.NEW);
    }

    @Test
    public void testConstructorWithThread() {
        mExecutorWrapper = new ExecutorWrapper(mHandlerThread);
        assertThat(mExecutorWrapper.getLooper()).isEqualTo(mHandlerThread.getLooper());
        assertThat(mExecutorWrapper.getHandler().getLooper()).isEqualTo(mHandlerThread.getLooper());
    }

    @Test
    public void testGetHandler() {
        assertThat(mExecutorWrapper.getHandler()).isEqualTo(mHandler);
    }

    @Test
    public void testGetLooper() {
        assertThat(mExecutorWrapper.getLooper()).isEqualTo(mLooper);
    }

    @Test
    public void testExecute() {
        mExecutorWrapper.execute(mRunnable);
        verify(mHandler).post(mRunnable);
    }

    @Test
    public void testSubmit() throws Exception {
        Callable callable = new Callable() {
            @Override
            public Object call() throws Exception {
                return mCallableResult;
            }
        };

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((Runnable) invocation.getArgument(0)).run();
                return null;
            }
        }).when(mHandler).post(any(Runnable.class));

        assertThat(mExecutorWrapper.submit(callable).get()).isEqualTo(mCallableResult);
    }

    @Test
    public void testExecuteDelayed() {
        mExecutorWrapper.executeDelayed(mRunnable, DELAY);
        verify(mHandler).postDelayed(mRunnable, DELAY);
    }

    @Test
    public void testExecuteDelayedWithTimeUnit() {
        mExecutorWrapper.executeDelayed(mRunnable, DELAY, TimeUnit.MILLISECONDS);
        verify(mHandler).postDelayed(mRunnable, DELAY);
    }

    @Test
    public void testRemove() {
        mExecutorWrapper.remove(mRunnable);
        verify(mHandler).removeCallbacks(mRunnable);
    }

    @Test
    public void removeAll() {
        mExecutorWrapper.removeAll();
        verify(mHandler).removeCallbacksAndMessages(null);
    }

    @Test
    public void testIsRunningForTrue() {
        testIsRunning(true);
    }

    @Test
    public void testIsRunningForFalse() {
        testIsRunning(false);
    }

    private void testIsRunning(boolean value) {
        when(mHandlerThread.isRunning()).thenReturn(value);
        assertThat(mExecutorWrapper.isRunning()).isEqualTo(value);
    }

    @Test
    public void testStopRunning() {
        mExecutorWrapper.stopRunning();
        verify(mHandlerThread).stopRunning();
    }
}
