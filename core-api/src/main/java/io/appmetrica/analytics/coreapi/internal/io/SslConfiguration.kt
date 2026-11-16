package io.appmetrica.analytics.coreapi.internal.io

import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

class SslConfiguration(
    val sslSocketFactory: SSLSocketFactory?,
    val trustManager: X509TrustManager?
) {
    companion object {
        @JvmField
        val EMPTY = SslConfiguration(null, null)
    }
}
