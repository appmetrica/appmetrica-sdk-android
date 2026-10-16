package io.appmetrica.analytics.impl.network.http

import io.appmetrica.analytics.coreapi.internal.io.SslSocketFactoryProvider
import io.appmetrica.analytics.impl.startup.StartupState
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

internal class ModuleSslSocketFactoryProvider : BaseSslSocketFactoryProvider {

    @Volatile
    private var delegate: SslSocketFactoryProvider? = null

    override val sslSocketFactory: SSLSocketFactory?
        get() = delegate?.sslSocketFactory

    override val trustManager: X509TrustManager?
        get() = delegate?.trustManager

    fun setDelegate(provider: SslSocketFactoryProvider?) {
        delegate = provider
    }

    override fun onStartupStateChanged(startupState: StartupState) {}
}
