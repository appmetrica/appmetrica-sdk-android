package io.appmetrica.analytics.coreutils.internal.parsing

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ParseUtilsTest : CommonTest() {

    @Test
    fun intValueOfForNull() {
        assertThat(ParseUtils.intValueOf(null)).isNull()
    }

    @Test
    fun intValueOf() {
        val value = 12312
        assertThat(ParseUtils.intValueOf(value.toString())).isEqualTo(value)
    }

    @Test
    fun intValueOfForWrongString() {
        assertThat(ParseUtils.intValueOf("wrong string")).isNull()
    }
}
