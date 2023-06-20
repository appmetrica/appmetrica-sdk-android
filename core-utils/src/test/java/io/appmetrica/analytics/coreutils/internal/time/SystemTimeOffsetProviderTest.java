package io.appmetrica.analytics.coreutils.internal.time;

import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class SystemTimeOffsetProviderTest extends CommonTest {

    private long mValue;
    private TimeUnit mTimeUnit;
    private long mExpectedValue;

    private static final long NOW = 3000L;

    public SystemTimeOffsetProviderTest(long value, TimeUnit timeUnit, long expectedValue) {
        mValue = value;
        mTimeUnit = timeUnit;
        mExpectedValue = expectedValue;
    }

    @ParameterizedRobolectricTestRunner.Parameters(name = "for {2} with expected = {3}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {1000000000L, TimeUnit.NANOSECONDS, 2000L},
                {5000000000L, TimeUnit.NANOSECONDS, -2000L},
                {1000000L, TimeUnit.MICROSECONDS, 2000L},
                {5000000L, TimeUnit.MICROSECONDS, -2000L},
                {1000L, TimeUnit.MILLISECONDS, 2000L},
                {5000L, TimeUnit.MILLISECONDS, -2000L},
                {1L, TimeUnit.SECONDS, 2000L},
                {5L, TimeUnit.SECONDS, -2000L},
        });
    }

    @Mock
    private SystemTimeProvider mSystemTimeProvider;
    private SystemTimeOffsetProvider mSystemTimeOffsetProvider;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        mSystemTimeOffsetProvider = new SystemTimeOffsetProvider(mSystemTimeProvider);
    }

    @Test
    public void testElapsedRealtimeOffset() {
        when(mSystemTimeProvider.elapsedRealtime()).thenReturn(NOW);
        assertThat(mSystemTimeOffsetProvider.elapsedRealtimeOffset(mValue, mTimeUnit)).isEqualTo(mExpectedValue);
    }

    @Test
    public void testElapsedRealtimeOffsetInSeconds() {
        when(mSystemTimeProvider.elapsedRealtime()).thenReturn(NOW);
        assertThat(mSystemTimeOffsetProvider.elapsedRealtimeOffsetInSeconds(mValue, mTimeUnit))
                .isEqualTo(TimeUnit.MILLISECONDS.toSeconds(mExpectedValue));
    }

    @Test
    public void testNanoTimeOffsetInNanos() {
        when(mSystemTimeProvider.systemNanoTime()).thenReturn(TimeUnit.MILLISECONDS.toNanos(NOW));
        assertThat(mSystemTimeOffsetProvider.systemNanoTimeOffsetInNanos(mValue, mTimeUnit))
                .isEqualTo(TimeUnit.MILLISECONDS.toNanos(mExpectedValue));
    }

    @Test
    public void testNanoTimeOffsetInSeconds() {
        when(mSystemTimeProvider.systemNanoTime()).thenReturn(TimeUnit.MILLISECONDS.toNanos(NOW));
        assertThat(mSystemTimeOffsetProvider.systemNanoTimeOffsetInSeconds(mValue, mTimeUnit))
                .isEqualTo(TimeUnit.MILLISECONDS.toSeconds(mExpectedValue));
    }

    @Test
    public void testOffsetInSecondsIfNotZero() {
        when(mSystemTimeProvider.currentTimeSeconds()).thenReturn(TimeUnit.MILLISECONDS.toSeconds(NOW));
        assertThat(mSystemTimeOffsetProvider.offsetInSecondsIfNotZero(mValue, mTimeUnit))
                .isEqualTo(TimeUnit.MILLISECONDS.toSeconds(mExpectedValue));
    }

    @Test
    public void testOffsetInSecondsIfZero() {
        when(mSystemTimeProvider.currentTimeSeconds()).thenReturn(TimeUnit.MILLISECONDS.toSeconds(NOW));
        assertThat(mSystemTimeOffsetProvider.offsetInSecondsIfNotZero(0, mTimeUnit)).isZero();
    }
}
