package io.appmetrica.analytics.impl.network.http

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.io.SslConfiguration
import io.appmetrica.analytics.impl.startup.StartupState

internal class SslSocketFactoryProviderImpl(val context: Context) : BaseSslSocketFactoryProvider {

    override val sslConfiguration = SslConfiguration.EMPTY

    override fun onStartupStateChanged(startupState: StartupState) {}
}
