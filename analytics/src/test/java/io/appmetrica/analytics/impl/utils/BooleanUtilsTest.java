package io.appmetrica.analytics.impl.utils;

import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class BooleanUtilsTest extends CommonTest {

    private final Boolean mInputValue;
    private final boolean isTrue;
    private final boolean isFalse;

    public BooleanUtilsTest(Boolean inputValue, boolean isTrue, boolean isFalse) {
        mInputValue = inputValue;
        this.isTrue = isTrue;
        this.isFalse = isFalse;
    }

    @ParameterizedRobolectricTestRunner.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {true, true, false},
            {false, false, true},
            {null, false, false}
        });
    }

    @Test
    public void testIsTrue() {
        assertThat(BooleanUtils.isTrue(mInputValue)).isEqualTo(isTrue);
    }

    @Test
    public void testIsNotTrue() {
        assertThat(BooleanUtils.isNotTrue(mInputValue)).isEqualTo(!isTrue);
    }

    @Test
    public void testIsFalse() {
        assertThat(BooleanUtils.isFalse(mInputValue)).isEqualTo(isFalse);
    }

    @Test
    public void testIsNotFalse() {
        assertThat(BooleanUtils.isNotFalse(mInputValue)).isEqualTo(!isFalse);
    }
}
