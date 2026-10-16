package io.appmetrica.analytics.certificates.impl.tls

import android.content.Context
import android.content.res.Resources
import android.os.Build
import io.appmetrica.analytics.certificates.R
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.isNull
import org.mockito.Mockito
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileNotFoundException
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

@Suppress("RobolectricUsage")
@RunWith(RobolectricTestRunner::class)
internal class CertificatesTrustFactoryTest : CommonTest() {

    @Test
    fun `creates socket factory for given trust manager via SSLContext`() {
        val trustManager = mock<X509TrustManager>()
        val expectedFactory = mock<SSLSocketFactory>()
        val sslContext = mock<SSLContext> {
            on { socketFactory } doReturn expectedFactory
        }
        Mockito.mockStatic(SSLContext::class.java).use {
            whenever(SSLContext.getInstance("TLS")).thenReturn(sslContext)

            val socketFactory = CertificatesTrustFactory.createSocketFactory(trustManager)

            assertThat(socketFactory).isSameAs(expectedFactory)
            verify(sslContext).init(isNull(), eq(arrayOf(trustManager)), isNull())
        }
    }

    @Test
    fun `returns the original system manager when module root is missing or invalid`() {
        val systemTm = mock<X509TrustManager>()
        val systemFactory = mock<TrustManagerFactory> {
            on { trustManagers } doReturn arrayOf(systemTm)
        }
        val missingRoot: Resources = mock {
            on { openRawResource(anyInt()) } doThrow RuntimeException("missing module root")
        }
        val invalidRoot: Resources = mock {
            on { openRawResource(anyInt()) } doReturn ByteArrayInputStream(byteArrayOf(1, 2, 3))
        }

        Mockito.mockStatic(TrustManagerFactory::class.java).use {
            whenever(TrustManagerFactory.getDefaultAlgorithm()).thenReturn("PKIX")
            whenever(TrustManagerFactory.getInstance("PKIX")).thenReturn(systemFactory)

            listOf(missingRoot, invalidRoot).forEach { resources ->
                val context: Context = mock {
                    on { this.resources } doReturn resources
                }

                assertThat(CertificatesTrustFactory.createSystemFirstModuleTrustManager(context))
                    .isSameAs(systemTm)
            }
        }
    }

    @Test
    fun `returns null when system trust factory fails or has no x509 manager`() {
        Mockito.mockStatic(TrustManagerFactory::class.java).use {
            whenever(TrustManagerFactory.getDefaultAlgorithm()).thenReturn("PKIX")
            whenever(TrustManagerFactory.getInstance("PKIX")).thenThrow(RuntimeException("factory failed"))

            assertThat(
                CertificatesTrustFactory.createSystemFirstModuleTrustManager(contextWithModuleRoot())
            ).isNull()
        }

        val emptyFactory = mock<TrustManagerFactory> {
            on { trustManagers } doReturn emptyArray()
        }
        Mockito.mockStatic(TrustManagerFactory::class.java).use {
            whenever(TrustManagerFactory.getDefaultAlgorithm()).thenReturn("PKIX")
            whenever(TrustManagerFactory.getInstance("PKIX")).thenReturn(emptyFactory)

            assertThat(
                CertificatesTrustFactory.createSystemFirstModuleTrustManager(contextWithModuleRoot())
            ).isNull()
        }

        val nonX509Factory = mock<TrustManagerFactory> {
            on { trustManagers } doReturn arrayOf(mock<TrustManager>())
        }
        Mockito.mockStatic(TrustManagerFactory::class.java).use {
            whenever(TrustManagerFactory.getDefaultAlgorithm()).thenReturn("PKIX")
            whenever(TrustManagerFactory.getInstance("PKIX")).thenReturn(nonX509Factory)

            assertThat(
                CertificatesTrustFactory.createSystemFirstModuleTrustManager(contextWithModuleRoot())
            ).isNull()
        }
    }

    @Test
    fun `returns null when module trust factory fails after root is loaded`() {
        val systemTm = mock<X509TrustManager>()
        val systemFactory = mock<TrustManagerFactory> {
            on { trustManagers } doReturn arrayOf(systemTm)
        }
        var calls = 0
        Mockito.mockStatic(TrustManagerFactory::class.java).use {
            whenever(TrustManagerFactory.getDefaultAlgorithm()).thenReturn("PKIX")
            whenever(TrustManagerFactory.getInstance("PKIX")).thenAnswer {
                when (calls++) {
                    0 -> systemFactory
                    else -> throw IllegalStateException("module factory failed")
                }
            }

            assertThat(
                CertificatesTrustFactory.createSystemFirstModuleTrustManager(contextWithModuleRoot())
            ).isNull()
        }
    }

    @Config(sdk = [Build.VERSION_CODES.M])
    @Test
    fun `creates CompositeTrustManager on api 23 with valid root`() {
        val created = CertificatesTrustFactory.createSystemFirstModuleTrustManager(contextWithModuleRoot())
        assertThat(created).isInstanceOf(CompositeTrustManager::class.java)
        assertThat((created as CompositeTrustManager).acceptedIssuers).isNotEmpty()
    }

    @Config(sdk = [Build.VERSION_CODES.N])
    @Test
    fun `creates CompositeExtendedTrustManager on api 24 with valid root`() {
        val created = CertificatesTrustFactory.createSystemFirstModuleTrustManager(contextWithModuleRoot())
        assertThat(created).isInstanceOf(CompositeExtendedTrustManager::class.java)
        assertThat((created as CompositeExtendedTrustManager).acceptedIssuers).isNotEmpty()
    }

    @Config(sdk = [Build.VERSION_CODES.M])
    @Test
    fun `api 23 composite checks system first then module`() {
        assertCompositeDelegateOrder(expectExtended = false)
    }

    @Config(sdk = [Build.VERSION_CODES.N])
    @Test
    fun `api 24 composite checks system first then module`() {
        assertCompositeDelegateOrder(expectExtended = true)
    }

    private fun assertCompositeDelegateOrder(expectExtended: Boolean) {
        val systemTm = mock<X509TrustManager>()
        val moduleTm = mock<X509TrustManager>()
        val systemFactory = mock<TrustManagerFactory> {
            on { trustManagers } doReturn arrayOf(systemTm)
        }
        val moduleFactory = mock<TrustManagerFactory> {
            on { trustManagers } doReturn arrayOf(moduleTm)
        }
        var calls = 0
        Mockito.mockStatic(TrustManagerFactory::class.java).use {
            whenever(TrustManagerFactory.getDefaultAlgorithm()).thenReturn("PKIX")
            whenever(TrustManagerFactory.getInstance("PKIX")).thenAnswer {
                when (calls++) {
                    0 -> systemFactory
                    else -> moduleFactory
                }
            }

            val created = CertificatesTrustFactory.createSystemFirstModuleTrustManager(contextWithModuleRoot())
            if (expectExtended) {
                assertThat(created).isInstanceOf(CompositeExtendedTrustManager::class.java)
            } else {
                assertThat(created).isInstanceOf(CompositeTrustManager::class.java)
            }

            val chain = arrayOf(mock<X509Certificate>())
            created!!.checkServerTrusted(chain, "RSA")
            verify(systemTm).checkServerTrusted(chain, "RSA")
            verify(moduleTm, never()).checkServerTrusted(anyOrNull(), anyOrNull())

            doThrow(CertificateException("system")).whenever(systemTm)
                .checkServerTrusted(anyOrNull(), anyOrNull())
            created.checkServerTrusted(chain, "RSA")
            verify(moduleTm).checkServerTrusted(chain, "RSA")
        }
    }

    private fun contextWithModuleRoot(): Context {
        val certBytes = moduleCertBytes()
        val resources: Resources = mock {
            on { openRawResource(R.raw.module_cert) } doReturn ByteArrayInputStream(certBytes)
        }
        return mock {
            on { this.resources } doReturn resources
        }
    }

    private fun moduleCertBytes(): ByteArray {
        val candidates = listOf(
            File("src/main/res/raw/module_cert.crt"),
            File("certificates/src/main/res/raw/module_cert.crt"),
        )
        val file = candidates.firstOrNull { it.isFile }
            ?: throw FileNotFoundException("module_cert.crt not found near ${File(".").absolutePath}")
        CertificateFactory.getInstance("X.509")
            .generateCertificate(file.inputStream()) as X509Certificate
        return file.readBytes()
    }
}
