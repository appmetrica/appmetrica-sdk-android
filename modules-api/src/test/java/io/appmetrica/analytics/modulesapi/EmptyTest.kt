package io.appmetrica.analytics.modulesapi

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EmptyTest : CommonTest() {

    @Test
    fun emptyTest() {
        assertThat(2).isEqualTo(2)
    }
}
