package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class ModuleEntryPointRegisterTest : CommonTest() {

    private lateinit var moduleEntryPointsRegister: ModuleEntryPointsRegister

    @Before
    fun setUp() {
        moduleEntryPointsRegister = ModuleEntryPointsRegister()
    }

    @Test
    fun `moduleEntryPoints for empty`() {
        assertThat(moduleEntryPointsRegister.classNames).isEmpty()
    }

    @Test
    fun `moduleEntryPoints after registration`() {
        val first = "first"
        val second = "second"
        moduleEntryPointsRegister.register(
            ConstantModuleEntryPointProvider(first),
            ConstantModuleEntryPointProvider(second)
        )
        assertThat(moduleEntryPointsRegister.classNames).containsExactly(first, second)
    }
}
