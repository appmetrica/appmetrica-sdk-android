package io.appmetrica.analytics.certificates.impl.tls

import android.os.Build
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import java.net.Socket
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLEngine
import javax.net.ssl.X509ExtendedTrustManager
import javax.net.ssl.X509TrustManager

/**
 * Composite trust manager that tries delegates in order and preserves socket and engine context when supported.
 */
@Suppress("CustomX509TrustManager")
@RequiresApi(Build.VERSION_CODES.N)
internal class CompositeExtendedTrustManager(
    private val delegates: List<X509TrustManager>,
) : X509ExtendedTrustManager() {

    @Throws(CertificateException::class)
    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        TrustManagerHelpers.checkTrusted(delegates, SERVER_ERROR) { it.checkServerTrusted(chain, authType) }
    }

    @Throws(CertificateException::class)
    override fun checkServerTrusted(
        chain: Array<out X509Certificate>?,
        authType: String?,
        socket: Socket?,
    ) {
        TrustManagerHelpers.checkTrusted(delegates, SERVER_ERROR) {
            checkServerTrusted(it, chain, authType, socket)
        }
    }

    @Throws(CertificateException::class)
    override fun checkServerTrusted(
        chain: Array<out X509Certificate>?,
        authType: String?,
        engine: SSLEngine?,
    ) {
        TrustManagerHelpers.checkTrusted(delegates, SERVER_ERROR) {
            checkServerTrusted(it, chain, authType, engine)
        }
    }

    @Throws(CertificateException::class)
    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        TrustManagerHelpers.checkTrusted(delegates, CLIENT_ERROR) { it.checkClientTrusted(chain, authType) }
    }

    @Throws(CertificateException::class)
    override fun checkClientTrusted(
        chain: Array<out X509Certificate>?,
        authType: String?,
        socket: Socket?,
    ) {
        TrustManagerHelpers.checkTrusted(delegates, CLIENT_ERROR) {
            checkClientTrusted(it, chain, authType, socket)
        }
    }

    @Throws(CertificateException::class)
    override fun checkClientTrusted(
        chain: Array<out X509Certificate>?,
        authType: String?,
        engine: SSLEngine?,
    ) {
        TrustManagerHelpers.checkTrusted(delegates, CLIENT_ERROR) {
            checkClientTrusted(it, chain, authType, engine)
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
        TrustManagerHelpers.checkTrusted(delegates, SERVER_ERROR) {
            TrustManagerHelpers.checkServerTrustedForHost(it, chain, authType, host)
        }

    @Keep
    @Suppress("unused")
    fun isSameTrustConfiguration(hostname1: String?, hostname2: String?): Boolean =
        delegates.all { TrustManagerHelpers.isSameTrustConfigurationOrTrue(it, hostname1, hostname2) }

    private fun checkServerTrusted(
        trustManager: X509TrustManager,
        chain: Array<out X509Certificate>?,
        authType: String?,
        socket: Socket?,
    ) {
        if (trustManager is X509ExtendedTrustManager) {
            trustManager.checkServerTrusted(chain, authType, socket)
        } else {
            trustManager.checkServerTrusted(chain, authType)
        }
    }

    private fun checkServerTrusted(
        trustManager: X509TrustManager,
        chain: Array<out X509Certificate>?,
        authType: String?,
        engine: SSLEngine?,
    ) {
        if (trustManager is X509ExtendedTrustManager) {
            trustManager.checkServerTrusted(chain, authType, engine)
        } else {
            trustManager.checkServerTrusted(chain, authType)
        }
    }

    private fun checkClientTrusted(
        trustManager: X509TrustManager,
        chain: Array<out X509Certificate>?,
        authType: String?,
        socket: Socket?,
    ) {
        if (trustManager is X509ExtendedTrustManager) {
            trustManager.checkClientTrusted(chain, authType, socket)
        } else {
            trustManager.checkClientTrusted(chain, authType)
        }
    }

    private fun checkClientTrusted(
        trustManager: X509TrustManager,
        chain: Array<out X509Certificate>?,
        authType: String?,
        engine: SSLEngine?,
    ) {
        if (trustManager is X509ExtendedTrustManager) {
            trustManager.checkClientTrusted(chain, authType, engine)
        } else {
            trustManager.checkClientTrusted(chain, authType)
        }
    }

    private companion object {
        const val SERVER_ERROR = "No trust managers to check server certificate"
        const val CLIENT_ERROR = "No trust managers to check client certificate"
    }
}
