package io.appmetrica.analytics.certificates.impl.tls

import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

internal class CompositeTrustManagerTest : CommonTest() {

    private val chain = arrayOf(mock<X509Certificate>())
    private val firstIssuer = mock<X509Certificate>()
    private val secondIssuer = mock<X509Certificate>()

    @Test
    fun `accepts first successful delegate and falls back then aggregates issuers`() {
        val first: X509TrustManager = mock {
            on { checkServerTrusted(anyOrNull(), anyOrNull()) } doThrow CertificateException("system")
            on { checkClientTrusted(anyOrNull(), anyOrNull()) } doThrow CertificateException("system")
            on { acceptedIssuers } doReturn arrayOf(firstIssuer)
        }
        val second: X509TrustManager = mock {
            on { acceptedIssuers } doReturn arrayOf(secondIssuer)
        }
        val underTest = CompositeTrustManager(listOf(first, second))

        underTest.checkServerTrusted(chain, "RSA")
        underTest.checkClientTrusted(chain, "RSA")

        verify(first).checkServerTrusted(chain, "RSA")
        verify(second).checkServerTrusted(chain, "RSA")
        verify(first).checkClientTrusted(chain, "RSA")
        verify(second).checkClientTrusted(chain, "RSA")
        assertThat(underTest.acceptedIssuers).containsExactly(firstIssuer, secondIssuer)

        val unused: X509TrustManager = mock()
        val acceptedFirst: X509TrustManager = mock {
            on { acceptedIssuers } doReturn emptyArray()
        }
        CompositeTrustManager(listOf(acceptedFirst, unused)).checkServerTrusted(chain, "RSA")
        verify(unused, never()).checkServerTrusted(chain, "RSA")
    }

    @Test
    fun `rethrows last error and fails when no delegates`() {
        val first: X509TrustManager = mock {
            on { checkServerTrusted(anyOrNull(), anyOrNull()) } doThrow CertificateException("first")
        }
        val second: X509TrustManager = mock {
            on { checkServerTrusted(anyOrNull(), anyOrNull()) } doThrow CertificateException("second")
        }

        assertThatThrownBy { CompositeTrustManager(listOf(first, second)).checkServerTrusted(chain, "RSA") }
            .isInstanceOf(CertificateException::class.java)
            .hasMessage("second")
        assertThatThrownBy { CompositeTrustManager(emptyList()).checkServerTrusted(chain, "RSA") }
            .isInstanceOf(CertificateException::class.java)
            .hasMessage("No trust managers to check server certificate")
    }

    @Test
    fun `host aware check falls back across delegates and treats missing config as same`() {
        val rejecting: X509TrustManager = mock {
            on { checkServerTrusted(anyOrNull(), anyOrNull()) } doThrow CertificateException("system")
        }
        val accepting: X509TrustManager = mock()
        val underTest = CompositeTrustManager(listOf(rejecting, accepting))

        assertThat(underTest.checkServerTrusted(chain, "RSA", "example.com")).containsExactly(*chain)
        verify(rejecting).checkServerTrusted(chain, "RSA")
        verify(accepting).checkServerTrusted(chain, "RSA")
        assertThat(underTest.isSameTrustConfiguration("a.example.com", "b.example.com")).isTrue()

        assertThatThrownBy {
            CompositeTrustManager(listOf(rejecting)).checkServerTrusted(chain, "RSA", "host")
        }.isInstanceOf(CertificateException::class.java).hasMessage("system")
        assertThatThrownBy {
            CompositeTrustManager(emptyList()).checkServerTrusted(chain, "RSA", "host")
        }.isInstanceOf(CertificateException::class.java)
            .hasMessage("No trust managers to check server certificate")

        val firstError = CertificateException("first-host")
        val secondError = CertificateException("second-host")
        val firstFail: X509TrustManager = mock {
            on { checkServerTrusted(anyOrNull(), anyOrNull()) } doThrow firstError
        }
        val secondFail: X509TrustManager = mock {
            on { checkServerTrusted(anyOrNull(), anyOrNull()) } doThrow secondError
        }
        assertThatThrownBy {
            CompositeTrustManager(listOf(firstFail, secondFail)).checkServerTrusted(chain, "RSA", "host")
        }.isInstanceOf(CertificateException::class.java)
            .hasCause(secondError)
            .satisfies({ thrown ->
                assertThat(thrown.suppressed).containsExactly(firstError)
                assertThat(firstError.suppressed).isEmpty()
                assertThat(secondError.suppressed).isEmpty()
            })
    }
}
