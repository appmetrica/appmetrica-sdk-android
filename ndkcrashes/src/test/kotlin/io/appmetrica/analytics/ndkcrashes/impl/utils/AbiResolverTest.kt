package io.appmetrica.analytics.ndkcrashes.impl.utils

import android.os.Build
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.util.ReflectionHelpers

@RunWith(RobolectricTestRunner::class)
class AbiResolverTest : CommonTest() {
    companion object {
        private const val ABI_32 = "32-bit-abi"
        private const val ABI_64 = "64-bit-abi"
        private const val ABI_OTHER = "other-abi"
    }

    @Before
    fun setUp() {
        mockAbis()
    }

    @Test
    fun `getAbi with empty supported`() {
        assertThat(AbiResolver(emptySet()).getAbi()).isNull()
    }

    @Test
    fun `getAbi with 32 supported`() {
        assertThat(AbiResolver(setOf(ABI_32, ABI_OTHER)).getAbi()).isEqualTo(ABI_32)
    }

    @Test
    fun `getAbi with 64 supported`() {
        assertThat(AbiResolver(setOf(ABI_OTHER, ABI_64)).getAbi()).isEqualTo(ABI_64)
    }

    @Test
    fun `getAbi all supported`() {
        assertThat(AbiResolver(setOf(ABI_32, ABI_64, ABI_OTHER)).getAbi()).isEqualTo(ABI_64)
    }

    @Test
    fun `getAbi not supported`() {
        assertThat(AbiResolver(setOf(ABI_OTHER)).getAbi()).isNull()
    }

    @Test
    fun `default abis`() {
        for (abi in listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")) {
            mockAbis(abi, abi)
            assertThat(AbiResolver.getAbi()).isEqualTo(abi)
        }
    }

    private fun mockAbis(abi32: String = ABI_32, abi64: String = ABI_64) {
        ReflectionHelpers.setStaticField(Build::class.java, "SUPPORTED_32_BIT_ABIS", arrayOf(abi32))
        ReflectionHelpers.setStaticField(Build::class.java, "SUPPORTED_64_BIT_ABIS", arrayOf(abi64))
    }
}
