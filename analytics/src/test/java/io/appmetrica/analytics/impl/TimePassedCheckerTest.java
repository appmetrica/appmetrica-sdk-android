package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.coreutils.internal.time.TimePassedChecker;
import io.appmetrica.analytics.coreutils.internal.time.TimeProvider;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class TimePassedCheckerTest extends CommonTest {

    @Mock
    private TimeProvider mTimeProvider;
    private TimePassedChecker mTimePassedChecker;
    private final long mCurrentTimeSeconds = 12345678;
    private final long mCurrentTimeMillis = 87654321;
    private static final String TAG = "tag";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mTimeProvider.currentTimeSeconds()).thenReturn(mCurrentTimeSeconds);
        when(mTimeProvider.currentTimeMillis()).thenReturn(mCurrentTimeMillis);
        mTimePassedChecker = new TimePassedChecker(mTimeProvider);
    }

    @Test
    public void testCurrentLessThanLastSeconds() {
        assertThat(mTimePassedChecker.didTimePassSeconds(mCurrentTimeSeconds + 1, 0, TAG)).isTrue();
    }

    @Test
    public void testIntervalPassedSeconds() {
        assertThat(mTimePassedChecker.didTimePassSeconds(mCurrentTimeSeconds - 11, 10, TAG)).isTrue();
    }

    @Test
    public void testIntervalNotPassedSeconds() {
        assertThat(mTimePassedChecker.didTimePassSeconds(mCurrentTimeSeconds - 9, 10, TAG)).isFalse();
    }

    @Test
    public void testIntervalPassedExactlySeconds() {
        assertThat(mTimePassedChecker.didTimePassSeconds(mCurrentTimeSeconds - 10, 10, TAG)).isTrue();
    }

    @Test
    public void testCurrentLessThanLastMillis() {
        assertThat(mTimePassedChecker.didTimePassMillis(mCurrentTimeMillis + 1, 0, TAG)).isTrue();
    }

    @Test
    public void testIntervalPassedMillis() {
        assertThat(mTimePassedChecker.didTimePassMillis(mCurrentTimeMillis - 11, 10, TAG)).isTrue();
    }

    @Test
    public void testIntervalNotPassedMillis() {
        assertThat(mTimePassedChecker.didTimePassMillis(mCurrentTimeMillis - 9, 10, TAG)).isFalse();
    }

    @Test
    public void testIntervalPassedExactlyMillis() {
        assertThat(mTimePassedChecker.didTimePassMillis(mCurrentTimeMillis - 10, 10, TAG)).isTrue();
    }
}
