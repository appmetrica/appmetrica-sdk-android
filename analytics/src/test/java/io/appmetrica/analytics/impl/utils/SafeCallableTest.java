package io.appmetrica.analytics.impl.utils;

import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SafeCallableTest extends CommonTest {

    private SafeCallable<Integer> mSafeCallable;
    private final Integer mExpected = 42;

    @Test
    public void testSuccess() {
        mSafeCallable = new SafeCallable<Integer>() {
            @Override
            public Integer callSafely() throws Exception {
                return mExpected;
            }
        };
        assertThat(mSafeCallable.call()).isEqualTo(mExpected);
    }

    @Test
    public void testException() {
        mSafeCallable = new SafeCallable<Integer>() {
            @Override
            public Integer callSafely() throws Exception {
                throw new Exception();
            }
        };
        assertThat(mSafeCallable.call()).isNull();
    }
}
