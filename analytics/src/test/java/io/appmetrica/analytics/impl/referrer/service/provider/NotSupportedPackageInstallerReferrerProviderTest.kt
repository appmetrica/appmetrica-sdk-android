package io.appmetrica.analytics.impl.referrer.service.provider

import io.appmetrica.analytics.impl.referrer.service.ReferrerListener
import io.appmetrica.analytics.impl.referrer.service.ReferrerResult
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@RunWith(Parameterized::class)
internal class NotSupportedPackageInstallerReferrerProviderTest(
    private val packageInstaller: String?,
    private val expectedReferrerName: String,
) : CommonTest() {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: packageInstaller={0}")
        fun data(): Collection<Array<Any?>> = listOf(
            arrayOf("com.unknown.store", "com.unknown.store"),
            arrayOf(null, "<unknown-package-installer>"),
        )
    }

    private val provider by setUp { NotSupportedPackageInstallerReferrerProvider(packageInstaller) }

    @Test
    fun `referrerName returns correct value`() {
        assertThat(provider.referrerName).isEqualTo(expectedReferrerName)
    }

    @Test
    fun `requestReferrer calls listener with Failure result`() {
        val listener: ReferrerListener = mock()

        provider.requestReferrer(listener)

        val resultCaptor = argumentCaptor<ReferrerResult>()
        verify(listener).onResult(resultCaptor.capture())

        assertThat(resultCaptor.firstValue).isInstanceOf(ReferrerResult.Failure::class.java)
        val failure = resultCaptor.firstValue as ReferrerResult.Failure
        assertThat(failure.message).isEqualTo("Package installer $packageInstaller is not supported")
    }
}
