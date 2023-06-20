package io.appmetrica.analytics.impl.network.http

import android.content.Context
import io.appmetrica.analytics.impl.startup.StartupState
import javax.net.ssl.SSLSocketFactory

internal class SslSocketFactoryProviderImpl(val context: Context) : BaseSslSocketFactoryProvider {

    override val sslSocketFactory: SSLSocketFactory?
        get() = null

    override fun onStartupStateChanged(startupState: StartupState) {}
}
