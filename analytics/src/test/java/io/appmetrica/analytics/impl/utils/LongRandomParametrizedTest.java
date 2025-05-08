package io.appmetrica.analytics.impl.utils;

import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class LongRandomParametrizedTest extends CommonTest {

    private final long mSeed;
    private final long mMinValue;
    private final long mMaxValue;
    private final long mExpectedValue;

    @Mock
    private Random mRandom;

    private LongRandom mLongRandom;

    public LongRandomParametrizedTest(long seed, long minValue, long maxValue, long expectedValue) {
        mSeed = seed;
        mMinValue = minValue;
        mMaxValue = maxValue;
        mExpectedValue = expectedValue;
    }

    @ParameterizedRobolectricTestRunner.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {Long.MIN_VALUE, 1L, 1000L, 1L},
            {0L, 1L, 1000L, 1L},
            {998L, 1L, 1000L, 999L},
            {-998L, 1L, 1000L, 999L},
            {448L, 1L, 1000L, 449L},
            {-448L, 1L, 1000L, 449L},
            {0L, 1000L, 2000L, 1000L},
            {998L, 1000L, 2000L, 1998L},
            {-998L, 1000L, 2000L, 1998L},
            {10450L, 1000L, 2000L, 1450L},
            {-10450L, 1000L, 2000L, 1450L},
            {500, -2000L, -1000L, -1500L},
            {-500, -2000L, -1000L, -1500L}
        });
    }

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mRandom.nextLong()).thenReturn(mSeed);
        mLongRandom = new LongRandom(mRandom);
    }

    @Test
    public void testNextValue() {
        assertThat(mLongRandom.nextValue(mMinValue, mMaxValue)).isEqualTo(mExpectedValue);
    }
}
