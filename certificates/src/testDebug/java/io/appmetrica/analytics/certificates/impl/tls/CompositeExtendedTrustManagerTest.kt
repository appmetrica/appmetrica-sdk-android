package io.appmetrica.analytics.certificates.impl.tls

import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.net.Socket
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLEngine
import javax.net.ssl.X509ExtendedTrustManager
import javax.net.ssl.X509TrustManager

internal class CompositeExtendedTrustManagerTest : CommonTest() {

    private val chain = arrayOf(mock<X509Certificate>())
    private val socket = mock<Socket>()
    private val engine = mock<SSLEngine>()

    @Test
    fun `uses first successful delegate for socket and engine checks`() {
        val rejecting: X509ExtendedTrustManager = mock {
            on { checkServerTrusted(anyOrNull(), anyOrNull(), anyOrNull<Socket>()) } doThrow
                CertificateException("system")
            on { checkServerTrusted(anyOrNull(), anyOrNull(), anyOrNull<SSLEngine>()) } doThrow
                CertificateException("system")
            on { checkClientTrusted(anyOrNull(), anyOrNull(), anyOrNull<Socket>()) } doThrow
                CertificateException("system")
            on { checkClientTrusted(anyOrNull(), anyOrNull(), anyOrNull<SSLEngine>()) } doThrow
                CertificateException("system")
            on { acceptedIssuers } doReturn emptyArray()
        }
        val accepting: X509ExtendedTrustManager = mock {
            on { acceptedIssuers } doReturn emptyArray()
        }
        val underTest = CompositeExtendedTrustManager(listOf(rejecting, accepting))

        underTest.checkServerTrusted(chain, "RSA", socket)
        underTest.checkServerTrusted(chain, "RSA", engine)
        underTest.checkClientTrusted(chain, "RSA", socket)
        underTest.checkClientTrusted(chain, "RSA", engine)

        verify(accepting).checkServerTrusted(chain, "RSA", socket)
        verify(accepting).checkServerTrusted(chain, "RSA", engine)
        verify(accepting).checkClientTrusted(chain, "RSA", socket)
        verify(accepting).checkClientTrusted(chain, "RSA", engine)
    }

    @Test
    fun `falls back to plain trust manager host checks and fails when no delegates`() {
        val plain: X509TrustManager = mock {
            on { acceptedIssuers } doReturn emptyArray()
        }
        val underTest = CompositeExtendedTrustManager(listOf(plain))
        underTest.checkServerTrusted(chain, "RSA", socket)
        underTest.checkServerTrusted(chain, "RSA")
        underTest.checkClientTrusted(chain, "RSA")
        assertThat(underTest.checkServerTrusted(chain, "RSA", "example.com")).containsExactly(*chain)
        assertThat(underTest.isSameTrustConfiguration("a", "b")).isTrue()
        assertThat(underTest.acceptedIssuers).isEmpty()
        verify(plain, times(3)).checkServerTrusted(chain, "RSA")

        assertThatThrownBy { CompositeExtendedTrustManager(emptyList()).checkServerTrusted(chain, "RSA") }
            .isInstanceOf(CertificateException::class.java)
            .hasMessage("No trust managers to check server certificate")
        assertThatThrownBy {
            CompositeExtendedTrustManager(emptyList()).checkServerTrusted(chain, "RSA", "host")
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
            CompositeExtendedTrustManager(listOf(firstFail, secondFail))
                .checkServerTrusted(chain, "RSA", "host")
        }.isInstanceOf(CertificateException::class.java)
            .hasCause(secondError)
            .satisfies({ thrown ->
                assertThat(thrown.suppressed).containsExactly(firstError)
                assertThat(firstError.suppressed).isEmpty()
                assertThat(secondError.suppressed).isEmpty()
            })
    }
}
