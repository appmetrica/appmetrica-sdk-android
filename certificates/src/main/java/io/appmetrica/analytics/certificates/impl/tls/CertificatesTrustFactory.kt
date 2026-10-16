package io.appmetrica.analytics.certificates.impl.tls

import android.content.Context
import android.os.Build
import io.appmetrica.analytics.certificates.R
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.io.ByteArrayInputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

internal object CertificatesTrustFactory {

    private const val tag = "[CertificatesTrustFactory]"

    fun createSocketFactory(trustManager: X509TrustManager): SSLSocketFactory {
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf(trustManager), null)
        return sslContext.socketFactory
    }

    fun createSystemFirstModuleTrustManager(context: Context): X509TrustManager? {
        return try {
            val systemTrustManager = defaultTrustManager() ?: return null
            val moduleTrustManager = trustManagerForModuleRoot(context) ?: return systemTrustManager
            val delegates = listOf(systemTrustManager, moduleTrustManager)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                CompositeExtendedTrustManager(delegates)
            } else {
                CompositeTrustManager(delegates)
            }
        } catch (throwable: Throwable) {
            DebugLogger.error(tag, throwable, "Failed to create certificates trust manager")
            null
        }
    }

    private fun trustManagerForModuleRoot(context: Context): X509TrustManager? {
        val certificate = loadModuleCertificate(context) ?: return null
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
            load(null)
            setCertificateEntry("module_cert", certificate)
        }
        val factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        factory.init(keyStore)
        return factory.trustManagers.filterIsInstance<X509TrustManager>().firstOrNull()
    }

    private fun loadModuleCertificate(context: Context): X509Certificate? {
        return try {
            context.resources.openRawResource(R.raw.module_cert).use { inputStream ->
                CertificateFactory.getInstance("X.509")
                    .generateCertificate(ByteArrayInputStream(inputStream.readBytes())) as X509Certificate
            }
        } catch (throwable: Throwable) {
            DebugLogger.error(tag, throwable, "Failed to load module root certificate")
            null
        }
    }

    private fun defaultTrustManager(): X509TrustManager? {
        return try {
            val factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            factory.init(null as KeyStore?)
            factory.trustManagers.filterIsInstance<X509TrustManager>().firstOrNull()
        } catch (throwable: Throwable) {
            DebugLogger.error(tag, throwable, "Failed to create system trust manager")
            null
        }
    }
}
