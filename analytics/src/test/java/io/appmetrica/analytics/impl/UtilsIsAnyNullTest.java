package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class UtilsIsAnyNullTest extends CommonTest {

    private Object[] mInputValue;
    private boolean mExpectedValue;

    public UtilsIsAnyNullTest(Object[] inputValue, boolean expectedValue) {
        mInputValue = inputValue;
        mExpectedValue = expectedValue;
    }

    @ParameterizedRobolectricTestRunner.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {null, false},
                {new Object[]{}, false},
                {new Object[]{null}, true},
                {new Object[]{new Object()}, false},
                {new Object[]{null, null}, true},
                {new Object[]{null, new Object()}, true},
                {new Object[]{new Object(), null}, true},
                {new Object[]{new Object(), new Object()}, false}
        });
    }

    @Test
    public void testIsAnyNull() {
        assertThat(Utils.isAnyNull(mInputValue)).isEqualTo(mExpectedValue);
    }
}
