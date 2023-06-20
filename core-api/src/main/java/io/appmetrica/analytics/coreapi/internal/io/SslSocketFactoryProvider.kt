package io.appmetrica.analytics.coreapi.internal.io

import javax.net.ssl.SSLSocketFactory

interface SslSocketFactoryProvider {
    val sslSocketFactory: SSLSocketFactory?
}
