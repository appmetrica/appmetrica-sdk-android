package io.appmetrica.analytics.impl.utils;

import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Random;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class LongRandomTest extends CommonTest {

    @Mock
    private Random mRandom;

    private LongRandom mLongRandom;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        mLongRandom = new LongRandom(mRandom);
    }

    @Test
    public void testDefaultConstructor() {
        assertThat(mLongRandom.getRandom()).isNotNull();
    }

    @Test
    public void testNextValueForMinGreaterThanMax() {
        assertThatThrownBy(
                new ThrowableAssert.ThrowingCallable() {
                    @Override
                    public void call() throws Throwable {
                        mLongRandom.nextValue(100L, 10L);
                    }
                }
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("min should be less than max");
    }

    @Test
    public void testNextValueForMinEqualMax() {
        assertThatThrownBy(
                new ThrowableAssert.ThrowingCallable() {
                    @Override
                    public void call() throws Throwable {
                        mLongRandom.nextValue(100L, 100L);
                    }
                }
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("min should be less than max");
    }
}
