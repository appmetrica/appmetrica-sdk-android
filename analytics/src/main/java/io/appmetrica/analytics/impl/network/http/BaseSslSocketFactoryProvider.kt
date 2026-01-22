package io.appmetrica.analytics.impl.network.http

import io.appmetrica.analytics.coreapi.internal.io.SslSocketFactoryProvider
import io.appmetrica.analytics.impl.StartupStateObserver

internal interface BaseSslSocketFactoryProvider : SslSocketFactoryProvider, StartupStateObserver
