package io.appmetrica.analytics.coreutils.internal.time;

import android.os.SystemClock;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class SystemTimeProviderTest {

    private SystemTimeProvider mSystemTimeProvider;

    @Before
    public void setUp() throws Exception {
        mSystemTimeProvider = new SystemTimeProvider();
    }

    @Test
    public void testCurrentTimeMillis() {
        long now = System.currentTimeMillis();
        assertThat(mSystemTimeProvider.currentTimeMillis()).isBetween(now - 1000, now + 1000);
    }

    @Test
    public void testCurrentTimeSeconds() {
        long now = System.currentTimeMillis() / 1000;
        assertThat(mSystemTimeProvider.currentTimeSeconds()).isBetween(now - 1, now + 1);
    }

    @Test
    public void testElapsedRealtime() {
        long time = 234233123L;
        try (MockedStatic<SystemClock> sSystemClock = Mockito.mockStatic(SystemClock.class)) {
            when(SystemClock.elapsedRealtime()).thenReturn(time);
            assertThat(mSystemTimeProvider.elapsedRealtime()).isEqualTo(time);
        }
    }

    @Test
    public void testSystemNanoTime() {
        long delta = TimeUnit.SECONDS.toNanos(1);
        long nanoTime = System.nanoTime();
        assertThat(mSystemTimeProvider.systemNanoTime()).isBetween(nanoTime - delta, nanoTime + delta);
    }

}
