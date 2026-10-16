package io.appmetrica.analytics.coreapi.internal.io

import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

interface SslSocketFactoryProvider {
    val sslSocketFactory: SSLSocketFactory?
    val trustManager: X509TrustManager?
}
