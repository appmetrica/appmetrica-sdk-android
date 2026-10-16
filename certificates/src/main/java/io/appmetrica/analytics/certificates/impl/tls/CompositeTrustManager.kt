package io.appmetrica.analytics.certificates.impl.tls

import androidx.annotation.Keep
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

/**
 * Composite trust manager that tries configured delegates in order.
 */
@Suppress("CustomX509TrustManager")
internal class CompositeTrustManager(
    private val delegates: List<X509TrustManager>,
) : X509TrustManager {

    @Throws(CertificateException::class)
    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        TrustManagerHelpers.checkTrusted(delegates, "No trust managers to check server certificate") {
            it.checkServerTrusted(chain, authType)
        }
    }

    @Throws(CertificateException::class)
    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        TrustManagerHelpers.checkTrusted(delegates, "No trust managers to check client certificate") {
            it.checkClientTrusted(chain, authType)
        }
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> =
        delegates.flatMap { it.acceptedIssuers.asList() }.toTypedArray()

    @Keep
    @Suppress("unused")
    @Throws(CertificateException::class)
    fun checkServerTrusted(
        chain: Array<out X509Certificate>,
        authType: String,
        host: String,
    ): List<X509Certificate> =
        TrustManagerHelpers.checkTrusted(delegates, "No trust managers to check server certificate") {
            TrustManagerHelpers.checkServerTrustedForHost(it, chain, authType, host)
        }

    @Keep
    @Suppress("unused")
    fun isSameTrustConfiguration(hostname1: String?, hostname2: String?): Boolean =
        delegates.all { TrustManagerHelpers.isSameTrustConfigurationOrTrue(it, hostname1, hostname2) }
}
