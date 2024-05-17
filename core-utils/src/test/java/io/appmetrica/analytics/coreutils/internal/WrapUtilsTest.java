package io.appmetrica.analytics.coreutils.internal;

import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class WrapUtilsTest extends CommonTest {

    @Test
    public void testDefaultNull() {
        assertThat(WrapUtils.getOrDefaultNullable((Object) null, null)).isNull();
    }

    @Test
    public void testDefaultNonNull() {
        int actual = 10;
        assertThat(WrapUtils.getOrDefaultNullable(null, actual)).isEqualTo(actual);
    }

    @Test
    public void testValueNonNull() {
        int actual = 100;
        assertThat(WrapUtils.getOrDefaultNullable(actual, 10)).isEqualTo(actual);
    }

    @Test
    public void testDefaultBoolean() {
        boolean actual = true;
        assertThat(WrapUtils.getOrDefault(null, actual)).isEqualTo(actual);
    }

    @Test
    public void testNonNullBoolean() {
        boolean actual = false;
        assertThat(WrapUtils.getOrDefault(actual, true)).isEqualTo(actual);
    }

    @Test
    public void testDefaultInt() {
        int actual = 10;
        assertThat(WrapUtils.getOrDefault((Integer) null, actual)).isEqualTo(actual);
    }

    @Test
    public void testNonNullInt() {
        int actual = 20;
        assertThat(WrapUtils.getOrDefault(actual, 10)).isEqualTo(actual);
    }

    @Test
    public void testDefaultLong() {
        int actual = 10;
        assertThat(WrapUtils.getOrDefault((Long) null, actual)).isEqualTo(actual);
    }

    @Test
    public void testNonNullLong() {
        long actual = 5L;
        assertThat(WrapUtils.getOrDefault(actual, 10)).isEqualTo(actual);
    }

    @Test
    public void testDefaultString() {
        String actual = "actual";
        assertThat(WrapUtils.getOrDefault(null, actual)).isEqualTo(actual);
    }

    @Test
    public void testNonNullString() {
        String actual = "actual";
        assertThat(WrapUtils.getOrDefault(actual, "default")).isEqualTo(actual);
    }

    @Test
    public void getOrDefaultNullableIfEmptyForDefaultNull() {
        assertThat(WrapUtils.getOrDefaultNullableIfEmpty("test", null)).isEqualTo("test");
    }

    @Test
    public void getOrDefaultNullableIfEmptyForDefaultEmpty() {
        assertThat(WrapUtils.getOrDefaultNullableIfEmpty("test", "")).isEqualTo("test");
    }

    @Test
    public void getOrDefaultNullableIfEmptyForDefaultNonEmpty() {
        assertThat(WrapUtils.getOrDefaultNullableIfEmpty("test", "default")).isEqualTo("test");
    }

    @Test
    public void getOrDefaultNullableIfEmptyForNullAndDefaultNull() {
        assertThat(WrapUtils.getOrDefaultNullableIfEmpty(null, null)).isNull();
    }

    @Test
    public void getOrDefaultIfEmptyForNullAndDefaultEmpty() {
        assertThat(WrapUtils.getOrDefaultNullableIfEmpty(null, "")).isEqualTo("");
    }

    @Test
    public void getOrDefaultIfEmptyForNullAndDefaultNonNull() {
        assertThat(WrapUtils.getOrDefaultNullableIfEmpty(null, "default")).isEqualTo("default");
    }

    @Test
    public void getOrDefaultIfEmptyForEmptyAndDefaultNull() {
        assertThat(WrapUtils.getOrDefaultNullableIfEmpty("", null)).isNull();
    }

    @Test
    public void getOrDefaultIfEmptyForEmptyAndDefaultEmpty() {
        assertThat(WrapUtils.getOrDefaultNullableIfEmpty("", "")).isEqualTo("");
    }

    @Test
    public void getOrDefaultIfEmptyForEmptyAndDefaultNonEmpty() {
        assertThat(WrapUtils.getOrDefaultNullableIfEmpty("", "default")).isEqualTo("default");
    }

    @Test
    public void wrapToTag() {
        assertThat(WrapUtils.wrapToTag("some_string")).isEqualTo("some_string");
    }

    @Test
    public void wrapToTagIfNull() {
        assertThat(WrapUtils.wrapToTag(null)).isEqualTo("<null>");
    }

    @Test
    public void wrapToTagIfEmpty() {
        assertThat(WrapUtils.wrapToTag("")).isEqualTo("<empty>");
    }

    @RunWith(ParameterizedRobolectricTestRunner.class)
    public static class GetMillisOrDefaultTest {

        private Long mInput;
        private TimeUnit mTimeUnit;
        private long mDefValue;
        private long mExpectedValue;

        public GetMillisOrDefaultTest(final Long input,
                                      final TimeUnit timeUnit,
                                      final long defValue,
                                      final long expectedValue) {
            mInput = input;
            mTimeUnit = timeUnit;
            this.mDefValue = defValue;
            mExpectedValue = expectedValue;
        }

        @ParameterizedRobolectricTestRunner.Parameters()
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {100L, TimeUnit.MILLISECONDS, 200L, 100L},
                    {null, TimeUnit.SECONDS, 200L, 200L},
                    {100L, TimeUnit.SECONDS, 200L, 100000L}
            });
        }

        @Test
        public void test() {
            assertThat(WrapUtils.getMillisOrDefault(mInput, mTimeUnit, mDefValue)).isEqualTo(mExpectedValue);
        }
    }
}
