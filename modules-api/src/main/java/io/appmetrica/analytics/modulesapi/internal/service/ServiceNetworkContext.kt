package io.appmetrica.analytics.modulesapi.internal.service

import io.appmetrica.analytics.coreapi.internal.io.IExecutionPolicy
import io.appmetrica.analytics.coreapi.internal.io.SslSocketFactoryProvider
import io.appmetrica.analytics.modulesapi.internal.network.SimpleNetworkApi

interface ServiceNetworkContext {

    val sslSocketFactoryProvider: SslSocketFactoryProvider

    val executionPolicy: IExecutionPolicy

    val userAgent: String

    val networkApi: SimpleNetworkApi
}
