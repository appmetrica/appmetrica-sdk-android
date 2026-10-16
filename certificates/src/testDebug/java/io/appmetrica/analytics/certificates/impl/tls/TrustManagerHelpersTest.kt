package io.appmetrica.analytics.certificates.impl.tls

import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

internal class TrustManagerHelpersTest : CommonTest() {

    private val chain = arrayOf(mock<X509Certificate>())

    @Test
    fun `checkTrusted falls back then aggregates errors without mutating delegates`() {
        val firstError = CertificateException("first")
        val secondError = CertificateException("second")
        val first: X509TrustManager = mock {
            on { checkServerTrusted(anyOrNull(), anyOrNull()) } doThrow firstError
        }
        val second: X509TrustManager = mock {
            on { checkServerTrusted(anyOrNull(), anyOrNull()) } doThrow secondError
        }
        val success: X509TrustManager = mock()

        TrustManagerHelpers.checkTrusted(listOf(first, success), "empty") {
            it.checkServerTrusted(chain, "RSA")
        }
        verify(success).checkServerTrusted(chain, "RSA")

        repeat(2) {
            assertThatThrownBy {
                TrustManagerHelpers.checkTrusted(listOf(first, second), "empty") {
                    it.checkServerTrusted(chain, "RSA")
                }
            }.isInstanceOf(CertificateException::class.java)
                .hasCause(secondError)
                .satisfies({ thrown ->
                    assertThat(thrown.suppressed).containsExactly(firstError)
                    assertThat(firstError.suppressed).isEmpty()
                    assertThat(secondError.suppressed).isEmpty()
                    assertThat(firstError.cause).isNull()
                    assertThat(secondError.cause).isNull()
                })
        }

        val reused = CertificateException("reused")
        val reusedDelegate: X509TrustManager = mock {
            on { checkServerTrusted(anyOrNull(), anyOrNull()) } doThrow reused
        }
        assertThatThrownBy {
            TrustManagerHelpers.checkTrusted(listOf(reusedDelegate, reusedDelegate), "empty") {
                it.checkServerTrusted(chain, "RSA")
            }
        }.isInstanceOf(CertificateException::class.java)
            .hasCause(reused)
            .satisfies({ thrown ->
                assertThat(thrown.suppressed).isEmpty()
                assertThat(reused.suppressed).isEmpty()
            })

        assertThatThrownBy {
            TrustManagerHelpers.checkTrusted(emptyList(), "empty") { }
        }.isInstanceOf(CertificateException::class.java).hasMessage("empty")

        val hostFirst = CertificateException("host-first")
        val hostSecond = CertificateException("host-second")
        val hostRejectFirst: X509TrustManager = mock {
            on { checkServerTrusted(anyOrNull(), anyOrNull()) } doThrow hostFirst
        }
        val hostRejectSecond: X509TrustManager = mock {
            on { checkServerTrusted(anyOrNull(), anyOrNull()) } doThrow hostSecond
        }
        assertThatThrownBy {
            TrustManagerHelpers.checkTrusted(listOf(hostRejectFirst, hostRejectSecond), "empty") {
                TrustManagerHelpers.checkServerTrustedForHost(it, chain, "RSA", "host")
            }
        }.isInstanceOf(CertificateException::class.java)
            .hasCause(hostSecond)
            .satisfies({ thrown ->
                assertThat(thrown.suppressed).containsExactly(hostFirst)
                assertThat(hostFirst.suppressed).isEmpty()
                assertThat(hostSecond.suppressed).isEmpty()
            })
    }

    @Test
    fun `host aware checks use reflection when present and fall back otherwise`() {
        val plain: X509TrustManager = mock()
        assertThat(TrustManagerHelpers.checkServerTrustedForHost(plain, chain, "RSA", "example.com"))
            .containsExactly(*chain)
        verify(plain).checkServerTrusted(chain, "RSA")
        assertThat(TrustManagerHelpers.isSameTrustConfigurationOrTrue(plain, "a", "b")).isTrue()

        val hostAware = HostAwareTrustManager(accepted = chain.asList())
        assertThat(TrustManagerHelpers.checkServerTrustedForHost(hostAware, chain, "RSA", "host"))
            .isEqualTo(chain.asList())
        assertThat(TrustManagerHelpers.isSameTrustConfigurationOrTrue(hostAware, "a", "b")).isFalse()

        val failing = HostAwareTrustManager(error = CertificateException("host fail"))
        assertThatThrownBy {
            TrustManagerHelpers.checkServerTrustedForHost(failing, chain, "RSA", "host")
        }.isInstanceOf(CertificateException::class.java).hasMessage("host fail")
    }

    @Test
    fun `host aware composites return the accepted chain from the successful delegate`() {
        val acceptedChain = listOf(mock<X509Certificate>(), mock<X509Certificate>())
        val delegates = listOf(
            HostAwareTrustManager(error = CertificateException("first")),
            HostAwareTrustManager(accepted = acceptedChain),
        )

        assertThat(CompositeTrustManager(delegates).checkServerTrusted(chain, "RSA", "host"))
            .isSameAs(acceptedChain)
        assertThat(CompositeExtendedTrustManager(delegates).checkServerTrusted(chain, "RSA", "host"))
            .isSameAs(acceptedChain)
    }

    @Suppress("CustomX509TrustManager")
    private class HostAwareTrustManager(
        private val accepted: List<X509Certificate> = emptyList(),
        private val error: CertificateException? = null,
    ) : X509TrustManager {

        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit

        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            error?.let { throw it }
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()

        @Suppress("unused")
        fun checkServerTrusted(
            chain: Array<out X509Certificate>,
            authType: String,
            host: String,
        ): List<X509Certificate> {
            error?.let { throw it }
            return accepted.ifEmpty { chain.asList() }
        }

        @Suppress("unused")
        fun isSameTrustConfiguration(hostname1: String?, hostname2: String?): Boolean = false
    }
}
