package io.appmetrica.analytics.impl.modules

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.io.IExecutionPolicy
import io.appmetrica.analytics.coreapi.internal.io.SslSocketFactoryProvider
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.network.CompositeExecutionPolicy
import io.appmetrica.analytics.impl.network.ConnectionBasedExecutionPolicy
import io.appmetrica.analytics.impl.network.ReporterRestrictionBasedPolicy
import io.appmetrica.analytics.impl.network.UserAgentProvider
import io.appmetrica.analytics.modulesapi.internal.network.SimpleNetworkApi
import io.appmetrica.analytics.modulesapi.internal.service.ServiceNetworkContext

internal class NetworkContextImpl(context: Context) : ServiceNetworkContext {

    override val sslSocketFactoryProvider: SslSocketFactoryProvider
        get() = GlobalServiceLocator.getInstance().sslSocketFactoryProvider

    override val executionPolicy: IExecutionPolicy = CompositeExecutionPolicy(
        ConnectionBasedExecutionPolicy(context),
        ReporterRestrictionBasedPolicy(GlobalServiceLocator.getInstance().dataSendingRestrictionController)
    )

    override val userAgent: String = UserAgentProvider().userAgent

    override val networkApi: SimpleNetworkApi = SimpleNetworkApiImpl()
}
