package io.appmetrica.analytics.impl.modules

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.io.IExecutionPolicy
import io.appmetrica.analytics.coreapi.internal.io.SslSocketFactoryProvider
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.network.ExecutionPolicyBasedOnConnection
import io.appmetrica.analytics.impl.network.UserAgentProvider
import io.appmetrica.analytics.modulesapi.internal.NetworkContext

internal class NetworkContextImpl(context: Context) : NetworkContext {

    override val sslSocketFactoryProvider: SslSocketFactoryProvider
        get() = GlobalServiceLocator.getInstance().sslSocketFactoryProvider

    override val executionPolicy: IExecutionPolicy = ExecutionPolicyBasedOnConnection(context)

    override val userAgent: String = UserAgentProvider().userAgent
}
