package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UtilsIsNullOrEmptyTest extends CommonTest {

    @Test
    public void isNullOrEmptyLongArrayForNull() {
        assertThat(Utils.isNullOrEmpty((long[]) null)).isTrue();
    }

    @Test
    public void isNullOrEmptyLongArrayForEmpty() {
        assertThat(Utils.isNullOrEmpty(new long[0])).isTrue();
    }

    @Test
    public void isNullOrEmptyLongArrayForFilledArray() {
        assertThat(Utils.isNullOrEmpty(new long[]{10L})).isFalse();
    }

    @Test
    public void isNullOrEmptyIntArrayForNull() {
        assertThat(Utils.isNullOrEmpty((int[]) null)).isTrue();
    }

    @Test
    public void isNullOrEmptyIntArrayForEmptyArray() {
        assertThat(Utils.isNullOrEmpty(new int[0])).isTrue();
    }

    @Test
    public void isNullOrEmptyIntArrayForFilledArray() {
        assertThat(Utils.isNullOrEmpty(new int[]{100500})).isFalse();
    }
}
