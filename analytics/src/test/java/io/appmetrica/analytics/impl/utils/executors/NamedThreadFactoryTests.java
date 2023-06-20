package io.appmetrica.analytics.impl.utils.executors;

import android.os.HandlerThread;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class NamedThreadFactoryTests extends CommonTest {

    final static String THREAD_NAME = "Test thread";

    private NamedThreadFactory mThreadFactory;
    private Runnable mRunnable;

    @Before
    public void setUp() {
        mThreadFactory = new NamedThreadFactory(THREAD_NAME);
        mRunnable = new Runnable() {
            public void run() {
            }
        };
    }

    @Test
    public void testThreadCreation() {
        Thread thread = mThreadFactory.newThread(mRunnable);
        assertThat(thread.getName()).contains(THREAD_NAME);
    }

    @Test
    public void testThreadCreationViaStaticMethod() {
        Thread thread = NamedThreadFactory.newThread(THREAD_NAME, mRunnable);
        assertThat(thread.getName()).contains(THREAD_NAME);
    }

    @Test
    public void testHandlerThreadCreation() {
        HandlerThread handlerThread = mThreadFactory.newHandlerThread();
        assertThat(handlerThread.getName()).contains(THREAD_NAME);
    }

    @Test
    public void testHandlerThreadCreationViaStaticMethod() {
        HandlerThread handlerThread = NamedThreadFactory.newHandlerThread(THREAD_NAME);
        assertThat(handlerThread.getName()).contains(THREAD_NAME);
    }
}
