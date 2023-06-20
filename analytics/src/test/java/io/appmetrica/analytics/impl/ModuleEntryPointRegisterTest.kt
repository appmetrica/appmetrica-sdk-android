package io.appmetrica.analytics.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class ModuleEntryPointRegisterTest {

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
        moduleEntryPointsRegister.register(first, second)
        assertThat(moduleEntryPointsRegister.classNames).containsExactlyInAnyOrder(first, second)
    }
}
