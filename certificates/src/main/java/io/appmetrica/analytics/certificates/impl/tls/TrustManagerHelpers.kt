package io.appmetrica.analytics.certificates.impl.tls

import java.lang.reflect.InvocationTargetException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

/**
 * Shared helpers for ordered trust-manager fallback and host-aware checks.
 */
internal object TrustManagerHelpers {

    @Throws(CertificateException::class)
    inline fun <T> checkTrusted(
        delegates: List<X509TrustManager>,
        noDelegatesMessage: String,
        check: (X509TrustManager) -> T,
    ): T {
        val errors = ArrayList<CertificateException>()
        for (delegate in delegates) {
            try {
                return check(delegate)
            } catch (e: CertificateException) {
                errors.add(e)
            }
        }
        if (errors.isEmpty()) {
            throw CertificateException(noDelegatesMessage)
        }
        val lastError = errors.last()
        val result = CertificateException(lastError.message)
        result.initCause(lastError)
        for (index in 0 until errors.lastIndex) {
            val previous = errors[index]
            if (previous !== lastError) {
                result.addSuppressed(previous)
            }
        }
        throw result
    }

    @Throws(CertificateException::class)
    fun checkServerTrustedForHost(
        trustManager: X509TrustManager,
        chain: Array<out X509Certificate>,
        authType: String,
        host: String,
    ): List<X509Certificate> {
        val method = try {
            trustManager::class.java.getDeclaredMethod(
                "checkServerTrusted",
                Array<X509Certificate>::class.java,
                String::class.java,
                String::class.java
            ).apply { isAccessible = true }
        } catch (_: Exception) {
            null
        } ?: run {
            trustManager.checkServerTrusted(chain, authType)
            return chain.asList()
        }
        return try {
            @Suppress("UNCHECKED_CAST")
            method.invoke(trustManager, chain, authType, host) as List<X509Certificate>
        } catch (e: InvocationTargetException) {
            throw (e.cause as? CertificateException)
                ?: CertificateException("checkServerTrusted(host) failed", e.cause)
        } catch (e: IllegalAccessException) {
            throw CertificateException("checkServerTrusted(host) failed", e)
        }
    }

    fun isSameTrustConfigurationOrTrue(
        trustManager: X509TrustManager,
        hostname1: String?,
        hostname2: String?,
    ): Boolean {
        val method = try {
            trustManager::class.java.getDeclaredMethod(
                "isSameTrustConfiguration",
                String::class.java,
                String::class.java
            ).apply { isAccessible = true }
        } catch (_: Exception) {
            null
        } ?: return true
        return try {
            method.invoke(trustManager, hostname1, hostname2) as Boolean
        } catch (_: Exception) {
            true
        }
    }
}
