package io.appmetrica.analytics.impl.network.http

import io.appmetrica.analytics.coreapi.internal.io.SslConfiguration
import io.appmetrica.analytics.coreapi.internal.io.SslSocketFactoryProvider
import io.appmetrica.analytics.impl.startup.StartupState

internal class ModuleSslSocketFactoryProvider : BaseSslSocketFactoryProvider {

    @Volatile
    private var delegate: SslSocketFactoryProvider? = null

    override val sslConfiguration: SslConfiguration
        get() = delegate?.sslConfiguration ?: SslConfiguration.EMPTY

    fun setDelegate(provider: SslSocketFactoryProvider?) {
        delegate = provider
    }

    override fun onStartupStateChanged(startupState: StartupState) {}
}
