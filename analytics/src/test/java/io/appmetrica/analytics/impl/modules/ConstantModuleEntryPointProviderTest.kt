package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class ConstantModuleEntryPointProviderTest : CommonTest() {

    private val value = "some value"

    private val constantModuleEntryPointProvider by setUp { ConstantModuleEntryPointProvider(value) }

    @Test
    fun className() {
        assertThat(constantModuleEntryPointProvider.className).isEqualTo(value)
    }
}
