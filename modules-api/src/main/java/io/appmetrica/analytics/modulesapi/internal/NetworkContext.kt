package io.appmetrica.analytics.modulesapi.internal

import io.appmetrica.analytics.coreapi.internal.io.IExecutionPolicy
import io.appmetrica.analytics.coreapi.internal.io.SslSocketFactoryProvider

interface NetworkContext {

    val sslSocketFactoryProvider: SslSocketFactoryProvider

    val executionPolicy: IExecutionPolicy

    val userAgent: String
}
