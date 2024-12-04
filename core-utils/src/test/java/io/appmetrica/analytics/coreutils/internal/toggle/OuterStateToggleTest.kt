package io.appmetrica.analytics.coreutils.internal.toggle

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class OuterStateToggleTest : CommonTest() {

    @Test
    fun initialState() {
        val outerStateToggle = OuterStateToggle(true, "tag")
        assertThat(outerStateToggle.actualState).isTrue()
    }

    @Test
    fun update() {
        val outerStateToggle = OuterStateToggle(true, "tag")
        outerStateToggle.update(false)
        assertThat(outerStateToggle.actualState).isFalse()
    }
}
