package io.appmetrica.analytics.coreutils.internal.services;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class FirstExecutionDelayCheckerTest {

    private long mFirstStartupTimestamp;
    private long mLastStartupTimestamp;
    private long mDelay;
    private boolean mExpectedValue;

    private FirstExecutionConditionServiceImpl.FirstExecutionDelayChecker mChecker;

    public FirstExecutionDelayCheckerTest(long firstStartupTimestamp,
                                          long lastStartupTimestamp,
                                          long delay,
                                          boolean expectedValue) {
        mFirstStartupTimestamp = firstStartupTimestamp;
        mLastStartupTimestamp = lastStartupTimestamp;
        mDelay = delay;
        mExpectedValue = expectedValue;
    }

    @ParameterizedRobolectricTestRunner.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {100000L, 200000L, 20L, true},
                {100000L, 200000L, 100000L, true},
                {100000L, 200000L, 200000L, false}
        });
    }

    @Before
    public void setUp() throws Exception {
        mChecker = new FirstExecutionConditionServiceImpl.FirstExecutionDelayChecker();
    }

    @Test
    public void testDelaySinceFirstStartupWasPassed() {
        assertThat(mChecker.delaySinceFirstStartupWasPassed(mFirstStartupTimestamp, mLastStartupTimestamp, mDelay))
                .isEqualTo(mExpectedValue);
    }
}
