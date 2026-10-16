package io.appmetrica.analytics.impl.network.http

import android.content.Context
import io.appmetrica.analytics.impl.startup.StartupState
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

internal class SslSocketFactoryProviderImpl(val context: Context) : BaseSslSocketFactoryProvider {

    override val sslSocketFactory: SSLSocketFactory?
        get() = null

    override val trustManager: X509TrustManager?
        get() = null

    override fun onStartupStateChanged(startupState: StartupState) {}
}
